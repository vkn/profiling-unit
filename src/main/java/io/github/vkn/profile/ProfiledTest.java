package io.github.vkn.profile;

import org.junit.jupiter.api.TestTemplate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a test method for profiling, indicating it should be executed
 * with specific profiling configurations. This includes the type of events to profile,
 * the profiling output type, and the number of repeat and warm-up iterations.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
public @interface ProfiledTest {

    /**
     * Specifies the profiling events to be captured. Default events are CPU usage,
     * memory allocation, and lock contention.
     *
     * For more information see  <a href="https://github.com/async-profiler/async-profiler#value">Async Profiler documentation</a>
     *
     * @return A comma-separated list of profiling events.
     */
    String event() default "cpu,alloc,lock";

    /**
     * Specifies the output type of the profiling data, e.g., Java Flight Recorder (JFR) or flame graphs.
     *
     * @return The type of the profiling output.
     */
    Type type() default Type.JFR;

    /**
     * Specifies the number of times the test should be repeated during the profiling session.
     *
     * @return The repeat count.
     */
    int repeat() default 1;

    /**
     * Specifies the number of initial warm-up invocations before the actual test invocations begin.
     * This is used to allow for JVM warm-up and JIT compilation.
     *
     * @return The warm-up count.
     */
    int warmup() default 0;
}
