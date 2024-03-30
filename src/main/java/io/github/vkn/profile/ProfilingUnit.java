package io.github.vkn.profile;

import io.github.vkn.profile.internal.ProfilingExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a test class to enable profiling. Classes annotated with {@link ProfilingUnit}
 * will have the {@link ProfilingExtension} applied to them, enabling the profiling of methods
 * annotated with {@link ProfiledTest}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ProfilingExtension.class)
public @interface ProfilingUnit {
}
