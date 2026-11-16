package az.bucard.reporting;

import az.bucard.config.Config;
import az.bucard.utils.Sanitizer;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class TelegramTestListener implements ITestListener, IExecutionListener {
    private static final AtomicInteger PASSED = new AtomicInteger();
    private static final AtomicInteger FAILED = new AtomicInteger();
    private static final AtomicInteger SKIPPED = new AtomicInteger();
    private static final List<TestRecord> TEST_RECORDS = new CopyOnWriteArrayList<>();
    private static Instant startedAt;
    private static final Path LAST_RUN_STATE_FILE = Path.of("target", "last_run_state.properties");
    private static final Map<String, String> PREVIOUS_RUN_STATES = new HashMap<>();

    public record TestRecord(
            String className,
            String methodName,
            String featureName,
            String status,
            double durationSeconds,
            String failureReason,
            String screenshotPath,
            boolean isRegression
    ) {}

    @Override
    public void onExecutionStart() {
        startedAt = Instant.now();
        PASSED.set(0);
        FAILED.set(0);
        SKIPPED.set(0);
        TEST_RECORDS.clear();
        loadPreviousRunState();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        PASSED.incrementAndGet();
        double duration = (result.getEndMillis() - result.getStartMillis()) / 1000.0;
        String cls = result.getTestClass().getRealClass().getSimpleName();
        String mtd = result.getMethod().getMethodName();
        String feature = mapToBusinessFeature(cls, mtd);
        TEST_RECORDS.add(new TestRecord(
                cls,
                mtd,
                feature,
                "PASSED",
                duration,
                null,
                null,
                false
        ));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        FAILED.incrementAndGet();
        double duration = (result.getEndMillis() - result.getStartMillis()) / 1000.0;
        String cls = result.getTestClass().getRealClass().getSimpleName();
        String mtd = result.getMethod().getMethodName();
        String feature = mapToBusinessFeature(cls, mtd);
        String reason = extractReason(result.getThrowable());
        String screenshotPath = (String) result.getAttribute("screenshotPath");

        String fullKey = cls + "." + mtd;
        boolean wasPassingPreviously = "PASSED".equalsIgnoreCase(PREVIOUS_RUN_STATES.get(fullKey));

        TEST_RECORDS.add(new TestRecord(
                cls,
                mtd,
                feature,
                "FAILED",
                duration,
                reason,
                screenshotPath,
                wasPassingPreviously
        ));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        SKIPPED.incrementAndGet();
        double duration = (result.getEndMillis() - result.getStartMillis()) / 1000.0;
        String cls = result.getTestClass().getRealClass().getSimpleName();
        String mtd = result.getMethod().getMethodName();
        String feature = mapToBusinessFeature(cls, mtd);
        TEST_RECORDS.add(new TestRecord(
                cls,
                mtd,
                feature,
                "SKIPPED",
                duration,
                null,
                null,
                false
        ));
    }

    @Override
    public void onExecutionFinish() {
        writeAllureEnvironment();
        saveCurrentRunState();

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

        ZonedDateTime bakuTime = ZonedDateTime.now(ZoneId.of("Asia/Baku"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
        String executionDateTime = bakuTime.format(timeFormatter) + " (AZT)";

        // 1. Ümumi sistem statusu: işləkdir / problem aşkarlanıb
        String systemStatusHeader = failed == 0
                ? "🟢 <b>SİSTEM STATUSU: İŞLƏKDİR</b>"
                : "🔴 <b>SİSTEM STATUSU: PROBLEM AŞKARLANIB (" + failed + " xəta)</b>";

        StringBuilder sb = new StringBuilder();
        sb.append(systemStatusHeader).append("\n");
        sb.append("🎯 <b>Hədəf:</b> ").append(escapeHtml(Config.baseUrl())).append("\n");
        sb.append("🕒 <b>İcra tarixi və vaxtı:</b> ").append(executionDateTime).append("\n");
        sb.append("⏱ <b>Müddət:</b> ").append(String.format("%.1fs", totalSeconds))
                .append(" | <b>Brauzer:</b> ").append(escapeHtml(Config.browser()))
                .append(Config.headless() ? " (headless)" : " (headed)").append("\n\n");

        // 2. Uğurlu və uğursuz testlərin sayı
        sb.append("[").append(progressBar).append("] <b>").append(passPercent).append("%</b>\n");
        sb.append("✅ <b>").append(passed).append("</b> Uğurlu  |  ❌ <b>").append(failed)
                .append("</b> Xətalı  |  ⚠️ <b>").append(skipped).append("</b> Ötürüldü\n\n");

        // 7. Əvvəlki yoxlama ilə müqayisədə yeni yaranmış problemlər (Regressiya)
        List<TestRecord> regressions = TEST_RECORDS.stream().filter(TestRecord::isRegression).toList();
        if (!regressions.isEmpty()) {
            sb.append("🚨 <b>YENİ YARANMIŞ REGRESSİYALAR (Əvvəl uğurlu idi):</b>\n");
            for (TestRecord r : regressions) {
                sb.append(" • <b>[").append(escapeHtml(r.featureName())).append("]</b> ")
                        .append(escapeHtml(r.methodName())).append("\n");
            }
            sb.append("\n");
        }

        // 3. Problem yaranan funksionallıq və 4. Aşkar edilmiş xətanın qısa təsviri
        List<TestRecord> failedRecords = new ArrayList<>();
        for (TestRecord rec : TEST_RECORDS) {
            if ("FAILED".equals(rec.status())) {
                failedRecords.add(rec);
            }
        }

        if (!failedRecords.isEmpty()) {
            sb.append("<b>Aşkar Edilmiş Problemlər:</b>\n");
            int maxFailures = Math.min(6, failedRecords.size());
            for (int i = 0; i < maxFailures; i++) {
                TestRecord f = failedRecords.get(i);
                sb.append("❌ <b>[").append(escapeHtml(f.featureName())).append("]</b> ")
                        .append(escapeHtml(f.className())).append(".").append(escapeHtml(f.methodName())).append("\n");
                if (f.failureReason() != null && !f.failureReason().isBlank()) {
                    sb.append("   ↳ <i>Xəta:</i> ").append(escapeHtml(f.failureReason())).append("\n");
                }
            }
            if (failedRecords.size() > maxFailures) {
                sb.append("   ... və daha ").append(failedRecords.size() - maxFailures).append(" xəta.\n");
            }
            sb.append("\n");
        }

        // Test icmalı (ilk 8 test)
        sb.append("<b>İcra Edilmiş Testlər:</b>\n");
        int maxListing = Math.min(8, TEST_RECORDS.size());
        for (int i = 0; i < maxListing; i++) {
            TestRecord rec = TEST_RECORDS.get(i);
            String icon = switch (rec.status()) {
                case "PASSED" -> "✅";
                case "FAILED" -> "❌";
                default -> "⚠️";
            };
            sb.append(icon).append(" [").append(escapeHtml(rec.featureName())).append("] ")
                    .append(escapeHtml(rec.methodName()))
                    .append(" (").append(String.format("%.1fs", rec.durationSeconds())).append(")\n");
        }
        if (TEST_RECORDS.size() > maxListing) {
            sb.append("... və daha ").append(TEST_RECORDS.size() - maxListing).append(" test.\n");
        }

        String allureUrl = Config.allureReportUrl();
        if (allureUrl != null && !allureUrl.isBlank()) {
            sb.append("\n📊 <a href=\"").append(escapeHtml(allureUrl))
                    .append("\"><b>👉 Canlı Allure hesabatı və Skrinşotlar Qalereyası</b></a>\n");
        }

        // Allure veb hesabatını Telegram bildirişindən ƏVVƏL generasiya et və GitHub Pages-ə göndər
        deployAllureReport();

        TelegramReporter reporter = new TelegramReporter();
        reporter.send(sb.toString().trim());

        // 5. Mümkün olduqda screenshot və texniki loglar: Telegram-a ən çox 2 skrinşot göndər
        int sentPhotos = 0;
        for (TestRecord f : failedRecords) {
            if (f.screenshotPath() != null && sentPhotos < 2) {
                Path photo = Path.of(f.screenshotPath());
                String caption = "❌ [" + escapeHtml(f.featureName()) + "] " + escapeHtml(f.className()) + "." + escapeHtml(f.methodName());
                reporter.sendPhoto(photo, caption);
                sentPhotos++;
            }
        }
    }

    public static String mapToBusinessFeature(String className, String methodName) {
        String combined = (className + " " + methodName).toLowerCase();
        if (combined.contains("auth") || combined.contains("login") || combined.contains("logout") || combined.contains("daxil")) {
            return "İstifadəçi autentifikasiyası";
        }
        if (combined.contains("profile") || combined.contains("bio") || combined.contains("avatar") || combined.contains("profil")) {
            if (combined.contains("visibility") || combined.contains("public") || combined.contains("private") || combined.contains("görünürlük")) {
                return "Profilin görünürlüyü";
            }
            if (combined.contains("share") || combined.contains("paylaş")) {
                return "Profilin paylaşılması";
            }
            return "İstifadəçi profili";
        }
        if (combined.contains("search") || combined.contains("axtar")) {
            return "Profil axtarışı";
        }
        if (combined.contains("box") || combined.contains("qutu")) {
            return "Qutuların idarə edilməsi (Boxes)";
        }
        if (combined.contains("contact") || combined.contains("kontakt")) {
            return "Kontaktların idarə edilməsi";
        }
        if (combined.contains("subscription") || combined.contains("plan") || combined.contains("abunə")) {
            return "Abunəlik və planlar";
        }
        if (combined.contains("bonus")) {
            return "Bonus sistemi";
        }
        if (combined.contains("notification") || combined.contains("bildiriş")) {
            return "Bildirişlər (Notifications)";
        }
        if (combined.contains("localization") || combined.contains("dil") || combined.contains("lang")) {
            return "Çoxdillilik (AZ / EN / RU)";
        }
        if (combined.contains("register") || combined.contains("qeyd")) {
            return "Qeydiyyat";
        }
        if (combined.contains("forgotpassword") || combined.contains("bərpa")) {
            return "Şifrə Bərpası";
        }
        if (combined.contains("negative") || combined.contains("neq")) {
            return "Neqativ və Təhlükəsizlik";
        }
        return "İctimai Naviqasiya";
    }

    private static void loadPreviousRunState() {
        PREVIOUS_RUN_STATES.clear();
        try {
            if (Files.exists(LAST_RUN_STATE_FILE)) {
                java.util.Properties props = new java.util.Properties();
                try (var reader = Files.newBufferedReader(LAST_RUN_STATE_FILE)) {
                    props.load(reader);
                    for (String key : props.stringPropertyNames()) {
                        PREVIOUS_RUN_STATES.put(key, props.getProperty(key));
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void saveCurrentRunState() {
        try {
            Files.createDirectories(LAST_RUN_STATE_FILE.getParent());
            java.util.Properties props = new java.util.Properties();
            for (TestRecord r : TEST_RECORDS) {
                props.setProperty(r.className() + "." + r.methodName(), r.status());
            }
            try (var writer = Files.newBufferedWriter(LAST_RUN_STATE_FILE)) {
                props.store(writer, "Bucard Last Execution States for Regression Tracking");
            }
        } catch (Exception ignored) {
        }
    }

    private static String extractReason(Throwable t) {
        if (t == null) {
            return "Bilinməyən xəta";
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
            Path allureResults = Path.of("target", "allure-results");
            if (Files.exists(allureResults)) {
                java.util.Properties props = new java.util.Properties();
                props.setProperty("Tətbiq (Application)", "Bucard Web (https://bucard.az)");
                props.setProperty("Mühit (Environment)", "Production");
                props.setProperty("Test Növü (Test Type)", "Smoke & UI Visual Verification");
                props.setProperty("Brauzer (Browser)", Config.browser() + (Config.headless() ? " (headless)" : " (headed)"));
                props.setProperty("Əməliyyat Sistemi (OS)", System.getProperty("os.name") + " " + System.getProperty("os.version"));
                props.setProperty("Java Versiyası", System.getProperty("java.version"));
                props.setProperty("Hesabat Portalı", "https://eminmemmedov.github.io/bucardwebauto/");
                try (var os = Files.newOutputStream(allureResults.resolve("environment.properties"))) {
                    props.store(os, "Allure Environment Properties");
                }

                var catIs = getClass().getClassLoader().getResourceAsStream("categories.json");
                if (catIs != null) {
                    Files.copy(catIs, allureResults.resolve("categories.json"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void deployAllureReport() {
        try {
            Path deployScript = Path.of("deploy-allure.sh");
            if (Files.exists(deployScript)) {
                System.out.println("🔄 Allure veb hesabatı Telegram bildirişindən əvvəl generasiya olunur və GitHub Pages-ə göndərilir...");
                Process process = new ProcessBuilder("bash", deployScript.toAbsolutePath().toString())
                        .directory(Path.of(".").toFile())
                        .redirectErrorStream(true)
                        .start();

                try (var is = process.getInputStream();
                     var reader = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[AllureDeploy] " + line);
                    }
                }

                boolean finished = process.waitFor(90, java.util.concurrent.TimeUnit.SECONDS);
                if (finished && process.exitValue() == 0) {
                    System.out.println("✅ Allure hesabatı uğurla GitHub Pages-ə göndərildi!");
                } else {
                    System.err.println("⚠️ Allure deploy çıxış kodu: " + (finished ? process.exitValue() : "timeout"));
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Allure hesabatının avtomatik yenilənməsi zamanı xəta: " + e.getMessage());
        }
    }
}
