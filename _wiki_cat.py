import json, urllib.request, urllib.parse

UA = {"User-Agent": "WithinLedger/1.0 (https://github.com/cyhkbl/Within)"}

def api(params):
    url = "https://commons.wikimedia.org/w/api.php?" + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=25) as r:
        return json.loads(r.read().decode("utf-8"))

for cat in ["Category:Logos of banks of China", "Category:Banks of China logos", "Category:Logos of Chinese banks"]:
    try:
        res = api({"action":"query","list":"categorymembers","cmtitle":cat,"cmlimit":"200","format":"json"})
        members = [m["title"] for m in res.get("query",{}).get("categorymembers",[])]
        svgs = [m for m in members if m.lower().endswith(".svg")]
        print(f"{cat}: total={len(members)} svg={len(svgs)}")
        for s in svgs[:30]:
            print("   ", s)
    except Exception as e:
        print(cat, "ERR", e)
