package self.robin.examples.hadoop.sql.gen.visitor;

import self.robin.examples.hadoop.sql.gen.expression.SelectQuery;

/**
 * ...
 *
 * @author robin
 */
public interface SqlQueryVisitor<R> {

    R visit(SelectQuery selectQuery) throws Exception;

}
