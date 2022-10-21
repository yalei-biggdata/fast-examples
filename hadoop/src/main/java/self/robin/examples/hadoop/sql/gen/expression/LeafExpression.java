package self.robin.examples.hadoop.sql.gen.expression;

import self.robin.examples.hadoop.sql.gen.visitor.ExpressionVisitor;

import java.util.Collections;
import java.util.List;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-10-18 11:18
 */
public abstract class LeafExpression implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public List<Expression> getChildren() {
        return Collections.emptyList();
    }
}
