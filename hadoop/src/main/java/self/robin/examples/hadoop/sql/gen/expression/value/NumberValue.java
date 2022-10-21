package self.robin.examples.hadoop.sql.gen.expression.value;

import lombok.Getter;
import self.robin.examples.hadoop.sql.gen.expression.LeafExpression;

/**
 * ...
 *
 * @author robin
 */
public class NumberValue extends LeafExpression {

    @Getter
    private Object value;

    public NumberValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String asSummaryString() {
        return String.valueOf(value);
    }
}
