#!/usr/bin/env python3
"""《方块之下》多语言代码生成器。

lore 铁律：
  * **中文原文只来自 data/awakening/function/eraN.mcfunction 与 STORY.md 尾声**，脚本一个字都不改。
  * 英译是独立的一份（data/awakening/lang/en/*.json），行数必须与中文一一对应，对不上直接报错退出。

产出三个 Java 文件（都是自动生成，不要手改）：
  1. StoryText.java    —— 中文剧情文本 + 每行颜色（lore 源：mcfunction）
  2. StoryTextEn.java  —— 英文剧情文本（翻译源：data/awakening/lang/en/）
  3. L10n.java         —— 界面文本 / 任务提示 / 纪元名的中英双份

用法：python3 tools/gen-i18n.py
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FUN = os.path.join(ROOT, "data/awakening/function")
EN_DIR = os.path.join(ROOT, "data/awakening/lang/en")
JAVA_DIR = os.path.join(ROOT, "src/main/java/net/awakeningrotb/story")

ERAS = [
    (1, "共生"), (2, "下界远征"), (3, "灾厄的善意"), (4, "请求复活"),
    (5, "凋零之罪"), (6, "凋零病毒"), (7, "坚守者"), (8, "信标净化"),
    (9, "灾厄的反噬"), (10, "再次躲避"), (11, "末地远征"), (12, "龙之战"),
    (13, "末地文明"), (14, "不归路"),
]

CN_NUMBERS = ["第一", "第二", "第三", "第四", "第五", "第六", "第七",
              "第八", "第九", "第十", "第十一", "第十二", "第十三", "第十四"]
EN_NUMBERS = ["Era I", "Era II", "Era III", "Era IV", "Era V", "Era VI", "Era VII",
              "Era VIII", "Era IX", "Era X", "Era XI", "Era XII", "Era XIII", "Era XIV"]
EN_TITLES = {
    1: "Symbiosis",
    2: "The Nether Expedition",
    3: "The Scourge's Benevolence",
    4: "A Plea for Resurrection",
    5: "The Sin of the Wither",
    6: "The Wither Blight",
    7: "The Echo Sentinel",
    8: "The Beacon's Cleansing",
    9: "The Scourge's Retaliation",
    10: "Hiding Again",
    11: "The Expedition to the End",
    12: "The War of Dragons",
    13: "The Civilization of the End",
    14: "The Road of No Return",
}

# ── 任务提示（14 段）────────────────────────────────────────────
OBJECTIVES = [
    ("与 3 位村民交谈，了解这片土地",
     "Talk to 3 villagers and learn about this land"),
    ("采集 8 个黑曜石，造出通往地狱的门",
     "Gather 8 obsidian and open a gate to the Nether"),
    ("与灾厄村民交谈 —— 他们不会伤害你",
     "Speak with the Scourge villagers — they will not harm you"),
    ("把 3 个灵魂碎片交给灾厄村民",
     "Hand 3 soul shards to the Scourge villagers"),
    ("在凋零之子的降临中活下来",
     "Survive the arrival of the Wither Son"),
    ("躲进地下，撑过凋零病毒的蔓延",
     "Hide underground and outlast the spread of the Wither Blight"),
    ("用灵魂与尖啸体召唤坚守者，击败凋零之子",
     "Summon the Echo Sentinel with a soul and the Screech Fungus, then defeat the Wither Son"),
    ("在远古古城放置共鸣方块，用下界之星净化世界",
     "Place resonance blocks in the Ancient City and purify the world with a nether star"),
    ("击败 3 个失控的邪术师",
     "Defeat 3 out-of-control Warlocks"),
    ("躲进 Y=-40 以下的要塞深处",
     "Hide deep inside a stronghold below Y=-40"),
    ("穿过末地传送门，离开这个世界",
     "Pass through an End portal and leave this world"),
    ("击败末影龙，为世界留下一条守门龙",
     "Defeat the Ender Dragon and leave a Gate Dragon behind for the world"),
    ("收集 16 个末地石与一副鞘翅，重建文明",
     "Gather 16 end stone and an elytra to rebuild civilization"),
    ("使用末影珍珠碎片，走上不归路",
     "Use a shard of an ender pearl and take the road of no return"),
]

# ── 界面文本：key → (中文, 英文, 颜色, 参数个数) ──────────────────
# 颜色用 MC 颜色名（gray / gold / …），Java 侧转成 Formatting。
MESSAGES = [
    ("msg.start.story", "《方块之下》—— 剧情开始。",
     "《方块之下》 — the story begins.", "gold", 0),
    ("msg.start.wake", "你在这个世界上醒来……但你好像已经走了很久。",
     "You wake in this world… and it feels as though you have been gone a long time.", "gray", 0),
    ("msg.start.already", "剧情已经在进行中了。",
     "The story is already in progress.", "gray", 0),
    ("msg.start.not_player", "需要在游戏内由玩家执行。",
     "This has to be run in-game by a player.", "gray", 0),
    ("msg.prologue.given", "床边放着一本《前情提要》。读完它，然后输入 /start 开始这段历史。",
     "A book called Prologue lies by your bed. Read it, then type /start to begin this history.",
     "gray", 0),
    ("msg.prologue.got", "《前情提要》已经交到你手上 —— 读完它，然后输入 /start。",
     "Prologue has been placed in your hands — read it, then type /start.", "gold", 0),
    ("msg.start.done", "你合上书，深吸一口气。历史开始了。",
     "You close the book and take a breath. History begins.", "gold", 0),
    ("msg.start.book", "《前情提要》又回到你手上。",
     "Prologue is back in your hands.", "gold", 0),
    ("msg.reset", "剧情进度已重置。", "Story progress has been reset.", "gray", 0),
    ("msg.freeplay", "已直接进入自由段。", "Jumped straight into free play.", "gold", 0),
    ("msg.freeplay.title", "自由段 —— 剧情已结束，世界是你的了。",
     "Free play — the story is over; the world is yours.", "gold", 0),
    ("msg.progress.title", "《方块之下》进度", "《方块之下》 progress", "gold", 0),
    ("msg.era", "纪元：", "Era: ", "gray", 0),
    ("msg.task", "任务：", "Objective: ", "gray", 0),
    ("msg.not_started", "剧情还没开始 —— 输入 /start", "The story has not begun — type /start", "gray", 0),
    ("msg.skipped", "已跳转到：", "Jumped to: ", "gold", 0),
    ("msg.objective.done", "✔ 任务完成", "✔ Objective complete", "green", 0),
    ("msg.objective.prefix", "⏳ ", "⏳ ", "yellow", 0),
    ("msg.task.title", "⛏ ", "⛏ ", "yellow", 0),
    ("msg.task.subtitle", " · 任务", " · Objective", "gray", 0),
    ("msg.ending.title", "终末之诗", "The Final Poem", "dark_purple", 0),
    ("msg.ending.subtitle", "尾声", "Epilogue", "light_purple", 0),
    ("msg.reward.title", "✦ 终末之诗已奏响 —— 你获得了自由段奖励",
     "✦ The Final Poem has sounded — you earned your free-play rewards", "gold", 0),
    ("msg.reward.items", "建造师长袍 · 共鸣石 · 灵魂罗盘 · 古城通行证 · 凋零核心 · 觉醒者信物 · 古城钥匙 · 古老唱片 A/B/C",
     "Builder's Robe · Resonance Stone · Soul Compass · Ancient City Pass · Wither Core · Awakened Token · Ancient City Key · Ancient Records A/B/C",
     "gray", 0),
    ("msg.reward.free", "这个世界从此是你的了 —— 拆古城、杀村民、退出游戏、玩创造模式，什么都行。",
     "The world is yours from here on — break the ancient city, kill villagers, quit the game, play creative. Anything goes.",
     "dark_gray", 0),
    ("msg.talk.villager", "你和村民聊了聊。（%s/3）",
     "You chat with a villager. (%s/3)", "gray", 1),
    ("msg.talk.warlock", "灾厄村民抬起头看你。它的眼神很凶，但它没有动手。",
     "The Scourge villager looks up at you. Its eyes are hard, but it does not strike.", "gray", 0),
    ("msg.talk.warlock.ask", "灾厄村民：「我们可以帮你们复活他们……但首先，我们需要灵魂碎片。」",
     "Scourge villager: 「We can bring them back for you… but first, we need soul shards.」", "gray", 0),
    ("msg.give.soul", "你把一枚灵魂碎片放进灾厄村民的掌心。（%s/3）",
     "You place a soul shard in the Scourge villager's palm. (%s/3)", "gray", 1),
    ("msg.give.soul.done", "灾厄村民们低下头，开始了复活术。",
     "The Scourge villagers bow their heads and begin the rite of return.", "dark_gray", 0),
    ("msg.kill.wither", "凋零之子被撕碎了。", "The Wither Son is torn apart.", "dark_purple", 0),
    ("msg.kill.warlock", "一个邪术师倒下了。", "A Warlock falls.", "dark_purple", 0),
    ("msg.kill.dragon", "末影龙坠落。最后一条守门龙留了下来。",
     "The Ender Dragon falls. One last Gate Dragon remains.", "gold", 0),
    ("msg.use.echo", "你握紧末影珍珠碎片。世界在你眼前碎成了像素。",
     "You grip the shard of an ender pearl. The world breaks into pixels before your eyes.", "dark_purple", 0),
    ("msg.purify.beam", "信标的光柱冲天而起 —— 金色的、纯净的。",
     "The beacon's pillar erupts into the sky — golden, and clean.", "gold", 0),
    ("msg.purify.blight", "凋零病毒被净化了。蓝色的雾气消散，地表重新长出绿草。",
     "The Wither Blight is cleansed. The blue haze thins, and grass returns to the surface.", "green", 0),
    ("msg.sentinel.wake", "地下的深处传来沉重的心跳 —— 坚守者苏醒了。",
     "A heavy heartbeat rises from deep below — the Echo Sentinel awakens.", "aqua", 0),
    ("msg.recorder.idle", "记录仪一片死寂 —— 剧情还没开始。",
     "The recorder is silent — the story has not begun.", "gray", 0),
    ("msg.recorder.dead", "记录仪的指针不再指向任何地方。",
     "The recorder's needle points nowhere at all.", "gray", 0),
    ("msg.recorder.recording", "记录仪正在记录 —— 等这段历史讲完。",
     "The recorder is still recording — wait for this era to finish.", "gray", 0),
    ("msg.recorder.done", "这一纪已经记下了 —— 任务还没完成呢。",
     "This era is already recorded — but the objective is not finished.", "gray", 0),
    ("msg.recorder.next", "记录仪翻到了下一页。", "The recorder turns to the next page.", "yellow", 0),
    ("msg.stone.hot", "共鸣石开始发热 —— 尖啸体就在附近。",
     "The Resonance Stone grows warm — the Screech Fungus is close.", "aqua", 0),
    ("msg.stone.heard", "你听懂了：那是死去的同伴在用震动说话。",
     "You understand it: the dead are speaking through vibration.", "gray", 0),
    ("msg.stone.cold", "共鸣石冰凉 —— 附近没有尖啸体。",
     "The Resonance Stone is cold — no Screech Fungus nearby.", "dark_gray", 0),
    ("msg.compass.stopped", "罗盘的指针缓缓转了一圈，然后停住了。",
     "The compass needle turns once, slowly, and stops.", "gray", 0),
    ("msg.compass.points", "灵魂罗盘指向：", "The Soul Compass points to:", "gold", 0),
    ("msg.relic.owner", "它属于远古建造师。", "It belonged to the ancient Builders.", "gold", 0),
    ("msg.relic.city", "考古学家在主世界地下能找到他们留下的城市 —— 规则化的方形大厅，柱子上嵌着共鸣石。",
     "Archaeologists find their cities deep beneath the Overworld — regular square halls, resonance stones set into the pillars.",
     "gray", 0),
    ("msg.lang.set", "语言已切换为：%s", "Language switched to: %s", "gray", 1),
    ("msg.lang.usage", "用法：/awakening lang zh|en", "Usage: /awakening lang zh|en", "gray", 0),
    ("msg.lang.bad", "语言只能是 zh 或 en。", "Language must be zh or en.", "gray", 0),
]


def java_str(s):
    return '"' + s.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n') + '"'


def parse_era(n):
    """从 mcfunction 提取中文原文 + 每行颜色（lore 铁律：只读不改）。"""
    path = os.path.join(FUN, f"era{n}.mcfunction")
    entries = []
    with open(path, encoding="utf-8") as f:
        for ln in f:
            ln = ln.strip()
            if not ln.startswith("tellraw @s "):
                continue
            payload = ln[len("tellraw @s "):].strip()
            try:
                obj = json.loads(payload)
            except json.JSONDecodeError:
                continue
            if isinstance(obj, str):
                text = obj
                if not text:
                    continue
                color = "white"
            elif isinstance(obj, dict):
                text = obj.get("text", "")
                if not text:
                    continue
                color = obj.get("color", "white")
            else:
                continue
            if set(text) <= set("═ "):
                continue
            if text.startswith("══"):
                continue
            entries.append((text, color))
    return entries


def parse_ending_zh():
    """终末之诗中文原文 —— 来自 STORY.md 尾声（运行时真正播放的那份）。"""
    story = open(os.path.join(ROOT, "STORY.md"), encoding="utf-8").read()
    if "尾声" not in story:
        raise SystemExit("STORY.md 里找不到「尾声」")
    tail = story.split("尾声", 1)[1]
    out = []
    for ln in tail.splitlines():
        ln = ln.strip()
        if not ln or ln.startswith("#") or ln.startswith("---"):
            continue
        out.append(ln)
    return out


def load_en(name):
    with open(os.path.join(EN_DIR, name), encoding="utf-8") as f:
        return json.load(f)


def gen_story_text(zh_eras, zh_ending):
    parts = [
        "package net.awakeningrotb.story;",
        "",
        "/**",
        " * 《方块之下》剧情文本（中文）—— 自动生成，请勿手改。",
        " *",
        " * <p>由 tools/gen-i18n.py 从 data/awakening/function/eraN.mcfunction 与 STORY.md 尾声提取，",
        " * 保留 Jimmy 原文（lore 铁律：改 lore 请改 mcfunction 后重新生成）。",
        " * 英文版见 StoryTextEn.java。",
        " */",
        "public final class StoryText {",
        "    private StoryText() {",
        "    }",
        "",
    ]
    for n in range(1, 15):
        entries = zh_eras[n]
        parts.append(f"    /** 第{CN_NUMBERS[n - 1]}纪 · {ERAS[n - 1][1]} */")
        parts.append(f"    public static final String[] ERA{n} = {{")
        for text, _c in entries:
            parts.append(f"            {java_str(text)},")
        parts.append("    };")
        parts.append("")
        parts.append(f"    /** 第{CN_NUMBERS[n - 1]}纪 · {ERAS[n - 1][1]} —— 每行颜色 */")
        parts.append(f"    public static final String[] ERA{n}_COLOR = {{")
        for _t, color in entries:
            parts.append(f"            {java_str(color)},")
        parts.append("    };")
        parts.append("")
    parts.append("    /** 终末之诗 —— 唯一结局 */")
    parts.append("    public static final String[] ENDING = {")
    for ln in zh_ending:
        parts.append(f"            {java_str(ln)},")
    parts.append("    };")
    parts.append("")
    parts.append("    /** 终末之诗每行颜色 */")
    parts.append("    public static final String[] ENDING_COLOR = {")
    for _ in zh_ending:
        parts.append('            "white",')
    parts.append("    };")
    parts.append("}")
    return "\n".join(parts) + "\n"


def gen_story_text_en(en_eras, en_ending, zh_eras, zh_ending):
    parts = [
        "package net.awakeningrotb.story;",
        "",
        "/**",
        " * 《方块之下》剧情文本（英文）—— 自动生成，请勿手改。",
        " *",
        " * <p>由 tools/gen-i18n.py 从 data/awakening/lang/en/*.json 生成，",
        " * 行数与中文版（StoryText.java）严格一一对应。",
        " */",
        "public final class StoryTextEn {",
        "    private StoryTextEn() {",
        "    }",
        "",
    ]
    for n in range(1, 15):
        entries = en_eras[n]
        parts.append(f"    /** Era {EN_NUMBERS[n - 1]} · {EN_TITLES[n]} */")
        parts.append(f"    public static final String[] ERA{n} = {{")
        for text in entries:
            parts.append(f"            {java_str(text)},")
        parts.append("    };")
        parts.append("")
        parts.append(f"    /** Era {EN_NUMBERS[n - 1]} · {EN_TITLES[n]} —— color per line */")
        parts.append(f"    public static final String[] ERA{n}_COLOR = {{")
        for _t, color in zh_eras[n]:
            parts.append(f"            {java_str(color)},")
        parts.append("    };")
        parts.append("")
    parts.append("    /** The Final Poem — the only ending */")
    parts.append("    public static final String[] ENDING = {")
    for ln in en_ending:
        parts.append(f"            {java_str(ln)},")
    parts.append("    };")
    parts.append("")
    parts.append("    /** The Final Poem — color per line */")
    parts.append("    public static final String[] ENDING_COLOR = {")
    for _ in zh_ending:
        parts.append('            "white",')
    parts.append("    };")
    parts.append("}")
    return "\n".join(parts) + "\n"


def gen_l10n():
    parts = [
        "package net.awakeningrotb.story;",
        "",
        "/**",
        " * 《方块之下》界面文本（中英双份）—— 自动生成，请勿手改。",
        " *",
        " * <p>由 tools/gen-i18n.py 生成。中文改 MESSAGES/OBJECTIVES，英文同步改。",
        " */",
        "public final class L10n {",
        "    private L10n() {",
        "    }",
        "",
        "    /** 全部界面文本的 key（顺序与 ZH / EN / COLOR / ARGS 一致） */",
        "    public static final String[] KEYS = {",
    ]
    for key, _zh, _en, _c, _a in MESSAGES:
        parts.append(f"            {java_str(key)},")
    parts.append("    };")
    parts.append("")
    parts.append("    /** 中文 */")
    parts.append("    public static final String[] ZH = {")
    for _k, zh, _e, _c, _a in MESSAGES:
        parts.append(f"            {java_str(zh)},")
    parts.append("    };")
    parts.append("")
    parts.append("    /** English */")
    parts.append("    public static final String[] EN = {")
    for _k, _z, en, _c, _a in MESSAGES:
        parts.append(f"            {java_str(en)},")
    parts.append("    };")
    parts.append("")
    parts.append("    /** 每条文本的颜色（MC 颜色名） */")
    parts.append("    public static final String[] COLOR = {")
    for _k, _z, _e, c, _a in MESSAGES:
        parts.append(f"            {java_str(c)},")
    parts.append("    };")
    parts.append("")
    parts.append("    /** 每条文本的参数个数 */")
    parts.append("    public static final int[] ARGS = {")
    for _k, _z, _e, _c, a in MESSAGES:
        parts.append(f"            {a},")
    parts.append("    };")
    parts.append("")
    parts.append("    /** 纪元编号（中文：第X纪） */")
    parts.append("    public static final String[] ERA_NUMBER_ZH = {")
    for s in CN_NUMBERS:
        parts.append(f"            {java_str(s + '纪')},")
    parts.append("    };")
    parts.append("")
    parts.append("    /** 纪元编号（English） */")
    parts.append("    public static final String[] ERA_NUMBER_EN = {")
    for s in EN_NUMBERS:
        parts.append(f"            {java_str(s)},")
    parts.append("    };")
    parts.append("")
    parts.append("    /** 纪元副标题（中文） */")
    parts.append("    public static final String[] ERA_NAME_ZH = {")
    for n, name in ERAS:
        parts.append(f"            {java_str(name)},")
    parts.append("    };")
    parts.append("")
    parts.append("    /** Era subtitle (English) */")
    parts.append("    public static final String[] ERA_NAME_EN = {")
    for n in range(1, 15):
        parts.append(f"            {java_str(EN_TITLES[n])},")
    parts.append("    };")
    parts.append("")
    parts.append("    /** 任务提示（中文） */")
    parts.append("    public static final String[] OBJECTIVE_ZH = {")
    for zh, _en in OBJECTIVES:
        parts.append(f"            {java_str(zh)},")
    parts.append("    };")
    parts.append("")
    parts.append("    /** Objectives (English) */")
    parts.append("    public static final String[] OBJECTIVE_EN = {")
    for _zh, en in OBJECTIVES:
        parts.append(f"            {java_str(en)},")
    parts.append("    };")
    parts.append("}")
    return "\n".join(parts) + "\n"


def main():
    zh_eras = {}
    en_eras = {}
    for n in range(1, 15):
        zh = parse_era(n)
        en = load_en(f"en_era{n}.json")
        if len(zh) != len(en):
            raise SystemExit(f"era{n}: 中文 {len(zh)} 行 vs 英文 {len(en)} 行 —— 行数必须一致")
        zh_eras[n] = zh
        en_eras[n] = en

    zh_ending = parse_ending_zh()
    en_ending = load_en("ending.json")
    if len(zh_ending) != len(en_ending):
        raise SystemExit(f"ending: 中文 {len(zh_ending)} 行 vs 英文 {len(en_ending)} 行 —— 行数必须一致")

    os.makedirs(JAVA_DIR, exist_ok=True)
    files = {
        "StoryText.java": gen_story_text(zh_eras, zh_ending),
        "StoryTextEn.java": gen_story_text_en(en_eras, en_ending, zh_eras, zh_ending),
        "L10n.java": gen_l10n(),
    }
    for name, content in files.items():
        path = os.path.join(JAVA_DIR, name)
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"✓ {path}")

    print(f"  纪元行数 {sum(len(v) for v in zh_eras.values())} · 终末之诗 {len(zh_ending)} 行 · "
          f"任务 {len(OBJECTIVES)} 条 · 界面文本 {len(MESSAGES)} 条")


if __name__ == "__main__":
    main()
