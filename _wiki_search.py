import json, urllib.request, urllib.parse, os

UA = {"User-Agent": "WithinLedger/1.0 (https://github.com/cyhkbl/Within)"}

def api(params):
    url = "https://commons.wikimedia.org/w/api.php?" + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=25) as r:
        return json.loads(r.read().decode("utf-8"))

queries = {
    "icbc": "Industrial and Commercial Bank of China logo",
    "boc": "Bank of China logo",
    "cmb": "China Merchants Bank logo",
    "ccb": "China Construction Bank logo",
    "abc": "Agricultural Bank of China logo",
    "bocom": "Bank of Communications logo",
}

for key, q in queries.items():
    try:
        res = api({"action":"query","list":"search","srsearch":q,"srnamespace":"6","srlimit":"6","format":"json"})
        hits = [h["title"] for h in res["query"]["search"]]
        svgs = [h for h in hits if h.lower().endswith(".svg")]
        print(key, "->", svgs[:3] if svgs else hits[:3])
    except Exception as e:
        print(key, "ERR", e)
