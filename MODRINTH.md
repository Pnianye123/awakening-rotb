# Modrinth 发布草稿 · Awakening: Return of the Builder

> Jimmy 专用的发布小抄。注册完 Modrinth 账号、Create Project 时照着抄就行。
> 改文字时只改这一个文件，别去改根 README——那是给 GitHub 看的，Modrinth 这里要更"项目页"一点。
>
> ⚠️ **2026-11-02 重写**：旧版（2026-09-29）是在项目还是「Datapack MVP」时写的，
> 全部字段按 datapack 填。**项目后来转型成 Fabric Java mod**，旧版填法会让 Modrinth
> 建出一个类型错误的项目页（datapack 的 project type 不收 jar、也不填 loader）。
> 本版按**实际交付物**核对过：21 个 Java 文件 / `fabric.mod.json` / MC 1.21.4 / Fabric 0.16.14。

---

## 一、Create Project 表单字段

| 字段 | 填什么 |
|---|---|
| **Project name** | `Awakening: Return of the Builder` |
| **Project slug** | `awakening-rotb`（自动从 name 生成，确认是这条就行） |
| **Project type** | **`Mod`** ⚠️ 不是 Datapack（老版本这里写错了） |
| **Short description** | 见下方"二" |
| **Categories** | `Adventure` 必选；可选加 `Roleplay` / `Scenario` |
| **Additional categories** | — |
| **License** | `MIT` |
| **Homepage URL** | `https://github.com/Pnianye123/awakening-rotb` |
| **Source URL** | 同上 |
| **Wiki URL** | 暂时不填 |
| **Issues URL** | `https://github.com/Pnianye123/awakening-rotb/issues` |

> ⚠️ Modrinth 创建 Project 时**不会**让你填 Minecraft 版本 / Loader——这些在上传 Version 时填。表单一提交立刻跳到 "Upload a version" 页面。
>
> ⚠️ 老版本这里写的是 "Datapack"。**这是错的**：这项目有 21 个 Java 文件、
> `fabric.mod.json`、自定义物品/方块/音效注册、Fabric API 依赖 → 只能选 **Mod**。
> 选错 Datapack 的话：上传页不会让你选 Fabric loader，而且 datapack 类型不接受 jar。

---

## 二、Short Description（≤200 字符，直接复制）

```
Wake as the ancient Builder and relive 14 continuous eras of Minecraft history — from the Symbiosis Era to the End Poem. A story-driven Fabric mod. One ending.
```

字符数：155 ✅（老版本写的是 "datapack"，已改）

---

## 三、Long Description（markdown，直接复制）

下面这一坨**整段复制**到 Project 页面的 "Body" 字段：

