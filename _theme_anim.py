import io
p = r"app\src\main\res\values\themes.xml"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if "windowAnimationStyle" not in s:
    s = s.replace("""    <style name="Theme.Ledger" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:windowBackground">@color/ledger_background</item>""",
"""    <!-- 打开/返回都用同一套淡入 + 轻推，配合 Android 14 的预见性返回手势 -->
    <style name="Theme.Ledger.Animation" parent="">
        <item name="android:activityOpenEnterAnimation">@anim/slide_in_right</item>
        <item name="android:activityOpenExitAnimation">@anim/slide_out_left</item>
        <item name="android:activityCloseEnterAnimation">@anim/slide_in_left</item>
        <item name="android:activityCloseExitAnimation">@anim/slide_out_right</item>
    </style>

    <style name="Theme.Ledger" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:windowAnimationStyle">@style/Theme.Ledger.Animation</item>
        <item name="android:windowBackground">@color/ledger_background</item>""")
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("theme OK")
else:
    print("theme already has animation")
