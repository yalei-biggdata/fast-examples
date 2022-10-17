import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.*;
import java.util.stream.Stream;

/**
 * 1. 地域幸运数字
 * 2. 日期数字
 * 3. 低概率数字
 *
 * @since: 2022-06-15 10:20
 */
public class SSQTest {

    @Test
    public void ssq() {
        // init
        int ballAll = 33;
        double initRate = 1.0D / ballAll;
        Map<Integer, Ball> ballMap = new HashMap<>();
        for (int i = 1; i <= ballAll; i++) {
            ballMap.put(i, new Ball(0, i, initRate));
        }

        // apply the group of 50
        int samplingRow = 50;
        Integer[][] samplingData = new Integer[samplingRow][ballAll];


    }

//    private Integer[][] trendPath(int effectivePathLen, Integer[][] samplingData) {
//
////        for (int i = samplingData.length - 1; i > 0; i--) {
////            for (int j = 0; j < samplingData[i].length; j++) {
////                Integer ball = samplingData[i][j];
////                if (ball <= 0) {
////                    continue;
////                }
////
////                samplingData[j]
////
////            }
////        }
//    }

    private List<Pair<Integer, Integer>> trendPathInternal(int ball, int startRow, int startCol, Integer[][] samplingData, int effectivePathLen) {
        List<Pair<Integer, Integer>> list = new ArrayList<>();

        for (int i = startRow - 1; i > 0; i--) {

            for (int j = startCol - 1; j < samplingData[i].length; j++) {
                if (startCol <= 0) {
                    continue;
                }
                Integer tempBall = samplingData[i][j];
                double sum = Math.pow(Math.abs(startRow - i), 2) + Math.pow(Math.abs(startCol - j), 2);
                // 有效路径长度判断
                if (Math.sqrt(sum) > effectivePathLen) {
                    break;
                }
                list.add(Pair.of(i, j));
                startCol++;
            }
            startRow--;
        }
        return list;
    }

    @Data
    @AllArgsConstructor
    public class Ball {

        private int color;

        private int num;

        private double rate;
    }

}
