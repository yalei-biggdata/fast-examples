package self.robin.javaagent.transformer;

import javassist.*;
import javassist.bytecode.CodeAttribute;
import self.robin.javaagent.domain.Demo;
import self.robin.javaagent.util.JavassistUtil;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Objects;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-11-08 21:24
 */
public class DemoTransformer implements ClassFileTransformer {


    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        System.out.println("hello: " + className + ", " + classBeingRedefined.getClass().getName());
        try {
            return enhanceSay(className, classfileBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classfileBuffer;
    }

    /**
     * 创建代理对象
     *
     * @return
     * @throws Exception
     */
    private byte[] enhanceSay(String className, byte[] classFile)
            throws NotFoundException, IOException, CannotCompileException {
        String name = className.replaceAll("/", ".");
        if (!Objects.equals(name, Demo.class.getName())) {
            System.out.println("skip.");
            return classFile;
        }

        ClassPool pool = JavassistUtil.getDefaultClassPool();
        //代理类
        System.out.println(name);
        ByteArrayClassPath byteArrayClassPath = new ByteArrayClassPath(name, classFile);
        pool.insertClassPath(byteArrayClassPath);
        CtClass ctClass = pool.getCtClass(name);
        ctClass.setName(name);
        CtMethod sayMethod = ctClass.getDeclaredMethod("say");
        CodeAttribute codeAttribute = sayMethod.getMethodInfo2().getCodeAttribute();

        int lineNum = JavassistUtil.getVariableLineNum(codeAttribute, "tName");
        if (lineNum != -1) {
            String codeSrc = "tName += \" hello\";";
            sayMethod.insertAt(lineNum, codeSrc);
            System.out.println("insert done.");
            //方法实现
            return ctClass.toBytecode();
        }
        return classFile;
    }

}
