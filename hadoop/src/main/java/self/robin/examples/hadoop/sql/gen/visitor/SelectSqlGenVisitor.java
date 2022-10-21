package self.robin.examples.hadoop.sql.gen.visitor;

import self.robin.examples.hadoop.sql.gen.expression.*;
import self.robin.examples.hadoop.sql.gen.expression.function.Function;
import self.robin.examples.hadoop.sql.gen.expression.value.ArrayValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ...
 *
 * @author robin
 */
public class SelectSqlGenVisitor implements SqlQueryVisitor<String>, ExpressionVisitor<String> {

    @Override
    public String visit(Function function) {
        return function.asSummaryString();
    }

    @Override
    public String visit(Expressions.Between between) {
        return between.asSummaryString();
    }

    @Override
    public String visit(ArrayValue arrayValue) {
        if (arrayValue.getChildren().isEmpty()) {
            return "()";
        }
        List<Expression> childList = arrayValue.getChildren();
        // 如果是 join 表达式, 用空格拼接
        if (childList.get(0) instanceof Expressions.JoinExpression) {
            return childList.stream()
                    .map(expression -> expression.accept(this))
                    .collect(Collectors.joining(" "));
        }
        return childList.stream()
                .map(expression -> expression.accept(this))
                .collect(Collectors.joining(", "));
    }

    @Override
    public String visit(UnaryExpression unaryExpression) {
        // alias类型 应当考虑 括号问题
        if (unaryExpression instanceof Expressions.Alias) {
            Expressions.Alias alias = ((Expressions.Alias) unaryExpression);
            if (alias.getExpression() instanceof SelectQuery) {
                return "(" + alias.getExpression().accept(this) + ") " + alias.getName();
            }
            if (alias.getExpression() instanceof Expressions.Table) {
                return alias.getExpression().accept(this) +
                        (StringUtils.isBlank(alias.getName()) ? "" : " " + alias.getName());
            }
            return alias.getExpression().accept(this) +
                    (StringUtils.isBlank(alias.getName()) ? "" : " " + alias.getName());
        }
        return unaryExpression.asSummaryString();
    }

    @Override
    public String visit(BinaryExpression binaryExpression) {
        Expression left = binaryExpression.getLeftExpression();
        Expression right = binaryExpression.getRightExpression();
        SymbolDescription.Symbol symbol = binaryExpression.getSymbol();

        if (binaryExpression instanceof Expressions.And) {
            String leftStr = left.accept(this);
            if (left instanceof Expressions.Or) {
                leftStr = "(" + leftStr + ")";
            }
            String rightStr = right.accept(this);
            if (right instanceof Expressions.Or) {
                rightStr = "(" + rightStr + ")";
            }
            return joinWithSpace(leftStr, symbol.symbol(), rightStr);
        }
        if (binaryExpression instanceof Expressions.Or) {
            String rightStr = right.accept(this);
            if (right instanceof Expressions.And) {
                rightStr = "(" + right.accept(this) + ")";
            }
            return joinWithSpace(left.accept(this), symbol.symbol(), rightStr);
        }
        if (binaryExpression instanceof Expressions.In || binaryExpression instanceof Expressions.NotIn) {
            return joinWithSpace(left.accept(this), symbol.symbol(),
                    "(" + right.accept(this) + ")");
        }
        // other
        return joinWithSpace(left.accept(this), symbol.symbol(), right.accept(this));
    }

    private final String joinWithSpace(String... args) {
        return String.join(" ", args);
    }

    @Override
    public String visit(LeafExpression leafExpression) {
        return leafExpression.asSummaryString();
    }

    @Override
    public String visit(Expressions.RelationExpression relation) {
        return relation.asSummaryString();
    }

    @Override
    public String visit(SelectQuery selectQuery) {
        // select
        Expression items = selectQuery.getSelectItems();
        Expression table = selectQuery.getTable();
        Expression join = selectQuery.getJoin();
        Expression where = selectQuery.getWhere();
        Expression groupBy = selectQuery.getGroupBy();
        Expression having = selectQuery.getHaving();
        Expression limit = selectQuery.getLimit();

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT " + items.accept(this));
        if (table != null) {
            builder.append(" FROM " + table.accept(this));
        }

        checkInputData(join, table, "table required");
        if (join != null) {
            builder.append(" " + join.accept(this));
        }

        checkInputData(where, table, "table required");
        if (where != null) {
            builder.append(" WHERE " + where.accept(this));
        }

        checkInputData(groupBy, table, "table required");
        if (groupBy != null) {
            builder.append(" GROUP BY " + groupBy.accept(this));
        }

        checkInputData(having, groupBy, "groupBy required");
        if (having != null) {
            builder.append(" HAVING " + having.accept(this));
        }

        if (limit != null) {
            builder.append(" LIMIT " + limit.accept(this));
        }
        return builder.toString();
    }

    @Override
    public String visit(CaseWhen caseWhenExpression) {
        Expressions.Field field = caseWhenExpression.getField();
        List<Pair<Expression, Expression>> whenThenPairs = caseWhenExpression.getWhenThenPairs();
        Expression other = caseWhenExpression.getOther();

        StringBuilder builder = new StringBuilder();
        builder.append("CASE");
        if (field != null) {
            builder.append(" " + field.accept(this));
        }
        for (Pair<Expression, Expression> wtPair : whenThenPairs) {
            builder.append(" WHEN " + wtPair.getKey().accept(this) +
                    " THEN " + wtPair.getValue().accept(this));
        }
        if (other != null) {
            builder.append(" ELSE " + other.accept(this));
        }
        builder.append(" END");
        return builder.toString();
    }

    @Override
    public String visit(TernaryExpression ternaryExpression) {
        if (ternaryExpression instanceof Formatter) {
            if (ternaryExpression instanceof Expressions.JoinExpression) {
                return ternaryExpression.format("",
                        ternaryExpression.getMiddle().accept(this),
                        ternaryExpression.getRight().accept(this)
                );
            }
        }
        return ternaryExpression.asSummaryString();
    }

    private void checkInputData(Expression current, Expression dependency, String message) {
        if (current != null && dependency == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
