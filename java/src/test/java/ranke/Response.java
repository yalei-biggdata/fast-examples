package ranke;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.openjdk.jmh.annotations.Setup;

import java.util.List;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-06-27 13:55
 */
@Data
public class Response {

    private String code;

    private String msg;

    private Data data;

    class Data {
        @Getter
        @Setter
        List<Entity> rankings;
    }

    @lombok.Data
    class Entity {

        /**
         * univNameCn : 哈尔滨工业大学
         * univUp : harbin-institute-of-technology
         * univLogo : logo/88331082.png
         * univLikeCount : 888
         * province : 黑龙江
         * city : 哈尔滨
         * liked : false
         * inbound : false
         * univTags : ["双一流","985","211"]
         * ranking : 1
         * grade : A+
         * indGrades : {"12":"A+","13":"A+","14":"A+","15":"A","16":"A+"}
         * score : 60.9
         */

        private String univNameCn;
        private String univUp;
        private String univLogo;
        private int univLikeCount;
        private String province;
        private String city;
        private boolean liked;
        private boolean inbound;
        private Integer ranking;
        private String grade;
        private double score;
        private List<String> univTags;

        private String major;
    }

}
