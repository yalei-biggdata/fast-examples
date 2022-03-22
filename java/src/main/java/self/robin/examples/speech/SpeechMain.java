package self.robin.examples.speech;

import com.iflytek.cloud.b.b.c;
import com.iflytek.cloud.speech.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: ...
 * @Author: Robin-Li
 * @DateTime: 2021-08-01 17:27
 */
public class SpeechMain {

    static {
        System.setProperty("my.speech.dir", System.getProperty("user.dir") + File.separator + "java" + File.separator + "speech");
    }


    public static void main(String[] args) throws InterruptedException {
        System.out.println(System.getProperty("user.dir") + File.separator + "java" + File.separator + "speech");

        System.out.println(System.getProperty("java.library.path"));
        System.setProperty("java.library.path", "");
        SpeechMain main = new SpeechMain();
        SpeechUtility.createUtility(SpeechConstant.APPID + "=0d281bfd ");

        // 初始化听写对象
        SpeechRecognizer mIat = SpeechRecognizer.createRecognizer();
        main.setting(mIat);
        mIat.startListening(recognizerListener);
        Thread.sleep(20000);
        mIat.startListening(null);
    }


    /**
     * 听写监听器
     */
    private static RecognizerListener recognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            System.out.println("onBeginOfSpeech enter");
        }

        @Override
        public void onEndOfSpeech() {
            System.out.println("onEndOfSpeech enter");
        }

        /**
         * 获取听写结果. 获取RecognizerResult类型的识别结果，并对结果进行累加，显示到Area里
         */
        @Override
        public void onResult(RecognizerResult results, boolean islast) {
            System.out.println("onResult enter");

            //如果要解析json结果，请考本项目示例的 com.iflytek.util.JsonParser类
//			String text = JsonParser.parseIatResult(results.getResultString());
            String text = results.getResultString();
            System.out.println("out=" + text);
        }

        @Override
        public void onVolumeChanged(int volume) {
            System.out.println("onVolumeChanged enter");
        }

        @Override
        public void onError(SpeechError error) {
            System.out.println("onError enter");
            if (null != error) {
                System.out.println("onError Code：" + error.getErrorCode());
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int agr2, String msg) {
            System.out.println("onEvent enter");
            //以下代码用于调试，如果出现问题可以将sid提供给讯飞开发者，用于问题定位排查
			/*if(eventType == SpeechEvent.EVENT_SESSION_ID) {
				DebugLog.Log("sid=="+msg);
			}*/
        }
    };

    void setting(SpeechRecognizer mIat) {
        final String engType = this.mParamMap.get(SpeechConstant.ENGINE_TYPE);

        for (Map.Entry<String, String> entry : this.mParamMap.entrySet()) {
            mIat.setParameter(entry.getKey(), entry.getValue());
        }

        //本地识别时设置资源，并启动引擎
        if (SpeechConstant.TYPE_LOCAL.equals(engType)) {
            //启动合成引擎
            mIat.setParameter(ResourceUtil.ENGINE_START, SpeechConstant.ENG_ASR);

            //设置资源路径
            final String rate = this.mParamMap.get(SpeechConstant.SAMPLE_RATE);
            final String tag = rate.equals("16000") ? "16k" : "8k";
            String curPath = System.getProperty("user.dir");
            System.out.println("Current path=" + curPath);
            String resPath = ResourceUtil.generateResourcePath(curPath + "/asr/common.jet")
                    + ";" + ResourceUtil.generateResourcePath(curPath + "/asr/src_" + tag + ".jet");
            System.out.println("resPath=" + resPath);
            mIat.setParameter(ResourceUtil.ASR_RES_PATH, resPath);
        }// end of if is TYPE_LOCAL

    }// end of function setting


    private Map<String, String> mParamMap = new HashMap<String, String>();

    {
        this.mParamMap.put(SpeechConstant.ENGINE_TYPE, DefaultValue.ENG_TYPE);
        this.mParamMap.put(SpeechConstant.SAMPLE_RATE, DefaultValue.RATE);
        this.mParamMap.put(SpeechConstant.NET_TIMEOUT, DefaultValue.NET_TIMEOUT);
        this.mParamMap.put(SpeechConstant.KEY_SPEECH_TIMEOUT, DefaultValue.SPEECH_TIMEOUT);

        this.mParamMap.put(SpeechConstant.LANGUAGE, DefaultValue.LANGUAGE);
        this.mParamMap.put(SpeechConstant.ACCENT, DefaultValue.ACCENT);
        this.mParamMap.put(SpeechConstant.DOMAIN, DefaultValue.DOMAIN);
        this.mParamMap.put(SpeechConstant.VAD_BOS, DefaultValue.VAD_BOS);

        this.mParamMap.put(SpeechConstant.VAD_EOS, DefaultValue.VAD_EOS);
        this.mParamMap.put(SpeechConstant.ASR_NBEST, DefaultValue.NBEST);
        this.mParamMap.put(SpeechConstant.ASR_WBEST, DefaultValue.WBEST);
        this.mParamMap.put(SpeechConstant.ASR_PTT, DefaultValue.PTT);

        this.mParamMap.put(SpeechConstant.RESULT_TYPE, DefaultValue.RESULT_TYPE);
        this.mParamMap.put(SpeechConstant.ASR_AUDIO_PATH, null);
    }


    public static class DefaultValue {
        public static final String ENG_TYPE = SpeechConstant.TYPE_CLOUD;
        public static final String SPEECH_TIMEOUT = "60000";
        public static final String NET_TIMEOUT = "20000";
        public static final String LANGUAGE = "zh_cn";

        public static final String ACCENT = "mandarin";
        public static final String DOMAIN = "iat";
        public static final String VAD_BOS = "5000";
        public static final String VAD_EOS = "1800";

        public static final String RATE = "16000";
        public static final String NBEST = "1";
        public static final String WBEST = "1";
        public static final String PTT = "1";

        public static final String RESULT_TYPE = "json";
        public static final String SAVE = "0";
    }
}
