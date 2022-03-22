import akka.actor.AbstractLoggingActor;
import akka.actor.ActorSystem;
import akka.actor.IllegalActorStateException;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-03-14 11:35
 */
public class ActorTest {

    public static void main(String[] args) {

        ActorSystem actor = ActorSystem.create("sap1");


    }

    static class MyActor extends AbstractLoggingActor {

        {
            receive(ReceiveBuilder.match(MyMessage.class, this::onMessage).build());
        }


        private void onMessage(MyMessage message) {
            System.out.println("MyMessage: " + message);
        }
    }


    static class MyMessage {

    }
}
