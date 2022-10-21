package self.robin.examples.hadoop.sql.gen.expression;

import self.robin.examples.hadoop.sql.gen.DataType;
import self.robin.examples.hadoop.sql.gen.visitor.ExpressionVisitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-10-18 16:00
 */
public interface Expressions {

    /**
     * 比较运算符相关
     *
     * @author Robin-Li
     * @since: 2022-10-18 12:31
     */
    public abstract class BinaryComparison<T extends Expression> extends BinaryExpression {

        public BinaryComparison(T leftExpression, T rightExpression) {
            super(leftExpression, rightExpression);
        }
    }

    public class EqualsTo<T extends Expression> extends BinaryComparison<T> {

        public EqualsTo(T leftExpression, T rightExpression) {
            super(leftExpression, rightExpression);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.EQUALS;
        }
    }

    public class NotEqualsTo extends BinaryExpression {

        public NotEqualsTo(Expression leftExpression, Expression rightExpression) {
            super(leftExpression, rightExpression);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.NOT_EQUALS;
        }
    }

    public class GreaterThan extends BinaryComparison {

        public GreaterThan(Expression leftExpression, Expression rightExpression) {
            super(leftExpression, rightExpression);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.GREATER_THAN;
        }
    }

    public class GreaterThanEquals extends BinaryComparison {

        public GreaterThanEquals(Expression leftExpression, Expression rightExpression) {
            super(leftExpression, rightExpression);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.GREATER_THAN_EQUALS;
        }
    }

    public class In extends BinaryComparison {

        public In(Expression leftExpression, Expression rightExpression) {
            super(leftExpression, rightExpression);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.IN;
        }

    }

    public class NotIn extends BinaryComparison {

        public NotIn(Expression leftExpression, Expression rightExpression) {
            super(leftExpression, rightExpression);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.NOT_IN;
        }
    }

    public class IsNull extends UnaryExpression {

        public IsNull(Expression expression) {
            super(expression);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.IS_NULL;
        }
    }

    public class IsNotNull extends UnaryExpression {

        public IsNotNull(Expression expression) {
            super(expression);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.IS_NOT_NULL;
        }
    }

    public class LessThan extends BinaryComparison {

        public LessThan(Expression leftExpression, Expression rightExpression) {
            super(leftExpression, rightExpression);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.LESS_THAN;
        }
    }

    public class LessThanEquals extends BinaryComparison {

        public LessThanEquals(Expression leftExpression, Expression rightExpression) {
            super(leftExpression, rightExpression);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.LESS_THAN_EQUALS;
        }
    }

    /**
     * 两元操作要求类型一致，TODO 加类型检查
     *
     * @author Robin-Li
     * @since: 2022-10-18 12:26
     */
    public abstract class BinaryOperator extends BinaryExpression {

        public BinaryOperator(Expression leftExpression, Expression rightExpression) {
            super(leftExpression, rightExpression);
        }
    }

    public class And extends BinaryOperator {

        public And(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.AND;
        }

    }

    public class Cast extends UnaryExpression {

        private DataType dataType;

        public Cast(Expression expression, DataType dataType) {
            super(expression);
            this.dataType = dataType;
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.CAST;
        }
    }

    public class Like extends BinaryOperator {

        public Like(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.LIKE;
        }
    }

    public class Or extends BinaryOperator {

        public Or(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.OR;
        }
    }

    public class Distinct extends UnaryExpression {

        public Distinct(Expression expression) {
            super(expression);
        }

        @Override
        public String asSummaryString() {
            return getSymbol().symbol();
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.DISTINCT;
        }
    }

    /**
     * T 应当是 Table类型，或者 SelectQuery类型；
     * 这里应该把范围缩小一些，而不是Expression类型
     *
     * @param <T>
     */
    public class Alias<T extends Expression> extends UnaryExpression {

        private String name;

        public Alias(T expression, String name) {
            super(expression);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.AS;
        }

        @Override
        public String asSummaryString() {
            return super.asSummaryString() + (name == null ? "" : " " + name);
        }
    }

    /**
     * 关系表达式，比如字段所属表，表所属库
     *
     * @author Robin-Li
     * @since: 2022-10-18 15:54
     */
    public static class RelationExpression implements Expression {

        protected final String parent;

        protected final String self;

        public RelationExpression(String parent, String self) {
            this.parent = parent;
            this.self = self;
        }

        @Override
        public <R> R accept(ExpressionVisitor<R> visitor) {
            return visitor.visit(RelationExpression.this);
        }

        @Override
        public String asSummaryString() {
            return parent == null || "".equals(parent) ? self : parent + "." + self;
        }

        @Override
        public List<Expression> getChildren() {
            return Collections.emptyList();
        }

    }

    public class Field extends RelationExpression {

        public Field(String fieldName) {
            this(null, fieldName);
        }

        public Field(String tableAlias, String fieldName) {
            super(tableAlias, fieldName);
        }
    }

    public class Table extends RelationExpression {

        public Table(String tableName) {
            this(null, tableName);
        }

        public String getDbName() {
            return parent;
        }

        public String getTableName() {
            return self;
        }

        public Table(String dbName, String tableName) {
            super(dbName, tableName);
        }
    }

    /**
     * ...
     *
     * @author robin
     */
    public class Between implements Expression {

        private Expression expression;
        private Expression lowerBound;
        private Expression upperBound;

        public Between(Expression expression, Expression lowerBound, Expression upperBound) {
            this.expression = expression;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        public Expression getExpression() {
            return expression;
        }

        public Expression getLowerBound() {
            return lowerBound;
        }

        public Expression getUpperBound() {
            return upperBound;
        }

        @Override
        public <R> R accept(ExpressionVisitor<R> visitor) {
            return visitor.visit(Between.this);
        }

        @Override
        public String asSummaryString() {
            return expression.asSummaryString() + " BETWEEN " + lowerBound.asSummaryString() + " AND " + upperBound.asSummaryString();
        }

        @Override
        public List<Expression> getChildren() {
            return Arrays.asList(expression, lowerBound, upperBound);
        }

    }

    public class OrderBy extends UnaryExpression {

        private final OrderType orderType;

        public OrderBy(Expression expression) {
            this(expression, OrderType.ASC);
        }

        public OrderBy(Expression expression, OrderType orderType) {
            super(expression);
            this.orderType = orderType;
        }

        @Override
        public String asSummaryString() {
            return getSymbol().symbol() + " " + getExpression().asSummaryString() + " " + orderType;
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.ORDER_BY;
        }
    }


    /**
     * 关联
     */

    public abstract class JoinExpression extends TernaryExpression implements SymbolDescription {

        public JoinExpression(Alias left, Alias right, EqualsTo<Field> on) {
            super(left, right, on);
        }

        @Override
        public String format(Object[] args) {
            String template = "%s " + getSymbol().symbol() +
                    " %s " + SymbolDescription.Symbol.ON.symbol() + " %s";
            return String.format(template, args);
        }
    }

    public class InnerJoin extends JoinExpression {

        public InnerJoin(Alias left, Alias right, EqualsTo<Field> on) {
            super(left, right, on);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.INNER_JOIN;
        }
    }

    public class LeftJoin extends JoinExpression {

        public LeftJoin(Alias left, Alias right, EqualsTo<Field> on) {
            super(left, right, on);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.LEFT_JOIN;
        }
    }

    public class RightJoin extends JoinExpression {

        public RightJoin(Alias left, Alias right, EqualsTo<Field> on) {
            super(left, right, on);
        }

        @Override
        public Symbol getSymbol() {
            return Symbol.RIGHT_JOIN;
        }
    }
}
