# Profiling Extension for JUnit Jupiter

A JUnit 5 Extension leveraging the capabilities of the [Async Profiler](https://github.com/async-profiler/async-profiler)
This extension allows profiling of JUnit Jupiter test methods using  [Async Profiler](https://github.com/async-profiler/async-profiler)
including CPU usage, memory allocation, and lock contention. 

For detailed profiler configuration see [Async Profiler](https://github.com/async-profiler/async-profiler)

With support for warm-up iterations and repeated test executions, it provides results independent of any IDE plugins. 
This project is inspired by the [JfrUnit](https://github.com/moditect/jfrunit) project and
[IntelliJ profiler plugin](https://github.com/parttimenerd/intellij-profiler-plugin).

## Features

- **Warm-up Iterations**: Configure a number of warm-up iterations for your tests to ensure the JVM is optimally prepared, providing more accurate profiling results.
- **Repeated Test Executions**: Repeat your tests a specific number of times to gather comprehensive performance data.
- **Configure Async Profiler Events**: Profile CPU, memory allocation and others
- **Output Compatibility**: Generate profiling data in Java Flight Recorder (JFR) format or as flame graphs
- **Run from IDE or in command line**
- **IDE Independent**: No dependency on any IDE plugins, making it versatile and easy to integrate into any development workflow.

## Installation

Execute the following commands to make profiling possible

```shell
sysctl kernel.perf_event_paranoid=1
sysctl kernel.kptr_restrict=0
````

Clone the repository and run `mvn install`, then add the dependency in your pom.xml

```xml
<dependency>
    <groupId>io.github.vkn</groupId>
    <artifactId>profiling-unit</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### Usage
To get started, annotate your test class with `@ProfilingUnit` and your test methods with `@ProfiledTest`
instead of junit`@Test`. Configure the `@ProfiledTest` annotation to specify warm-up iterations, 
the number of repetitions, and the profiling events yo~~~~u're interested in.

```java
@ProfilingUnit
public class MyTest {

    @ProfiledTest(warmup = 5, repeat = 10, event = "cpu,alloc,lock", type = Type.JFR)
    public void myMethod() {
        // Your test code here
    }
}
```

This setup will run `MyTest.myMethod` with 5 warm-up iterations followed by 10 repetitions, 
profiling CPU usage, memory allocation, and lock contention, with the results output in JFR format.
The result file will be stored ander `target/profiling/` directory

![image](https://github.com/vkn/profiling-unit/assets/1523371/67b57012-fd52-4d49-8f9b-69fe22a14eaf)

The `@ProfilingUnit` can also be combined with `@QuarkusTest`

![image](https://github.com/vkn/profiling-unit/assets/1523371/55f54c58-010a-4d1c-b736-b6741f5b923b)

### Contributing
Contributions are welcome! If you have suggestions for improvements or encounter any issues, 
please feel free to open an issue or submit a pull request.

### License
This project is licensed under the Apache Licence 2.0. See the LICENSE file for more details.

