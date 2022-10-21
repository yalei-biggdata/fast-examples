package self.robin.examples.hadoop.sql.gen.expression;

import org.apache.commons.lang3.tuple.Pair;
import self.robin.examples.hadoop.sql.gen.visitor.ExpressionVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-10-19 14:43
 */
public class CaseWhen implements Expression {

    private Expressions.Field field;

    private List<Pair<Expression, Expression>> whenThenPairs;

    /**
     * else
     */
    private Expression other;

    CaseWhen(Expressions.Field field, List<Pair<Expression, Expression>> whenThenPairs, Expression other) {
        this.field = field;
        this.whenThenPairs = whenThenPairs;
        this.other = other;
    }

    public static CaseWhenBuilder builder() {
        return new CaseWhenBuilder();
    }

    public void setOther(Expression other) {
        this.other = other;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String asSummaryString() {
        return "CaseWhen.CaseWhenBuilder(field=" + this.field + ", whenThenPairs="
                + this.whenThenPairs + ", other=" + this.other + ")";
    }

    public Expressions.Field getField() {
        return field;
    }

    public List<Pair<Expression, Expression>> getWhenThenPairs() {
        return whenThenPairs;
    }

    public Expression getOther() {
        return other;
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> childList = new ArrayList<>();
        childList.add(field);
        for (Pair<Expression, Expression> whenThenPair : whenThenPairs) {
            childList.add(whenThenPair.getKey());
            childList.add(whenThenPair.getValue());
        }
        childList.add(other);
        return childList;
    }


    public static class CaseWhenBuilder {

        private Expressions.Field field;
        private List<Pair<Expression, Expression>> whenThenPairs;

        private Expression other;

        private Expression when;

        private Expression then;

        CaseWhenBuilder() {
            this.whenThenPairs = new ArrayList<>();
        }

        public CaseWhenBuilder case_(Expressions.Field field) {
            this.field = field;
            return this;
        }

        public CaseWhenBuilder when(Expression when) {
            this.when = when;
            return this;
        }

        public CaseWhenBuilder then(Expression then) {
            if (when == null) {
                throw new IllegalArgumentException("then only is after when");
            }
            this.then = then;
            this.whenThenPairs.add(Pair.of(when, then));
            this.when = null;
            return this;
        }

        public CaseWhenBuilder else_(Expression other) {
            if (then == null) {
                throw new IllegalArgumentException("else only is after then");
            }
            this.other = other;
            return this;
        }

        public CaseWhen build() {
            return new CaseWhen(field, whenThenPairs, other);
        }

    }
}
