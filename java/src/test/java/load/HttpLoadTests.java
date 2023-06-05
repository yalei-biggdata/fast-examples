package load;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import self.robin.examples.utils.OkHttp3Simple;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-05-04 12:11
 */
@Fork(0)
@State(Scope.Thread)
public class HttpLoadTests {

    private java.util.Iterator<String> urlIte;

    OkHttp3Simple client;

    @Setup
    public void init() throws Exception {
        client = OkHttp3Simple.newBuilder()
                .init(builder -> builder.readTimeout(1000, TimeUnit.MILLISECONDS))
                .build();
        String filePath = "D:\\log\\request.log";
        urlIte = FileUtils.lineIterator(new File(filePath));
    }

    private String nextUrl() {
        if (urlIte.hasNext()) {
            String url = urlIte.next();
            int idx = url.indexOf("http");
            return idx == -1 ? url : url.substring(idx);
        }
        return "http://localhost:8080/engine/v1.0/r271/baseline_phone/most_popular/all/get_content?authcookie=5bRZgtxn4xWkg4t8A9NNMp6EJm1geVIxEwTJm28tRkg7om24SGCctiIsoiARdZ6PNdutv9c&resource_id=306115012&device_id=3a08e75f177c28759389af8a6f0b3cc91106&passport_id=1741324617&ip=124.78.24.151&wifi_mac=&gps=&ret_num=9&page_id=1&play_platform=IOS_PHONE_IQIYI&network=1&is_unified_interface=1&forbid_degradation=false&vip_type=1&qishow_on=0&frame_mode=8";
    }

    @Threads(20)
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 15, time = 1, timeUnit = TimeUnit.SECONDS)
    @Benchmark
    public String thr() {
        String url = nextUrl();
        url = replaceDomain("10.5.138.23:8080", url);
        return client.get(url);
    }

    @Threads(25)
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 15, time = 1, timeUnit = TimeUnit.SECONDS)
    //@Benchmark
    public String avg() {
        String url = nextUrl();
        url = replaceDomain("10.5.138.23:8080", url);
        return client.get(url);
    }

    @Test
    public void main() throws RunnerException {
        Options opts = new OptionsBuilder()
                .include(HttpLoadTests.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .build();
        new Runner(opts).run();
    }

    private String replaceDomain(String domain, String url) {
        if (StringUtils.isBlank(domain) || url == null) {
            return url;
        }
        url = url.trim();
        if ("".equals(url)) {
            return url;
        }
        int left = url.indexOf("://") + 3;
        int right = url.indexOf("/", left);
        if (left == 2 || right == -1) {
            return url;
        }
        return url.substring(0, left) + domain + url.substring(right);
    }
}
