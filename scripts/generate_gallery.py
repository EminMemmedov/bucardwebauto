#!/usr/bin/env python3
"""
Generates data/gallery.json from Allure test-case data.
Scans all test-cases JSON files, extracts screenshot attachments, categories,
test steps, and historical last-passed run information for failed tests.
"""

import datetime
import glob
import json
import os
import re
import sys

KNOWN_STEPS = {
    "DYN-QEYD-01": [
        "1. Ödənişsiz Temp-Mail REST API-yə qoşul",
        "2. Unikal müvəqqəti test e-poçt ünvanı generasiya et",
        "3. E-poçt formatının və domeninin (@sharklasers.com) düzgünlüyünü təsdiqlə"
    ],
    "DYN-QEYD-02": [
        "1. /register səhifəsini aç və formu yoxla",
        "2. Dinamik yaradılmış e-poçt və güclü şifrə daxil et",
        "3. Qeydiyyatı təsdiqlə və /dashboard səhifəsinə yönləndirməni yoxla",
        "4. Sessiyadan çıxış (Logout) et və /login səhifəsinə keçidi təsdiqlə"
    ],
    "DYN-QEYD-03": [
        "1. /login səhifəsində yeni qeydiyyatdan keçmiş dinamik e-poçtu daxil et",
        "2. Şifrəni daxil edərək Daxil ol düyməsinə kliklə",
        "3. Sessiyanın bərpasını və /dashboard-a uğurlu təkrar girişi təsdiqlə"
    ],
    "BƏRPA-E2E-01": [
        "1. /forgot-password/email səhifəsini aç və dinamik e-poçtu daxil et",
        "2. Təsdiq düyməsinə kliklə və 4-rəqəmli OTP formasının açıldığını yoxla",
        "3. REST API vasitəsilə müvəqqəti poçt qutusundan OTP kodunu oxu",
        "4. Kodu 4 xanaya daxil et və təsdiqlə",
        "5. /recover-password səhifəsində yeni güclü şifrə təyin et",
        "6. /login səhifəsində yeni şifrə ilə uğurlu girişi təsdiqlə"
    ],

    "BIZ-P0-01": [
        "1. /login səhifəsini aç və etibarlı kredensiallarla daxil ol",
        "2. /dashboard səhifəsinin açıldığını təsdiqlə",
        "3. Sistemdən çıxış (Logout) əməliyyatını icra et",
        "4. Təkrar /login səhifəsinə yönləndirməni yoxla"
    ],
    "BIZ-P0-02": [
        "1. Dashboard-dan istifadəçi profilini aç",
        "2. Avatar və profil məlumatlarının əks olunduğunu yoxla",
        "3. Profil redaktə rejiminə keç və Bio mətnini yenilə",
        "4. Dəyişikliyi yadda saxla və əks olunduğunu təsdiqlə"
    ],
    "BIZ-P0-03": [
        "1. İstifadəçi profilinin görünürlük statusunu (Public/Private) oxu",
        "2. Status indikatorunun düzgün əks olunduğunu təsdiqlə"
    ],
    "BIZ-P0-04": [
        "1. Dashboard-dan Search səhifəsinə keç",
        "2. Axtarış xanasına istifadəçi sorğusu daxil et",
        "3. Axtarış nəticələrinin gəldiyini təsdiqlə"
    ],
    "BIZ-P0-05": [
        "1. İstifadəçi profilinin paylaşım linkini əldə et",
        "2. Kuki və sessiyanı təmizləyərək anonim rejimdə linki aç",
        "3. Paylaşılan profil kartının ictimai əlçatanlığını yoxla"
    ],
    "BIZ-P1-01": [
        "1. Qutuların idarəetmə bölməsinə keç",
        "2. AUTO_TEST_BOX adlı yeni qutu yarat",
        "3. Qutunun siyahıda əks olunduğunu təsdiqlə",
        "4. Qutunun detallarını aç və sınaqdan sonra qutunu sil"
    ],
    "BIZ-P1-02": [
        "1. Qutu detalları bölməsini aç",
        "2. Yeni test kontaktı (Ad, Telefon, Email) əlavə et",
        "3. Kontaktın siyahıda göründüyünü yoxla",
        "4. Test kontaktını sil və təmizlənməni təsdiqlə"
    ],
    "BIZ-P1-03": [
        "1. Settings -> Plan / Abunəlik bölməsinə keç",
        "2. Cari plan statusunu (Free / Pro) oxu",
        "3. Mövcud tarifləri və paket limitlərini təsdiqlə"
    ],
    "BIZ-P1-04": [
        "1. Dashboard-da bonus balansı indikatorunu yoxla",
        "2. Bonus bölməsini aç və balans rəqəmini oxu",
        "3. Əməliyyat tarixçəsi bölməsinin yükləndiyini təsdiqlə"
    ],
    "BIZ-P2-01": [
        "1. Bildirişlər panelini (Notification Drawer) aç",
        "2. Bildirişlərin siyahısını yoxla",
        "3. Hamısını oxunmuş et əməliyyatını icra et və statusu yenilə"
    ],

    "DİL-01": [
        "1. Bucard ana səhifəsini (/) aç",
        "2. Xederdə .selectLang dil elementini tap",
        "3. İlkin seçilmiş dil göstəricisini oxu",
        "4. Tam ekran sübut skrinşotunu çək"
    ],
    "DİL-02": [
        "1. Bucard ana səhifəsini aç",
        "2. Dil seçim menyusunu (.selectLang) aç",
        "3. 'English' (li#en-US) bəndini seç",
        "4. Dil göstəricisinin 'EN' olduğunu təsdiqlə",
        "5. İnterfeys mətnlərinin ingilis dilinə keçdiyini yoxla"
    ],
    "DİL-03": [
        "1. Bucard ana səhifəsini aç",
        "2. Dil seçim menyusunu (.selectLang) aç",
        "3. 'Русский' (li#ru-RU) bəndini seç",
        "4. Dil göstəricisinin 'РУ' olduğunu təsdiqlə",
        "5. İnterfeys mətnlərinin rus dilinə keçdiyini yoxla"
    ],
    "DİL-04": [
        "1. Dili əvvəlcə 'English' (EN) rejiminə dəyiş",
        "2. Yenidən dil menyusunu aç",
        "3. 'Azərbaycan' (li#az-AZ) bəndini seç",
        "4. Göstəricinin 'AZ' olduğunu təsdiqlə",
        "5. Keçid öncəsi və sonrası skrinşotları qeydə al"
    ],
    "DİL-05": [
        "1. Dili 'English' (EN) olaraq təyin et",
        "2. Brauzer səhifəsini yenilə (F5 / Reload)",
        "3. Səhifə yeniləndikdən sonra dil göstəricisinin 'EN' qaldığını təsdiqlə",
        "4. Yaddaşın (i18nextLng) saxlanmasını təsdiqlə"
    ],
    "GİRİŞ-01": [
        "1. /login ünvanını brauzerdə aç",
        "2. Başlıq, logo və form elementlərini yoxla",
        "3. E-poçt və şifrə sahələrinin mövcudluğunu təsdiqlə",
        "4. Qeydiyyat və şifrə bərpası linklərini yoxla"
    ],
    "GİRİŞ-02": [
        "1. /login səhifəsini aç",
        "2. 'Telefon' tabına kliklə",
        "3. Telefon daxiletmə sahəsinin aktivləşməsini yoxla",
        "4. 'Email' tabına təkrar klikləyib geri qayıt"
    ],
    "GİRİŞ-03": [
        "1. Şifrə sahəsinin ilkin maskalı (type='password') olduğunu yoxla",
        "2. Maskalı vəziyyətin skrinşotunu çək",
        "3. Göz ikonuna (Eye Toggle) klikləyib şifrəni aşkar et (type='text')",
        "4. Aşkar vəziyyətin skrinşotunu çək və təsdiqlə"
    ],
    "GİRİŞ-04": [
        "1. /login formasını boş burax",
        "2. 'Daxil ol' düyməsinə kliklə",
        "3. HTML5 / react form validasiya reaksiyasını yoxla"
    ],
    "GİRİŞ-05": [
        "1. /login səhifəsində 'Şifrəni unutmusunuz?' linkini tap",
        "2. Linkə kliklə",
        "3. /forgot-password/email səhifəsinə yönləndirməni təsdiqlə"
    ],
    "GİRİŞ-06": [
        "1. /login səhifəsində 'Qeydiyyat' linkini tap",
        "2. Linkə kliklə",
        "3. /register səhifəsinə təmiz keçidi yoxla"
    ],
    "GİRİŞ-07": [
        "1. 'E-poçt' və 'Telefon' tabları arasında ardıcıl keçid et",
        "2. Hər iki rejimdə müvafiq sahələrin əks olunduğunu təsdiqlə"
    ],
    "QEYD-01": [
        "1. /register səhifəsini aç",
        "2. Ad, Soyad, E-poçt, Telefon və Şifrə sahələrini yoxla",
        "3. Qaydalarla razılıq checkbox-unu təsdiqlə"
    ],
    "QEYD-02": [
        "1. Qeydiyyat şifrə sahəsinə test mətni yaz",
        "2. Göz ikonuna klikləyərək şifrənin açıldığını yoxla",
        "3. Təkrar klikləyərək şifrənin yenidən maskalandığını təsdiqlə"
    ],
    "QEYD-03": [
        "1. /register səhifəsində 'Giriş edin' linkinə kliklə",
        "2. /login səhifəsinə problemsiz qayıdışı təsdiqlə"
    ],
    "QEYD-04": [
        "1. Qeydiyyat formasını boş saxlayaraq submit et",
        "2. Sahələrin tələb olunan (required) olduğunu yoxla"
    ],
    "QEYD-05": [
        "1. Qeyri-valid formatlı e-poçt ('test_invalid@') daxil et",
        "2. Formanın qəbul olunmadığını və xəta indikatorunu yoxla"
    ],
    "BƏRPA-01": [
        "1. /forgot-password/email ünvanını aç",
        "2. E-poçt bərpa forması və təsdiq düyməsini yoxla",
        "3. Telefon bərpasına keçid linkini təsdiqlə"
    ],
    "BƏRPA-02": [
        "1. /forgot-password/phone ünvanını aç",
        "2. Telefon nömrəsi daxiletmə sahəsinin mövcudluğunu yoxla",
        "3. E-poçt sahəsinin ekranda olmadığını təsdiqlə"
    ],
    "BƏRPA-03": [
        "1. E-poçt xanası boş ikən təsdiq düyməsinin passivliyini yoxla",
        "2. İlkin vəziyyətin skrinşotunu çək",
        "3. E-poçt ünvanı daxil et və düymənin aktivləşdiyini yoxla",
        "4. Aktivləşmiş vəziyyətin skrinşotunu çək"
    ],
    "BƏRPA-04": [
        "1. Şifrə bərpası səhifəsində 'Geri qayıt' düyməsinə kliklə",
        "2. /login səhifəsinə uğurla qayıdıldığını təsdiqlə"
    ],
    "BƏRPA-05": [
        "1. /forgot-password/email və /forgot-password/phone marşrutlarını yoxla",
        "2. Hər iki səhifənin ayrılmış və müstəqil işlədiyini təsdiqlə"
    ],
    "ANA-01": [
        "1. https://bucard.az ana səhifəsinə daxil ol",
        "2. Bucard loqosunun (.logo) görünməsini təsdiqlə",
        "3. Naviqasiya menyusunun və qeydiyyat düyməsinin mövcudluğunu yoxla"
    ],
    "ANA-02": [
        "1. https://bucard.az ana səhifəsinə daxil ol",
        "2. Mobil tətbiq mağazaları (App Store, Google Play) və ya əsas bölmələri yoxla",
        "3. Əsas səhifənin tam yüklənməsini və mobil tətbiq linklərini təsdiqlə"
    ],
    "TARİF-02": [
        "1. Ana səhifədə tariflər bölməsinə keç",
        "2. 'İllik' tabına kliklə və illik tariflərin skrinşotunu çək",
        "3. 'Aylıq' tabına kliklə və aylıq tariflərin skrinşotunu çək"
    ],
    "XƏTA-04": [
        "1. Mövcud olmayan /bu-sehife-yoxdur-404 ünvanına keç",
        "2. Səhifənin ağ ekrana (white screen) düşmədiyini yoxla",
        "3. Təmiz 404 səhifəsinin və ya yönləndirmənin olduğunu təsdiqlə"
    ],
    "SAYT-01": [
        "1. https://bucard.az ünvanına HTTPS bağlantısını yoxla",
        "2. Səhifənin 200 OK statusu ilə yükləndiyini təsdiqlə",
        "3. HTML DOM və #root strukturunun qurulduğunu yoxla"
    ],
    "NEQ-GİRİŞ-01": [
        "1. /login səhifəsini aç",
        "2. Mövcud olmayan e-poçt və yanlış şifrə daxil et",
        "3. 'Daxil ol' düyməsinə kliklə",
        "4. Dashboard-a keçilmədiyini və giriş formasının aktiv qaldığını təsdiqlə"
    ],
    "NEQ-GİRİŞ-02": [
        "1. /login səhifəsini aç",
        "2. Sintaktik qeyri-valid e-poçt formatı (məs. @ olmadan) daxil et",
        "3. Formanı submit etməyə cəhd et",
        "4. HTML5 / React validasiyasının sorğunu saxladığını təsdiqlə"
    ],
    "NEQ-GİRİŞ-03": [
        "1. /login səhifəsində 'Telefon' tabına keç",
        "2. Natamam nömrə (məs. cəmi 4 rəqəm) daxil et",
        "3. Giriş cəhdinin bloklandığını və dashboard-a keçilmədiyini yoxla"
    ],
    "NEQ-GİRİŞ-04": [
        "1. /login səhifəsini aç",
        "2. E-poçt və şifrə sahələrinə SQLi və XSS simvolları daxil et",
        "3. Formanı göndər",
        "4. Server xətası (500) yaranmadığını və daxiletmələrin təhlükəsiz emal olunduğunu təsdiqlə"
    ],
    "NEQ-GİRİŞ-05": [
        "1. /login formasını aç",
        "2. Sahəyə 500+ simvoldan ibarət ekstremal uzun mətn yaz",
        "3. CSS və vizual interfeys strukturunun pozulmadığını yoxla"
    ],
    "NEQ-QEYD-01": [
        "1. /register qeydiyyat səhifəsini aç",
        "2. Zəif / qısa şifrə ('123') daxil et",
        "3. Qeydiyyatın tamamlanmadığını və formanın xəta bildirdiyini təsdiqlə"
    ],
    "NEQ-QEYD-02": [
        "1. /register səhifəsini aç",
        "2. Bütün sahələri boş buraxaraq qeydiyyat düyməsinə kliklə",
        "3. Formanın göndərilmədiyini və istifadəçinin qeydiyyatda qaldığını yoxla"
    ],
    "NEQ-QEYD-03": [
        "1. /register səhifəsini aç",
        "2. İkiqat @@ və ya yanlış domenli e-poçt daxil et",
        "3. Qeyri-valid e-poçt ilə qeydiyyatın bloklandığını təsdiqlə"
    ],
    "NEQ-QEYD-04": [
        "1. /register səhifəsini aç",
        "2. Yalnız e-poçt yazıb şifrəni boş saxla",
        "3. Natamam məlumatla qeydiyyatın qarşısının alındığını yoxla"
    ],
    "NEQ-BƏRPA-01": [
        "1. /forgot-password/email səhifəsini aç",
        "2. Qeydiyyatdan keçməmiş e-poçt daxil edib sorğu göndər",
        "3. 500 server xətasının yaranmadığını və səhifənin stabil qaldığını təsdiqlə"
    ],
    "NEQ-BƏRPA-02": [
        "1. /forgot-password/phone səhifəsini aç",
        "2. Telefon nömrəsini natamam saxla",
        "3. 'Təsdiqlə' düyməsinin qeyri-aktiv (disabled) qaldığını təsdiqlə"
    ],
    "NEQ-BƏRPA-03": [
        "1. /forgot-password/email səhifəsini aç",
        "2. Sahəyə XSS inyeksiyası daxil et",
        "3. Skriptin icra olunmadığını və sahənin təhlükəsiz emal edildiyini yoxla"
    ],
    "NEQ-AUTH-01": [
        "1. Qonaq sessiyasında birbaşa /dashboard ünvanına keçid et",
        "2. Protected Route Guard tərəfindən icazəsiz girişin bloklandığını yoxla",
        "3. İstifadəçinin avtomatik /login səhifəsinə yönləndirildiyini təsdiqlə"
    ],
    "NEQ-AUTH-02": [
        "1. Qonaq sessiyasında birbaşa /profile ünvanına keçid et",
        "2. Şəxsi məlumatların sızmadığını yoxla",
        "3. /login səhifəsinə yönləndirməni təsdiqlə"
    ],
    "NEQ-AUTH-03": [
        "1. Qonaq sessiyasında birbaşa /settings ünvanına keçid et",
        "2. Tənzimləmələrə girişin məhdudlaşdırıldığını yoxla",
        "3. /login səhifəsinə yönləndirməni təsdiqlə"
    ],
    "NEQ-AUTH-04": [
        "1. Qonaq sessiyasında mövcud olmayan qutuya (/boxes/invalid-id-999) keç",
        "2. Ağ ekranın (white screen) baş vermədiyini yoxla",
        "3. Təhlükəsiz vəziyyətin qorunduğunu təsdiqlə"
    ]
}

