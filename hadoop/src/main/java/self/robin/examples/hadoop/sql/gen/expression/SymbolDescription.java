package self.robin.examples.hadoop.sql.gen.expression;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-10-18 12:10
 */
public interface SymbolDescription {

    /**
     * 符号表示
     *
     * @return
     */
    Symbol getSymbol();

    enum Symbol {

        CAST("CAST"),
        DISTINCT("DISTINCT"),
        ALIAS("ALIAS"),
        AS("AS"),
        ON("ON"),

        AND("AND"),
        OR("OR"),
        IN("IN"),
        NOT_IN("NOT IN"),
        LIKE("LIKE"),
        IS_NULL("IS NULL"),
        IS_NOT_NULL("IS NOT NULL"),
        BETWEEN("BETWEEN"),

        /* comparison */
        EQUALS("="),
        NOT_EQUALS("!="),
        GREATER_THAN_EQUALS(">="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        LESS_THAN_EQUALS("<="),

        ORDER_BY("ORDER BY"),

        INNER_JOIN("INNER JOIN"),
        LEFT_JOIN("LEFT JOIN"),
        RIGHT_JOIN("RIGHT JOIN");

        private String symbol;

        Symbol(String symbol) {
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }
    }
}
