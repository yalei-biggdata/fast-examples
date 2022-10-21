package self.robin.examples.hadoop.sql.gen.expression;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import self.robin.examples.hadoop.sql.gen.expression.value.ArrayValue;
import self.robin.examples.hadoop.sql.gen.expression.value.NumberValue;
import self.robin.examples.hadoop.sql.gen.visitor.ExpressionVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ...
 *
 * @author robin
 */
@Getter
public class SelectQuery implements Expression {

    private static final Logger log = LoggerFactory.getLogger(SelectQuery.class);

    @Setter
    private Expression selectItems;

    @Setter
    private Expression table;

    @Setter
    private Expression join;

    @Setter
    private Expression where;

    @Setter
    private Expression groupBy;

    @Setter
    private Expression orderBy;

    @Setter
    private Expression limit;

    @Setter
    private Expression having;

    private SelectQuery() {
    }

    public static SelectQueryBuilder builder() {
        return new SelectQueryBuilder();
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String asSummaryString() {
        return toString();
    }

    @Override
    public List<Expression> getChildren() {
        return Arrays.asList(selectItems, table, where, groupBy, having, limit);
    }

    public static class SelectQueryBuilder extends JoinBuilder<SelectQueryBuilder> {

        private SelectQuery selectQuery;

        SelectQueryBuilder() {
            super(new ArrayList<>());
            this.selectQuery = new SelectQuery();
        }

        public SelectQueryBuilder selectItems(Expression... items) {
            return this.selectItems(new ArrayValue(items));
        }

        public SelectQueryBuilder selectItems(ArrayValue selectItems) {
            this.selectQuery.selectItems = selectItems;
            return this;
        }

        public SelectQueryBuilder from(Expressions.Table table) {
            return from(new Expressions.Alias(table, null));
        }

        public SelectQueryBuilder from(Expressions.Table table, String alias) {
            return from(new Expressions.Alias(table, alias));
        }

        public SelectQueryBuilder from(Expressions.Alias table) {
            this.selectQuery.table = table;
            return this;
        }

        public SelectQueryBuilder where(Expression where) {
            this.selectQuery.where = where;
            return this;
        }

        public SelectQueryBuilder groupBy(ArrayValue groupBy) {
            this.selectQuery.groupBy = groupBy;
            return this;
        }

        public SelectQueryBuilder orderBy(Expressions.OrderBy orderBy) {
            this.selectQuery.orderBy = orderBy;
            return this;
        }

        public SelectQueryBuilder limit(NumberValue limit) {
            this.selectQuery.limit = limit;
            return this;
        }

        public SelectQueryBuilder having(Expression having) {
            this.selectQuery.having = having;
            return this;
        }

        public SelectQuery build() {
            if (this.joins != null && !this.joins.isEmpty()) {
                this.selectQuery.setJoin(new ArrayValue<>(this.joins.toArray(new Expressions.JoinExpression[0])));
            }
            return this.selectQuery;
        }

        @Override
        protected Expressions.Alias getLeftExpression() {
            if (this.selectQuery.table instanceof Expressions.Alias) {
                Expressions.Alias alias = ((Expressions.Alias) this.selectQuery.table);
                if (StringUtils.isNotBlank(alias.getName())) {
                    return alias;
                }
                /*if (alias.getExpression() instanceof Expressions.Table) {
                    log.warn("The name of left block is missing, use table name as default");
                    alias.setName(((Expressions.Table) alias.getExpression()).getTableName());
                    return alias;
                }*/
                throw new IllegalArgumentException("The name of left block is required, use Alias expression please");
            }
            throw new IllegalArgumentException("The type of table is wrong, Alias type required");
        }
    }
}