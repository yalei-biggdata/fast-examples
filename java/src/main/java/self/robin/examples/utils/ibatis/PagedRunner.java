package self.robin.examples.utils.ibatis;

import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import org.apache.ibatis.session.Configuration;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author mrli
 * @date 2020/8/3
 */
public class PagedRunner extends CurdRunner {

    public PagedRunner(DataSource dataSource, Configuration configuration) {
        super(dataSource, configuration);
    }

    public <T> Page<T> selectList(String id, Object param, Class<T> resultType, Page<T> page) {
        return selectList(id, param, resultType, page, null);
    }

    public <T> Page<T> selectList(String id, Object param, Class<T> resultType, Page<T> page, String... orderBy) {
        Assert.notNull(param, "param 不能为null");
        MappersParser.SqlModel sqlModel = super.mappersParser.getAndFlatParam(id, param);

        int parallel = 2;
        CountDownLatch countDownLatch = new CountDownLatch(parallel);
        AtomicLong total = new AtomicLong(0);
        new Thread(()->{
            total.set(countTotal(sqlModel));
            countDownLatch.countDown();
        }).start();
        List<T> pageData = new ArrayList<>();
        new Thread(()->{
            pageData.addAll(pageData(sqlModel, resultType, page, orderBy));
            countDownLatch.countDown();
        }).start();
        try {
            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        page.setTotal(total.get());
        page.addAll(pageData);
        return page;
    }

    public <T> Page<T> selectList(String sqlStr, Object[] params, Class<T> resultType, Page<T> page, String... orderBy) {
        return null;
    }

    public <T> Page<T> selectList(String sqlStr, Object[] params, Class<T> resultType, Page<T> page) {
        return null;
    }


    private <T> List<T> pageData(MappersParser.SqlModel sqlModel, Class<T> resultType, Page<T> page, String... orderBy){

        AtomicBoolean hasOrder = new AtomicBoolean(false);
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sqlModel.getSql(), com.alibaba.druid.util.JdbcUtils.MYSQL);
        SQLASTVisitor visitor = new SQLASTVisitorAdapter(){

            @Override
            public boolean visit(SQLOrderBy x) {
                if(orderBy!=null){
                    for (String s : orderBy) {
                        SQLSelectOrderByItem item = new SQLSelectOrderByItem();
                        item.setExpr(new SQLCharExpr(s));
                        x.addItem(item);
                        hasOrder.set(true);
                    }
                }
                return true;
            }

            @Override
            public boolean visit(SQLLimit x) {
                return super.visit(x);
            }
        };
        SQLStatement select = parser.parseSelect();
        select.accept(visitor);

        String sql = select.toString();
        if(!hasOrder.get()){
            sql += " ORDER BY "+ String.join(",", orderBy);
        }

        int offset = (page.getPageNum()>0?page.getPageNum()-1:0)*page.getPageSize();
        sql += " LIMIT "+offset+","+page.getPageSize();

        List<T> list = selectList(sql, sqlModel.getParams(), resultType);
        return list;
    }

    private Long countTotal(MappersParser.SqlModel sqlModel){

        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sqlModel.getSql(), com.alibaba.druid.util.JdbcUtils.MYSQL);
        SQLASTVisitor visitor = new SQLASTVisitorAdapter(){

            @Override
            public boolean visit(SQLSelect x) {
                List<SQLSelectItem> selectList = x.getFirstQueryBlock().getSelectList();
                selectList.clear();

                SQLAggregateExpr expr = new SQLAggregateExpr("count");
                expr.addArgument(new SQLCharExpr("1"));

                SQLSelectItem item = new SQLSelectItem();
                item.setExpr(expr);
                selectList.add(item);

                return super.visit(x);
            }

        };
        SQLStatement select = parser.parseSelect();
        select.accept(visitor);

        String countSqlStr = select.toString();
        Long count = selectOne(countSqlStr, sqlModel.getParams(), Long.class);

        return count==null ? Long.valueOf(0) : count.longValue();
    }

    public static class Page<T>{

        private int pageNum;

        private int pageSize;

        private long total;

        private List<T> data = new ArrayList<>();

        public Page() {
        }

        public void addAll(List<T> data){
            this.data.addAll(data);
        }

        public int getPageNum() {
            return this.pageNum;
        }

        public int getPageSize() {
            return this.pageSize;
        }

        public long getTotal() {
            return this.total;
        }

        public List<T> getData() {
            return this.data;
        }

        public void setPageNum(int pageNum) {
            this.pageNum = pageNum;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public void setData(List<T> data) {
            this.data = data;
        }

    }

}
