import json, re, sys, torch
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM
sys.path.insert(0, "."); from en import EN
torch.set_num_threads(7)
M = "facebook/nllb-200-distilled-600M"
tok = AutoTokenizer.from_pretrained(M, src_lang="eng_Latn"); model = AutoModelForSeq2SeqLM.from_pretrained(M).eval()
CODES = {"ti":"tir_Ethi","fr":"fra_Latn","es":"spa_Latn","ar":"arb_Arab","zh":"zho_Hans"}
PH = re.compile(r"%(\d)\$(\.\df|[sd])")
SPLIT = re.compile(r"(?<=[.!?])\s+(?=[A-Z0-9])")
multi = {k: SPLIT.split(v) for k, v in EN.items() if len(SPLIT.split(v)) > 1}
print(len(multi), "multi-sentence keys")
def tr(code, texts):
    out = []
    for i in range(0, len(texts), 12):
        b = texts[i:i+12]
        enc = tok(b, return_tensors="pt", padding=True, truncation=True, max_length=200)
        with torch.no_grad():
            g = model.generate(**enc, forced_bos_token_id=tok.convert_tokens_to_ids(CODES[code]), max_new_tokens=160, num_beams=2, no_repeat_ngram_size=4)
        out += tok.batch_decode(g, skip_special_tokens=True)
    return out
for code in sys.argv[1:]:
    p = f"mt_{code}.json"; res = json.load(open(p))
    flat = []; idx = []
    for k, sents in multi.items():
        for s in sents:
            m = {}
            def rep(mo):
                t = ["X", "Y", "Z"][int(mo.group(1)) - 1]; m[t] = mo.group(0); return t
            flat.append(PH.sub(rep, s)); idx.append((k, m))
    outs = tr(code, flat)
    joined = {}
    for (k, m), o in zip(idx, outs):
        for t, ph in m.items(): o = re.sub(r"(?<![A-Za-z])" + t + r"(?![A-Za-z])", ph.replace("\\", "\\\\"), o, count=1)
        joined.setdefault(k, []).append(o)
    sep = "" if code == "zh" else " "
    for k, parts in joined.items(): res[k] = sep.join(parts)
    json.dump(res, open(p, "w"), ensure_ascii=False, indent=0); print(code, "done", flush=True)
