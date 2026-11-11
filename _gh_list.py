import json, urllib.request

UA = {"User-Agent": "WithinLedger/1.0", "Accept": "application/vnd.github+json"}

def get(url):
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=25) as r:
        return json.loads(r.read().decode())

for repo in ["orzFly/payment-webfont-cn", "milesking/china-bank-logo-vector-svg"]:
    print("===", repo)
    try:
        items = get("https://api.github.com/repos/%s/contents" % repo)
        for it in items[:25]:
            print("   ", it["type"], it["name"], it.get("size", ""))
    except Exception as e:
        print("   ERR", e)
