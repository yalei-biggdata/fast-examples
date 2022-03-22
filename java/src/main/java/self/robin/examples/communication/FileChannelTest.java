package self.robin.examples.communication;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @Description: ...
 * @Author: Robin-Li
 * @DateTime: 2021-08-01 21:15
 */
public class FileChannelTest {

    public static class App1 {

        public static void main(String[] args) throws Exception {

            //一个byte占1B，所以共向文件中存128M的数据
            int length = 0x8FFFFFF;
            try (FileChannel channel = FileChannel.open(
                    Paths.get("E:/codes/c.txt"),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE
                    )) {
                MappedByteBuffer mapBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, length);
                for (int i = 0; i < 32; i++) {
                    mapBuffer.put((byte) 1);
                }
                System.out.println();
            }
        }
    }

    public static class App2 {

        public static void main(String[] args) throws Exception {

            //一个byte占1B，所以共向文件中存128M的数据
            int length = 0x8FFFFFF;
            try (FileChannel channel = FileChannel.open(Paths.get("E:/codes/c.txt"),
                    StandardOpenOption.READ, StandardOpenOption.WRITE);) {
                MappedByteBuffer mapBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, length);
                for (int i = 0; i < length ; i++) {
                    //像数组一样访问
                    System.out.println(mapBuffer.get(i));
                    if(i == 12){
                        break;
                    }
                }
            }
        }
    }
}
