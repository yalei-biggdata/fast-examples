package self.robin.examples.hadoop.sql.gen.expression;

import self.robin.examples.hadoop.sql.gen.visitor.ExpressionVisitor;

import java.util.Arrays;
import java.util.List;

/**
 * ...
 *
 * @author robin
 */
public abstract class BinaryExpression implements Expression, SymbolDescription {

    protected Expression leftExpression;

    protected Expression rightExpression;

    public BinaryExpression(Expression leftExpression, Expression rightExpression) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public Expression getRightExpression() {
        return rightExpression;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public List<Expression> getChildren() {
        return Arrays.asList(leftExpression, rightExpression);
    }

    @Override
    public String asSummaryString() {
        return getLeftExpression().asSummaryString() +
                " " + getSymbol().symbol() + " "
                + getRightExpression().asSummaryString();
    }
}
