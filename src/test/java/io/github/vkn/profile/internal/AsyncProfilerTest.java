package io.github.vkn.profile.internal;

import io.github.vkn.profile.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncProfilerTest {

    @Test
    void canRun(@TempDir Path tmpDir) {
        var rand = ThreadLocalRandom.current().nextInt();
        var absolutePath = tmpDir.resolve("testresult" + rand).toAbsolutePath();
        var profiler = new AsyncProfiler(Type.JFR, "cpu,lock", absolutePath.toString());
        profiler.start();
        profiler.stop();
        assertThat(Files.exists(Path.of(absolutePath + "_cpu_lock.jfr"))).isTrue();
    }

}