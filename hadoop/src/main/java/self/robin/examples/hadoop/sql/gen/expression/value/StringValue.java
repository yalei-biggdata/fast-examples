package self.robin.examples.hadoop.sql.gen.expression.value;

import self.robin.examples.hadoop.sql.gen.expression.LeafExpression;

/**
 * ...
 *
 * @author robin
 */
public class StringValue extends LeafExpression {

    private String value;

    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public String asSummaryString() {
        return "'" + value + "'";
    }

}
