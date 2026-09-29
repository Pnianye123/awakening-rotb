# Modrinth 发布草稿 · Awakening: Return of the Builder

> Jimmy 专用的发布小抄。注册完 Modrinth 账号、Create Project 时照着抄就行。
> 改文字时只改这一个文件，别去改根 README——那是给 GitHub 看的，Modrinth 这里要更"项目页"一点。

---

## 一、Create Project 表单字段

| 字段 | 填什么 |
|---|---|
| **Project name** | `Awakening: Return of the Builder` |
| **Project slug** | `awakening-rotb`（自动从 name 生成，确认是这条就行） |
| **Project type** | **Datapack**（不是 Mod，要选对） |
| **Short description** | 见下方"二" |
| **Categories** | `Adventure` 必选；可选加 `Quest`（剧情向） |
| **Additional categories** | — |
| **License** | `MIT` |
| **Homepage URL** | `https://github.com/Pnianye123/awakening-rotb` |
| **Source URL** | 同上 |
| **Wiki URL** | 暂时不填 |
| **Issues URL** | `https://github.com/Pnianye123/awakening-rotb/issues` |

> ⚠️ Modrinth 创建 Project 时**不会**让你填 Minecraft 版本 / Loader——这些在上传 Version 时填。表单一提交立刻跳到 "Upload a version" 页面。

---

## 二、Short Description（≤200 字符，直接复制）

```
Wake as the ancient Builder and relive 14 continuous eras of Minecraft history — from the Symbiosis Era to the End Poem. A story-driven datapack. One ending.
```

字符数：157 ✅

---

## 三、Long Description（markdown，直接复制）

下面这一坨**整段复制**到 Project 页面的 "Body" 字段：

```markdown
# Awakening: Return of the Builder
## 《方块之下》

> *You are not from this world. But you have walked this land for a very long time.*

A story-driven Minecraft datapack. You wake as the ancient Builder — asleep for millennia — and relive 14 continuous eras of history: from the Symbiosis Era with the villagers, through the Wither's birth, the Echo Sentinel, the Dragon War, and finally the End Poem.

**This is not a chapter-select menu. It is one continuous story.** When one thing ends, the next begins. There is only one ending: the End Poem.

---

## The 14 Eras

| # | Era | Summary |
|---|-----|---------|
| — | Awakening | You wake. The world has been empty for thousands of years. |
| 1 | Symbiosis | Villagers and Builders live in peace. |
| 2 | Nether Expedition | The Builders open the Nether portal. Many do not return. |
| 3 | The Calamity's Mercy | Some villagers turn to dark arts — but they will not harm you. |
| 4 | Request to Resurrect | The Builders ask the Calamity villagers to bring back the dead. |
| 5 | The Wither's Sin | A forbidden experiment gives birth to the Wither. |
| 6 | The Wither Plague | The plague spreads. The overworld falls. |
| 7 | The Echo Sentinel | You summon the Sentinel from soul and shriek. |
| 8 | Beacon Purification | You raise the beacon. The plague is cleansed. |
| 9 | The Calamity's Revenge | The Calamity villagers, their resurrection broken, become dark mages. |
| 10 | The Second Hiding | You retreat into the stronghold. |
| 11 | The End Expedition | Eyes of Ender open the End portal. |
| 12 | Dragon War | You fight every Ender Dragon — and leave one to guard the gate. |
| 13 | End Civilization | You found the End City. You invent Elytra. |
| 14 | The No-Return Path | You become an Enderman... then wake in the present. |
| — | End Poem | The only ending. Two voices speak. You do not fully understand. You know it is not over. |

---

## Current Version: v0.1 — Datapack MVP

A prototype of the full story arc. **No Fabric. No Java. Vanilla-compatible.**

- ✅ All 14 eras + End Poem trigger automatically
- ✅ Continuous playback (~2.5 minutes total)
- ✅ Title, lore text, and advancement per era
- ⏳ Cutscenes — planned for v0.5
- ⏳ Builder's Robe skin — planned for v0.5
- ⏳ Echo Sentinel boss fight — planned for v1.0

### Install (v0.1)

1. Download `awakening-rotb-v0.1.zip` from the version page below.
2. Extract to `.minecraft/saves/<your world>/datapacks/awakening-rotb/`.
3. Enter the world.
4. Run `/reload`, then `/function awakening:setup`.

### Debug

```
/function awakening:era1          # jump to era 1
/function awakening:era7          # jump to era 7 (Echo Sentinel)
/function awakening:ending        # jump to End Poem
/scoreboard players set @s awakening.current_era 14   # then /function awakening:advance_era
/advancement revoke @s everything # reset all progress
```

---

## Roadmap

- **v0.5** — Fabric Java mod core: Builder's Robe, cutscenes, Awakener NPC
- **v1.0** — Full story: Echo Sentinel boss, beacon quest, End Poem playback
- **v1.5** — Free-play rewards: Builder's Robe, Resonance Stone, 3 original disc tracks
- **v2.0+** — Multi-language, Modpack companion

---

## Lore Rules (do not change)

These are hard rules from the project lead. They define the world:

1. The Sentinel is **not** the vanilla Warden. English: *The Echo Sentinel*. Chinese: 坚守者.
2. The "shriek" is not Sculk/Skulk. It is a fungal resonance network the Builders cultivated.
3. The Ancient City was built by the Builders — not a natural cave.
4. The Calamity villagers do **not** harm villagers or Builders — initially. They turn hostile only after their resurrection ritual fails.
5. Their dark magic comes from **cultivation**, not natural occurrence.
6. Wither Plague and Undead Plague are two independent events. Wither Plague is radiation damage; Undead Plague is necromancy.
7. The Ender Dragon has a "population." After being defeated, one remains to guard the gate.
8. Endermen are Builders who stayed in the End too long.

---

## Credits & License

Created by Jimmy (Pnianye123). All lore and design by the project lead.

**MIT License** — Copyright (c) 2026 Jimmy (Pnianye123)

Source code: https://github.com/Pnianye123/awakening-rotb
Issues: https://github.com/Pnianye123/awakening-rotb/issues
```

