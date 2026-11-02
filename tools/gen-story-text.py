#!/usr/bin/env python3
"""从 data/awakening/function/eraN.mcfunction 提取 Jimmy 写的剧情文本，生成 Java 数组源码。
保留原文（lore 铁律），只做格式转换。"""
import json, os, re, sys

ROOT = "/home/yifan/awakening-rotb"
FUN = os.path.join(ROOT, "data/awakening/function")
OUT = os.path.join(ROOT, "src/main/java/net/awakeningrotb/story/StoryText.java")

ERAS = [
    (1, "共生"), (2, "下界远征"), (3, "灾厄的善意"), (4, "请求复活"),
    (5, "凋零之罪"), (6, "凋零病毒"), (7, "坚守者"), (8, "信标净化"),
    (9, "灾厄的反噬"), (10, "再次躲避"), (11, "末地远征"), (12, "龙之战"),
    (13, "末地文明"), (14, "不归路"),
]
CN_NUM = {1:"一",2:"二",3:"三",4:"四",5:"五",6:"六",7:"七",8:"八",9:"九",10:"十",11:"十一",12:"十二",13:"十三",14:"十四"}

def java_str(s):
    out = s.replace('\\', '\\\\').replace('"', '\\"')
    out = out.replace('\n', '\\n')
    return '"' + out + '"'

def parse_era(n):
    path = os.path.join(FUN, f"era{n}.mcfunction")
    with open(path, encoding="utf-8") as f:
        lines = f.read().splitlines()
    entries = []
    for ln in lines:
        ln = ln.strip()
        if not ln.startswith("tellraw @s "):
            continue
        payload = ln[len("tellraw @s "):].strip()
        try:
            obj = json.loads(payload)
        except json.JSONDecodeError:
            continue
        if isinstance(obj, str):
            # tellraw @s "" —— 纯字符串 payload（空行或纯文本）
            text, color, bold = obj, "white", False
            if not text:
                continue
        elif isinstance(obj, dict):
            text = obj.get("text", "")
            if not text:
                continue
            color = obj.get("color", "white")
            bold = bool(obj.get("bold", False))
        else:
            continue
        if set(text) <= set("═ "):
            continue  # 分隔线
        if text.startswith("══"):
            continue
        entries.append((text, color, bold))
    return entries

def main():
    parts = []
    parts.append("package net.awakeningrotb.story;")
    parts.append("")
    parts.append("/**")
    parts.append(" * 《方块之下》剧情文本 —— 自动生成，请勿手改。")
    parts.append(" *")
    parts.append(" * 由 tools/gen-story-text.py 从 data/awakening/function/eraN.mcfunction 提取，")
    parts.append(" * 保留 Jimmy 原文（lore 铁律，改动请改 mcfunction 后重新生成）。")
    parts.append(" */")
    parts.append("public final class StoryText {")
    parts.append("    private StoryText() {")
    parts.append("    }")
    parts.append("")

    for n, name in ERAS:
        entries = parse_era(n)
        parts.append(f"    /** 第{CN_NUM[n]}纪 · {name} */")
        parts.append(f"    public static final String[] ERA{n} = {{")
        for text, color, bold in entries:
            parts.append(f"            {java_str(text)},")
        parts.append("    };")
        parts.append("")
        parts.append(f"    /** 第{CN_NUM[n]}纪 · {name} —— 每行对应的颜色（与 ERA{n} 一一对应） */")
        parts.append(f"    public static final String[] ERA{n}_COLOR = {{")
        for text, color, bold in entries:
            parts.append(f"            {java_str(color)},")
        parts.append("    };")
        parts.append("")

    # 结尾：终末之诗 —— 从 STORY.md 尾声提取
    story = open(os.path.join(ROOT, "STORY.md"), encoding="utf-8").read()
    ending = []
    if "尾声" in story:
        tail = story.split("尾声", 1)[1]
        for ln in tail.splitlines():
            ln = ln.strip()
            if not ln or ln.startswith("#"):
                continue
            if ln.startswith("---"):
                continue
            ending.append(ln)
    parts.append("    /** 终末之诗 —— 唯一结局 */")
    parts.append("    public static final String[] ENDING = {")
    for ln in ending:
        parts.append(f"            {java_str(ln)},")
    parts.append("    };")
    parts.append("")
    parts.append("    /** 终末之诗每行颜色（与 ENDING 一一对应） */")
    parts.append("    public static final String[] ENDING_COLOR = {")
    for ln in ending:
        parts.append('            "white",')
    parts.append("    };")
    parts.append("}")
    parts.append("")

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        f.write("\n".join(parts))
    print(f"生成 {OUT}")
    print(f"  纪元行数: {sum(len(parse_era(n)) for n,_ in ERAS)}")
    print(f"  结尾行数: {len(ending)}")

if __name__ == "__main__":
    main()
