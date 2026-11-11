import json, urllib.request, urllib.parse

UA = {"User-Agent": "WithinLedger/1.0", "Accept": "application/vnd.github+json"}

def gh(path):
    url = "https://api.github.com" + path
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=25) as r:
        return json.loads(r.read().decode())

try:
    for q in ["bank logo china svg", "中国 银行 logo svg", "chinese bank icons"]:
        res = gh("/search/repositories?" + urllib.parse.urlencode({"q": q, "sort": "stars", "per_page": 6}))
        print("=== query:", q, "total:", res.get("total_count"))
        for item in res.get("items", []):
            print("   ", item["full_name"], "★", item["stargazers_count"], "-", (item.get("description") or "")[:70])
except Exception as e:
    print("ERR", e)
