import urllib.request
import json
import time
import os

BASE_URL = "https://api.quran.com/api/v4"
OUTPUT_FILE = "app/src/main/assets/quran/quran_en.json"
RECITER_ID = 7 # Mishary Alafasy

# Ensure dir exists
os.makedirs(os.path.dirname(OUTPUT_FILE), exist_ok=True)

all_ayahs = []

def fetch_json(url):
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as response:
        return json.loads(response.read().decode())

print("Downloading Quran data...")
for chapter in range(1, 115):
    print(f"Fetching chapter {chapter}...")
    
    # Fetch verses and translation (translation id 85 is English Clear Quran)
    verses_url = f"{BASE_URL}/verses/by_chapter/{chapter}?language=en&translations=85&fields=text_uthmani&per_page=300"
    try:
        verses_resp = fetch_json(verses_url)
    except Exception as e:
        print(f"Error fetching chapter {chapter} verses: {e}")
        time.sleep(1)
        verses_resp = fetch_json(verses_url)

    # Fetch audio
    audio_url = f"{BASE_URL}/recitations/{RECITER_ID}/by_chapter/{chapter}?per_page=300"
    try:
        audio_resp = fetch_json(audio_url)
    except Exception as e:
        print(f"Error fetching chapter {chapter} audio: {e}")
        time.sleep(1)
        audio_resp = fetch_json(audio_url)

    audio_map = {}
    if "audio_files" in audio_resp:
        for a in audio_resp["audio_files"]:
            vkey = a.get("verse_key", "")
            if ":" in vkey:
                num = int(vkey.split(":")[1])
            else:
                num = a.get("verse_number")
            url = a.get("url", "")
            if url:
                if url.startswith("//"):
                    url = "https:" + url
                elif not url.startswith("http"):
                    url = "https://verses.quran.foundation/" + url.lstrip("/")
            if num:
                audio_map[num] = url
            
    if "verses" in verses_resp:
        for v in verses_resp["verses"]:
            ayah_num = v.get("verse_number")
            arabic = v.get("text_uthmani", "")
            translations = v.get("translations", [])
            translation = translations[0].get("text", "") if translations else ""
            
            # remove html tags from translation
            translation = translation.replace("<sup", " <sup").replace("</sup", " </sup")
            import re
            translation = re.sub('<[^<]+>', '', translation).strip()
            
            all_ayahs.append({
                "sura": chapter,
                "ayah": ayah_num,
                "arabicText": arabic,
                "translation": translation,
                "audioUrl": audio_map.get(ayah_num, "")
            })

    time.sleep(0.5) # Sleep to avoid rate limits

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    json.dump(all_ayahs, f, ensure_ascii=False)

print(f"Saved {len(all_ayahs)} ayahs to {OUTPUT_FILE}")
