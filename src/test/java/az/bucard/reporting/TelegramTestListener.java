package az.bucard.reporting;

import az.bucard.config.Config;
import az.bucard.utils.Sanitizer;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class TelegramTestListener implements ITestListener, IExecutionListener {
    private static final AtomicInteger PASSED = new AtomicInteger();
    private static final AtomicInteger FAILED = new AtomicInteger();
    private static final AtomicInteger SKIPPED = new AtomicInteger();
    private static final List<TestRecord> TEST_RECORDS = new CopyOnWriteArrayList<>();
    private static Instant startedAt;

    public record TestRecord(
            String className,
            String methodName,
            String status,
            double durationSeconds,
            String failureReason,
            String screenshotPath
    ) {}

    @Override
    public void onExecutionStart() {
        startedAt = Instant.now();
        PASSED.set(0);
        FAILED.set(0);
        SKIPPED.set(0);
        TEST_RECORDS.clear();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        PASSED.incrementAndGet();
        double duration = (result.getEndMillis() - result.getStartMillis()) / 1000.0;
        TEST_RECORDS.add(new TestRecord(
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName(),
                "PASSED",
                duration,
                null,
                null
        ));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        FAILED.incrementAndGet();
        double duration = (result.getEndMillis() - result.getStartMillis()) / 1000.0;
        String reason = extractReason(result.getThrowable());
        String screenshotPath = (String) result.getAttribute("screenshotPath");
        TEST_RECORDS.add(new TestRecord(
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName(),
                "FAILED",
                duration,
                reason,
                screenshotPath
        ));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        SKIPPED.incrementAndGet();
        double duration = (result.getEndMillis() - result.getStartMillis()) / 1000.0;
        TEST_RECORDS.add(new TestRecord(
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName(),
                "SKIPPED",
                duration,
                null,
                null
        ));
    }

    @Override
    public void onExecutionFinish() {
        writeAllureEnvironment();
        Instant endedAt = Instant.now();
        double totalSeconds = startedAt == null ? 0 : Duration.between(startedAt, endedAt).toMillis() / 1000.0;

        int passed = PASSED.get();
        int failed = FAILED.get();
        int skipped = SKIPPED.get();
        int total = passed + failed + skipped;

        if (total == 0) {
            return;
        }

        int passPercent = (int) Math.round(((double) passed / total) * 100);
        int greenBlocks = (int) Math.round(((double) passed / total) * 8);
        int redBlocks = 8 - greenBlocks;
        String progressBar = "🟩".repeat(greenBlocks) + "🟥".repeat(redBlocks);

        String statusHeader = failed == 0
                ? "🟢 <b>Bucard Smoke Suite: UĞURLU</b>"
                : "🔴 <b>Bucard Smoke Suite: XƏTA (" + failed + ")</b>";

        StringBuilder sb = new StringBuilder();
        sb.append(statusHeader).append("\n");
        sb.append("🎯 <b>Hədəf:</b> ").append(escapeHtml(Config.baseUrl())).append("\n");
        sb.append("⏱ <b>Müddət:</b> ").append(String.format("%.1fs", totalSeconds))
                .append(" | <b>Brauzer:</b> ").append(escapeHtml(Config.browser()))
                .append(Config.headless() ? " (headless)" : " (headed)").append("\n\n");

        sb.append("[").append(progressBar).append("] <b>").append(passPercent).append("%</b>\n");
        sb.append("✅ ").append(passed).append(" Uğurlu  |  ❌ ").append(failed)
                .append(" Xətalı  |  ⚠️ ").append(skipped).append(" Ötürüldü\n\n");

        sb.append("<b>Testlər:</b>\n");
        int maxListing = Math.min(10, TEST_RECORDS.size());
        for (int i = 0; i < maxListing; i++) {
            TestRecord rec = TEST_RECORDS.get(i);
            String icon = "PASSED".equals(rec.status()) ? "✅" : ("FAILED".equals(rec.status()) ? "❌" : "⚠️");
            sb.append("• ").append(icon).append(" <code>").append(escapeHtml(rec.className()))
                    .append(".").append(escapeHtml(rec.methodName())).append("</code> (")
                    .append(String.format("%.1fs", rec.durationSeconds())).append(")\n");
        }
        if (TEST_RECORDS.size() > 10) {
            sb.append("• ... və daha ").append(TEST_RECORDS.size() - 10).append(" test\n");
        }

        List<TestRecord> failedRecords = new ArrayList<>();
        for (TestRecord rec : TEST_RECORDS) {
            if ("FAILED".equals(rec.status())) {
                failedRecords.add(rec);
            }
        }

        if (!failedRecords.isEmpty()) {
            sb.append("\n<b>⚠️ Xətaların səbəbləri:</b>\n");
            int maxFailures = Math.min(3, failedRecords.size());
            for (int i = 0; i < maxFailures; i++) {
                TestRecord f = failedRecords.get(i);
                sb.append("• <b>").append(escapeHtml(f.methodName())).append("</b>: ")
                        .append(escapeHtml(f.failureReason())).append("\n");
            }
            if (failedRecords.size() > 3) {
                sb.append("• ... və daha ").append(failedRecords.size() - 3).append(" xəta\n");
            }
        }

        String allureUrl = Config.allureReportUrl();
        if (allureUrl != null && !allureUrl.isBlank()) {
            sb.append("\n📊 <a href=\"").append(escapeHtml(allureUrl))
                    .append("\"><b>👉 Ətraflı Allure hesabatına baxın</b></a>");
        }

        TelegramReporter reporter = new TelegramReporter();
        reporter.send(sb.toString().trim());

        // Xəta baş verdikdə skrinşotların birbaşa qrupa göndərilməsi (ən çox 2 ədəd)
        int sentPhotos = 0;
        for (TestRecord f : failedRecords) {
            if (f.screenshotPath() != null && sentPhotos < 2) {
                Path photo = Path.of(f.screenshotPath());
                String caption = "❌ <b>Xəta skrinşotu:</b> " + escapeHtml(f.className()) + "." + escapeHtml(f.methodName());
                reporter.sendPhoto(photo, caption);
                sentPhotos++;
            }
        }
    }

    private static String extractReason(Throwable t) {
        if (t == null) {
            return "Unknown error";
        }
        String type = t.getClass().getSimpleName();
        String message = Sanitizer.sanitizeErrorMessage(t.getMessage());
        return type + (message.isEmpty() ? "" : ": " + message);
    }

    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
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
