package az.bucard.email;

import java.time.Duration;

public interface TempEmailService {
    EmailAccount createAccount();

    String waitForLatestEmail(EmailAccount account, Duration timeout);

    String extractConfirmationLink(String emailContent);

    String extractVerificationCode(String emailContent);
}
