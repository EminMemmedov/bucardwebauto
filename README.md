# Bucard Web Automation

**Java 17+ / Selenium WebDriver 4 / TestNG / Maven** steki üzərində qurulmuş [Bucard](https://bucard.az) veb-tətbiqinin tam avtomatlaşdırılmış test freymvorku.

Layihə 10 əsas biznes modulunu (P0, P1, P2), ictimai və autentifikasiyalı marşrutları, dinamik müvəqqəti poçt (Temp-Mail) generasiyasını, OTP ilə şifrə bərpasını, neqativ və kibertəhlükəsizlik ssenarilərini (SQLi, XSS, sessiya qorunması) və 7 tələbli reqressiya hesabatını dəstəkləyir.

---

## Memarlıq və struktur

```text
bucardwebauto/
├── .github/workflows/
│   └── smoke.yml                  # GitHub Actions üçün CI/CD workflow və Allure deploy
├── docs/
│   └── test-plan.md               # 10 biznes modulu və 7-nöqtəli reqressiya planı
├── pom.xml                        # Maven konfiqurasiyası (Java 17, Selenium 4, TestNG, Allure)
├── README.md                      # Layihənin sənədləşməsi
├── deploy-allure.sh               # GitHub Pages Allure hesabatının avtomatlaşdırılmış yerləşdirilməsi
├── scripts/
│   ├── generate_gallery.py        # Allure testləri əsasında qalereya məlumatlarının generasiyası
│   ├── gallery.js                 # İnteraktiv skrinşot qalereyası, Lightbox və axtarış
│   └── gallery.css                # Qalereyanın vizual üslubları
└── src/
    ├── main/java/az/bucard/
    │   ├── auth/
    │   │   └── DynamicCredentialsHolder.java # Dinamik hesabların və poçt sessiyalarının idarə edilməsi
    │   ├── config/
    │   │   └── Config.java        # System properties, mühit dəyişənləri və .env oxuyucusu
    │   ├── driver/
    │   │   └── DriverFactory.java # WebDriver fabriki (Chrome headless/headed, timeout-lar)
    │   ├── email/                 # Müvəqqəti poçt və OTP oxuma servisləri
    │   │   ├── EmailAccount.java  # Poçt hesabı və token modeli
    │   │   ├── TempEmailService.java # Poçt və OTP oxuma interfeysi
    │   │   ├── GuerrillaMailClient.java # GuerrillaMail REST API müştərisi
    │   │   └── MailTmClient.java  # Mail.tm REST API müştərisi
    │   ├── pages/                 # Page Objects (17 səhifə və komponent)
    │   │   ├── BasePage.java      # WebDriverWait və JS helper baza səhifəsi
    │   │   ├── LandingPage.java   # Ana səhifə, naviqasiya və dil seçimi
    │   │   ├── LoginPage.java     # Giriş forması (/login)
    │   │   ├── RegisterPage.java  # Qeydiyyat forması (/register)
    │   │   ├── ForgotPasswordPage.java # Şifrə bərpası və 4-rəqəmli OTP daxiletmə addımı
    │   │   ├── RecoverPasswordPage.java # Yeni şifrənin təyin edilməsi (/recover-password)
    │   │   ├── DashboardPage.java # İdarəetmə paneli (All Boxes, My Box, bonuslar)
    │   │   ├── ProfilePage.java   # İstifadəçi profili (bio, avatar, status)
    │   │   ├── PublicProfilePage.java # Anonim baxış üçün ictimai profil
    │   │   ├── SearchPage.java    # Şəxsi və biznes profillərinin axtarışı
    │   │   ├── BoxManagementPage.java # Qutuların yaradılması, ad dəyişməsi və silinməsi
    │   │   ├── BoxDetailsPage.java # Qutunun təfərrüatları və kontakt axtarışı
    │   │   ├── ContactsPage.java  # Kontaktların əlavə edilməsi və silinməsi
    │   │   ├── SubscriptionPage.java # Abunəlik və tarif planları (Free / Pro)
    │   │   ├── BonusPage.java     # Bonus balansı və tarixçəsi
    │   │   ├── NotificationsPage.java # Bildirişlər və oxunmuş qeyd etmə
    │   │   └── SettingsPage.java  # Tənzimləmələr bölmələri
    │   ├── reporting/
    │   │   └── TelegramReporter.java # Telegram Bot API müştərisi
    │   └── utils/
    │       ├── Sanitizer.java     # Məxfi məlumatların (token, şifrə) təmizlənməsi
    │       └── ScreenshotUtil.java# Avtomatik skrinşot çəkilməsi
    └── test/
        ├── java/az/bucard/
        │   ├── base/
        │   │   └── BaseTest.java     # Test həyat dövrü, ThreadLocal WebDriver, Allure screenshot
        │   ├── business/             # 10 Biznes Modulu üzrə testlər (P0, P1, P2)
        │   │   ├── AuthAndProfileBusinessTest.java       # P0: Giriş, profil, görünürlük, axtarış, paylaşılan link
        │   │   ├── BoxesAndContactsBusinessTest.java     # P1: Qutu CRUD, ad dəyişmə, kontakt CRUD
        │   │   ├── SubscriptionAndBonusBusinessTest.java # P1: Free/Pro limitləri, bonus balansı
        │   │   └── NotificationsBusinessTest.java        # P2: Bildirişlər siyahısı və oxunma statusu
        │   ├── reporting/
        │   │   └── TelegramTestListener.java # 7-nöqtəli reqressiya hesabatı və Allure mühit faylı
        │   ├── smoke/                # İctimai və təhlükəsizlik testləri
        │   │   ├── SiteAvailabilityTest.java         # Saytın və title-ın əlçatanlığı
        │   │   ├── PublicRoutesSmokeTest.java       # İctimai marşrutlar və 500/502 yoxlanışı
        │   │   ├── LoginPageSmokeTest.java          # Giriş forması, eye toggle, tablar
        │   │   ├── RegisterPageSmokeTest.java       # Qeydiyyat forması və validasiyalar
        │   │   ├── ForgotPasswordSmokeTest.java     # Bərpa forması və reaktiv düymələr
        │   │   ├── ForgotPasswordOtpEndToEndTest.java # Poçtdan OTP oxunması və şifrə bərpası E2E
        │   │   ├── LocalizationSmokeTest.java       # AZ / EN / RU çoxdillilik və reload
        │   │   ├── NegativeScenariosSmokeTest.java  # 22 Neqativ və təhlükəsizlik ssenarisi
        │   │   ├── AuthenticatedNavigationSmokeTest.java # Read-only naviqasiya
        │   │   └── DynamicRegistrationSmokeTest.java# Dinamik qeydiyyat və təkrar giriş
        │   └── unit/
        │       ├── ConfigTest.java    # Konfiqurasiya unit testləri
        │       └── SanitizerTest.java # Məlumat təmizlənməsi unit testləri
        └── resources/
            ├── categories.json        # Allure xəta kateqoriyaları
            └── testng.xml             # TestNG test dəstlərinin konfiqurasiyası
```

---

## Əsas Biznes Modulları (10 Funksionallıq)

| № | Funksionallıq | Yoxlanılan biznes davranışı | Prioritet | Test Sinfi |
|---|---|---|---|---|
| 1 | **İstifadəçi autentifikasiyası (Login)** | İstifadəçi hesabına daxil ola və problemsiz çıxa bilməlidir. | **P0** | `AuthAndProfileBusinessTest` |
| 2 | **İstifadəçi profili** | Profil açılmalı, məlumatlar göstərilməli və saxlanılmış dəyişikliklər əks olunmalıdır. | **P0** | `AuthAndProfileBusinessTest` |
| 3 | **Profilin görünürlüyü** | Public profillər əlçatan olmalı, Private profillərə icazəsiz giriş mümkün olmamalıdır. | **P0** | `AuthAndProfileBusinessTest` |
| 4 | **Profil axtarışı** | İstifadəçilər digər profilləri axtara və nəticələri görə bilməlidirlər. | **P0** | `AuthAndProfileBusinessTest` |
| 5 | **Profilin paylaşılması** | Paylaşılan profil linki anonim rejimdə düzgün profilə yönləndirməlidir. | **P0** | `AuthAndProfileBusinessTest` |
| 6 | **Qutuların idarə edilməsi (Boxes)** | İstifadəçi qutu yarada, adını dəyişə, qutunu aça və silə bilməlidir. | **P1** | `BoxesAndContactsBusinessTest` |
| 7 | **Kontaktların idarə edilməsi** | İstifadəçi kontakt əlavə edə, baxa və silə bilməlidir. | **P1** | `BoxesAndContactsBusinessTest` |
| 8 | **Abunəlik və planlar (Subscription)** | Free və Pro istifadəçilərin funksionallıqlara çıxışı və məhdudiyyətləri yoxlanılır. | **P1** | `SubscriptionAndBonusBusinessTest` |
| 9 | **Bonus sistemi** | Bonus balansı və bonus tarixçəsi düzgün göstərilməlidir. | **P1** | `SubscriptionAndBonusBusinessTest` |
| 10 | **Bildirişlər (Notifications)** | Bildirişlər göstərilməli və oxunma statusu yenilənməlidir. | **P2** | `NotificationsBusinessTest` |

---

## Avtomatlaşdırılmış 7-Nöqtəli Hesabat Tələbləri (Telegram)

Hər test icrasından sonra `TelegramTestListener` vasitəsilə göndərilən hesabat aşağıdakı 7 tələbi tam əhatə edir:
1. **Ümumi sistem statusu:** `🟢 SİSTEM STATUSU: İŞLƏKDİR` və ya `🔴 SİSTEM STATUSU: PROBLEM AŞKARLANIB`.
2. **Uğurlu və uğursuz testlərin sayı:** vizual tərəqqi paneli (`[🟩🟩🟩🟥] 95%`) və dəqiq saylar.
3. **Problem yaranan funksionallıq:** biznes modulu üzrə kateqoriyalaşdırma (`[İstifadəçi autentifikasiyası]`, `[Qutuların idarə edilməsi]` və s.).
4. **Aşkar edilmiş xətanın qısa təsviri:** texniki həssas məlumatlardan təmizlənmiş xəta mətni.
5. **Skrinşot və texniki loglar:** uğursuz olmuş testlərin anlıq ekran görüntüləri birbaşa Telegram çatına göndərilir.
6. **Testin icra olunma tarixi və vaxtı:** Bakı vaxtı ilə dəqiq icra zamanı (`AZT`).
7. **Əvvəlki yoxlama ilə müqayisədə yeni yaranmış problemlər:** `target/last_run_state.properties` faylı ilə müqayisə edilərək yalnız əvvəl uğurlu olub indi sınan reqressiyaların qeyd olunması.

---

## Dinamik Temp-Mail və OTP ilə Bərpa Mexanizmi

1. **Qeydiyyat zamanı OTP tələb olunmur:**
   * `/register` səhifəsində yalnız e-poçt və güclü şifrə (böyük hərf, rəqəm, xüsusi simvol — məsələn `BucardTest2026!`) tələb olunur. Forma göndərildikdə istifadəçi birbaşa `/dashboard`-a keçir.
2. **Şifrə bərpasında OTP-nin avtomatik oxunması:**
   * `/forgot-password/email` forması göndərildikdə sistem e-poçta 4-rəqəmli OTP kodu göndərir və ekranda 4 xana açır.
   * `TempEmailService` arxa fonda REST API ilə poçt qutusunu dinləyir, gələn məktubdan 4 rəqəmi oxuyur, formaya daxil edir və `/recover-password/...` səhifəsində yeni şifrə təyin edir.

---

## Testlərin işə salınması

```bash
# Bütün test dəstinin (79 test) işə salınması
mvn test

# Yalnız vahid (Unit) testlərin işə salınması
mvn test -Dsurefire.suiteXmlFiles="" -Dtest="ConfigTest,SanitizerTest"

# OTP ilə şifrə bərpası E2E ssenarisi
mvn test -Dsurefire.suiteXmlFiles="" -Dtest="DynamicRegistrationSmokeTest,ForgotPasswordOtpEndToEndTest"

# 10 Biznes modulunun işə salınması
mvn test -Dsurefire.suiteXmlFiles="" -Dtest="AuthAndProfileBusinessTest,BoxesAndContactsBusinessTest,SubscriptionAndBonusBusinessTest,NotificationsBusinessTest"
```

---

## Allure və Vizual Qalereya

* **Lokal Allure hesabatı:** `mvn allure:serve`
* **Canlı Allure Hesabat Portalı:** [https://bucard-tech.github.io/bucardwebauto/](https://bucard-tech.github.io/bucardwebauto/)
