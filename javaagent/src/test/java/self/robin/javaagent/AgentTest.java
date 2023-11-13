package self.robin.javaagent;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import org.junit.Assert;
import org.junit.Test;
import self.robin.javaagent.domain.Demo;
import self.robin.javaagent.util.JavassistUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-11-08 21:28
 */
public class AgentTest {

    @Test
    public void printDemo() throws InterruptedException {
        Demo demo = new Demo();
        Thread thread = new Thread(() -> {
            while (true) {
                System.out.println("print => " + demo.say());
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
    }

    private VirtualMachineDescriptor getVmDescriptor(String name) {
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            if (descriptor.displayName().contains(name)) {
                return descriptor;
            }
        }
        return null;
    }

    @Test
    public void testAttach() throws Exception {
        VirtualMachineDescriptor vmDescriptor = getVmDescriptor("printDemo");
        Objects.requireNonNull(vmDescriptor);
        try {
            VirtualMachine attach = VirtualMachine.attach(vmDescriptor);
            attach.loadAgent("D:/javaagent.jar", "run-" + System.currentTimeMillis());
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("attached");
    }

    @Test
    public void testLineNum() throws Exception {
        ClassPool classPool = JavassistUtil.getClassPool(AgentTest.class.getClassLoader());
        CtClass ctClass = classPool.get(Demo.class.getCanonicalName());
        CtMethod say = ctClass.getDeclaredMethod("say");
        CodeAttribute codeAttribute = say.getMethodInfo2().getCodeAttribute();
        LocalVariableAttribute va = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        LineNumberAttribute la = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);
        int laLen = la.tableLength();
        for (int i = 0; i < laLen; i++) {
            System.out.println(la.lineNumber(i) + ", " + la.toLineNumber(i) + ", " + la.toNearPc(i).line);
        }

        int vaLen = va.tableLength();
        for (int i = 0; i < vaLen; ++i) {
            System.out.println(va.descriptor(i) + ", " + va.variableName(i) + ", " + va.index(i));
        }
        System.out.println();
    }

    @Test
    public void testVarLineNum() throws Exception {
        ClassPool classPool = JavassistUtil.getClassPool(AgentTest.class.getClassLoader());
        CtClass ctClass = classPool.get(Demo.class.getCanonicalName());
        CtMethod say = ctClass.getDeclaredMethod("say");
        CodeAttribute codeAttribute = say.getMethodInfo2().getCodeAttribute();
        System.out.println("name lineNum=" + JavassistUtil.getVariableLineNum(codeAttribute, "tName"));
        System.out.println("idx lineNum=" + JavassistUtil.getVariableLineNum(codeAttribute, "startIdx"));
        System.out.println("st lineNum=" + JavassistUtil.getVariableLineNum(codeAttribute, "st"));
    }

    @Test
    public void testInsertCode() throws Exception {
        ClassPool classPool = JavassistUtil.getClassPool(AgentTest.class.getClassLoader());
        CtClass ctClass = classPool.get(Demo.class.getCanonicalName());
        CtMethod sayMethod = ctClass.getDeclaredMethod("say");
        CodeAttribute codeAttribute = sayMethod.getMethodInfo2().getCodeAttribute();
        int lineNum = JavassistUtil.getVariableLineNum(codeAttribute, "tName");
        Assert.assertTrue(lineNum != -1);
        String codeSrc = "tName += \" hello\";";
        sayMethod.insertAt(lineNum, codeSrc);
        ctClass.writeFile("./output");
    }

    @Test
    public void testInsetCodeBatch() throws Exception {
        ClassPool classPool = JavassistUtil.getClassPool(AgentTest.class.getClassLoader());
        CtClass ctClass = classPool.get(Demo.class.getCanonicalName());
        CtMethod sayMethod = ctClass.getDeclaredMethod("run");
        CodeAttribute codeAttribute = sayMethod.getMethodInfo2().getCodeAttribute();
        Map<String, Integer> map1 = JavassistUtil.getLineNumsByName(codeAttribute, Collection.class.getName());
        System.out.println("map1=" + map1);
        Map<String, Integer> map2 = JavassistUtil.getLineNumsByName(codeAttribute, Collection.class.getName(), Integer.class.getName());
        System.out.println("map2=" + map2);
    }

}