```markdown
# Awakening: Return of the Builder
## 《方块之下》

> *You are not from this world. But you have walked this land for a very long time.*

A story-driven Minecraft mod for **Fabric**. You wake as the ancient Builder — asleep for millennia — and relive 14 continuous eras of history: from the Symbiosis Era with the villagers, through the Wither's birth, the Echo Sentinel, the Dragon War, and finally the End Poem.

**This is not a chapter-select menu. It is one continuous story.** When one thing ends, the next begins. There is only one ending: the End Poem.

---

## The 14 Eras

| # | Era | Summary |
|---|-----|---------|
| — | Awakening | You wake. The world has been empty for thousands of years. |
| 1 | 共生 Symbiosis | Villagers and Builders live in peace. The Awakener appears as your guide. |
| 2 | 下界远征 Nether Expedition | The Builders open the Nether portal. Many do not return. |
| 3 | 灾厄的善意 The Calamity's Mercy | Some villagers turn to dark arts — but they will not harm you. |
| 4 | 请求复活 Request to Resurrect | The Builders ask the Calamity villagers to bring back the dead. |
| 5 | 凋零之罪 The Wither's Sin | A forbidden experiment gives birth to the Wither. |
| 6 | 凋零病毒 The Wither Plague | The plague spreads. The overworld falls. |
| 7 | 坚守者 The Echo Sentinel | You summon the Sentinel from soul and shriek. |
| 8 | 信标净化 Beacon Purification | You raise the beacon. The plague is cleansed. |
| 9 | 灾厄的反噬 The Calamity's Revenge | The Calamity villagers, their resurrection broken, become dark mages. |
| 10 | 再次躲避 The Second Hiding | You retreat into the stronghold. |
| 11 | 末地远征 The End Expedition | Eyes of Ender open the End portal. |
| 12 | 龙之战 Dragon War | You fight every Ender Dragon — and leave one to guard the gate. |
| 13 | 末地文明 End Civilization | You found the End City. You invent Elytra. |
| 14 | 不归路 The No-Return Path | You become an Enderman... then wake in the present. |
| — | End Poem | The only ending. Two voices speak. You do not fully understand. You know it is not over. |

---

## What's in the mod

- **14 eras of continuous story** with original lore text, a title card and an advancement per era
- **Every era has a task you must complete by hand** — talk to villagers, mine obsidian, hand over a soul, summon the boss, raise the beacon
- **Two-part structure**: a 60–90 minute story arc, then a permanent free-play segment with 10 unlockable rewards
- **Story bosses** built on vanilla entities (Awakener, Calamity Cultists, Wither Son, the Echo Sentinel)
- **The Echo Sentinel summoning ritual** — find a sculk sensor, use the soul shard + echo stone to wake the guardian yourself
- **The Beacon Purification ritual** — place the resonance block, then strike it with a Nether Star to cleanse the world
- **13 custom items** (Builder's Robe, Resonance Stone, Soul Compass, Soul Shard, Echo Shard, Ancient City Key/Pass, Awakened Token, Wither Core, Recorder, 3 music discs)
- **7 custom blocks** (Resonance Block, Soul Lantern, Builder's Brick, Reinforced Echo Stone, Wither Rift, Awakened Totem, Builder's Robe block)
- **3 original music discs** (72s / 66s / 115s) — composed for this mod
- **Ancient City world generation** — a custom structure feature
- **A `/start` command** — the story does not begin until you ask it to
- **A prologue book** — every new player is handed a written book that explains who they are and what happened, and tells them to type `/start`
- **Full English / Chinese localization** — the whole story, all objectives and every interface line follow your client language
- **A `/awakening` command** for inspecting, skipping eras, switching language and free-play rewards

---

## Current Version: v1.0.0

A complete playthrough of the 14-era story arc.

- ✅ All 14 eras + the End Poem, played as one continuous flow
- ✅ One hand-completed objective per era
- ✅ **The story waits for you** — nothing auto-starts; you get a prologue book and type `/start` when you are ready
- ✅ **Full English + Chinese localization** — 218 lines of story, all objectives and every UI message
- ✅ Echo Sentinel summoning ritual (era 7)
- ✅ Beacon Purification ritual (era 8)
- ✅ Wither Son boss fight, Calamity Cultist encounters
- ✅ 13 items / 7 blocks / 3 original music discs / 20 recipes
- ✅ Ancient City structure generation
- ✅ Free-play segment with 10 rewards
- ✅ Commands: `/start` plus `/awakening status` · `book` · `lang` · `skip` · `freeplay` · `debug` · `reset`

### Requirements

- Minecraft **1.21.4**
- **Fabric Loader** 0.16.14+
- **Fabric API** 0.110.0+ (required — see the Dependencies tab)
- Java 21+

### Install

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for 1.21.4
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) 0.110.0+ for 1.21.4
3. Put both `fabric-api` and `awakening-rotb-1.0.0.jar` into your `mods/` folder
4. Launch the game, create a world, and enter the world
5. You will be handed a **prologue book** — read it, then type **`/start`** in chat

Nothing happens until you type `/start`. The book explains who you are and what happened before the story began.

> This mod must be installed on **both** the client and the server.

### Commands

```
/start                # begin the story (the only command a player needs)
/awakening status     # current era, boss counts, resonance blocks, inventory
/awakening book       # get another copy of the prologue book
/awakening lang zh|en # switch language manually (auto = follow client)
/awakening skip <n>   # jump to era n (debugging / skipping ahead)
/awakening freeplay   # unlock the free-play segment and its rewards
/awakening debug      # detailed diagnostic readout
/awakening reset      # wipe all story progress
```

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

## 四、Upload Version 表单字段

| 字段 | 填什么 |
|---|---|
| **Version number** | `1.0.0` ⚠️（老版本写的 `0.1.0`） |
| **Version title** | `v1.0.0 — The 14 Eras` |
| **Version changelog** | 见下方 |
| **Minecraft versions** | `1.21.4` ⚠️ **只勾这一个**（老版本勾了 1.21–1.21.4 五个，本 mod 只在 1.21.4 上验证过） |
| **Mod loaders** | **`Fabric`** ⚠️（老版本写的 `datapack`） |
| **Client side** | `Required` — 本 mod 注册了自定义物品/方块/音效，客户端必须装 |
| **Server side** | `Required` — `environment: *` |
| **File** | 上传 **`awakening-rotb-1.0.0.jar`**（3.9 MB）⚠️ 不是 zip、不是 v0.1 |

> ⚠️ **File 上传的是 jar 本体**，不是 `awakening-rotb-v1.0-test.zip`。
> zip 是给玩家的「源码 + 中文说明」完整包，走 8099 / file-share 那个渠道；
> Modrinth 只收 jar。

### 依赖（Dependencies 标签页）

| 关系 | 平台 | 项目 | 版本 |
|---|---|---|---|
| **Required** | Client + Server | [Fabric API](https://modrinth.com/mod/fabric-api) | `>=0.110.0` |

> `fabric.mod.json` 里 `depends` 写的是 `fabric-api: *`，但实际在 1.21.4 上验证的是 0.110.0+，
> 所以在 Modrinth 上按 `>=0.110.0` 填更准。

### Changelog（直接复制）

```
The complete 14-era story arc.

New in this version:
- The story no longer starts on its own. Every new player is handed a written
  "Prologue" book that tells them who they are and what happened before the
  story begins - and tells them to type /start. Nothing advances until they do.
