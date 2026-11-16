package az.bucard.utils;

public final class Sanitizer {
    private Sanitizer() {}

    public static String sanitizeErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }
        String firstLine = message.split("\r?\n")[0].trim();
        String sanitized = firstLine
                .replaceAll("(?i)(password|token|secret|key|authorization|bearer|cookie|auth)=[^\\s&,;]+", "$1=[REDACTED]")
                .replaceAll("(?i)https?://[^\\s?]+\\?[^\\s]+", "[REDACTED_URL]");
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 97) + "...";
        }
        return sanitized;
    }
}
