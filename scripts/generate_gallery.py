#!/usr/bin/env python3
"""
Generates data/gallery.json from Allure test-case data.
Scans all test-cases JSON files, extracts screenshot attachments, categories,
and metadata so the custom gallery widget can display visual test results directly.
"""

import glob
import json
import os
import sys

def generate_gallery(report_dir):
    test_cases_dir = os.path.join(report_dir, "data", "test-cases")
    if not os.path.isdir(test_cases_dir):
        print(f"Error: {test_cases_dir} does not exist", file=sys.stderr)
        return False

    records = []
    category_order = [
        "İctimai Səhifələr və Naviqasiya",
        "Çoxdillilik (AZ / EN / RU)",
        "Giriş və Avtorizasiya",
        "Qeydiyyat",
        "Şifrə Bərpası",
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

        if not images:
            continue

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
        if name_clean.startswith("DİL") or "Localization" in full_name:
            category = "Çoxdillilik (AZ / EN / RU)"
        elif name_clean.startswith("GİRİŞ") or "LoginPage" in full_name:
            category = "Giriş və Avtorizasiya"
        elif name_clean.startswith("QEYD") or "RegisterPage" in full_name:
            category = "Qeydiyyat"
        elif name_clean.startswith("BƏRPA") or "ForgotPassword" in full_name:
            category = "Şifrə Bərpası"
        elif name_clean.startswith("ANA") or "PublicRoutes" in full_name or "SiteAvailability" in full_name:
            category = "İctimai Səhifələr və Naviqasiya"

        duration_sec = round(data.get("time", {}).get("duration", 0) / 1000.0, 1)

        records.append({
            "uid": data.get("uid"),
            "name": name,
            "feature": feature,
            "story": story,
            "category": category,
            "status": data.get("status", "passed"),
            "duration": duration_sec,
            "description": data.get("description", ""),
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
