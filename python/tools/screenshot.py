# coding=utf-8
# https://python-selenium-zh.readthedocs.io/zh_CN/latest/

import base64
import os
import random
import string
import threading
import time
import urllib

import uvicorn as uvicorn
from PIL import Image
from fastapi import FastAPI, Request
from pydantic import BaseModel
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

'''
----------
| 截图服务 |
----------
'''

# ======== global config =======
# service start port
port = 8090
# chrome install path
binary_location = None
# chrome 驱动path
driver_path = "C:\Program Files (x86)\Google\Chrome\Application\chromedriver.exe"
# 是否开启伪多线程加速, 暂时处于调试中，不可靠，不要开启
multi_thread_task = False


# =========== commons ===========
def printLog(log=None, mark=None):
    print(time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time())) +
          ('' if mark is None else ' ' + mark) + ' ' + log)


# =========== models ============
class Auth(BaseModel):
    """授权信息"""
    type: str = None  # 授权类型 cookie or header, default cookie
    key: str  # 授权 key
    val: str  # 授权 value


class Locator(BaseModel):
    """选择器"""
    by: str  # id or class or tag or name
    val: str  # value


class InputParam(BaseModel):
    """截图服务入参"""
    url: str  # 待截图的页面地址
    waitLoadTime: int = None  # 等待页面加载时间，单位秒
    auth: Auth = None
    locator: Locator = None
    sessionId: str = ''


# ========= screenshot =========
class ScreenshotTool(object):
    """ 截图工具 """

    def __init__(self, path: str):
        options = webdriver.ChromeOptions()
        if binary_location is not None:
            options.binary_location = binary_location
        options.add_argument('--headless')
        options.add_argument('--no-sandbox')
        options.add_argument('--disable-gpu')
        options.add_argument('--disable-dev-shm-usage')

        # chrome_driver_path = "C:\Program Files (x86)\Google\Chrome\Application\chromedriver.exe"
        driver = webdriver.Chrome(executable_path=path, options=options)
        driver.fullscreen_window()
        driver.maximize_window()
        driver.implicitly_wait(10)
        driver.set_script_timeout(10)
        driver.set_page_load_timeout(10)
        self.driver = driver

    def __del__(self):
        if hasattr(self, 'driver'):
            self.driver.close()
            self.driver.quit()
        print("tool quit.")

    @staticmethod
    def validUrl(url):
        if (not url.startswith('http')) and (not url.startswith('https')):
            raise Exception("url 不合法，必须以http 或 https开头")
        try:
            parse = urllib.parse.urlparse(url)
            url_prefix = parse.scheme + '://' + parse.netloc
            return parse.netloc, url_prefix, url
        except Exception as e:
            raise Exception('url不合法，' + ''.join(e.args))

    def addAuth(self, auth: Auth, domain: string = ''):
        driver = self.driver
        if auth is None:
            return
        # add auth info
        if auth.type is None or auth.type == 'cookie':
            # default or specify cookie
            cookie = {'name': auth.key, 'value': auth.val}
            if domain != '':
                cookie['domain'] = domain
            driver.add_cookie(cookie)
        elif auth.type == 'header':
            # specify header
            pass
        else:
            raise Exception('不支持的type in auth , type=' + auth.type)

    @staticmethod
    def toBase64(path):
        try:
            with open(path, 'rb') as f:
                b64_data = base64.b64encode(f.read())
                b64_str = str(b64_data, 'utf-8')
                return b64_str
        finally:
            f.close()

    @staticmethod
    def parseLocator(locator: Locator):
        if locator.by == 'id':
            return By.ID, locator.val
        if locator.by == 'class':
            return By.CLASS_NAME, locator.val
        if locator.by == 'css':
            return By.CSS_SELECTOR, locator.val
        if locator.by == 'tag':
            return By.TAG_NAME, locator.val
        if locator.by == 'css':
            return By.CSS_SELECTOR, locator.val
        if locator.by == 'name':
            return By.NAME, locator.val
        raise Exception('不支持的by in locator, by=' + locator.by)

    def getBase64Img(self, param: InputParam):
        session_id = param.sessionId
        domain, url_prefix, url = self.validUrl(param.url)

        driver = self.driver
        # 1. init domain
        driver.get(url_prefix + '/just_for_trigger_domain')
        printLog('trigger domain for ' + url_prefix, session_id)
        # 2. add auth info
        self.addAuth(param.auth)
        # 3. request url
        driver.get(url)
        printLog('request ' + url + ', current_url: ' + driver.current_url, session_id)
        # 4. wait load time
        if param.waitLoadTime is not None:
            time.sleep(param.waitLoadTime)
        # 5. find locator
        image_name = ''.join(random.sample(string.ascii_letters + string.digits, 16)) + '.png'
        if param.locator is None:
            # 如果没有 locator 全屏截图返回
            driver.save_screenshot(image_name)
            return self.toBase64(image_name)

        # 如果有locator
        locator = self.parseLocator(param.locator)
        try:
            element = WebDriverWait(driver, 8).until(EC.presence_of_element_located(locator))
        except Exception as e:
            raise Exception('未找到元素，定位参数：' + str(locator) + ', msg=' + str(e))
        # get element
        printLog('find element done.', session_id)

        # 5. screenshot
        right = element.location['x'] + element.size['width']
        bottom = element.location['y'] + element.size['height']
        driver.set_window_size(right, bottom)

        driver.execute_script("document.getElementsByTagName('body')[0].style.overflowX='hidden'")
        driver.execute_script("document.getElementsByTagName('body')[0].style.overflowY='hidden'")
        driver.save_screenshot(image_name)  # 对整个浏览器页面进行截图

        # 6. crop img
        left = element.location['x']
        top = element.location['y']

        crop_image = 'c_' + image_name
        im = Image.open(image_name)
        im = im.crop((left, top, right, bottom))  # 对浏览器截图进行裁剪
        im.save(crop_image)

        printLog("img generated.", session_id)
        try:
            return self.toBase64(crop_image)
        finally:
            os.remove(image_name)
            os.remove(crop_image)


def invoke(param: InputParam):
    try:
        printLog('start.', param.sessionId)
        return {'code': 200,
                'data': {'prefix': 'data:image/jpeg;base64,', 'base64': tool.getBase64Img(param)},
                'msg': '操作成功'}
    except Exception as e:
        printLog('Exception: ' + str(e), param.sessionId)
        return {'code': 1001, 'data': '', 'msg': str(e)}
    finally:
        printLog('done.', param.sessionId)


# ======= thread task =======
class ImageTask(threading.Thread):

    def __init__(self, param):
        threading.Thread.__init__(self)
        self.args = param
        self.res = None

    def run(self):
        self.res = invoke(self.args)

    def getResult(self):
        return self.res


# ============ api =============
app = FastAPI()
tool = ScreenshotTool(driver_path)


@app.post("/v1/screenshot")
def screenshot(param: InputParam, request: Request):
    param.sessionId = request.cookies.get('JSESSIONID')
    printLog('param=' + param.json())
    # do work and return
    if multi_thread_task:
        task = ImageTask(param)
        task.start()
        task.join()
        return task.getResult()
    else:
        return invoke(param)  # 简单任务模式


# ===========  main ============
if __name__ == '__main__':
    uvicorn.run(app, host='0.0.0.0', port=port)
