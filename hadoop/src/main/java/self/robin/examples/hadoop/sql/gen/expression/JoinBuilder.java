package self.robin.examples.hadoop.sql.gen.expression;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-10-19 20:44
 */
public abstract class JoinBuilder<T extends JoinBuilder> {

    protected List<Expressions.JoinExpression> joins;

    private Expressions.Alias rightExpression;

    private SymbolDescription.Symbol symbol;

    protected JoinBuilder(List<Expressions.JoinExpression> joins) {
        this.joins = joins;
    }

    /**
     * 关联的左边部分
     *
     * @return
     */
    protected abstract Expressions.Alias getLeftExpression();

    private final Expressions.Alias getLeft() {
        Expressions.Alias left = getLeftExpression();
        if (StringUtils.isBlank(left.getName())) {
            throw new IllegalArgumentException("The name of left expression required when join");
        }
        if (left == null) {
            throw new IllegalArgumentException("left expression required when join");
        }
        return left;
    }

    public JoinBuilder<T> leftJoin(Expressions.Table table, String alias) {
        return this.leftJoin(new Expressions.Alias(table, alias));
    }


    public JoinBuilder<T> leftJoin(Expressions.Alias table) {
        this.rightExpression = table;
        this.symbol = SymbolDescription.Symbol.LEFT_JOIN;
        return this;
    }

    public T on(Expressions.Field leftTableField, Expressions.Field rightTableField) {
        Expressions.EqualsTo<Expressions.Field> on = new Expressions.EqualsTo<>(leftTableField, rightTableField);
        Objects.requireNonNull(symbol);
        switch (symbol) {
            case LEFT_JOIN:
                joins.add(new Expressions.LeftJoin(getLeft(), rightExpression, on));
                break;
            case RIGHT_JOIN:
                joins.add(new Expressions.RightJoin(getLeft(), rightExpression, on));
                break;
            case INNER_JOIN:
                joins.add(new Expressions.InnerJoin(getLeft(), rightExpression, on));
                break;
            default:
        }
        this.rightExpression = null;
        return (T) this;
    }

    public JoinBuilder<T> innerJoin(Expressions.Table table, String alias) {
        return this.innerJoin(new Expressions.Alias(table, alias));
    }

    public JoinBuilder<T> innerJoin(Expressions.Alias table) {
        this.rightExpression = table;
        this.symbol = SymbolDescription.Symbol.INNER_JOIN;
        return (T) this;
    }

    public JoinBuilder<T> rightJoin(Expressions.Table table, String alias) {
        return this.rightJoin(new Expressions.Alias(table, alias));
    }

    public JoinBuilder<T> rightJoin(Expressions.Alias table) {
        this.rightExpression = table;
        this.symbol = SymbolDescription.Symbol.RIGHT_JOIN;
        return this;
    }

}
