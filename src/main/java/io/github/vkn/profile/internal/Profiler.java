package io.github.vkn.profile.internal;

/**
 * Interface defining the operations for starting and stopping a profiler.
 * Implementations of this interface can encapsulate different profiling tools or mechanisms.
 */
interface Profiler {
    /**
     * Starts the profiler.
     */
    void start();

    /**
     * Stops the profiler and captures the collected profiling data.
     */
    void stop();
}
