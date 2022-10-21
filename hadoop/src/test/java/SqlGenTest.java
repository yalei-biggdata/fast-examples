import org.junit.Test;
import self.robin.examples.hadoop.sql.gen.expression.CaseWhen;
import self.robin.examples.hadoop.sql.gen.expression.Expression;
import self.robin.examples.hadoop.sql.gen.expression.Expressions.*;
import self.robin.examples.hadoop.sql.gen.expression.SelectQuery;
import self.robin.examples.hadoop.sql.gen.expression.value.ArrayValue;
import self.robin.examples.hadoop.sql.gen.expression.value.NumberValue;
import self.robin.examples.hadoop.sql.gen.expression.value.StringValue;
import self.robin.examples.hadoop.sql.gen.visitor.SelectSqlGenVisitor;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-10-17 11:57
 */
public class SqlGenTest {

    SelectSqlGenVisitor visitor = new SelectSqlGenVisitor();

    @Test
    public void testSimple() {

        EqualsTo eq1 = new EqualsTo(new Field("id"), new StringValue("112"));
        GreaterThan gt1 = new GreaterThan(new Field("age"), new NumberValue(16));
        EqualsTo eq2 = new EqualsTo(new Field("name"), new StringValue("xyz"));
        Or or = new Or(eq2, new And(eq1, gt1));
        Expression where = or;

        Expression selectQuery = SelectQuery.builder()
                .selectItems(
                        new Field("id"),
                        new Field("name"),
                        CaseWhen.builder()
                                .when(new GreaterThan(new Field("age"), new NumberValue(11))).then(new StringValue("a"))
                                .else_(new StringValue("z"))
                                .build()
                )
                .from(new Table("table_a"))
                .where(where)
                .build();

        String sql = selectQuery.accept(visitor);
        System.out.println("SQL: " + sql);
    }


    @Test
    public void testSubQuery() {

        EqualsTo eq1 = new EqualsTo(new Field("id"), new StringValue("112"));
        GreaterThan gt1 = new GreaterThan(new Field("age"), new NumberValue(16));
        EqualsTo eq2 = new EqualsTo(new Field("name"), new StringValue("xyz"));
        Or or = new Or(eq2, new And(eq1, gt1));

        // 子查询
        Expression subQuery = SelectQuery.builder()
                .selectItems(new ArrayValue(new Field("cty")))
                .from(new Table("table_a"))
                .where(new EqualsTo(new NumberValue(1), new NumberValue(1)))
                .build();
        In in = new In(new Field("country"), subQuery);
        Expression where = new And(or, in);

        Table mainTable = new Table("temp_table");
        Table table2 = new Table("table_2");

        SelectQuery select = SelectQuery.builder()
                .selectItems(
                        new Field("id"),
                        new Field("age"))
                .from(new Alias(mainTable, "a"))
                .leftJoin(new Alias(table2, "b"))
                .on(new Field("a", "id"), new Field("b", "id"))
                .where(where)
                .limit(new NumberValue(12))
                .build();

        SelectSqlGenVisitor visitor = new SelectSqlGenVisitor();
        String sql = select.accept(visitor);
        System.out.println(sql);
    }

    @Test
    public void testJoin() {
        Table mainTable = new Table("temp_table");
        Table table2 = new Table("table_2");
        Table table3 = new Table("table_3");

        SelectQuery select = SelectQuery.builder()
                .selectItems(
                        new Field("id"),
                        new Field("age"))
                .from(mainTable, "a")
                // 支持alias 类型
                .leftJoin(new Alias(table2, "b"))
                .on(new Field("a", "id"), new Field("b", "id"))
                // 也可以直接 table 类型
                .leftJoin(table3, "c")
                .on(new Field("a", "id"), new Field("c", "id"))
                .where(new EqualsTo(new Field("id"), new StringValue("112")))
                .limit(new NumberValue(12))
                .build();

        String sql = select.accept(visitor);
        System.out.println("SQL: " + sql);
    }

}
