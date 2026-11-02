#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generate all resource JSON files for the mod "awakening" (Awakening: Return of the Builder).

MC 1.21.4 / Fabric.  Writes ONLY json files under src/main/resources/assets/awakening/:
  - lang/en_us.json, lang/zh_cn.json
  - models/item/<name>.json
  - models/block/<name>.json
  - blockstates/<name>.json

Never touches Java or gradle files.
"""

import json
import os

ROOT = "/home/yifan/awakening-rotb"
RES = os.path.join(ROOT, "src/main/resources/assets/awakening")

# --------------------------------------------------------------------------
# Data tables
# --------------------------------------------------------------------------

# name -> (zh_cn, en_us)
ITEMS = {
    "builder_robe":        ("建造师长袍",         "Builder's Robe"),
    "recorder":            ("记录仪",             "Recorder"),
    "resonance_stone":     ("共鸣石",             "Resonance Stone"),
    "soul_compass":        ("灵魂罗盘",           "Soul Compass"),
    "ancient_city_pass":   ("古城通行证",         "Ancient City Pass"),
    "wither_core":         ("凋零核心",           "Wither Core"),
    "ancient_city_key":    ("古城钥匙",           "Ancient City Key"),
    "record_a":            ("古老唱片 A",         "Ancient Record A"),
    "record_b":            ("古老唱片 B",         "Ancient Record B"),
    "record_c":            ("古老唱片 C",         "Ancient Record C"),
    "awakened_token":      ("觉醒者信物",         "Awakened Token"),
    "soul_shard":          ("灵魂碎片",           "Soul Shard"),
    "echo_shard":          ("末影珍珠碎片",       "Echo Shard"),
    "ancient_totem_item":  ("觉醒者图腾",         "Awakened Totem"),
    "resonance_block_item":("古城共鸣方块",       "Resonance Block"),
    "wither_core_item":    ("凋零核心块",         "Wither Core Block"),
}

# name -> (zh_cn, en_us)
BLOCKS = {
    "resonance_block":       ("古城共鸣方块",     "Resonance Block"),
    "awakened_totem":        ("觉醒者图腾",       "Awakened Totem"),
    "builder_brick":         ("建造师石砖",       "Builder's Brick"),
    "wither_rift":           ("凋零裂隙",         "Wither Rift"),
    "reinforced_echo_stone": ("强化回响石",       "Reinforced Echo Stone"),
    "soul_lantern":          ("灵魂灯",           "Soul Lantern"),
    "builder_robe_block":    ("建造师长袍方块",   "Builder's Robe Block"),
}

# Blocks rendered with minecraft:block/cube_column instead of cube_all
COLUMN_BLOCKS = {"awakened_totem"}

# key -> (zh_cn, en_us)
MISC_LANG = {
    "itemGroup.awakening": ("方块之下", "Below the Blocks"),
    "entity.awakening.awakener":       ("觉醒者",     "The Awakener"),
    "entity.awakening.warlock":        ("邪术师",     "The Warlock"),
    "entity.awakening.ender_guardian": ("末影守护者", "Ender Guardian"),
    "entity.awakening.echo_sentinel":  ("坚守者",     "The Echo Sentinel"),
    "entity.awakening.wither_son":     ("凋零之子",   "The Wither Son"),
    "entity.awakening.gate_dragon":    ("守门龙",     "Gate Dragon"),
}

# tip key -> (zh_cn, en_us)
TIPS = {
    "item.awakening.recorder.tip": (
        "记录仪会记下你听见的每一句话。右击使用，可以推进你的纪元。",
        "The Recorder notes every word you hear. Right-click to advance your era.",
    ),
    "item.awakening.resonance_stone.tip": (
        "在潜影之声附近，共鸣石会微微发热——它听见了你听得见的东西。",
        "Near sculk voices the Resonance Stone grows warm; it hears what you hear.",
    ),
    "item.awakening.soul_compass.tip": (
        "指针永远指向你遗失的那一部分自己。",
        "The needle always points to the part of yourself you have lost.",
    ),
    "item.awakening.ancient_city_pass.tip": (
        "古老的守卫认得这块通行牌。它会为你开门，然后开始提问。",
        "The ancient guardians recognize this pass. It opens the way — and starts asking questions.",
    ),
    "item.awakening.awakened_token.tip": (
        "只有真正觉醒的人才能握紧它；其余的人只会感到烫手。",
        "Only the truly awakened can grip it. For anyone else, it burns.",
    ),
    "item.awakening.wither_core.tip": (
        "它仍在缓慢跳动，像一颗不肯熄灭的心脏。",
        "It still beats, slowly, like a heart that refuses to stop.",
    ),
    "item.awakening.soul_shard.tip": (
        "碎片里有一个残缺的声音，正在反复呼喊一个名字。",
        "A broken voice inside the shard keeps calling out one name.",
    ),
    "item.awakening.echo_shard.tip": (
        "末影珍珠碎裂后留下的东西，还在回响着它坠落的声音。",
        "What is left of a broken ender pearl still echoes the sound of its fall.",
    ),
    "item.awakening.ancient_city_key.tip": (
        "钥匙齿纹与通行牌上的刻痕完全吻合。",
        "Its teeth match the notches cut into the Ancient City Pass.",
    ),
    "block.awakening.awakened_totem.tip": (
        "图腾低声念诵着早已失传的名字，每念一个，石像的眼睛就亮一分。",
        "The totem murmurs names long forgotten; each one lights another pair of stone eyes.",
    ),
    "block.awakening.wither_rift.tip": (
        "裂缝对面的空气是暖的。偶尔有别的东西在另一侧贴过来。",
        "The air on the other side is warm. Sometimes something else presses against it.",
    ),
    "block.awakening.soul_lantern.tip": (
        "灯芯里封着一缕呼吸，靠近时能听见极轻的叹息。",
        "A held breath burns inside the wick; stand close and you can hear it sigh.",
    ),
    "block.awakening.builder_brick.tip": (
        "每一块砖上都刻着建造者的印记。触摸它们，能读出半个纪元。",
        "Each brick carries a Builder's mark. Touch one and you can read half an era.",
    ),
}

# modmenu keys already present in the repo — preserved verbatim
PRESERVED = {
    "modmenu.nameTranslation.awakening": (
        "方块之下",
        "Awakening: Return of the Builder",
    ),
    "modmenu.descriptionTranslation.awakening": (
        "一个剧情驱动的 Minecraft mod：你扮演远古建造师，重演十四纪历史。",
        "A story-driven Minecraft mod: you are the ancient Builder, reliving 14 eras of history.",
    ),
}


# --------------------------------------------------------------------------
# Helpers
# --------------------------------------------------------------------------

def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(obj, fh, ensure_ascii=False, indent=2)
        fh.write("\n")
    print("wrote", os.path.relpath(path, ROOT))


def build_lang():
    """Build the zh_cn / en_us dicts with an identical key set."""
    zh, en = {}, {}

    def put(key, pair):
        z, e = pair
        zh[key] = z
        en[key] = e

    # items
    for name, pair in ITEMS.items():
        put("item.awakening." + name, pair)
    # blocks
    for name, pair in BLOCKS.items():
        put("block.awakening." + name, pair)
    # misc
    for key, pair in MISC_LANG.items():
        put(key, pair)
    # tips
    for key, pair in TIPS.items():
        put(key, pair)
    # preserved modmenu entries
    for key, pair in PRESERVED.items():
        put(key, pair)

    assert set(zh.keys()) == set(en.keys()), "zh/en key mismatch"
    return zh, en


def main():
    # ---- lang ----
    zh, en = build_lang()
    write_json(os.path.join(RES, "lang/zh_cn.json"), zh)
    write_json(os.path.join(RES, "lang/en_us.json"), en)

    # ---- item models ----
    for name in ITEMS:
        write_json(
            os.path.join(RES, "models/item", name + ".json"),
            {
                "parent": "minecraft:item/generated",
                "textures": {"layer0": "awakening:item/" + name},
            },
        )

    # ---- block models ----
    for name in BLOCKS:
        if name in COLUMN_BLOCKS:
            tex = "awakening:block/" + name
            model = {
                "parent": "minecraft:block/cube_column",
                "textures": {"end": tex, "side": tex, "top": tex},
            }
        else:
            model = {
                "parent": "minecraft:block/cube_all",
                "textures": {"all": "awakening:block/" + name},
            }
        write_json(os.path.join(RES, "models/block", name + ".json"), model)

    # ---- blockstates ----
    for name in BLOCKS:
        write_json(
            os.path.join(RES, "blockstates", name + ".json"),
            {"variants": {"": {"model": "awakening:block/" + name}}},
        )

    # ---- item models that point at a block model ----
    for name in BLOCKS:
        write_json(
            os.path.join(RES, "models/item", name + ".json"),
            {"parent": "awakening:block/" + name},
        )

    print("\ndone.")


if __name__ == "__main__":
    main()
