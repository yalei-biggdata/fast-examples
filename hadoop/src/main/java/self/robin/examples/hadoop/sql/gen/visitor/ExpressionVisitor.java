package self.robin.examples.hadoop.sql.gen.visitor;

import self.robin.examples.hadoop.sql.gen.expression.*;
import self.robin.examples.hadoop.sql.gen.expression.function.Function;
import self.robin.examples.hadoop.sql.gen.expression.value.ArrayValue;


/**
 * ...
 *
 * @author robin
 */
public interface ExpressionVisitor<R> {

    R visit(Function function);

    R visit(Expressions.Between between);

    R visit(ArrayValue arrayValue);

    R visit(UnaryExpression unaryExpression);

    R visit(BinaryExpression binaryExpression);

    R visit(LeafExpression leafExpression);

    R visit(Expressions.RelationExpression relation);

    R visit(SelectQuery relation);

    R visit(CaseWhen caseWhenExpression);

    R visit(TernaryExpression ternaryExpression);

}
