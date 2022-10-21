package self.robin.examples.hadoop.sql.gen.expression;

/**
 * 根据给定的参数，将表达式的格式化展示
 *
 * @author Robin-Li
 * @since: 2022-10-19 19:44
 */
public interface Formatter<T> {

    /**
     * 根据给定的参数，将表达式的格式化展示
     *
     * @param args
     * @return
     */
    String format(T... args);

}
