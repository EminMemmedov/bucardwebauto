package az.bucard.auth;

import az.bucard.config.Config;
import az.bucard.email.EmailAccount;
import az.bucard.email.GuerrillaMailClient;
import az.bucard.email.TempEmailService;
import az.bucard.pages.RegisterPage;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.atomic.AtomicReference;

public final class DynamicCredentialsHolder {
    private static final AtomicReference<DynamicAccount> ACTIVE_ACCOUNT = new AtomicReference<>();
    public static final String DEFAULT_PASSWORD = "BucardTest2026!";

    public record DynamicAccount(
            String email,
            String password,
            boolean registered,
            EmailAccount emailAccount,
            TempEmailService emailService
    ) {}

    private DynamicCredentialsHolder() {}

    public static void setCredentials(String email, String password) {
        setCredentials(email, password, null, null);
    }

    public static void setCredentials(String email, String password, EmailAccount emailAccount, TempEmailService emailService) {
        ACTIVE_ACCOUNT.set(new DynamicAccount(email, password, true, emailAccount, emailService));
    }

    public static void updatePassword(String newPassword) {
        DynamicAccount curr = ACTIVE_ACCOUNT.get();
        if (curr != null) {
            ACTIVE_ACCOUNT.set(new DynamicAccount(curr.email(), newPassword, curr.registered(), curr.emailAccount(), curr.emailService()));
        }
    }

    public static boolean hasDynamicCredentials() {
        DynamicAccount acc = ACTIVE_ACCOUNT.get();
        return acc != null && acc.registered() && acc.email() != null && !acc.email().isBlank();
    }

    public static String getEffectiveUser() {
        if (Config.isAuthAvailable()) {
            return Config.testUser();
        }
        DynamicAccount acc = ACTIVE_ACCOUNT.get();
        if (acc != null && acc.email() != null) {
            return acc.email();
        }
        return null;
    }

    public static String getEffectivePassword() {
        if (Config.isAuthAvailable()) {
            return Config.testPassword();
        }
        DynamicAccount acc = ACTIVE_ACCOUNT.get();
        if (acc != null && acc.password() != null) {
            return acc.password();
        }
        return DEFAULT_PASSWORD;
    }

    public static EmailAccount getEmailAccount() {
        DynamicAccount acc = ACTIVE_ACCOUNT.get();
        return acc != null ? acc.emailAccount() : null;
    }

    public static TempEmailService getEmailService() {
        DynamicAccount acc = ACTIVE_ACCOUNT.get();
        if (acc != null && acc.emailService() != null) {
            return acc.emailService();
        }
        return new GuerrillaMailClient();
    }

    public static boolean isAvailable() {
        return Config.isAuthAvailable() || hasDynamicCredentials();
    }

    public static synchronized void ensureAccountRegistered(WebDriver driver) {
        if (!isAvailable()) {
            TempEmailService emailService = new GuerrillaMailClient();
            EmailAccount account = emailService.createAccount();
            RegisterPage registerPage = new RegisterPage(driver);
            registerPage.open(Config.baseUrl());
            registerPage.registerNewUser(account.address(), DEFAULT_PASSWORD);
            setCredentials(account.address(), DEFAULT_PASSWORD, account, emailService);
        }
    }

    public static void clear() {
        ACTIVE_ACCOUNT.set(null);
    }
}
