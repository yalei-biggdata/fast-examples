package self.robin.examples.utils;

import javassist.ClassPool;
import javassist.LoaderClassPath;

import java.lang.reflect.Field;

public class JavassistUtil {

    private static ClassPool pool = null;

    public static synchronized ClassPool getClassPool() throws Exception{
        if(pool==null) {
            pool = new ClassPool(ClassPool.getDefault());
            pool.appendClassPath(new LoaderClassPath(JavassistUtil.class.getClassLoader()));
            pool.childFirstLookup = true;
        }
        return pool;
    }

    public static final String firstLetterToUpper(String propertyName){
        return  (propertyName.charAt(0)+"").toUpperCase() + propertyName.substring(1);
    }

    public static final Class<?> getFieldType(Class target, String fieldName) throws Exception{
        Field cla = getDeclaredField(target, fieldName);
        if(cla!=null){
            return cla.getType();
        }

        Class<?> superclass = target.getSuperclass();
        while (superclass!=null){
            cla = getDeclaredField(superclass, fieldName);
            if(cla!=null){
                return cla.getType();
            }
            superclass = superclass.getSuperclass();
        }
        throw new RuntimeException("Doesn't found field "+fieldName);
    }

    public static final Field getDeclaredFieldInFamily(Class target, String fieldName) throws Exception{
        Field field = getDeclaredField(target, fieldName);
        if(field!=null){
            return field;
        }
        Class clazz = target.getSuperclass();
        while (clazz!=Object.class){
            field = getDeclaredField(clazz, fieldName);
            if(field!=null){
                return field;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static final Field getDeclaredField(Class target, String fieldName){
        Field[] fields = target.getDeclaredFields();
        for (Field field : fields) {
            if(field.getName().equals(fieldName)){
                return field;
            }
        }
        return null;
    }

    public static final <T> Object getValueSafely(ThrowableSupplier<T> supplier){
        try{
            return supplier.get();
        }catch (Throwable throwable){
            throwable.printStackTrace();
            return null;
        }
    }

    public interface ThrowableSupplier<T>{
        /**
         * Gets a result.
         *
         * @return a result
         */
        T get() throws Throwable;
    }
}
