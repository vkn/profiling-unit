package io.github.vkn.profile.internal;

import io.github.vkn.profile.ProfiledTest;
import io.github.vkn.profile.ProfilingUnit;
import io.github.vkn.profile.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ProfilingExtensionTest {

    private static final String PROFILED_METHOD = "profiledMethod";
    private TestExtensionContext extensionContext;
    private TestedExtension extension;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        extensionContext = new TestExtensionContext();
        extensionContext.setMethod(TestClass.class.getDeclaredMethod(PROFILED_METHOD));
        extension = new TestedExtension();
    }

    @Test
    void defaults() throws NoSuchMethodException {
        var annotation = TestClass.class.getDeclaredMethod("defaults")
                .getAnnotation(ProfiledTest.class);
        assertThat(annotation.repeat()).isOne();
        assertThat(annotation.warmup()).isZero();
        assertThat(annotation.event()).isEqualTo("cpu,alloc,lock");
        assertThat(annotation.type()).isEqualTo(Type.JFR);
    }

    @Test
    void supportsTestTemplate() {
        assertThat(extension.supportsTestTemplate(extensionContext)).isTrue();
    }

    @Test
    void beforeEach() {
        extension.provideTestTemplateInvocationContexts(extensionContext);
        assertThatNoException().isThrownBy(() -> extension.beforeEach(extensionContext));
        assertThatNoException().isThrownBy(() -> extension.beforeEach(extensionContext));
    }

    @Test
    void afterEach() {
        extension.provideTestTemplateInvocationContexts(extensionContext);
        extension.beforeEach(extensionContext);
        extension.beforeEach(extensionContext);
        assertThatNoException().isThrownBy(() -> extension.afterEach(extensionContext));
        assertThatNoException().isThrownBy(() -> extension.afterEach(extensionContext));
    }

    @Test
    void provideTestTemplateInvocationContexts() {
        Stream<TestTemplateInvocationContext> stream = extension.provideTestTemplateInvocationContexts(extensionContext);
        assertThat(stream).isNotEmpty();
    }


    @Test
    void dumpDir() throws NoSuchMethodException {
        var path = getPath();
        assertThat(path.toString()).endsWith("target/profiling");
    }

    private Path getPath() throws NoSuchMethodException {
        Method profiledMethod = TestClass.class.getDeclaredMethod(PROFILED_METHOD);
        return extension.dumpDir(profiledMethod);
    }

    @Test
    void getProfiler() throws NoSuchMethodException {
        assertThat(extension.getProfiler(Type.JFR, "cpu", getPath().toString()))
                .isInstanceOf(Profiler.class);

        assertThat(new ProfilingExtension().getProfiler(Type.JFR, "cpu", getPath().toString()))
                .isInstanceOf(Profiler.class);
    }

    private static class TestedExtension extends ProfilingExtension {
        @Override
        Profiler getProfiler(Type type, String events, String absoluteDirPath) {
            return new TestProfiler();
        }
    }


    private static class TestProfiler implements Profiler {

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }
    }


    @SuppressWarnings("JUnitMalformedDeclaration")
    @ProfilingUnit
    private static class TestClass {

        @ProfiledTest(repeat = 1, warmup = 1)
        void profiledMethod() {
        }
        @ProfiledTest
        void defaults() {}
        @Test
        void methodNoAnnotation() {
        }

    }


    private static class TestExtensionContext implements ExtensionContext {
        private final TestStore store = new TestStore();
        private Method method;

        @Override
        public Optional<ExtensionContext> getParent() {
            return Optional.empty();
        }

        @Override
        public ExtensionContext getRoot() {
            return this;
        }

        @Override
        public String getUniqueId() {
            return "";
        }

        @Override
        public String getDisplayName() {
            return "unit test context";
        }

        @Override
        public Set<String> getTags() {
            return Set.of();
        }

        @Override
        public Optional<AnnotatedElement> getElement() {
            return Optional.empty();
        }

        @Override
        public Optional<Class<?>> getTestClass() {
            return Optional.empty();
        }

        @Override
        public Optional<TestInstance.Lifecycle> getTestInstanceLifecycle() {
            return Optional.empty();
        }

        @Override
        public Optional<Object> getTestInstance() {
            return Optional.empty();
        }

        @Override
        public Optional<TestInstances> getTestInstances() {
            return Optional.empty();
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        @Override
        public Optional<Method> getTestMethod() {
            return Optional.ofNullable(method);
        }

        @Override
        public Optional<Throwable> getExecutionException() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getConfigurationParameter(String s) {
            return Optional.empty();
        }

        @Override
        public <T> Optional<T> getConfigurationParameter(String s, Function<String, T> function) {
            return Optional.empty();
        }

        @Override
        public void publishReportEntry(Map<String, String> map) {

        }

        @Override
        public TestStore getStore(Namespace namespace) {
            return store;
        }

        @Override
        public ExecutionMode getExecutionMode() {
            return ExecutionMode.SAME_THREAD;
        }

        @Override
        public ExecutableInvoker getExecutableInvoker() {
            return null;
        }
    }


    private static class TestStore implements ExtensionContext.Store {

        private final Map<Object, Object> store = new ConcurrentHashMap<>();

        @Override
        public Object get(Object o) {
            return store.get(o);
        }

        @Override
        public <V> V get(Object o, Class<V> aClass) {
            return aClass.cast(store.get(o));
        }

        @Override
        @SuppressWarnings("unchecked")
        public <K, V> Object getOrComputeIfAbsent(K key, Function<K, V> function) {
            return store.computeIfAbsent(key, o -> function.apply((K) o));
        }

        @Override
        @SuppressWarnings("unchecked")
        public <K, V> V getOrComputeIfAbsent(K key, Function<K, V> function, Class<V> aClass) {
            return aClass.cast(store.computeIfAbsent(key, o -> function.apply((K) o)));
        }

        @Override
        public void put(Object o, Object o1) {
            store.put(o, o1);
        }

        @Override
        public Object remove(Object o) {
            return store.remove(o);
        }

        @Override
        public <V> V remove(Object o, Class<V> aClass) {
            return aClass.cast(store.remove(o));
        }

    }
}