import re, os, sys, importlib, json
sys.path.insert(0, os.path.dirname(__file__))
from en import EN
RES = "/workspace/ethiopia-hotel-app/app/src/main/res"
LANGS = {"am": "values-am", "om": "values-om", "ti": "values-ti", "ar": "values-ar", "fr": "values-fr", "zh": "values-zh-rCN", "es": "values-es"}
PH = re.compile(r"%\d\$[sd]|%\d\$\.\df")

def esc(s):
    s = s.replace("\\", "\\\\").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    s = s.replace("'", "\\'").replace('"', '\\"').replace("\n", "\\n")
    if s.startswith("@") or s.startswith("?"): s = "\\" + s
    return s

PLURAL_ONE = {"nights_count": "%1$d night", "guests_count": "%1$d guest", "reviews_count": "%1$d review", "hotels_count": "%1$d hotel",
              "stays_n": "%1$d stay", "days_before": "%1$d day before", "rooms_left": "Only %1$d left"}
QTY = ["zero", "one", "two", "few", "many", "other"]

def write(folder, d, extra_attr="", lang="en"):
    os.makedirs(f"{RES}/{folder}", exist_ok=True)
    lines = ['<?xml version="1.0" encoding="utf-8"?>', '<resources xmlns:tools="http://schemas.android.com/tools">' if folder == "values" else "<resources>"]
    for k in sorted(d):
        if k in PLURAL_ONE:
            lines.append(f'    <plurals name="{k}">')
            if lang == "en":
                lines.append(f'        <item quantity="one">{esc(PLURAL_ONE[k])}</item>')
                lines.append(f'        <item quantity="other">{esc(d[k])}</item>')
            else:
                for q in QTY: lines.append(f'        <item quantity="{q}">{esc(d[k])}</item>')
            lines.append('    </plurals>')
            continue
        attrs = ' formatted="false"' if ("%" in d[k] and not PH.search(d[k])) else ""
        lines.append(f'    <string name="{k}"{attrs}>{esc(d[k])}</string>')
    lines.append("</resources>")
    open(f"{RES}/{folder}/strings.xml", "w", encoding="utf-8").write("\n".join(lines) + "\n")

write("values", EN)
report = {}
for code, folder in LANGS.items():
    d = {}; hand = set()
    for src in (f"mt_{code}.json", f"hand_{code}.json"):   # hand translations override machine translations
        p = os.path.join(os.path.dirname(__file__), src)
        if os.path.exists(p):
            part = json.load(open(p, encoding="utf-8")); d.update(part)
            if src.startswith("hand_"): hand |= set(part)
    good = {}
    for k, v in d.items():
        if k not in EN or not v.strip(): continue
        if sorted(PH.findall(v)) != sorted(PH.findall(EN[k])): continue   # placeholder mismatch -> fall back to English
        w = v.split()
        if k not in hand and any(w[i] == w[i+1] == w[i+2] for i in range(len(w) - 2)): continue  # degenerate MT repetition
        if k not in hand and len(v) > 3.2 * len(EN[k]) + 12: continue
        good[k] = v
    report[code] = (len(good), len(EN))
    if good: write(folder, good, lang=code)
print(report)
