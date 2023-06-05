import akka.actor.AbstractLoggingActor;
import akka.actor.ActorSystem;
import akka.actor.IllegalActorStateException;
import akka.japi.pf.ReceiveBuilder;
import org.junit.Before;
import org.junit.Test;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.io.*;
import java.util.UUID;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-03-14 11:35
 */
public class ActorTest {

    private String filePath;

    @Before
    public void init() {
        String userDir = System.getenv("tmp");
        filePath = userDir + File.separator + "appendFile.txt";
    }

    @Test
    public void readFile() {
        try (FileReader in = new FileReader(filePath);
             BufferedReader reader = new BufferedReader(in);
        ) {
            while (true) {
                Thread.sleep(1000);
                String line = reader.readLine();
                if (line == null) {
                    continue;
                }
                System.out.println("read: " + line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void writeFile() throws Exception {
        try (FileWriter in = new FileWriter(filePath);
             BufferedWriter writer = new BufferedWriter(in);
        ) {
            while (true) {
                writer.append(UUID.randomUUID().toString());
                writer.append('\n');
                writer.flush();
                Thread.sleep(5000);
                System.out.println("-------");
            }
        }
    }
}
