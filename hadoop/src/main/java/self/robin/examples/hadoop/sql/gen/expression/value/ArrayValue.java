package self.robin.examples.hadoop.sql.gen.expression.value;

import self.robin.examples.hadoop.sql.gen.expression.Expression;
import self.robin.examples.hadoop.sql.gen.visitor.ExpressionVisitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ...
 *
 * @author robin
 */
public class ArrayValue<T extends Expression> implements Expression {

    private List<T> values = Collections.emptyList();

    public ArrayValue(T... values) {
        if (values != null) {
            this.values = Arrays.asList(values);
        }
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String asSummaryString() {
        return values.stream().map(expression -> expression.asSummaryString())
                .collect(Collectors.joining(", "));
    }

    @Override
    public List<Expression> getChildren() {
        return (List<Expression>) values;
    }
}
