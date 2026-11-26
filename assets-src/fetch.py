import json, urllib.request, urllib.parse, sys, os, re, time
UA={"User-Agent":"EthiopiaHotelAppBuilder/1.0 (educational; contact rebuma)"}
def api(params):
    params.update(format="json")
    url="https://commons.wikimedia.org/w/api.php?"+urllib.parse.urlencode(params)
    for i in range(4):
        try:
            return json.load(urllib.request.urlopen(urllib.request.Request(url,headers=UA),timeout=30))
        except Exception as e:
            time.sleep(3)
    raise
queries=json.load(open(sys.argv[1]))
out={}
for key,q in queries.items():
    r=api(dict(action="query",generator="search",gsrsearch=q+" filetype:bitmap",gsrnamespace=6,gsrlimit=6,prop="imageinfo",iiprop="url|extmetadata|size",iiurlwidth=1000))
    pages=sorted(r.get("query",{}).get("pages",{}).values(),key=lambda p:p.get("index",0))
    cands=[]
    for p in pages:
        ii=p["imageinfo"][0]; m=ii.get("extmetadata",{})
        lic=m.get("LicenseShortName",{}).get("value","")
        if ii["width"]<900: continue
        if not re.search(r"CC|Public domain|PD",lic,re.I): continue
        artist=re.sub("<[^>]+>","",m.get("Artist",{}).get("value","")).strip()
        cands.append(dict(title=p["title"],thumb=ii["thumburl"],page=ii["descriptionurl"],license=lic,artist=artist,w=ii["width"],h=ii["height"]))
    out[key]=cands[:3]
    os.makedirs("cand",exist_ok=True)
    for i,c in enumerate(cands[:3]):
        fn=f"cand/{key}_{i}.jpg"
        if not os.path.exists(fn):
            try:
                open(fn,"wb").write(urllib.request.urlopen(urllib.request.Request(c["thumb"],headers=UA),timeout=60).read())
            except Exception as e: print("fail",fn,e)
            time.sleep(0.5)
    print(key,len(cands),flush=True)
json.dump(out,open(sys.argv[2],"w"),indent=1)
