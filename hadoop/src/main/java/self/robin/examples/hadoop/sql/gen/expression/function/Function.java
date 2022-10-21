package self.robin.examples.hadoop.sql.gen.expression.function;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-10-21 12:56
 */

import self.robin.examples.hadoop.sql.gen.expression.Expression;
import self.robin.examples.hadoop.sql.gen.expression.value.ArrayValue;
import self.robin.examples.hadoop.sql.gen.visitor.ExpressionVisitor;

import java.util.List;

/**
 * 函数定义
 *
 * @author robin
 */
public class Function implements Expression {

    private final String symbol;

    private ArrayValue parameters;

    public Function(String name) {
        this(name, new ArrayValue());
    }

    public Function(String symbol, ArrayValue parameters) {
        this.symbol = symbol;
        this.parameters = parameters;
    }

    public Function(String symbol, Expression... parameters) {
        this.symbol = symbol;
        this.parameters = new ArrayValue(parameters);
    }

    public ArrayValue getParameters() {
        return parameters;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String asSummaryString() {
        if (parameters.getChildren().isEmpty()) {
            return symbol;
        }
        return symbol + "(" + parameters.asSummaryString() + ")";
    }

    @Override
    public List<Expression> getChildren() {
        return parameters.getChildren();
    }

}

