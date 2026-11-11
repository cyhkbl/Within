import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\assets\AssetsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

start = s.find('                Text("渠道", style = MaterialTheme.typography.labelLarge)')
end = s.find('                Spacer(Modifier.height(4.dp))\n                OutlinedTextField(')
if start < 0 or end < 0:
    print("MISS block", start, end)
else:
    new_block = '''                AccountChannels.all
                    .groupBy { it.group }
                    .forEach { (group, channels) ->
                        Text(
                            AccountChannels.groupLabel(group),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        channels.chunked(4).forEach { rowChannels ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowChannels.forEach { option ->
                                    val selected = option.key == channel
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                channel = option.key
                                                if (!nameTouched) {
                                                    name = AccountChannels.nextName(
                                                        option.key,
                                                        existingNames.filter { it != initial?.name }
                                                    )
                                                }
                                            }
                                            .padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        ChannelGlyph(channel = option.key, size = 40.dp)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            option.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            color = if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                repeat(4 - rowChannels.size) {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                        Spacer(Modifier.height(6.dp))
                    }

'''
    s = s[:start] + new_block + s[end:]
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("dialog rebuilt")
