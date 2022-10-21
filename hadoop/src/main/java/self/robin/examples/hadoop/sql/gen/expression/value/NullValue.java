package self.robin.examples.hadoop.sql.gen.expression.value;

import self.robin.examples.hadoop.sql.gen.expression.LeafExpression;

public class NullValue extends LeafExpression {

    private final String value = "NULL";

    public String getValue() {
        return value;
    }

    @Override
    public String asSummaryString() {
        return value;
    }
}
