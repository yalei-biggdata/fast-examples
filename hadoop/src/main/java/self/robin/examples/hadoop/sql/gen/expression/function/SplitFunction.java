package self.robin.examples.hadoop.sql.gen.expression.function;

import com.iqiyi.pat.meta.corpus.sql.gen.expression.Expression;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-10-21 12:57
 */
public class SplitFunction extends Function {

    private int index;

    public SplitFunction(Expression... parameters) {
        super("split", parameters);
        this.index = index;
    }

    /**
     * 创建一个新的
     *
     * @param index
     * @return
     */
    public SplitFunction index(int index) {
        this.index = index;
        SplitFunction func = new SplitFunction(this.getParameters());
        func.index = index;
        return func;
    }

    @Override
    public String asSummaryString() {
        return super.asSummaryString() + "[" + index + "]";
    }

}
