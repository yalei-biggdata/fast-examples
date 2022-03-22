package self.robin.examples.utils;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/3/9 11:50
 */
public class ObjectUtilsX extends org.apache.commons.lang3.ObjectUtils {

    /**
     * 合并 o1, o2 两个对象
     * 当o1, o2其中有一个为null时，返回返回非null那个对象; 全为null时，返回null
     * 都不为null时，将o2的字段值合并到 o1中。
     * <p>
     * 合并规则：
     * 1. 值相同，保留o1中的字段值
     * 2. 值不同，a.有一个不为null，保留非null的那个值
     * b.都不为null，由用户决定如何合并（调用operationWhenConflict方法，方法返回值为合并后的值，将赋值给o1的当前字段）
     * <p>
     * 注意：合并的属性需要提供 set, get方法
     *
     * @param o1
     * @param o2
     * @param operationWhenConflict 当发生字段不一致时候用户的操作
     *                              Field：发生冲突的字段，Pair: o1,o2中对应的冲突字段值
     * @param <T>                   T: 待合并的对象，V:对象中的字段的值
     */
    public static <T, V> T merge(T o1, T o2, BiFunction<Field, Pair<V, V>, V> operationWhenConflict) throws IllegalAccessException, InvocationTargetException {

        if (o1 == null || o2 == null ? true : false) {
            return o1 == null ? o2 : o1;
        }
        Class<?> clazz = o1.getClass();
        //两者都不为null
        for (Field field : FieldUtils.getAllFieldsList(clazz)) {
            Method getMethod = getGetMethod(clazz, field.getName());
            Method setMethod = getSetMethod(clazz, field.getName(), new Class[]{field.getType()});
            if (getMethod == null || setMethod == null) {
                //没有set, get方法，跳过
                continue;
            }
            // 值不一致
            field.setAccessible(true);
            if (notEqual(field.get(o1), field.get(o2))) {
                Object v1 = field.get(o1);
                ;
                Object v2 = field.get(o2);
                if (v1 == null || v2 == null ? true : false) {
                    // 两者有且仅有一个为null
                    field.set(o1, v1 == null ? v2 : v1);
                    continue;
                }
                //两者都不为null
                field.set(o1, operationWhenConflict.apply(field, new ImmutablePair<>((V) v1, (V) v2)));
            }
        }
        return o1;
    }

    public static Method getGetMethod(Class<?> clazz, String fieldName) throws IllegalAccessException {
        return setGetMethod(clazz, fieldName, "get");
    }

    public static Method getSetMethod(Class<?> clazz, String fieldName, Class<?>[] args) throws IllegalAccessException {
        return setGetMethod(clazz, fieldName, "set", args);
    }

    private static Method setGetMethod(Class<?> clazz, String fieldName, String prefix, Class<?>... args) throws IllegalAccessException {
        if (fieldName == null || "".equals(fieldName)) {
            throw new IllegalAccessException("The filed name isn't present, name=" + fieldName);
        }
        String methodName = prefix;
        if (fieldName.length() == 1) {
            methodName += (fieldName.charAt(0) + "").toUpperCase();
        } else {
            methodName += (fieldName.charAt(0) + "").toUpperCase() + fieldName.substring(1);
        }
        Method method = MethodUtils.getAccessibleMethod(clazz, methodName, args);
        return method;
    }

//    /**
//     * 比较两个对象的内容（即字段）是否相等，
//     * 可指定忽略哪些字段，参见：EqualIgnored 注解
//     * <p>
//     * 注意：不会比较父类继承过来的属性
//     *
//     * @param t1
//     * @param t2
//     * @param deeply 目前实现较浅，没有实现
//     * @return
//     */
//    public static <T> boolean equalsX(T t1, T t2, boolean deeply) {
//        if (t1 == null || t2 == null) {
//            return t1 == t2;
//        }
//        Class<?> clazz = t1.getClass();
//        Field[] fields = clazz.getDeclaredFields();
//
//        boolean eq = true;
//        try {
//            for (Field field : fields) {
//                EqualIgnored anno = field.getAnnotation(EqualIgnored.class);
//                if (anno != null) {
//                    continue;
//                }
//                field.setAccessible(true);
//                eq = Objects.equals(field.get(t1), field.get(t2));
//                if (!eq) {
//                    break;
//                }
//            }
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
//        return eq;
//    }

    /**
     * @param t1
     * @param t2
     * @param deeply 是否检查父类字段
     * @param handle
     * @param <T>
     * @return
     */
    /*public static <T, U> void notEqWithHandle(T t1, T t2, boolean deeply, Consumer<Tuple3<U, U, Field>> handle) {
        if (t1 == null || t2 == null) {
            return;
        }
        Class<?> clazz = t1.getClass();
        Field[] fields = clazz.getDeclaredFields();
        try {
            do {
                for (Field field : fields) {
                    EqualIgnored anno = field.getAnnotation(EqualIgnored.class);
                    if (anno != null) {
                        continue;
                    }
                    field.setAccessible(true);
                    boolean eq = Objects.equals(field.get(t1), field.get(t2));
                    if (!eq) {
                        handle.accept(Tuple3.of(field.get(t1), field.get(t2), field));
                    }
                }
                if (deeply) {
                    Class<?> parentClazz = clazz.getSuperclass();
                    if (parentClazz == Object.class) {
                        break;
                    }
                    fields = parentClazz.getDeclaredFields();
                    clazz = parentClazz;
                } else {
                    break;
                }
            } while (true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }*/

    /**
     * 使用序列化，反序列化的方式，复制对象，要求被复制的对象必须可序列化
     *
     * @param t
     * @param <T>
     * @return
     */
    public static <T extends Serializable> T clone2(T t) throws NotSerializableException {
        if (t instanceof Serializable) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
                out.writeObject(t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
                return (T) ois.readObject();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

//    public static void appendValue(WorkbenchVo.Row row, SFunction<WorkbenchVo.Row, Long> func, Long value) {
//        try {
//            Objects.requireNonNull(row);
//            Long origin = func.apply(row);
//            SerializedLambda lambda = SerializedLambda.resolve(func);
//            // get field name
//            String methodName = lambda.getImplMethodName().replaceFirst("get", "");
//            methodName = Character.toLowerCase(methodName.charAt(0)) + (methodName.length() == 1 ? "" : methodName.substring(1));
//            // get field
//            Field filed = lambda.getImplClass().getDeclaredField(methodName);
//            filed.setAccessible(true);
//            // add and set value
//            Object v = origin == null ? (Object) value : (value == null ? (Object) origin : value + origin);
//            filed.set(row, v);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