def extract_steps_for_test(data):
    # Check if Allure testStage steps exist
    steps_raw = data.get("testStage", {}).get("steps", [])
    if steps_raw:
        return [s.get("name", "Addım") for s in steps_raw if s.get("name")]

    name = data.get("name", "")
    for code, steps_list in KNOWN_STEPS.items():
        if code in name:
            return steps_list

    # Fallback: parse description sentences
    desc = data.get("description", "")
    if desc:
        sentences = [s.strip() for s in re.split(r"[.!?]\s+", desc) if len(s.strip()) > 5]
        if sentences:
            return [f"{i+1}. {st}" for i, st in enumerate(sentences)]

    return ["1. Test səhifəsini aç", "2. UI elementlərini yoxla", "3. Nəticəni təsdiqlə"]

def generate_gallery(report_dir):
    test_cases_dir = os.path.join(report_dir, "data", "test-cases")
    if not os.path.isdir(test_cases_dir):
        print(f"Error: {test_cases_dir} does not exist", file=sys.stderr)
        return False

    history_data = {}
    history_file = os.path.join(report_dir, "history", "history.json")
    if os.path.isfile(history_file):
        try:
            with open(history_file, "r", encoding="utf-8") as hf:
                history_data = json.load(hf)
        except Exception as e:
            print(f"Warning: could not read history: {e}", file=sys.stderr)

    records = []
    category_order = [
        "İctimai Səhifələr və Naviqasiya",
        "Çoxdillilik (AZ / EN / RU)",
        "Giriş və Avtorizasiya",
        "Qeydiyyat",
        "Şifrə Bərpası",
        "Neqativ və Təhlükəsizlik Ssenariləri",
        "Dinamik Qeydiyyat və Giriş",
        "Biznes Funksionallıqları (P0, P1, P2)",
        "Vahid (Unit) Testlər",
        "Digər Testlər"
    ]

    for fpath in sorted(glob.glob(os.path.join(test_cases_dir, "*.json"))):
        try:
            with open(fpath, "r", encoding="utf-8") as f:
                data = json.load(f)
        except Exception:
            continue

        images = []
        def collect_images(att_list):
            for a in att_list:
                if a.get("type") == "image/png" and a.get("source"):
                    images.append({
                        "name": a.get("name", "Ekran görüntüsü"),
                        "src": "data/attachments/" + a.get("source"),
                        "size": a.get("size", 0)
                    })

        collect_images(data.get("attachments", []))
        collect_images(data.get("testStage", {}).get("attachments", []))
        for step in data.get("testStage", {}).get("steps", []):
            collect_images(step.get("attachments", []))

        name = data.get("name", "")
        feature = ""
        story = ""
        for lbl in data.get("labels", []):
            if lbl.get("name") == "feature":
                feature = lbl.get("value", "")
            elif lbl.get("name") == "story":
                story = lbl.get("value", "")

        full_name = data.get("fullName", "")
        name_clean = name.strip()

        category = "Digər Testlər"
        if "unit" in full_name.lower() or "ConfigTest" in full_name or "SanitizerTest" in full_name:
            category = "Vahid (Unit) Testlər"
        elif name_clean.startswith("DYN") or "DynamicRegistration" in full_name:
            category = "Dinamik Qeydiyyat və Giriş"
        elif name_clean.startswith("BIZ") or "business" in full_name.lower() or "BoxesAndContacts" in full_name or "AuthAndProfile" in full_name or "SubscriptionAndBonus" in full_name or "Notifications" in full_name:
            category = "Biznes Funksionallıqları (P0, P1, P2)"
        elif name_clean.startswith("NEQ") or "NegativeScenarios" in full_name:
            category = "Neqativ və Təhlükəsizlik Ssenariləri"
        elif name_clean.startswith("DİL") or "Localization" in full_name:
            category = "Çoxdillilik (AZ / EN / RU)"
        elif name_clean.startswith("GİRİŞ") or "LoginPage" in full_name:
            category = "Giriş və Avtorizasiya"
        elif name_clean.startswith("QEYD") or "RegisterPage" in full_name:
            category = "Qeydiyyat"
        elif name_clean.startswith("BƏRPA") or "ForgotPassword" in full_name:
            category = "Şifrə Bərpası"
        elif name_clean.startswith("ANA") or name_clean.startswith("TARİF") or name_clean.startswith("SAYT") or name_clean.startswith("XƏTA") or "PublicRoutes" in full_name or "SiteAvailability" in full_name:
            category = "İctimai Səhifələr və Naviqasiya"

        duration_sec = round(data.get("time", {}).get("duration", 0) / 1000.0, 1)

        # Failure / History Analysis
        status = data.get("status", "passed")
        history_id = data.get("historyId", "")
        last_passed_info = None

        if history_id and history_id in history_data:
            hist_entry = history_data[history_id]
            items = hist_entry.get("items", [])
            for item in items:
                if item.get("status") == "passed":
                    stop_ms = item.get("time", {}).get("stop")
                    if stop_ms:
                        dt = datetime.datetime.fromtimestamp(stop_ms / 1000.0)
                        last_passed_info = {
                            "uid": item.get("uid"),
                            "formatted": dt.strftime("%d.%m.%Y, %H:%M"),
                            "timestamp": stop_ms
                        }
                    break

        steps = extract_steps_for_test(data)

        records.append({
            "uid": data.get("uid"),
            "name": name,
            "feature": feature,
            "story": story,
            "category": category,
            "status": status,
            "duration": duration_sec,
            "description": data.get("description", ""),
            "steps": steps,
            "lastPassed": last_passed_info,
            "images": images
        })

    # Sort records by category order and test name
    def sort_key(item):
        cat = item["category"]
        order = category_order.index(cat) if cat in category_order else 99
        return (order, item["name"])

    records.sort(key=sort_key)

    output_path = os.path.join(report_dir, "data", "gallery.json")
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(records, f, ensure_ascii=False, indent=2)

    print(f"Generated {output_path} with {len(records)} test records and {sum(len(r['images']) for r in records)} screenshots.")
    return True

if __name__ == "__main__":
    target_dir = sys.argv[1] if len(sys.argv) > 1 else "/tmp/allure-deploy"
    generate_gallery(target_dir)
