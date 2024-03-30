package io.github.vkn.profile.internal;

import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

/**
 * Provides a context for test template invocations with support for distinguishing
 * between warm-up and actual test invocations. This context enhances test output
 * by labeling invocations as either warm-up or profiling, based on the configuration
 * provided at construction.
 */
class ProfiledTestTemplateInvocationContext implements TestTemplateInvocationContext {

    private final int repeat;
    private final int warmUp;

    /**
     * Constructs a new {@link ProfiledTestTemplateInvocationContext} with the specified
     * repeat and warm-up counts.
     *
     * @param repeat The number of times the test should be repeated.
     * @param warmUp The number of initial warm-up invocations before the actual test invocations begin.
     */
    ProfiledTestTemplateInvocationContext(int repeat, int warmUp) {
        this.repeat = repeat;
        this.warmUp = warmUp;
    }

    /**
     * Provides a display name for the test invocation based on its index, indicating
     * whether it is part of the warm-up or actual profiling phase.
     *
     * @param invocationIndex The index of the test invocation.
     * @return A string representing the display name for the test invocation.
     */
    @Override
    public String getDisplayName(int invocationIndex) {
        if (invocationIndex <= warmUp) {
            return "Warmup invocation %d/%d".formatted(invocationIndex, warmUp);
        }
        return "Profiling invocation %d/%d".formatted(invocationIndex - warmUp, repeat);
    }
}
