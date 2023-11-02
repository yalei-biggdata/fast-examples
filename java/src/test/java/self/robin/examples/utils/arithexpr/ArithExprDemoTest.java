package self.robin.examples.utils.arithexpr;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Variable;

import javax.script.*;
import java.util.concurrent.TimeUnit;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-06-05 10:36
 */
@Fork(0)
@State(Scope.Thread)
public class ArithExprDemoTest {

    ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine engine = mgr.getEngineByName("JavaScript");
    CompiledScript compile;

    {
        try {
            compile = ((Compilable) engine).compile("2 * (7 - 5) * 3.14159 * x^(12-10) * Math.sin(-3.141)");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    @Threads(2)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 50, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    //@Benchmark
    public Object shift() throws Exception {
        Bindings bindings = new SimpleBindings();
        bindings.put("x", 3);
        return compile.eval(bindings);
    }

    @Threads(2)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 50, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    @Benchmark
    public Object parsii() throws Exception {
        parsii.eval.Scope scope = new parsii.eval.Scope();
        Expression expr = Parser.parse("((x–0.5)^2+1)*(1+y)*(1+z)", scope);
        Variable x = scope.getVariable("x");
        x.setValue(4);
        Variable y = scope.getVariable("y");
        y.setValue(4);
        Variable z = scope.getVariable("z");
        z.setValue(4);
        return expr.evaluate();
    }

    public static void main(String[] args) throws Exception {
        parsii.eval.Scope scope = new parsii.eval.Scope();
        // ((x – 0.5) * (x - 0.5) +1)*(1 +y)*(1+z)
        Expression expr = Parser.parse("(o/30-0.5)^0.5", scope);
//        Variable x = scope.getVariable("x");
//        x.setValue(3.5);
        Variable y = scope.getVariable("m");
        y.setValue(0);
        Variable z = scope.getVariable("n");
        z.setValue(4);
        Variable o = scope.getVariable("o");
        o.setValue(10);
        double value = expr.evaluate();
        System.out.println(value);

//        Options opts = new OptionsBuilder()
//                .include(ArithExprDemoTest.class.getSimpleName())
//                .resultFormat(ResultFormatType.JSON)
//                .build();
//        new Runner(opts).run();
    }

}