package self.robin.examples.utils.arithexpr;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Variable;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

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

    @Threads(2)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    //@Benchmark
    public Object shift() throws Exception {
        engine.eval("40 + 20 - 100");
        engine.eval("40 * 88 / 31");
        engine.eval("Math.pow(5, 8) * 21");
        CompiledScript script = null;
        Bindings bindings = null;
        script.eval(bindings);
        return engine.eval("40+2");
    }

    @Threads(2)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    @Benchmark
    public Object parsii() throws Exception {
        parsii.eval.Scope scope = new parsii.eval.Scope();
        Variable a = scope.getVariable("a");
        Expression expr = Parser.parse("3 + a * 4 + sin(0)", scope);
        a.setValue(4);
        //System.out.println(expr.evaluate());
        a.setValue(5);
        //System.out.println(expr.evaluate());
        return a;
    }


    public static void main(String[] args) throws Exception {
        Options opts = new OptionsBuilder()
                .include(ArithExprDemoTest.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .build();
        new Runner(opts).run();
    }

}