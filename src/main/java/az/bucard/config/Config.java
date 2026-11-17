package az.bucard.config;

public final class Config {
    private static final java.util.Map<String, String> DOTENV = loadDotEnv();

    private Config() {}

    private static java.util.Map<String, String> loadDotEnv() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        java.nio.file.Path envPath = java.nio.file.Paths.get(".env");
        if (java.nio.file.Files.exists(envPath)) {
            try {
                for (String line : java.nio.file.Files.readAllLines(envPath)) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                        int idx = trimmed.indexOf('=');
                        String key = trimmed.substring(0, idx).trim();
                        String val = trimmed.substring(idx + 1).trim();
                        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                            val = val.substring(1, val.length() - 1);
                        }
                        map.put(key, val);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return map;
    }

    private static String getSetting(String sysProp, String envVar, String defaultValue) {
        String val = System.getProperty(sysProp);
        if (val != null && !val.isBlank()) {
            return val;
        }
        val = System.getenv(envVar);
        if (val != null && !val.isBlank()) {
            return val;
        }
        if (envVar != null && DOTENV.containsKey(envVar)) {
            String dotVal = DOTENV.get(envVar);
            if (dotVal != null && !dotVal.isBlank()) {
                return dotVal;
            }
        }
        return defaultValue;
    }

    public static String baseUrl() {
        return getSetting("bucard.baseUrl", "BUCARD_BASE_URL", "https://bucard.az");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(getSetting("browser.headless", "BROWSER_HEADLESS", "true"));
    }

    public static String browser() {
        return getSetting("browser.name", "BROWSER_NAME", "chrome");
    }

    public static int explicitTimeoutSeconds() {
        try {
            return Integer.parseInt(getSetting("timeout.explicit.seconds", "TIMEOUT_EXPLICIT_SECONDS", "10"));
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    public static int pageLoadTimeoutSeconds() {
        try {
            return Integer.parseInt(getSetting("timeout.pageload.seconds", "TIMEOUT_PAGELOAD_SECONDS", "30"));
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    public static int slowdownMs() {
        try {
            return Integer.parseInt(getSetting("browser.slowdown.ms", "BROWSER_SLOWDOWN_MS", "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String telegramBotToken() {
        return getSetting("telegram.bot.token", "TELEGRAM_BOT_TOKEN", null);
    }

    public static String telegramChatId() {
        return getSetting("telegram.chat.id", "TELEGRAM_CHAT_ID", null);
    }

    public static String testUser() {
        return getSetting("bucard.test.user", "BUCARD_TEST_USER", null);
    }

    public static String testPassword() {
        return getSetting("bucard.test.password", "BUCARD_TEST_PASSWORD", null);
    }

    public static String allureReportUrl() {
        return getSetting("allure.report.url", "ALLURE_REPORT_URL", "https://bucard-tech.github.io/bucardwebauto/");
    }

    public static boolean isAuthAvailable() {
        String user = testUser();
        String pass = testPassword();
        return user != null && !user.isBlank() && pass != null && !pass.isBlank();
    }
}
