package ranke;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import self.robin.examples.utils.OkHttp3Simple;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-06-27 13:45
 */
public class RankEReport {

    @Test
    public void main() throws Exception {
        Map<String, List<String>> input = new HashMap<>();
        input.put("集美大学", Arrays.asList("数据科学与大数据技术", "网络空间安全", "人工智能", "智能制造工程", "数字经济", "数学与应用数学"));
        input.put("上海工程技术大学", Arrays.asList("电子信息类", "智能制造工程", "电子信息工程", "光电信息科学与工程", "数据计算及应用", "机器人工程"));
        input.put("青岛理工大学", Arrays.asList("自动化", "智能制造工程", "软件工程", "网络空间安全", "数学与应用数学", "电子信息工程"));
        input.put("华东交通大学", Arrays.asList("计算机科学与技术", "自动化", "人工智能", "智能制造工程", "数据科学与大数据技术"));
        input.put("济南大学", Arrays.asList("数据科学与大数据技术", "智能制造工程", "电子信息类", "自动化", "光电信息科学与工程", "新能源科学与技术"));
        input.put("上海第二工业大学", Arrays.asList("软件工程", "计算机科学与技术", "自动化", "网络工程", "材料化学", "电子信息工程"));
        input.put("哈尔滨理工大学", Arrays.asList("数据科学与大数据技术", "电子信息工程", "网络空间安全", "自动化", "人工智能", "光电信息科学与工程"));
        input.put("南京工程学院", Arrays.asList("计算机科学与技术", "自动化", "人工智能", "数据科学与大数据技术", "数学与应用数学", "网络工程"));
        input.put("南昌航天大学", Arrays.asList("计算机科学与技术", "自动化", "人工智能", "电子信息工程", "智能制造工程", "数学与应用数学"));
        input.put("郑州轻工业大学", Arrays.asList("智能制造工程", "计算机科学与技术", "人工智能", "软件工程", "自动化", "网络空间安全"));
        input.put("河南农业大学", Arrays.asList("数字经济", "计算机科学与技术", "人工智能", "电子信息工程", "轻工类", "烟草"));
        input.put("信阳师范学院", Arrays.asList("数学与应用数学", "计算机科学与技术", "物理学", "化学", "英语", "生物科学"));

        Map<String, List<Pair<String, String>>> majorAndUty = input.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(major -> Pair.of(major, entry.getKey())))
                .collect(Collectors.groupingBy(Pair::getKey, Collectors.toList()));

        List<String> lines = IOUtils.readLines(getClass().getResource("/majors.json").openStream(), StandardCharsets.UTF_8);
        String jsonDocument = String.join("", lines);

        List<Response.Entity> utyMajorRank = new ArrayList<>();
        for (String major : majorAndUty.keySet()) {
            JSONArray items = JsonPath.read(jsonDocument, "$..[?(@.name=='" + major.trim() + "')]");
            if (items.size() != 1) {
                System.out.println("异常的专业：" + major + ", " + items);
                continue;
            }
            Map<String, String> map = (Map<String, String>) items.get(0);
            // 数据科学与大数据技术 080910T
            String majorCode = Validate.notBlank(map.get("code"));
            String url = "https://www.shanghairanking.cn/api/pub/v1/bcmr/rank?year=2023&majorCode=" + majorCode;
            // invoke
            Response response = OkHttp3Simple.DEFAULT.getWithBuilder(url, builder -> {
                builder.header("cookie", "Hm_lvt_af1fda4748dacbd3ee2e3a69c3496570=1687841473; _clck=1p6wrdj|2|fct|0|1273; Hm_lpvt_af1fda4748dacbd3ee2e3a69c3496570=1687844460; TOKEN=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2ODc4NzcxMjIsImp0aSI6Ijc5ODI3NyBOVUxMIiwiaXNzIjoiMTU5KioqKjM3NTkifQ._185OU-5pGBYaQ5X0Oq8jk-LTUQKivjhGAuoe1lmkWE; _clsk=174gobt|1687859210126|8|1|u.clarity.ms/collect");
            }, Response.class);
            Validate.isTrue(Objects.equals(response.getCode(), "200"));

            List<Response.Entity> entities = Optional.ofNullable(response.getData())
                    .map(Response.Data::getRankings).orElse(Collections.emptyList());
            entities.stream().filter(entity -> input.containsKey(entity.getUnivNameCn().trim()))
                    .map(entity -> {
                        entity.setMajor(major);
                        return entity;
                    }).forEach(utyMajorRank::add);
        }

        // output
        Map<String, List<Response.Entity>> map = utyMajorRank.stream().collect(Collectors.groupingBy(Response.Entity::getMajor));
        System.out.println("     专业    |   学校排名   ");
        System.out.println("------------------------------------------------");
        map.forEach((major, entityList) -> {
            Collections.sort(entityList, Comparator.comparingInt(Response.Entity::getRanking));
            String msg = entityList.stream().map(entity -> entity.getUnivNameCn() + ":" + entity.getRanking())
                    .collect(Collectors.joining(" "));
            System.out.println(major + "    " + msg);
        });
    }
}
