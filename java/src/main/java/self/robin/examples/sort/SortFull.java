package self.robin.examples.sort;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2021-12-30 9:04
 */
public class SortFull {

    private static void valid(Consumer<int[]> sortFunction) {
        valid(8, 3, sortFunction);
    }

    private static void valid(int size, int exprTimes, Consumer<int[]> sortFunction) {
        for (int i = 0; i < exprTimes; i++) {
            System.out.println("第" + i + "次实验：");
            int[] arr = genArr(size);
            System.out.println("print origin: " + Arrays.toString(arr));
            sortFunction.accept(arr);
            System.out.println("print sorted: " + Arrays.toString(arr));
            for (int j = 0; j < arr.length - 2; j++) {
                if (arr[j] > arr[j + 1]) {
                    throw new RuntimeException("排序失败");
                }
            }
        }
    }

    private static int[] genArr(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = (int) (Math.random() * 100);
        }
        return arr;
    }

    public static void main(String[] args) {
        System.out.println("--- 直接插入排序，方法1 ---");
        valid(SortFull::directInsert);

        System.out.println("--- 直接插入排序，方法2 ---");
        valid(SortFull::directInsert2);

        System.out.println("--- 希尔排序 ---");
        valid(SortFull::shaSort);

        System.out.println("done.");
    }

    public static void directInsert(int[] arr) {
        // 外层控制循环次数
        for (int i = 1; i < arr.length; i++) {
            for (int j = i; j > 0 && arr[j] < arr[j - 1]; j--) {
                int temp = arr[j];
                arr[j] = arr[j - 1];
                arr[j - 1] = temp;
            }
        }
    }


    // 直接差人排序
    public static void directInsert2(int[] arr) {
        // 外层控制循环次数
        for (int i = 1; i < arr.length; i++) {
            int num = arr[i];
            // 寻找合适的位置
            int j = i;
            for (; j > 0 && num < arr[j - 1]; j--) {
                arr[j] = arr[j - 1];
            }
            arr[j] = num;
        }
    }

//    // 直接插入排序，借鉴冒泡排序
//    public static void directInsert(int[] a) {
//        // 外层控制循环次数
//        for (int i = 0; i < a.length - 1; i++) {
//            // 内层寻找合适的位置
//            for (int j = i + 1; j > 0; j--) {
//                // 找到合适的位置，交换
//                if (a[j] < a[j - 1]) {
//                    int t = a[j - 1];
//                    a[j - 1] = a[j];
//                    a[j] = t;
//                }
//            }
//        }
//    }

    public static void shaSort(int[] a) {
        int len = a.length / 2;
        for (; len >= 1; len /= 2) {
//            for (int i = 0; i < a.length - len; i += len) {
//                for (int j = i + len; j > 0; j -= len) {
//                    if (a[j] < a[j - len]) {
//                        int tmp = a[j];
//                        a[j] = a[j - len];
//                        a[j - len] = tmp;
//                    }
//                }
//            }

            for (int i = 0; i < a.length - len; i += len) {
                int j = i + len;
                int tmp = a[j];
                for (; j > 0 && tmp < a[j - len]; j -= len) {
                    a[j] = a[j - len];
                }
                a[j] = tmp;
            }

        }

    }


}
