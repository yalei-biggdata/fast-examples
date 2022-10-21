package self.robin.examples.hadoop.sql.gen.expression;

import self.robin.examples.hadoop.sql.gen.visitor.ExpressionVisitor;

import java.util.Arrays;
import java.util.List;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-10-19 19:39
 */
public abstract class TernaryExpression implements Expression, Formatter {

    private Expression left;

    private Expression middle;

    private Expression right;

    public TernaryExpression(Expression left, Expression middle, Expression right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public Expression getMiddle() {
        return middle;
    }

    @Override
    public String asSummaryString() {
        return "left: " + left.asSummaryString() +
                ", middle: " + middle.asSummaryString() +
                ", right: " + right.asSummaryString();
    }

    @Override
    public List<Expression> getChildren() {
        return Arrays.asList(left, middle, right);
    }
}
