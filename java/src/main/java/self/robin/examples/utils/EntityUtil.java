package self.robin.examples.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class EntityUtil {

    /**
     * 将实体中的null和空字符串，都统一成null.
     */
    public static void blankFmt2Null(Object entity) {
        blankFmt2Default(entity, null);
    }

    /**
     * 将实体属性为空的，赋默认值
     */
    public static void blankFmt2Default(Object entity, String defaultVal) {
        Class<?> clazz = entity.getClass();


        Field[] fields = clazz.getDeclaredFields();
        try {
            Arrays.asList(fields).stream().forEach(field -> {
                try {
                    field.setAccessible(true);
                    if (String.class.equals(field.getType()) && StringUtils.isBlank(String.valueOf(field.get(entity)))) {
                        field.set(entity, defaultVal);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <R> R defaultValueIfNull(R r, R defaultValue) {
        return r == null ? defaultValue : r;
    }

    public static <R> R getSafely(Supplier<R> func) {
        try {
            return func.get();
        } catch (Throwable e) {
            Logger.getLogger(EntityUtil.class.getSimpleName()).warning(e.getMessage());
            return null;
        }
    }

    public static String getString(Map obj, String key) {
        return obj.get(key) == null ? null : String.valueOf(obj.get(key));
    }

    public static Integer getInteger(Map obj, String key) {
        return obj.get(key) == null ? null : Integer.valueOf(String.valueOf(obj.get(key)));
    }

    public static Long getLong(Map obj, String key) {
        return obj.get(key) == null ? null : Long.valueOf(String.valueOf(obj.get(key)));
    }

    /**
     * 比较两个对象的值，把变化列举出来。
     * 返回值 Map<属性名,旧值:新值>
     *
     * @param old
     * @param fresh
     * @param <T>
     * @return
     */
    public static <T> Map diff(T old, T fresh) {

        Field[] fields = old.getClass().getDeclaredFields();

        try {
            Map map = new HashMap();
            for (Field f : fields) {
                f.setAccessible(true);
                if (f.get(old) == null && f.get(fresh) == null) {

                } else if (f.get(old) == null && f.get(fresh) != null) {
                    map.put(f.getName(), f.get(old) + ":" + f.get(fresh));
                } else if (f.get(old) != null) {
                    if (!f.get(old).equals(f.get(fresh))) {
                        map.put(f.getName(), f.get(old) + ":" + f.get(fresh));
                    }
                }
            }

            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将集合字典化
     * 根据指定的字段，转换成keyValue的集合，方便做字典集合
     *
     * @param collection
     * @param func
     * @param <T>
     * @param <K>
     * @return
     */
    public static <T, K> Map<K, T> dictionary(Collection<T> collection, Function<T, K> func) {
        if(collection==null || collection.isEmpty()){
            return new HashMap<>();
        }

        Iterator<T> ite = collection.iterator();

        Map<K, T> reMap = new HashMap<>();
        while (ite.hasNext()) {
            T obj = ite.next();
            K key = func.apply(obj);
            reMap.put(key, obj);
        }
        return reMap;
    }


    public static <T, K, V> Map<K, V> dictionary(Collection<T> collection, Function<T, K> func, Function<T, V> func2) {
        Iterator<T> ite = collection.iterator();

        Map<K, V> reMap = new HashMap<>();
        while (ite.hasNext()) {
            T obj = ite.next();

            K key = func.apply(obj);
            V value = func2.apply(obj);
            reMap.put(key, value);
        }
        return reMap;
    }

    /**
     * 比较两个版本v1 和 v2的大小
     * @param v1
     * @param v2
     * @return -1 ,0, 1
     */
    public static int compareVersion(String v1, String v2) {
        if (v1.equals(v2)) {
            return 0;
        }
        String[] version1Array = v1.split("[._]");
        String[] version2Array = v2.split("[._]");
        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        long diff = 0;

        while (index < minLen
                && (diff = Long.parseLong(version1Array[index])
                - Long.parseLong(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Long.parseLong(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Long.parseLong(version2Array[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }

    public static <T extends Serializable> T clone(T object) {
        try {
            //将对象写到流里
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(object);
            //从流里读出来
            ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
            ObjectInputStream oi = new ObjectInputStream(bi);
            return (T) (oi.readObject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T ifNullDefault(T t, T defaultValue) {
        return t == null ? defaultValue : t;
    }

    /**
     * 安全的获取一个元素, data可以为任意值；
     * 若data为集合则返回集合的第一个元素，若为对象直接返回对象，
     * 并将返回值包装成optional对象。
     *
     * @param data     原始数据data
     * @param consumer 返回对象的同时，提供一个回调方法，方法的入参为原始数据data.
     * @param <T>
     */
    public static <T, E> Optional<T> fetchOneSafely(E data, Consumer<E> consumer) {

        if (data == null) {
            return Optional.empty();
        }

        if (Collection.class.isAssignableFrom(data.getClass())) {
            Collection<T> result = (Collection<T>) data;
            if (result.isEmpty()) {
                return Optional.empty();
            }
            if (result.size() > 1) {
                try {
                    if (consumer != null) {
                        consumer.accept(data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return Optional.of(result.iterator().next());
        } else {
            return Optional.of((T) data);
        }
    }

    /**
     * 安全的获取一个元素, data可以为任意值；
     * 若data为集合则返回集合的第一个元素，若为对象直接返回对象，
     * 并将返回值包装成optional对象。
     *
     * @param data 原始数据data
     * @param <T>
     */
    public static <T, E> Optional<T> fetchOneSafely(E data) {
        return fetchOneSafely(data, null);
    }


    /**
     * 按照实体类clazz整理数据
     * @param targetClazz
     * @param <T>
     * @return
     */
    public static <T,E> List<T> wrinkle(List<E> source, Class<T> targetClazz){
        if(source==null || source.isEmpty() || targetClazz==null){
            return new ArrayList<T>();
        }

        Field[] fields = targetClazz.getDeclaredFields();
        Map<String, Class<?>> targetFieldsMap = new HashMap<>();
        for (Field field : fields) {
            targetFieldsMap.put(field.getName(), field.getType());
        }

        Class<? extends Object> srcClazz = source.get(0).getClass();
        Map<String, Field> srcFieldsMap = new HashMap<>();
        for (Field field : srcClazz.getDeclaredFields()) {
            srcFieldsMap.put(field.getName(), field);
        }

        HashKeyList<T> list = new HashKeyList<>();
        try{
            for (E e : source) {
                recursion(list, targetClazz, srcFieldsMap, e);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    private static void recursion(HashKeyList hashKeyList, Class targetClass, Map<String,Field> srcFieldMap, Object srcObj) throws Exception{

        List<Field> singleFields = new ArrayList<>();//基本类型字段
        List<Field> collectionFields = new ArrayList<>();//集合类型

        for (Field targetField : targetClass.getDeclaredFields()) {
            if(Collection.class.isAssignableFrom(targetField.getType())) {
                collectionFields.add(targetField);
            } else {
                singleFields.add(targetField);
            }
        }

        //取非集合字段组key
        String key = "";
        Object tmpTarget = targetClass.newInstance();
        for (Field singleField : singleFields) {
            Field srcField = srcFieldMap.get(singleField.getName());
            if(srcField==null){
                continue;
            }
            srcField.setAccessible(true);
            Object srcValue = srcField.get(srcObj);
            singleField.setAccessible(true);
            singleField.set(tmpTarget, srcValue);
            key += srcValue;
        }

        Object targetObj = hashKeyList.getTarget(key);
        if(targetObj==null){
            targetObj = tmpTarget;
            hashKeyList.put(key, targetObj);
        }

        //递归停止条件
        if(collectionFields.isEmpty()){
            return;
        }

        //检查, 并赋值集合字段
        for (Field collectionField : collectionFields) {
            collectionField.setAccessible(true);
            Object targetCollValue = collectionField.get(targetObj);
            //检查集合字段是否为null
            if(targetCollValue==null){
                targetCollValue = new HashKeyList<>();
                collectionField.set(targetObj, targetCollValue);
            }
            Type targetCollFieldType = collectionField.getGenericType();

            ParameterizedType pt = (ParameterizedType) targetCollFieldType;
            Class<?> clazz = ((Class) pt.getActualTypeArguments()[0]);

            //如果clazz是基本类型的停止递归
            if(isPrimitive(clazz)){
                Field srcField = srcFieldMap.get(collectionField.getName());
                if(srcField==null){
                    continue;
                }
                srcField.setAccessible(true);
                Object srcValue = srcField.get(srcObj);
                if(srcValue!=null){
                    ((HashKeyList) targetCollValue).add(srcValue);
                }
                continue;
            }

            recursion(((HashKeyList) targetCollValue), clazz, srcFieldMap, srcObj);
        }

    }

    public static boolean isPrimitive(Class<?> clazz){
        if(String.class==clazz || clazz.isPrimitive() || Number.class.isAssignableFrom(clazz)
                || Boolean.class==clazz || Date.class==clazz){
            return true;
        }
        return false;
    }

    static class HashKeyList<T> extends ArrayList<T>{
        private Map<String,T> inMap = new HashMap<>();

        public T getTarget(String key){
            return this.inMap.get(key);
        }

        public void put(String key,T t){
            inMap.put(key, t);
            this.add(t);
        }
    }

    public static List<String> underscoreToCamelCase(List<String> names){
        List<String> retName = new ArrayList<>();
        if(names == null){
            return retName;
        }
        for (String name : names) {
            retName.add(underscoreToCamelCase(name));
        }
        return retName;
    }

    /**
     * 连字符命名转驼峰命名
     * @return
     */
    public static String underscoreToCamelCase(String name){
        int startIndex = 0;
        int index;

        StringBuilder builder = new StringBuilder();
        do{
            index = name.indexOf("_", startIndex);
            if(index < name.length()) {
                String subStr;
                if(index==-1){
                    index = name.length();
                }
                subStr = name.substring(startIndex, index);
                if(startIndex!=0){
                    subStr = JavassistUtil.firstLetterToUpper(subStr);
                }
                builder.append(subStr);
                startIndex = index + 1;
            }else {
                break;
            }
        }while (startIndex < name.length());
        return builder.toString();
    }

    public static List<String> buildOrderedDateListBy(String startDate,String endDate){
        LocalDate lsd = LocalDate.parse(startDate);
        LocalDate led = LocalDate.parse(endDate);

        List<String> retList = new ArrayList<>();
        while (!lsd.isAfter(led)){
            retList.add(lsd.toString());
            lsd = lsd.plusDays(1);
        }
        return retList;
    }

}
