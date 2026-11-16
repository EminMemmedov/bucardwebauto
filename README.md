# Bucard Web Automation

Фреймворк автоматизированного тестирования веб-приложения [Bucard](https://bucard.az) на стеке **Java 17+ / Selenium WebDriver 4 / TestNG / Maven**.

Проект спроектирован для безопасных проверок доступности и read-only сценариев, с поддержкой локального запуска и CI (GitHub Actions), сбором артефактов при падениях (скриншоты) и уведомлениями в Telegram.

---

## Архитектура и структура

```text
bucardwebauto/
├── .github/workflows/
│   └── smoke.yml                  # CI workflow для GitHub Actions
├── docs/
│   └── test-plan.md               # Детальный план тестирования и границы безопасности
├── pom.xml                        # Maven-конфигурация (Java 17, Selenium 4, TestNG)
├── README.md                      # Документация проекта
└── src/
    ├── main/java/az/bucard/
    │   ├── config/
    │   │   └── Config.java        # Чтение конфигурации из System properties и переменных окружения
    │   ├── driver/
    │   │   └── DriverFactory.java # Фабрика WebDriver (Chrome headless/headed, таймауты, флаги безопасности)
    │   ├── pages/                 # Page Objects (паттерн Page Object Model)
    │   │   ├── BasePage.java      # Базовый класс страниц с явными ожиданиями (WebDriverWait)
    │   │   ├── LoginPage.java     # Форма входа (/login)
    │   │   ├── DashboardPage.java # Дашборд All Boxes / My Box
    │   │   ├── SearchPage.java    # Поиск персональных и бизнес-профилей
    │   │   ├── ProfilePage.java   # Страница профиля (Contact info, Portfolio, Experience и т.д.)
    │   │   ├── BoxDetailsPage.java# Детали бокса (контакты, пустые состояния)
    │   │   └── SettingsPage.java  # Разделы настроек (Account Center, Plan, Payments, Terms, Help)
    │   ├── reporting/
    │   │   └── TelegramReporter.java # HTTP-клиент отправки сводки в Telegram Bot API
    │   └── utils/
    │       └── ScreenshotUtil.java   # Снятие и сохранение скриншотов при сбоях (без PII)
    └── test/
        ├── java/az/bucard/
        │   ├── base/
        │   │   └── BaseTest.java     # Жизненный цикл тестов, ThreadLocal WebDriver, снятие скриншотов
        │   ├── reporting/
        │   │   └── TelegramTestListener.java # Слушатель TestNG, сбор статистики и причин сбоев
        │   └── smoke/
        │       ├── SiteAvailabilityTest.java         # Проверка доступности публичного сайта и title
        │       ├── PublicRoutesSmokeTest.java       # Проверка публичных маршрутов (/login)
        │       └── AuthenticatedNavigationSmokeTest.java # Read-only навигация (пропускается без credentials)
        └── resources/
            └── testng.xml             # Конфигурация тестовых наборов TestNG
```

---

## Требования к окружению

- **JDK 17+** (протестировано на OpenJDK 17 / 21 / 25)
- **Maven 3.9+**
- **Google Chrome** (Selenium Manager автоматически загружает и синхронизирует нужный `chromedriver`)

---

## Запуск тестов

### 1. Стандартный запуск (headless)

```bash
mvn test
```

### 2. Переопределение параметров через System Properties

```bash
mvn test -Dbucard.baseUrl=https://bucard.az -Dbrowser.headless=true -Dtimeout.pageload.seconds=45
```

### 3. Переопределение параметров через переменные окружения

```bash
export BUCARD_BASE_URL="https://bucard.az"
export BROWSER_HEADLESS="false"
export TIMEOUT_EXPLICIT_SECONDS="15"
mvn test
```

### 4. Запуск только компиляции без браузера

```bash
mvn test-compile
```

---

## Конфигурационные параметры

| Параметр (Property) | Переменная окружения | Значение по умолчанию | Описание |
|---|---|---|---|
| `bucard.baseUrl` | `BUCARD_BASE_URL` | `https://bucard.az` | Базовый URL тестируемого приложения |
| `browser.headless` | `BROWSER_HEADLESS` | `true` | Запуск браузера в headless-режиме |
| `browser.name` | `BROWSER_NAME` | `chrome` | Тип браузера (Chrome) |
| `timeout.explicit.seconds` | `TIMEOUT_EXPLICIT_SECONDS` | `10` | Явное ожидание элементов (ExpectedConditions) |
| `timeout.pageload.seconds` | `TIMEOUT_PAGELOAD_SECONDS` | `30` | Таймаут полной загрузки страниц |
| `telegram.bot.token` | `TELEGRAM_BOT_TOKEN` | *не задан* | Токен бота Telegram для отправки отчёта |
| `telegram.chat.id` | `TELEGRAM_CHAT_ID` | *не задан* | ID чата/канала для получения отчёта |
| `bucard.test.user` | `BUCARD_TEST_USER` | *не задан* | Логин тестового аккаунта для read-only тестов |
| `bucard.test.password` | `BUCARD_TEST_PASSWORD` | *не задан* | Пароль тестового аккаунта |

---

## Отчётность в Telegram

По завершении тестового прогона `TelegramTestListener` формирует и отправляет сообщение с результатами:
- Базовый URL стенда.
- Количество успешных, упавших и пропущенных проверок.
- Общая длительность выполнения.
- При падениях: краткая безопасная причина ошибки (тип исключения + краткое описание, без токенов, паролей и персональных данных).

> **Примечание:** Если `TELEGRAM_BOT_TOKEN` или `TELEGRAM_CHAT_ID` не заданы, отправка отчёта **безопасно пропускается**, не прерывая выполнение и не вызывая ошибок сборки.

---

## Скриншоты при ошибках

При падении любого теста `BaseTest` автоматически делает скриншот страницы в момент сбоя и сохраняет его в:
```text
target/screenshots/<ИмяТеста>_<ГГГГММДД_ЧЧММСС_МСС>.png
```
В GitHub Actions этот каталог автоматически архивируется в артефакты сборки (`failure-screenshots`).

---

## Безопасность и границы проверок

1. **Строго Read-Only**: реализованы исключительно проверки доступности и чтения данных.
2. **Запрещённые действия**: автотесты **не выполняют**:
   - оформление заказов, платежи, смену тарифов и подписок;
   - удаление или деактивацию аккаунта, боксов или контактов;
   - изменение статуса приватности (Private -> Public);
   - отправку реальных сообщений, шеринг или подтверждение email;
   - обход MFA/OTP или капчи.
3. **Безопасность секретов**: пароли, токены и cookies никогда не логируются и не сохраняются в репозитории.
4. **Условный запуск авторизованных тестов**: тесты из класса `AuthenticatedNavigationSmokeTest` автоматически помечаются как `SKIPPED`, если в переменных окружения не переданы тестовые учётные данные.
