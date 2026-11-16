package az.bucard.config;

public final class Config {
    private Config() {}

    private static String getSetting(String sysProp, String envVar, String defaultValue) {
        String val = System.getProperty(sysProp);
        if (val != null && !val.isBlank()) {
            return val;
        }
        val = System.getenv(envVar);
        if (val != null && !val.isBlank()) {
            return val;
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

    public static boolean isAuthAvailable() {
        String user = testUser();
        String pass = testPassword();
        return user != null && !user.isBlank() && pass != null && !pass.isBlank();
    }
}
