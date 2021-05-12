package self.robin.examples.utils.query;

import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * @author mrli
 * @date 2020/7/31
 */
public class QueryResponse<T> {

    private String taskId;

    private List<T> result;

    private String sql;

    private Throwable throwable;

    public QueryResponse(String taskId, String sql, List<T> result, Throwable throwable) {
        this.taskId = taskId;
        this.sql = sql;
        this.result = result;
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public State getState(){
        if(throwable instanceof CancellationException){
            return State.CANCELED;
        }
        else if(throwable == null){
            return State.SUCCESS;
        }else {
            return State.FAILED;
        }
    }

    public String getTaskId() {
        return taskId;
    }

    public List<T> getResult() {
        return result;
    }

    public String getSql() {
        return sql;
    }

    public enum State{
        FAILED,SUCCESS,CANCELED
    }

}