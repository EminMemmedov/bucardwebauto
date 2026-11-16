package az.bucard.reporting;

import az.bucard.config.Config;
import az.bucard.utils.Sanitizer;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class TelegramTestListener implements ITestListener, IExecutionListener {
    private static final AtomicInteger PASSED = new AtomicInteger();
    private static final AtomicInteger FAILED = new AtomicInteger();
    private static final AtomicInteger SKIPPED = new AtomicInteger();
    private static final List<String> FAILURE_SUMMARIES = new CopyOnWriteArrayList<>();
    private static Instant startedAt;

    @Override
    public void onExecutionStart() {
        startedAt = Instant.now();
        PASSED.set(0);
        FAILED.set(0);
        SKIPPED.set(0);
        FAILURE_SUMMARIES.clear();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        PASSED.incrementAndGet();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        FAILED.incrementAndGet();
        recordFailure(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        SKIPPED.incrementAndGet();
    }

    @Override
    public void onExecutionFinish() {
        writeAllureEnvironment();
        Instant endedAt = Instant.now();
        long seconds = startedAt == null ? 0 : Duration.between(startedAt, endedAt).toSeconds();
        int passed = PASSED.get();
        int failed = FAILED.get();
        int skipped = SKIPPED.get();

        StringBuilder sb = new StringBuilder();
        sb.append("Bucard Web Automation Summary\n");
        sb.append("Target: ").append(Config.baseUrl()).append("\n");
        sb.append("Result: ").append(passed).append(" passed, ")
                .append(failed).append(" failed, ")
                .append(skipped).append(" skipped\n");
        sb.append("Duration: ").append(seconds).append("s");

        if (failed > 0 && !FAILURE_SUMMARIES.isEmpty()) {
            sb.append("\n\nFailures:\n");
            int maxReported = Math.min(5, FAILURE_SUMMARIES.size());
            for (int i = 0; i < maxReported; i++) {
                sb.append("• ").append(FAILURE_SUMMARIES.get(i)).append("\n");
            }
            if (FAILURE_SUMMARIES.size() > 5) {
                sb.append("• ... and ").append(FAILURE_SUMMARIES.size() - 5).append(" more");
            }
        }

        new TelegramReporter().send(sb.toString().trim());
    }

    private void recordFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getRealClass().getSimpleName();
        Throwable t = result.getThrowable();
        String reason = "Unknown error";
        if (t != null) {
            String exceptionType = t.getClass().getSimpleName();
            String rawMessage = t.getMessage();
            String sanitizedMessage = Sanitizer.sanitizeErrorMessage(rawMessage);
            reason = exceptionType + (sanitizedMessage.isEmpty() ? "" : ": " + sanitizedMessage);
        }
        FAILURE_SUMMARIES.add(className + "." + testName + ": " + reason);
    }

    private void writeAllureEnvironment() {
        try {
            java.nio.file.Path allureResults = java.nio.file.Paths.get("target", "allure-results");
            if (java.nio.file.Files.exists(allureResults)) {
                java.util.Properties props = new java.util.Properties();
                props.setProperty("Application", "Bucard Web");
                props.setProperty("Base_URL", Config.baseUrl());
                props.setProperty("Browser", Config.browser() + (Config.headless() ? " (headless)" : " (headed)"));
                props.setProperty("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
                props.setProperty("Java_Version", System.getProperty("java.version"));
                try (java.io.OutputStream os = java.nio.file.Files.newOutputStream(allureResults.resolve("environment.properties"))) {
                    props.store(os, "Allure Environment Properties");
                }

                java.io.InputStream catIs = getClass().getClassLoader().getResourceAsStream("categories.json");
                if (catIs != null) {
                    java.nio.file.Files.copy(catIs, allureResults.resolve("categories.json"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
