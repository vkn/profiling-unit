package io.github.vkn.profile.internal;

import io.github.vkn.profile.ProfiledTest;
import io.github.vkn.profile.Type;
import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A JUnit Jupiter extension that provides test template invocation contexts for
 * profiling test methods annotated with {@link ProfiledTest}. This extension
 * handles the lifecycle of profiling sessions, including initialization,
 * warm-up iterations, and capturing profiling data according to the specified
 * configuration.
 */
public class ProfilingExtension implements TestTemplateInvocationContextProvider, BeforeEachCallback, AfterEachCallback {
    private static final Logger LOGGER = Logger.getLogger(ProfilingExtension.class.getName());
    public static final String _KEY_PROFILER = ProfilingExtension.class.getName() + "syncProfilerKey";
    public static final String _KEY_COUNT = ProfilingExtension.class.getName() + "syncProfilerKeyCnt";
    public static final String _KEY_WARMUP = ProfilingExtension.class.getName() + "syncProfilerKeyWarmUpCnt";
    private ExtensionContext.Namespace namespace;


    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        AtomicInteger warmUpCount = getStore(context).get(_KEY_WARMUP, AtomicInteger.class);
        if(warmUpCount == null) {
            // method not profiled
            return;
        }
        var warmUp = getWarmUpCount(context);
        if (warmUpCount.getAndIncrement() < warmUp) {
            LOGGER.log(Level.FINE, "Warm-up iteration %d".formatted(warmUpCount.get()));
            return;
        }
        Profiler profiler = getStore(context).get(_KEY_PROFILER, Profiler.class);
        AtomicInteger profilingCount = getStore(context).get(_KEY_COUNT, AtomicInteger.class);
        profilingCount.getAndIncrement();
        profiler.start();

    }

    @Override
    public void afterEach(ExtensionContext context) {
        int warmUp = getWarmUpCount(context);
        AtomicInteger profilingCount = getStore(context).get(_KEY_COUNT, AtomicInteger.class);
        AtomicInteger warmUpCount = getStore(context).get(_KEY_WARMUP, AtomicInteger.class);
        if(profilingCount == null) {
            // method not profiled
            return;
        }
        if (warmUpCount.get() < warmUp+1) {
            return;
        }
        Profiler profiler = getStore(context).get(_KEY_PROFILER, Profiler.class);

        LOGGER.log(Level.FINE, "Profiling iteration %s".formatted(profilingCount.get()));
        if (profilingCount.get() >= getRepeatCount(context)) {
            profiler.stop();
        }
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        namespace = getNamespace(context);
        ExtensionContext.Store store = getStore(context);
        store.put(_KEY_WARMUP, new AtomicInteger());
        store.put(_KEY_COUNT, new AtomicInteger());

        String fileName = context.getTestMethod().map(Method::getName).orElse("nomethod");
        String className = context.getTestMethod()
                .map(Method::getDeclaringClass)
                .map(Class::getName)
                .orElse("");
        Path dir = dumpDir(context.getTestMethod().orElse(null));
        String events = getProfilingEvents(context);
        Type type = getProfilingType(context);
        String filePath = dir.resolve(className + "." + fileName).toAbsolutePath().toString();
        LOGGER.info("File: %s".formatted(filePath));
        store.put(_KEY_PROFILER, getProfiler(type, events, filePath));

        int repeatCount = getRepeatCount(context);
        int warmUpCount = getWarmUpCount(context);
        int total = repeatCount + warmUpCount;
        TestTemplateInvocationContext ctx = getInvocationContext(repeatCount, warmUpCount);
        return Stream.generate(() -> ctx)
                .limit(total);
    }

    Profiler getProfiler(Type type, String events, String absoluteDirPath) {
        return new AsyncProfiler(type, events, absoluteDirPath);
    }

    Path dumpDir(Method testMethod) {
        try {
            URI testSourceUri = testMethod.getDeclaringClass().getProtectionDomain().getCodeSource().getLocation()
                    .toURI();
            return Files.createDirectories(Path.of(testSourceUri).getParent().resolve("profiling"));
        }
        catch (FileSystemNotFoundException e) {
            try {
                return Files.createTempDirectory(null);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }


    private static TestTemplateInvocationContext getInvocationContext(int repeat, int warmUp) {
        return new ProfiledTestTemplateInvocationContext(repeat, warmUp);
    }

    private int getRepeatCount(ExtensionContext context) {
        return getAnnotation(context).map(ProfiledTest::repeat).orElse(1);
    }


    private String getProfilingEvents(ExtensionContext context) {
        return getAnnotation(context).map(ProfiledTest::event).orElse("cpu,alloc,lock");
    }

    private Type getProfilingType(ExtensionContext context) {
        return getAnnotation(context).map(ProfiledTest::type).orElse(Type.JFR);
    }

    private int getWarmUpCount(ExtensionContext context) {
        return getAnnotation(context).map(ProfiledTest::warmup).orElse(0);
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(namespace);
    }

    private static Optional<ProfiledTest> getAnnotation(ExtensionContext context) {
        return Optional.ofNullable(context.getRequiredTestMethod().getAnnotation(ProfiledTest.class));
    }

    private ExtensionContext.Namespace getNamespace(ExtensionContext context) {
        return ExtensionContext.Namespace.create(getClass().getName() + context.getRequiredTestMethod().getName());
    }

}
