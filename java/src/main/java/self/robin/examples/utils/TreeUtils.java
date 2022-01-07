package self.robin.examples.utils;

import lombok.Data;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TreeUtils {

    /**
     * 根据提供<T extends @TreeNode>的集合构造树状结构
     * 注意：保持parentId，primaryKey，firstLevel类型的一致
     *
     * @param collection {@link TreeNode} 子类的集合
     * @param parentId   {@link Function}类型，集合中元素引用的字段（关联字段）
     * @param primaryKey {@link Function}类型
     * @param firstLevel 关联字段的第一层的值
     * @return 返回一个树林
     */
    public static <K, T extends TreeNode<T>> List<T> buildTreesRecurse(Collection<T> collection, Function<T, K> parentId, Function<T, K> primaryKey, K firstLevel) {
        validData(collection, parentId, primaryKey, firstLevel);
        //分组递归
        Map<K, List<T>> levelGroup = collection.stream().collect(Collectors.groupingBy(parentId));
        return recurseBuildLevel(levelGroup, primaryKey, firstLevel);
    }

    private static <K, T extends TreeNode<T>> void validData(Collection<T> collection, Function<T, K> parentId, Function<T, K> primaryKey, K firstLevel) {
        //类型一致性检查
        T t = collection.iterator().next();
        boolean uniformity = TypeUtils.equals(parentId.apply(t).getClass(), primaryKey.apply(t).getClass())
                && (firstLevel == null ? true : TypeUtils.equals(parentId.apply(t).getClass(), firstLevel.getClass()));
        if (!uniformity) {
            throw new RuntimeException("parentId, primaryKey, firstLevel 类型不一致");
        }
    }

    /**
     * {@see buildTrees}
     */
    private static <K, T extends TreeNode<T>> List<T> recurseBuildLevel(Map<K, List<T>> levelGroup, Function<T, K> primaryKey, K level) {
        if (levelGroup == null) {
            return new ArrayList<>();
        }
        List<T> currentLevel = levelGroup.get(level);
        if (currentLevel == null || currentLevel.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> retList = new ArrayList<>();
        for (T t : currentLevel) {
            K nextLevel = primaryKey.apply(t);
            List<T> nextLevelList = recurseBuildLevel(levelGroup, primaryKey, nextLevel);
            t.setKids(nextLevelList);
            retList.add(t);
        }
        return retList;
    }

    public static <K, T extends TreeNode<T>> List<T> buildTreesWhile(Collection<T> collection, Function<T, K> parentId, Function<T, K> primaryKey) {
        validData(collection, parentId, primaryKey, null);
        //分组递归
        Map<K, T> primaryKeyMap = collection.stream().collect(Collectors.toMap(primaryKey, o -> o));
        Iterator<Map.Entry<K, T>> ite = primaryKeyMap.entrySet().iterator();

        List<T> treeList = new ArrayList<>();
        while (ite.hasNext()) {
            Map.Entry<K, T> one = ite.next();
            K parentKey = parentId.apply(one.getValue());
            T parent = primaryKeyMap.get(parentKey);
            if (parent == null || Objects.equals(parentKey, one.getKey())) {
                treeList.add(one.getValue());
                continue;
            }
            List<T> kids = parent.getKids();
            if (kids == null || kids.isEmpty()) {
                kids = new ArrayList<>();
                parent.setKids(kids);
            }
            kids.add(one.getValue());
        }
        return treeList;
    }

    @Data
    public static class TreeNode<T> {
        /**
         * 子树节点
         */
        private List<T> kids;
    }

    /**
     * 按节点访问所有数据
     */
    public static <T> void visitNode(List<T> collection, Consumer<T> node) throws Exception {
        visit(collection, level -> {
        }, node);
    }

    /**
     * 按层访问所有数据
     */
    public static <T> void visitLevel(List<T> collection, Consumer<List<T>> level) throws Exception {
        visit(collection, level, node -> {
        });
    }

    /**
     * 访问具有树形结构的数据
     * 结构类似：
     * class T{
     * List<T> list;
     * }
     *
     * @param collection 数据集合
     * @param node       按节点访数据
     * @param <T>
     * @throws Exception 可能发生 IllegalAccessException
     * @Param level 按层访问数据
     */
    public static <T> void visit(List<T> collection, Consumer<List<T>> level, Consumer<T> node) throws Exception {
        if (collection == null) {
            return;
        }
        level.accept(collection);
        for (T t : collection) {
            node.accept(t);
            Field[] fields = t.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (!List.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                ParameterizedType pt = (ParameterizedType) field.getGenericType();
                Class<?> clazz = ((Class) pt.getActualTypeArguments()[0]);
                if (clazz == t.getClass()) {
                    visit((List<T>) field.get(t), level, node);
                }
            }
        }
    }

}
