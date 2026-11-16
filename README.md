# Bucard Web Automation

**Java 17+ / Selenium WebDriver 4 / TestNG / Maven** steki üzərində qurulmuş [Bucard](https://bucard.az) veb-tətbiqinin avtomatlaşdırılmış test freymvorku.

Layihə əlçatanlığın təhlükəsiz yoxlanılması və "read-only" (yalnız oxuma) ssenariləri üçün layihələndirilib; lokal icranı və CI (GitHub Actions) mühitini, testlərin uğursuz olduğu hallarda artefaktların (skrinşotların) toplanmasını və Telegram bildirişlərini dəstəkləyir.

---

## Memarlıq və struktur

```text
bucardwebauto/
├── .github/workflows/
│   └── smoke.yml                  # GitHub Actions üçün CI workflow
├── docs/
│   └── test-plan.md               # Ətraflı test planı və təhlükəsizlik sərhədləri
├── pom.xml                        # Maven konfiqurasiyası (Java 17, Selenium 4, TestNG)
├── README.md                      # Layihənin sənədləşməsi
└── src/
    ├── main/java/az/bucard/
    │   ├── config/
    │   │   └── Config.java        # Konfiqurasiyanın System properties və mühit dəyişənlərindən oxunması
    │   ├── driver/
    │   │   └── DriverFactory.java # WebDriver fabriki (Chrome headless/headed, timeout-lar, təhlükəsizlik parametrləri)
    │   ├── pages/                 # Page Objects (Page Object Model şablonu)
    │   │   ├── BasePage.java      # Açıq gözləmələrlə (WebDriverWait) baza səhifə sinfi
    │   │   ├── LoginPage.java     # Giriş forması (/login)
    │   │   ├── DashboardPage.java # All Boxes / My Box idarəetmə paneli (dashboard)
    │   │   ├── SearchPage.java    # Şəxsi və biznes profillərinin axtarışı
    │   │   ├── ProfilePage.java   # Profil səhifəsi (Contact info, Portfolio, Experience və s.)
    │   │   ├── BoxDetailsPage.java# Boks təfərrüatları (kontaktlar, boş vəziyyətlər)
    │   │   └── SettingsPage.java  # Tənzimləmələr bölmələri (Account Center, Plan, Payments, Terms, Help)
    │   ├── reporting/
    │   │   └── TelegramReporter.java # Nəticələri Telegram Bot API-yə göndərmək üçün HTTP müştəri
    │   └── utils/
    │       └── ScreenshotUtil.java   # Uğursuzluqlar zamanı skrinşotların çəkilməsi və saxlanması (şəxsi məlumatlarsız)
    └── test/
        ├── java/az/bucard/
        │   ├── base/
        │   │   └── BaseTest.java     # Testlərin həyat dövrü, ThreadLocal WebDriver, skrinşotların çəkilməsi
        │   ├── reporting/
        │   │   └── TelegramTestListener.java # TestNG dinləyicisi, statistikanın və xəta səbəblərinin toplanması
        │   └── smoke/
        │       ├── SiteAvailabilityTest.java         # İctimai saytın və title-ın əlçatanlığının yoxlanması
        │       ├── PublicRoutesSmokeTest.java       # İctimai marşrutların yoxlanması (/login)
        │       └── AuthenticatedNavigationSmokeTest.java # Read-only naviqasiya (hesab məlumatları olmadıqda ötürülür)
        └── resources/
            └── testng.xml             # TestNG test dəstlərinin konfiqurasiyası
```

---

## Mühit tələbləri

- **JDK 17+** (OpenJDK 17 / 21 / 25 üzərində sınaqdan keçirilib)
- **Maven 3.9+**
- **Google Chrome** (Selenium Manager uyğun gələn `chromedriver`-i avtomatik yükləyir və sinxronlaşdırır)

---

## Testlərin işə salınması

### 1. Standart işə salma (headless)

```bash
mvn test
```

### 2. Parametrlərin System Properties vasitəsilə təyin edilməsi

```bash
mvn test -Dbucard.baseUrl=https://bucard.az -Dbrowser.headless=true -Dtimeout.pageload.seconds=45
```

### 3. Parametrlərin mühit dəyişənləri (Environment Variables) vasitəsilə təyin edilməsi

```bash
export BUCARD_BASE_URL="https://bucard.az"
export BROWSER_HEADLESS="false"
export TIMEOUT_EXPLICIT_SECONDS="15"
mvn test
```

### 4. Yalnız kompilyasiya (brauzersiz)

```bash
mvn test-compile
```

---

## Konfiqurasiya parametrləri

| Parametr (Property) | Mühit dəyişəni | Standart dəyər | Təsvir |
|---|---|---|---|
| `bucard.baseUrl` | `BUCARD_BASE_URL` | `https://bucard.az` | Test olunan tətbiqin baza URL ünvanı |
| `browser.headless` | `BROWSER_HEADLESS` | `true` | Brauzerin headless (arxa planda) rejimdə açılması |
| `browser.name` | `BROWSER_NAME` | `chrome` | Brauzer növü (Chrome) |
| `timeout.explicit.seconds` | `TIMEOUT_EXPLICIT_SECONDS` | `10` | Elementlərin açıq gözləmə müddəti (ExpectedConditions) |
| `timeout.pageload.seconds` | `TIMEOUT_PAGELOAD_SECONDS` | `30` | Səhifənin tam yüklənməsi üçün timeout |
| `telegram.bot.token` | `TELEGRAM_BOT_TOKEN` | *təyin edilməyib* | Hesabat göndərmək üçün Telegram bot tokeni |
| `telegram.chat.id` | `TELEGRAM_CHAT_ID` | *təyin edilməyib* | Hesabatın göndəriləcəyi çat/kanal ID-si |
| `bucard.test.user` | `BUCARD_TEST_USER` | *təyin edilməyib* | Read-only testlər üçün test hesabının loqini (telefon/email) |
| `bucard.test.password` | `BUCARD_TEST_PASSWORD` | *təyin edilməyib* | Test hesabının şifrəsi |

---

## Telegram hesabatı

Test icrası başa çatdıqdan sonra `TelegramTestListener` nəticələri əks etdirən mesaj formalaşdırır və göndərir:
- Test stendinin baza URL-i.
- Uğurlu, uğursuz və ötürülmüş yoxlamaların sayı.
- Ümumi icra müddəti.
- Uğursuzluqlar zamanı: xətanın qısa və təhlükəsiz səbəbi (istisna növü + qısa təsvir, tokenlər, şifrələr və fərdi məlumatlar olmadan).

> **Qeyd:** Əgər `TELEGRAM_BOT_TOKEN` və ya `TELEGRAM_CHAT_ID` təyin edilməyibsə, hesabatın göndərilməsi **təhlükəsiz şəkildə ötürülür**, icra kəsilmir və qurulma xətası yaranmır.

---

## Xətalar zamanı skrinşotlar

İstənilən test uğursuz olduqda, `BaseTest` avtomatik olaraq həmin anın skrinşotunu çəkir və aşağıdakı ünvanda saxlayır:
```text
target/screenshots/<TestAdı>_<İİİİAADD_SSDDSS_MSS>.png
```
GitHub Actions daxilində bu qovluq avtomatik olaraq CI artefaktlarına (`failure-screenshots`) əlavə olunur.

---

## Təhlükəsizlik və yoxlama sərhədləri

1. **Ciddi şəkildə Read-Only**: yalnız əlçatanlıq və məlumatların oxunması yoxlamaları həyata keçirilir.
2. **Qadağan olunmuş əməliyyatlar**: autotestlər aşağıdakıları **etmir**:
   - sifarişlərin rəsmiləşdirilməsi, ödənişlər, tarif və abunəliklərin dəyişdirilməsi;
   - hesabın, boksların və ya kontaktların silinməsi/deaktiv edilməsi;
   - məxfilik statusunun dəyişdirilməsi (Private -> Public);
   - real mesajların göndərilməsi, paylaşım (share) və ya email təsdiqlənməsi;
   - MFA/OTP və ya captcha-nın keçilməsi.
3. **Məxfilik təhlükəsizliyi**: şifrələr, tokenlər və cookies heç vaxt loqlara yazılmır və repozitoriyada saxlanılmır.
4. **Autentifikasiyalı testlərin şərti icrası**: `AuthenticatedNavigationSmokeTest` sinfindəki testlər mühit dəyişənlərində test hesabı məlumatları təyin edilmədikdə avtomatik olaraq `SKIPPED` kimi qeyd olunur.
