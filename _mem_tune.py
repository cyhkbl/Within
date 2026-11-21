import io
p = r"gradle.properties"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8",
              "org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=768m -Dfile.encoding=UTF-8")
if "kotlin.daemon.jvmargs" not in s:
    s = s.replace("kotlin.code.style=official",
                  "kotlin.code.style=official\nkotlin.daemon.jvmargs=-Xmx1536m")
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("gradle.properties tuned")
