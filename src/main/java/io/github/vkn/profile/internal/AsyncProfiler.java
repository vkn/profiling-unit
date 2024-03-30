package io.github.vkn.profile.internal;


import io.github.vkn.profile.Type;

import java.io.IOException;
import java.util.logging.Logger;

import static io.github.vkn.profile.Type.JFR;
import static java.util.logging.Level.INFO;

class AsyncProfiler implements Profiler {
    private static final Logger LOGGER = Logger.getLogger(AsyncProfiler.class.getName());
    private final String file;
    private final String command;
    private final Type type;
    private boolean isStarted;

    private final one.profiler.AsyncProfiler profiler = one.profiler.AsyncProfiler.getInstance();

    AsyncProfiler(Type type, String profilingEvent, String filename) {
        this.type = type;
        command = "%sevent=%s".formatted(type == JFR ? "jfr," : "", profilingEvent);

        String suffix = "_%s".formatted(profilingEvent.replace(",", "_"));
        String ext = type == JFR ? "jfr" : "html";
        file = "%s%s.%s".formatted(filename, suffix, ext);

    }

    @Override
    public void start() {
        if (isStarted) {
            return;
        }
        LOGGER.log(INFO, "Start profiling");
        try {
            String cmd = String.format("start,%s,%sfile=%s", command, (type == JFR ? "jfrsync=profile,": ""), file);
            LOGGER.info(cmd);
            profiler.execute(cmd);
            isStarted = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        if (!isStarted) {
            return;
        }
        try {
            LOGGER.log(INFO, "Stop profiling");
            profiler.execute(String.format("stop,file=%s", file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            isStarted = false;
        }
    }
}
