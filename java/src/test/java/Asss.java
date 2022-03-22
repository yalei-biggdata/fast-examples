/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-03-16 18:10
 */
public class Asss {

    public void doMethod(){
        new Dog(this::process);
    }

    class Dog{
        public Dog(Action action){
        }
    }

    public void process(Action.Controller c) {
    }

    public interface Action {

        void runDefaultAction(Controller controller) throws Exception;

        interface Controller {
        }
    }
}
