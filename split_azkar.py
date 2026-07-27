import json
import os

def split_azkar():
    input_path = os.path.join('app', 'src', 'main', 'assets', 'azkar.json')
    output_dir = os.path.join('app', 'src', 'main', 'assets', 'azkar')
    
    os.makedirs(output_dir, exist_ok=True)
    
    with open(input_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    category_filename_map = {
        "أذكار الصباح": "morning_azkar.json",
        "أذكار المساء": "evening_azkar.json",
        "أذكار بعد السلام من الصلاة المفروضة": "after_prayer_azkar.json",
        "أذكار النوم": "sleep_azkar.json",
        "أذكار الاستيقاظ": "wakeup_azkar.json",
        "دعاء الاستخارة": "istikhara_dua.json",
        "دعاء للمريض": "sick_dua.json"
    }

    for cat_name, items in data.items():
        filename = category_filename_map.get(cat_name)
        if not filename:
            # Fallback slug if category name isn't mapped explicitly
            slug = cat_name.replace(' ', '_')
            filename = f"{slug}.json"
        
        file_path = os.path.join(output_dir, filename)
        content = {
            "category": cat_name,
            "items": items
        }
        with open(file_path, 'w', encoding='utf-8') as out_f:
            json.dump(content, out_f, ensure_ascii=False, indent=3)
        print(f"Created {file_path} with {len(items)} items.")

if __name__ == '__main__':
    split_azkar()
