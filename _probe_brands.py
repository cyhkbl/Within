import json, urllib.request, urllib.parse

UA = {"User-Agent": "WithinLedger/1.0"}

def search(q, prefixes=""):
    url = "https://api.iconify.design/search?" + urllib.parse.urlencode(
        {"query": q, "limit": 12, **({"prefixes": prefixes} if prefixes else {})})
    req = urllib.request.Request(url, headers=UA)
    try:
        with urllib.request.urlopen(req, timeout=20) as r:
            return json.loads(r.read().decode())["icons"]
    except Exception as e:
        return ["ERR " + str(e)]

brands = ["wechat","alipay","unionpay","jd","meituan","douyin","tiktok","qq","huawei",
          "pinduoduo","apple","paypal","bitcoin","icbc","bank of china","china merchants",
          "china construction","agricultural bank","bank of communications","postal savings",
          "industrial bank","spdb","minsheng","everbright","citic","pingan"]
for b in brands:
    hits = search(b, "simple-icons,logos,thesvg-color,arcticons")
    print(f"{b:24s} -> {', '.join(hits[:4]) if hits else '(none)'}")
