package self.robin.javaagent.util;

import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.LocalVariableTypeAttribute;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/4/22 17:28
 */
public class JavassistUtil {

    private final static ConcurrentHashMap<ClassLoader, ClassPool> CLASS_POOL_MAP = new ConcurrentHashMap<>();

    /**
     * 不同的ClassLoader返回不同的ClassPool
     *
     * @param loader
     * @return
     */
    public static ClassPool getClassPool(ClassLoader loader) {
        if (null == loader) {
            return ClassPool.getDefault();
        }

        ClassPool pool = CLASS_POOL_MAP.get(loader);
        if (null == pool) {
            pool = new ClassPool(true);
            pool.appendClassPath(new LoaderClassPath(loader));
            CLASS_POOL_MAP.put(loader, pool);
        }
        return pool;
    }

    public static ClassPool getDefaultClassPool() {
        ClassPool classPool = getClassPool(JavassistUtil.class.getClassLoader());
        classPool.childFirstLookup = true;
        return classPool;
    }

    public static int getVariableLineNum(CodeAttribute codeAttribute, String varName) {
        Objects.requireNonNull(codeAttribute);
        LocalVariableAttribute va = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        LineNumberAttribute la = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);

        int startPc = -1;
        for (int i = 0; i < va.tableLength(); ++i) {
            if (Objects.equals(va.variableName(i), varName)) {
                startPc = va.startPc(i);
                break;
            }
        }
        if (startPc == -1) {
            return -1;
        }
        return la.toLineNumber(startPc);
    }


    public static Map<String, Integer> getLineNumsByName(CodeAttribute codeAttribute, String name) {
        return getLineNumsByName(codeAttribute, name, null);
    }

    /**
     * ...
     *
     * @param codeAttribute
     * @param name          变量类型全限定名
     * @param genericName   泛型全限定名
     * @return
     */
    public static Map<String, Integer> getLineNumsByName(CodeAttribute codeAttribute, String name, String genericName) {
        Objects.requireNonNull(codeAttribute);
        Objects.requireNonNull(name);

        LineNumberAttribute la = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);
        LocalVariableAttribute va;
        String descriptor;
        if (StringUtils.isBlank(genericName)) {
            va = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
            descriptor = "L" + name.replaceAll("\\.", "/") + ";";
        } else {
            va = (LocalVariableTypeAttribute) codeAttribute.getAttribute(LocalVariableTypeAttribute.tag);
            descriptor = "L" + name.replaceAll("\\.", "/") +
                    "<L" + genericName.replaceAll("\\.", "/") + ";>;";
        }
        Map<String, Integer> varLineNumMap = new HashMap<>();
        for (int i = 0; i < va.tableLength(); ++i) {
            if (Objects.equals(descriptor, va.descriptor(i))) {
                varLineNumMap.put(va.variableName(i), la.toLineNumber(va.startPc(i)));
            }
            System.out.println(va.descriptor(i));
        }
        return varLineNumMap;
    }

}
