import io, os
D = r"app\src\main\res\anim"
os.makedirs(D, exist_ok=True)

files = {
"slide_in_right.xml": '''<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:interpolator="@android:interpolator/fast_out_slow_in">
    <translate
        android:fromXDelta="12%p"
        android:toXDelta="0"
        android:duration="260" />
    <alpha
        android:fromAlpha="0.0"
        android:toAlpha="1.0"
        android:duration="220" />
</set>
''',
"slide_out_left.xml": '''<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:interpolator="@android:interpolator/fast_out_linear_in">
    <translate
        android:fromXDelta="0"
        android:toXDelta="-6%p"
        android:duration="260" />
    <alpha
        android:fromAlpha="1.0"
        android:toAlpha="0.0"
        android:duration="200" />
</set>
''',
"slide_in_left.xml": '''<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:interpolator="@android:interpolator/fast_out_slow_in">
    <translate
        android:fromXDelta="-6%p"
        android:toXDelta="0"
        android:duration="260" />
    <alpha
        android:fromAlpha="0.0"
        android:toAlpha="1.0"
        android:duration="220" />
</set>
''',
"slide_out_right.xml": '''<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:interpolator="@android:interpolator/fast_out_linear_in">
    <translate
        android:fromXDelta="0"
        android:toXDelta="12%p"
        android:duration="260" />
    <alpha
        android:fromAlpha="1.0"
        android:toAlpha="0.0"
        android:duration="200" />
</set>
''',
}
for name, body in files.items():
    io.open(os.path.join(D, name), "w", encoding="utf-8", newline="\n").write(body)
    print("wrote", name)
