package self.robin.examples.utils.query;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;

import java.io.Closeable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author mrli
 * @date 2020/7/28
 */
public class ZkOperator implements Closeable {

    private ZkClient client;

    private String zkServer;

    /** 工作目录 */
    private String homePath;

    /** task目录 */
    private String taskPath;


    /** taskIds 回调 */
    private Consumer<List<String>> taskIdsConsumer;


    public ZkOperator(String zkServer, String homePath){
        this(zkServer, homePath, true);
    }

    /**
     * @param zkServer 集群地址
     * @param homePath 工作目录
     * @param lazyInit 是否延迟初始化
     */
    public ZkOperator(String zkServer, String homePath, boolean lazyInit){
        this.zkServer = zkServer;
        if(homePath.indexOf("\\")>=0){
            throw new RuntimeException("homePath 中不能包含 \\");
        }

        if(homePath.endsWith("/")){
            homePath = homePath.substring(0, homePath.length()-1);
        }
        this.homePath = homePath;
        this.taskPath = homePath + "/task";

        if(!lazyInit){
            getClient();
        }
    }

    public void setChildChangesOperate(Consumer<List<String>> taskIdsConsumer){
        this.taskIdsConsumer = taskIdsConsumer;
    }

    private ZkClient getClient() {
        if(client==null){
            //初始化客户端
            client = new ZkClient(zkServer);
            //初始化 tasks 目录
            if(!client.exists(taskPath)){
                client.createPersistent(taskPath, true);
            }
            //注册监听
            client.subscribeChildChanges(taskPath, (s, list) -> {
                if(taskIdsConsumer!=null){
                    taskIdsConsumer.accept(list);
                }
            });
        }
        return client;
    }

    /**
     * 申请一个唯一标识，系统重启后丢失
     * @return
     */
    public String applyId(){
        return applyId(false);
    }

    /**
     * 申请一个唯一标识
     * @param persistent 是否永久保存，系统重启也不会丢失
     */
    public String applyId(boolean persistent){
        ZkClient c = getClient();

        String idPrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yy"));
        String nodePathPrefix = getTaskPathPrefix()+idPrefix;

        String nodePath;
        if(persistent){
            nodePath = c.createPersistentSequential(nodePathPrefix, CreateMode.PERSISTENT_SEQUENTIAL.toFlag());
        }else {
            nodePath = c.createEphemeralSequential(nodePathPrefix, CreateMode.EPHEMERAL_SEQUENTIAL.toFlag());
        }
        return nodePath.replace(getTaskPathPrefix(),"");
    }

    /**
     * 如果节点已存在，创建会失败
     * 创建的节点为临时节点（系统重启会丢失）
     *
     * @param rowId 数据库表记录Id
     * @throws IllegalStateException
     */
    public void register(Object rowId) throws IllegalStateException{
        register(rowId, false);
    }

    /**
     * 如果节点已存在，创建会失败
     *
     * @param rowId 数据库表记录Id
     * @param persistent 是否永久保存，系统重启也不会丢失
     * @throws IllegalStateException
     */
    public void register(Object rowId, boolean persistent) throws IllegalStateException{
        if(rowId==null){
            throw new RuntimeException("rowId不能为空");
        }
        ZkClient c = getClient();
        String rowIdString = rowId.toString();
        String node = getTaskPathPrefix()+rowIdString;
        if(c.exists(node)){
            throw new IllegalStateException("node already exists for "+rowIdString);
        }
        if(persistent){
            c.createPersistent(node, CreateMode.PERSISTENT.toFlag());
        }else {
            c.createEphemeral(node, CreateMode.EPHEMERAL.toFlag());
        }
    }

    /**
     * 获取所有注册的id
     * @return
     */
    public List<String> getAllIds(){
        return getClient().getChildren(taskPath);
    }

    /**
     * 释放(移除)唯一标识
     */
    public void release(String id){
        ZkClient c = getClient();
        c.delete(getTaskPathPrefix()+id);
    }

    private String getTaskPathPrefix(){
        return taskPath+"/";
    }

    @Override
    public void close() {
        getClient().close();
    }

    /**
     * 路径是否存在
     * @param id
     * @return
     */
    public boolean exist(Object id) {
        return getClient().exists(getTaskPathPrefix()+id);
    }

}
