# Bucard Web Automation

Java + Selenium WebDriver + TestNG + Maven starter for Bucard UI smoke checks.

## Requirements
- JDK 17+
- Maven 3.9+
- Google Chrome (Selenium Manager resolves the matching driver)

## Run

```bash
mvn test
```

Set a different target with `-Dbucard.baseUrl=https://...`. Headless Chrome is enabled by default and can be disabled with `-Dbrowser.headless=false`.

## Telegram
Set `TELEGRAM_BOT_TOKEN` and `TELEGRAM_CHAT_ID` in CI secrets. The listener sends only result counts and duration. No credentials, OTPs, cookies, or profile details are included.

## Scope
The first smoke test checks site availability only. Authenticated workflows require a dedicated test account and confirmed login flow. Payment, account deletion, privacy changes, and external sharing are excluded from routine runs.
