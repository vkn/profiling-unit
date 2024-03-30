package io.github.vkn.profile.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfiledTestTemplateInvocationContextTest {
    @Test
    void testDisplayName() {
        test(0, 0, 1, "Profiling invocation 1/0"); // Edge case: no repeats, no warm-ups
        test(1, 0, 1, "Profiling invocation 1/1"); // Single repeat, no warm-ups
        test(2, 1, 1, "Warmup invocation 1/1"); // Warm-up phase
        test(2, 1, 2, "Profiling invocation 1/2"); // Profiling phase after warm-up
        test(5, 3, 4, "Profiling invocation 1/5"); // Profiling phase after multiple warm-ups
        test(3, 5, 1, "Warmup invocation 1/5"); // All warm-up, more warm-ups than repeats
        test(3, 5, 6, "Profiling invocation 1/3"); // Beyond warm-up phase, more warm-ups than repeats
    }

    void test(int repeat, int warmUp, int invocationIndex, String expectedDisplayName) {
        ProfiledTestTemplateInvocationContext context = new ProfiledTestTemplateInvocationContext(repeat, warmUp);
        String actual = context.getDisplayName(invocationIndex);
        assertThat(actual).isEqualTo(expectedDisplayName);
    }

}