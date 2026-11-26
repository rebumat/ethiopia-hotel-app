import json, re, sys, os, time, torch
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM
sys.path.insert(0, "."); from en import EN
torch.set_num_threads(7)
M = "facebook/nllb-200-distilled-600M"
tok = AutoTokenizer.from_pretrained(M, src_lang="eng_Latn"); model = AutoModelForSeq2SeqLM.from_pretrained(M).eval()
CODES = {"ti":"tir_Ethi","fr":"fra_Latn","es":"spa_Latn","ar":"arb_Arab","zh":"zho_Hans","am":"amh_Ethi","om":"gaz_Latn"}
PH = re.compile(r"%(\d)\$(\.\df|[sd])")
keys = sorted(EN)
for code in sys.argv[1:]:
    out_path = f"mt_{code}.json"; res = json.load(open(out_path)) if os.path.exists(out_path) else {}
    todo = [k for k in keys if k not in res]
    t0 = time.time()
    for i in range(0, len(todo), 12):
        batch = todo[i:i+12]
        src = []; maps = []
        for k in batch:
            m = {}
            def rep(mo):
                tokn = ["X", "Y", "Z"][int(mo.group(1)) - 1]; m[tokn] = mo.group(0); return tokn
            src.append(PH.sub(rep, EN[k])); maps.append(m)
        enc = tok(src, return_tensors="pt", padding=True, truncation=True, max_length=200)
        with torch.no_grad():
            gen = model.generate(**enc, forced_bos_token_id=tok.convert_tokens_to_ids(CODES[code]), max_new_tokens=220, num_beams=2)
        for k, txt, m in zip(batch, tok.batch_decode(gen, skip_special_tokens=True), maps):
            for tokn, ph in m.items():
                txt = re.sub(r"(?<![A-Za-z])" + tokn + r"(?![A-Za-z])", ph.replace("\\", "\\\\"), txt, count=1)
            res[k] = txt
        json.dump(res, open(out_path, "w"), ensure_ascii=False, indent=0)
        print(code, i + len(batch), "/", len(todo), int(time.time() - t0), "s", flush=True)
