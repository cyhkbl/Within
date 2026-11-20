import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\more\MoreScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

s = s.replace("""                                        R.string.more_08, R.string.more_10 -> context.startActivity(
                                            android.content.Intent(context, ImportExportActivity::class.java)
                                        )""",
"""                                        R.string.more_08 -> context.startActivity(
                                            android.content.Intent(context, ImportExportActivity::class.java)
                                                .putExtra(
                                                    ImportExportActivity.EXTRA_MODE,
                                                    ImportExportActivity.MODE_IMPORT
                                                )
                                        )
                                        R.string.more_10 -> context.startActivity(
                                            android.content.Intent(context, ImportExportActivity::class.java)
                                                .putExtra(
                                                    ImportExportActivity.EXTRA_MODE,
                                                    ImportExportActivity.MODE_EXPORT
                                                )
                                        )""")

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("MoreScreen mode OK")
