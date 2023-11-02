package jsonpath;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.Validate;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import self.robin.examples.utils.OkHttp3Simple;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-05-06 13:37
 */
@RunWith(Parameterized.class)
public class MostTest {

    @Parameterized.Parameter
    public String url;

    static OkHttp3Simple client = OkHttp3Simple.newBuilder()
            .init(builder -> builder.readTimeout(1000, TimeUnit.MILLISECONDS))
            .build();

    private String jsonDocument;

    private Map<String, String> reqParam;

    @Before
    public void init() throws Exception {
        reqParam = URLTool.parseParams(url);
        System.out.println("url=" + url);
        // do request
        String response = client.get(url);
        // valid
        Validate.isTrue(response.contains("A00000"));
        jsonDocument = response;
    }

    @Parameterized.Parameters
    public static Collection parametersFromFile() throws Exception {
        final String domain = "mesos-slave-online096-cnhb4-2.cloud.qiyi.domain:31207/";
//        final String domain = "localhost:8080";
        // iterate times
        final int iterates = 20;
        // request.log path
        String filePath = "D:\\log\\request.log";
        // fetch urls from request log
        LineIterator ite = FileUtils.lineIterator(new File(filePath));
        Object[][] params = new Object[iterates][1];
        for (int i = 0; i < iterates && ite.hasNext(); i++) {
            String url = ite.nextLine();
            int idx = url.indexOf("http");
            // design domain
            url = URLTool.replaceDomain(domain, idx == -1 ? url : url.substring(idx));
            params[i][0] = url;
        }
        return Arrays.asList(params);
    }

    private JSONArray getFollowItems() {
        // 按103 过滤出追剧数据
        return JsonPath.read(jsonDocument, "$..rec_items[?(@.pingback.r_source=~/.*103.*/i)]");
    }

    /**
     * 追剧帧校验
     */
    @Test
    public void followTest() {
        JSONArray items = getFollowItems();
        /* ---------- 追剧位置校验 ---------- */
        // 弱追不出现在非第2帧
        JSONArray sndFw = JsonPath.parse(items).read("[?(!@.type && @.rank!=2)]");
        Assertions.assertThat(sndFw).isEmpty();

        // 追剧帧不出现在非第一帧
        JSONArray frtFw = JsonPath.parse(items).read("[?(@.type==9 && @.rank!=1)]");
        Assertions.assertThat(frtFw).isEmpty();

        // 弱追存在，且第一帧是追剧
        sndFw = JsonPath.parse(items).read("[?(!@.type)]");
        if (sndFw.size() == 1) {
            frtFw = JsonPath.parse(items).read("[?(@.type==9 && @.rank==1)]");
            Assertions.assertThat(frtFw).size().isEqualTo(1);
        }
    }

    /**
     * 非首屏号不出现在前6帧
     */
    @Test
    public void nfsAt7Or8() {
        String jsonPath = "$..rec_items[?(@.rank<=6 && @.extension.hp_frt_screen==false)]";
        JSONArray object = JsonPath.read(jsonDocument, jsonPath);
        Assertions.assertThat(object).size().isEqualTo(0);
    }

    /**
     * 一级池个数小于等于6
     */
    @Test
    public void pool1SizeIsLessThanOrEqualTo6() {
        JSONArray items = JsonPath.read(jsonDocument, "$..rec_items[?(@.pingback.r_source=~/.*906.*/i)]");
        Assertions.assertThat(items).size().isLessThanOrEqualTo(6);
    }

    /**
     * 运营模式校验
     */
    @Test
    public void operationModeTest() {
        String ext = JsonPath.read(jsonDocument, "$.data[0].pingback.ext").toString();
        String operationMode = JsonPath.read(ext, "$.operation_mode").toString();
        Assertions.assertThat(operationMode).isNotEqualTo("");
        // 追剧内容
        JSONArray items = getFollowItems();
        int followSize = items.size();

        // 定帧内容
        JSONArray item903 = JsonPath.read(jsonDocument, "$.data[0].rec_items[?(@.extension.manual_position && @.pingback.r_source=~/.*903.*/i)]");
        int fixateNum = 0;
        if (Objects.equals(operationMode, "1") || Objects.equals(operationMode, "3")) {
            fixateNum = 2;
        }
        if (Objects.equals(operationMode, "2") || Objects.equals(operationMode, "4")) {
            fixateNum = 1;
        }
        // check num
        Assertions.assertThat(item903).size().isEqualTo(fixateNum);
        // check rank
        String rankNum = String.format("$[?(@.rank>%s && @.rank<=%s)]", followSize, followSize + fixateNum);
        JSONArray ranks = JsonPath.parse(item903).read(rankNum);
        Assertions.assertThat(ranks).size().isEqualTo(2);
    }

    /**
     * 播单个数不超过两个
     */
    @Test
    public void playlistLimit2() {
        // todo make sure playlist tag
        JSONArray items = JsonPath.read(jsonDocument, "$.data[0].rec_items[?(@.extension.content_type=='')]");
        //Assertions.assertThat(items).size().isLessThanOrEqualTo(2);
    }

    /**
     * 预告片个数不超过两个
     */
    @Test
    public void prevueLimit2() {
        JSONArray items = JsonPath.read(jsonDocument, "$.data[0].rec_items[?(@.extension.content_type=='PREVUE')]");
        Assertions.assertThat(items).size().isLessThanOrEqualTo(2);
    }

    /**
     * 返回个数验证
     */
    @Test
    public void retNum() {
        JSONArray items = JsonPath.read(jsonDocument, "$.data[0].rec_items");
        int retNum = Integer.parseInt(reqParam.getOrDefault("ret_num", "-1"));
        Assertions.assertThat(items).size().isLessThanOrEqualTo(retNum);
    }
}
