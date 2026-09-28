# 方块之下 · datapack 子模块 README

> 这是 v0.1 datapack 版的安装与测试文档。
> 完整项目 README 见 [根目录 README](../../README.md)。

---

## 这是什么

《方块之下》v0.1 是一个 **Minecraft 原版 datapack**，不需要 Fabric、不需要 Java。
原版 1.21.x 客户端/服务端即可运行。

它演示了剧情段的核心结构：
- 序（玩家醒来）
- 14 段连续剧情（不是分章节，是连续触发）
- 终末之诗（唯一结局）

---

## 安装

### 方法 1：手动复制（推荐新手）

1. 从 GitHub releases 下载 v0.1 zip，或 clone 本仓库
2. 把 `data/` 和 `pack.mcmeta` 复制到你的 Minecraft 世界目录下的 `datapacks/awakening-rotb/`

```
你的世界/
├── datapacks/
│   ├── awakening-rotb/      ← 复制到这里
│   │   ├── pack.mcmeta
│   │   └── data/
│   │       └── ...
│   ├── ...
```

3. 进入该世界
5. 输入 `/reload`
6. 听到"数据包已加载"的声音就算 OK

### 方法 2：用脚本安装（如果有 Java 17+）

参见未来 releases 的 installer。

---

## 测试

进入世界后，剧情会自动触发：
- **第一次进入**：序 → 第一纪 → 第二纪 → ... → 第十四纪 → 终末之诗
- 每个纪元之间间隔约 10 秒（200 ticks）
- 完整剧情播放约 **2.5 分钟**

### 调试指令

```
/function awakening:setup          # 重新触发序
/function awakening:era1           # 跳到第一纪
/function awakening:era7           # 跳到第七纪
/function awakening:ending         # 跳到终末之诗
/function awakening:tick           # 手动跑一次 tick（一般不需要）

/scoreboard players set @s awakening.delay 50   # 把延迟改成 2.5 秒，加速剧情
/scoreboard players set @s awakening.current_era 14   # 跳到第 14 纪（再用 /function awakening:advance_era）

/advancement revoke @s everything   # 清空所有进度，重新触发
```

---

## 它做了什么（剧情段 MVP）

| 步骤 | 行为 |
|---|---|
| 玩家进入世界 | tick.mcfunction 检测到玩家没有 root 进度 |
| 自动调用 | init_player.mcfunction：发放 root 进度、设置 scoreboard、播放序 |
| 序播放完毕 | 等待 100 ticks 后，advance_era 自动触发 |
| 每一纪 | 播放标题（5 sec fade）+ 完整 lore 文本 + 右上角 toast |
| 间隔 | 每个纪元之间 200 ticks（10 秒） |
| 终末之诗 | 第十四纪之后自动触发 |

---

## 文件结构

```
data/
├── minecraft/
│   └── tags/
│       └── functions/
│           └── tick.json           ← 加载 awakening:tick 每 tick
└── awakening/
    ├── function/
    │   ├── setup.mcfunction        ← 序：醒来
    │   ├── init_player.mcfunction  ← 新玩家初始化
    │   ├── tick.mcfunction         ← 主循环（每 tick 跑）
    │   ├── advance_era.mcfunction  ← 推进到下一纪
    │   ├── era1.mcfunction ~ era14.mcfunction  ← 14 纪的剧情显示
    │   └── ending.mcfunction       ← 终末之诗
    └── advancement/
        ├── root.json               ← 起点
        ├── era1.json ~ era14.json  ← 14 纪 advancement
        └── ending.json             ← 终末之诗 advancement
```

---

## 已知限制（v0.1 MVP）

- **没有 cutscene**：v0.1 只用标题 + 聊天文本呈现剧情，没有过场动画
- **自动连续播放**：玩家不需要做任何事，剧情按时间自动推进
- **没有玩家身份自定义**：v0.5 才会做"建造师长袍"皮肤
- **没有觉醒者 NPC**：v0.5 才会做村民 NBT 改名版觉醒者
- **没有奖励解锁**：v1.5 才会做"剧情结束 → 自由段奖励"

---

## 设定要求（**不要改**）

来自 [awakening-mod-design-v3.md](../../awakening-mod-design-v3.md) 备注：

1. **坚守者不要用 vanilla Warden 名字**——英文 The Echo Sentinel，中文"坚守者"
2. **不要把尖啸体写成 Sculk/Skulk**——它是建造师主动培育的菌类共鸣网络
3. **古城是远古建造师创造的**——不是天然洞穴
4. **灾厄村民不会伤害村民和建造师**（初设），后来因为复活术失败才暴怒
5. **灾厄村民的暗黑魔法起源于修炼**，不是天然存在
6. **凋零病毒和亡灵病毒是两个独立事件**——凋零病毒是辐射伤害，亡灵病毒是死灵操纵
7. **末影龙有"所有"，被打败后留下一条守门**
8. **末影人是在末地待久了变成的建造师**

---

## 路线图

- **v0.5** — Fabric Java mod：cutscene、玩家皮肤、觉醒者 NPC
- **v1.0** — 完整剧情段、坚守者 Boss、信标净化任务、终末之诗播放
- **v1.5** — 自由段奖励：建造师长袍、共鸣石、3 张唱片、远古古城生成
- **v2.0** — Modrinth 发布

---

## 反馈

发现 bug 或剧情建议？在 GitHub 开 issue：
https://github.com/Pnianye123/awakening-rotb/issues

---

MIT License — Copyright (c) 2026 Jimmy