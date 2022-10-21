package self.robin.examples.hadoop.sql.gen.expression;

import self.robin.examples.hadoop.sql.gen.visitor.ExpressionVisitor;

import java.util.List;

/**
 * General interface for all kinds of expressions.
 *
 * @author robin
 */
public interface Expression {

    <R> R accept(ExpressionVisitor<R> visitor);

    /**
     * Returns a string that this expression for printing to console
     *
     * @return
     */
    String asSummaryString();

    /**
     * 所有的子表达式
     *
     * @return
     */
    List<Expression> getChildren();
}
