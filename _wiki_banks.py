import json, urllib.request, urllib.parse, os, re

UA = {"User-Agent": "WithinLedger/1.0 (https://github.com/cyhkbl/Within)"}
OUT = r"samples\banks"
os.makedirs(OUT, exist_ok=True)

def api(params, host="commons.wikimedia.org"):
    url = "https://%s/w/api.php?" % host + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=25) as r:
        return json.loads(r.read().decode("utf-8"))

banks = {
    "icbc": "Industrial and Commercial Bank of China",
    "boc": "Bank of China",
    "cmb": "China Merchants Bank",
    "ccb": "China Construction Bank",
    "abc": "Agricultural Bank of China",
    "bocom": "Bank of Communications",
    "psbc": "Postal Savings Bank of China",
    "cib": "Industrial Bank",
    "spdb": "Shanghai Pudong Development Bank",
    "cmbc": "China Minsheng Bank",
    "ceb": "China Everbright Bank",
    "citic": "China CITIC Bank",
    "pab": "Ping An Bank",
    "hxb": "Hua Xia Bank",
    "cgb": "China Guangfa Bank",
}

found = {}
for key, name in banks.items():
    try:
        res = api({"action":"query","list":"search","srsearch":name + " logo","srnamespace":"6","srlimit":"8","format":"json"})
        hits = [h["title"] for h in res.get("query",{}).get("search",[])]
        svgs = [h for h in hits if h.lower().endswith(".svg")]
        found[key] = svgs[:2]
        print(f"{key:6s} {name[:38]:40s} -> {svgs[:2] if svgs else 'none'}")
    except Exception as e:
        print(key, "ERR", e)

json.dump(found, open(r"samples\banks\_found.json","w"), ensure_ascii=False, indent=2)