- Full English + Chinese localization: all 218 lines of story text, every
  objective and every interface message now follow the player's client
  language. Use /awakening lang zh|en to override it.
- /awakening book hands out another copy of the prologue book.
- /awakening reset now also clears the story items from your inventory.

Story:
- All 14 eras plus the End Poem, played as one continuous flow
- One hand-completed objective per era (talk, mine, craft, fight, purify)
- The Echo Sentinel summoning ritual — wake the guardian yourself with a soul shard and a shriek
- The Beacon Purification ritual — place the resonance block, strike it with a Nether Star
- Wither Son boss fight and the three Calamity Cultists

Content:
- 13 custom items, 7 custom blocks
- 3 original music discs (72s / 66s / 115s)
- 20 crafting recipes
- Ancient City structure generation
- Free-play segment with 10 rewards after the End Poem
- /awakening command: start, status, skip, freeplay, debug, reset

Requires Minecraft 1.21.4, Fabric Loader 0.16.14+, Fabric API 0.110.0+, Java 21+.
Install on both client and server.
```

---

## 五、图标 / 横幅

- **Icon**：**仓库根目录就有现成的** —— `icon.png`，**512×512 PNG**（Modrinth 硬性要求，正好达标）。
  重新生成（改视觉后）：
  ```bash
  cd /home/yifan/awakening-rotb
  node tools/make-icon.js icon.png 512     # 尺寸可传，默认 512
  ```
  生成器是自包含的（只用 Node 内置 zlib，无第三方依赖），128→512 最近邻放大保持像素画风硬边。

  > ⚠️ 注意区分两个 icon：
  > - `icon.png`（根目录）= **Modrinth 上传用的**，512×512，黑金兜帽建造师剪影
  > - `assets/awakening/icon.png`（jar 内）= Fabric mod 列表里显示的，512×512，另一套设计
  > 上传 Modrinth 用**根目录那个**。

- **Banner**：**已经做好了** —— `assets/media/banner.png`，**1280×720 PNG**（Modrinth 推荐尺寸）。
  黑金主题的兜帽建造师 + 远古古城，和 icon 同一套视觉。
- **截图**：`assets/media/screenshot-beacon.png`（1280×720，第八纪信标净化场景）。
  Modrinth 至少要 1 张截图，banner 可选 —— 建议两个都传。
- 颜色建议：黑底 + 金色（呼应建造师主题）。

---

## 六、提交前 checklist

- [ ] Project name: `Awakening: Return of the Builder`
- [ ] Slug: `awakening-rotb`
- [ ] Project type: **`Mod`**（⚠️ 不是 Datapack —— 这是旧版最大的坑）
- [ ] Categories: `Adventure`
- [ ] License: `MIT`
- [ ] Homepage URL: `https://github.com/Pnianye123/awakening-rotb`
- [ ] Long description 已经粘进去
- [ ] 上传 `awakening-rotb-1.0.0.jar`
- [ ] Version number `1.0.0`
- [ ] Minecraft versions: **只勾 `1.21.4`**
- [ ] Mod loader: **`Fabric`**
- [ ] Client side: `Required` / Server side: `Required`
- [ ] Dependencies: Fabric API `>=0.110.0`（Required，Client+Server）
- [ ] changelog 已经粘进去
- [ ] icon.png 已经传（512×512，仓库根目录那个）
- [ ] banner.png 已经传（1280×720，`assets/media/banner.png`）
- [ ] screenshot-beacon.png 已经传（1280×720，`assets/media/screenshot-beacon.png`）

完成后 Modrinth 会给你一个项目链接，类似 `https://modrinth.com/mod/awakening-rotb`。
把那个链接发给 Jimmy 我们再继续下一步（README 加 Modrinth badge / Modpack 那一侧）。

---

## 七、⚠️ 版本号不一致（发布前值得想一下）

| 地方 | 值 |
|---|---|
| git commit message | `v1.0.1`（两个 commit 都写 v1.0.1） |
| `gradle.properties` 的 `mod_version` | `1.0.0` |
| jar 内 `fabric.mod.json` 的 `version` | `1.0.0` |
| 交付文件名 | `awakening-rotb-1.0.0.jar` |

**Modrinth 的 Version number 要跟 jar 内部版本号一致**（游戏里 `/awakening status` 显示的就是它），
所以填 `1.0.0` 是对的、也是当前唯一自洽的选择。

如果想让 commit 里的 "v1.0.1" 也对上，**先改 `gradle.properties` 的 `mod_version=1.0.1` 再重新 build**，
然后 Modrinth 填 `1.0.1`。代价是重 build + 重跑 e2e + 重传 jar —— 建议**先按 1.0.0 发布**，
回头再统一升版号（Modrinth 支持后续再发新 version）。

---

_Updated by Steve · 2026-11-02 09:30（新增 /start 门槛、前情提要书、中英双语、banner/截图素材）_

_此前：Efe · 2026-11-02（原文 Steve · 2026-09-29）_
