package self.robin.examples.utils.flume;

import net.bytebuddy.implementation.bytecode.assign.TypeCasting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.UUID;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-01-17 13:14
 */
public class FileTracer {

    public static void main(String[] args) {

    }

    public static void readFile(String filePath) {
        try (FileReader in = new FileReader(filePath);
             BufferedReader reader = new BufferedReader(in);
        ) {
            System.out.println(reader.readLine());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String filePath) {
        try (FileWriter in = new FileWriter(filePath);
             BufferedWriter writer = new BufferedWriter(in);
        ) {
            writer.write(UUID.randomUUID().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