---

## 四、Upload Version 表单字段（v0.1 那一步用）

| 字段 | 填什么 |
|---|---|
| **Version number** | `0.1.0` |
| **Version title** | `v0.1 — Datapack MVP` |
| **Version changelog** | 见下方 |
| **Minecraft versions** | `1.21`, `1.21.1`, `1.21.2`, `1.21.3`, `1.21.4`（多选） |
| **Mod loaders** | `datapack`（多选；v0.5+ 才有 Fabric 选项） |
| **File** | 上传 `awakening-rotb-v0.1.zip`（29 KB，在 file-share 里有） |

### Changelog（直接复制）

```
First public release — Datapack MVP.

The 14-era story arc plays end-to-end as a continuous flow:
- Awakening → 14 Eras → End Poem
- Automatic progression (~2.5 min total, ~10s between eras)
- Title + lore text + advancement per era
- Debug functions for jumping between eras
- Pure datapack — vanilla 1.21.x only, no Fabric or Java needed

Cutscenes, the Builder's Robe, and the Echo Sentinel boss are planned for v0.5+ and not included here.

This is a prototype. The lore and pacing are still being tuned. Bug reports and story feedback welcome on GitHub.
```

---

## 五、图标 / 横幅

- **Icon**：512×512 PNG，正方形。Modrinth 必填。**v0.1 阶段可以用占位符**——比如一张古城远景图、或者"AB"两个字母 + 黑金主题。Jimmy 之后再画正式的。
- **Banner**：项目页顶部横幅，可选，v0.1 不传也行。
- 颜色建议：黑底 + 金色（呼应建造师主题）。可以用 `#000000` 背景 + `#FFD700` 文字。

---

## 六、提交前 checklist

- [ ] Project name: `Awakening: Return of the Builder`
- [ ] Slug: `awakening-rotb`
- [ ] Project type: **Datapack**（不是 Mod）
- [ ] Categories: `Adventure`（可选加 `Quest`）
- [ ] License: `MIT`
- [ ] Homepage URL: `https://github.com/Pnianye123/awakening-rotb`
- [ ] Long description 已经粘进去
- [ ] 上传 v0.1 zip，version number `0.1.0`
- [ ] Minecraft versions: 1.21 / 1.21.1 / 1.21.2 / 1.21.3 / 1.21.4
- [ ] Mod loader: `datapack`
- [ ] changelog 已经粘进去
- [ ] 图标（占位符也行）已经传

完成后 Modrinth 会给你一个项目链接，类似 `https://modrinth.com/datapack/awakening-rotb`。把那个链接发给 Jimmy 我们再继续下一步（README 加 Modrinth badge / Modpack 那一侧）。

---

_Generated by Steve · 2026-09-29_