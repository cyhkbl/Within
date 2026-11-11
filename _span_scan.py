import os
from PIL import Image

TMP = r"samples\bank-render"

def is_ink(r, g, b):
    return r < 245 or g < 245 or b < 245

# 逐列统计墨水的垂直跨度：标志的跨度大，文字小，用这个分界
for f in sorted(os.listdir(TMP)):
    if not f.endswith(".png"): continue
    im = Image.open(os.path.join(TMP, f)).convert("RGB")
    W, H = im.size
    px = im.load()
    spans = []
    for x in range(W):
        top, bot = None, None
        for y in range(H):
            if is_ink(*px[x, y]):
                if top is None: top = y
                bot = y
        spans.append(0 if top is None else bot - top + 1)

    maxspan = max(spans) if spans else 0
    # 找第一个跨度明显下降的位置（标志结束、文字开始）
    cut = W
    for x in range(1, W):
        if spans[x] < maxspan * 0.55 and spans[x] > 0:
            # 确认后面持续是小跨度
            tail = spans[x:x + 20]
            if tail and all(s < maxspan * 0.6 for s in tail):
                cut = x
                break
    print(f"{f:16s} W={W} H={H} maxspan={maxspan} cut={cut}")
