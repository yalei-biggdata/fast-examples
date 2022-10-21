package self.robin.examples.hadoop.sql.gen;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-10-20 10:11
 */
public class Assemble<T> {

    private Function<T, Boolean> okFunction;

    private Supplier<T> supplier;

    private T production;

    private Class<T> clazz;

    public Assemble(Supplier<T> supplier, Function<T, Boolean> okFunction) {
        this.okFunction = okFunction;
        this.supplier = supplier;
        this.clazz = (Class<T>) supplier.get().getClass();
    }

    public void assemble(Object value, Collection<T> collector) {
        if (production == null) {
            production = supplier.get();
        }
        if (okFunction.apply(production)) {
            collector.add(production);
            production = null;
        } else {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    field.set(production, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
