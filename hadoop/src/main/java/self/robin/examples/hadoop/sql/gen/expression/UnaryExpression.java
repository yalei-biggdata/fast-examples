package self.robin.examples.hadoop.sql.gen.expression;

import lombok.Getter;
import self.robin.examples.hadoop.sql.gen.visitor.ExpressionVisitor;

import java.util.Collections;
import java.util.List;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-10-17 16:44
 */
public abstract class UnaryExpression implements Expression, SymbolDescription {

    @Getter
    protected Expression expression;

    public UnaryExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public List<Expression> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String asSummaryString() {
        return getExpression().asSummaryString() + " " + getSymbol().symbol();
    }
}
