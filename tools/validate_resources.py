#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Validate the generated resource JSON files for the mod "awakening".

Checks:
  1. every .json under assets/awakening/ parses with json.load
  2. zh_cn.json and en_us.json have EXACTLY the same key set (and no empty values)
  3. every item has models/item/<name>.json
     every block has models/block/<name>.json, blockstates/<name>.json,
     models/item/<name>.json
  4. required lang keys exist (item./block./itemGroup./entity./tips)
  5. model parents & texture refs are well-formed and point at existing files
  6. formatting: 2-space indent, trailing newline, no comments / trailing commas
"""

import json
import os
import re
import sys

ROOT = "/home/yifan/awakening-rotb"
RES = os.path.join(ROOT, "src/main/resources/assets/awakening")
MOD = "awakening"

ITEMS = [
    "builder_robe", "recorder", "resonance_stone", "soul_compass",
    "ancient_city_pass", "wither_core", "ancient_city_key",
    "record_a", "record_b", "record_c", "awakened_token",
    "soul_shard", "echo_shard", "ancient_totem_item",
    "resonance_block_item", "wither_core_item",
]

BLOCKS = [
    "resonance_block", "awakened_totem", "builder_brick", "wither_rift",
    "reinforced_echo_stone", "soul_lantern", "builder_robe_block",
]

COLUMN_BLOCKS = {"awakened_totem"}

TIP_ITEMS = ["recorder", "resonance_stone", "soul_compass",
             "ancient_city_pass", "awakened_token"]

ENTITIES = ["awakener", "warlock", "ender_guardian",
            "echo_sentinel", "wither_son", "gate_dragon"]

errors = []
warnings = []
checks = []


def ok(label):
    checks.append(label)
    print("  [OK]   " + label)


def fail(label, detail):
    errors.append(label + " -> " + detail)
    print("  [FAIL] " + label + " -> " + detail)


def warn(label, detail):
    warnings.append(label + " -> " + detail)
    print("  [WARN] " + label + " -> " + detail)


def load(path):
    with open(path, encoding="utf-8") as fh:
        return json.load(fh)


def rel(p):
    return os.path.relpath(p, ROOT)


def strip_json_comments(text):
    """Remove // and /* */ comments + trailing commas so we can detect them."""
    out = []
    i, n = 0, len(text)
    in_str = False
    while i < n:
        c = text[i]
        if in_str:
            out.append(c)
            if c == "\\" and i + 1 < n:
                out.append(text[i + 1])
                i += 2
                continue
            if c == '"':
                in_str = False
            i += 1
            continue
        if c == '"':
            in_str = True
            out.append(c)
            i += 1
            continue
        if text.startswith("//", i):
            while i < n and text[i] != "\n":
                i += 1
            continue
        if text.startswith("/*", i):
            j = text.find("*/", i + 2)
            i = n if j == -1 else j + 2
            continue
        out.append(c)
        i += 1
    return "".join(out)


print("=" * 78)
print("VALIDATION REPORT — assets/awakening")
print("=" * 78)

# ---------------------------------------------------------------- 1. parse
print("\n[1] All JSON files parse + formatting check")
all_json = []
for dirpath, _dirnames, filenames in os.walk(RES):
    for fn in sorted(filenames):
        if fn.endswith(".json"):
            all_json.append(os.path.join(dirpath, fn))
all_json.sort()

for path in all_json:
    r = rel(path)
    try:
        with open(path, encoding="utf-8") as fh:
            raw = fh.read()
        json.loads(raw)
    except Exception as exc:  # noqa: BLE001
        fail("parse " + r, repr(exc))
        continue

    problems = []
    if not raw.endswith("\n"):
        problems.append("no trailing newline")
    if raw.endswith("\n\n"):
        problems.append("extra blank line at EOF")
    if "//" in raw or "/*" in raw:
        problems.append("possible comment")
    if re.search(r",\s*[}\]]", raw):
        problems.append("trailing comma")
    # 2-space indent check: re-dump and compare structure spacing
    obj = json.loads(raw)
    canon = json.dumps(obj, ensure_ascii=False, indent=2) + "\n"
    if canon != raw:
        problems.append("not canonical 2-space-indent JSON")
    if problems:
        fail("format " + r, "; ".join(problems))
    else:
        ok("format " + r)

print("\n    -> %d json files scanned under %s" % (len(all_json), rel(RES)))

# ---------------------------------------------------- 2. zh / en key parity
print("\n[2] zh_cn / en_us key-set parity")
zh_path = os.path.join(RES, "lang/zh_cn.json")
en_path = os.path.join(RES, "lang/en_us.json")
zh, en = load(zh_path), load(en_path)

zh_keys, en_keys = set(zh.keys()), set(en.keys())
missing_in_en = sorted(zh_keys - en_keys)
missing_in_zh = sorted(en_keys - zh_keys)
if missing_in_en:
    fail("lang parity", "missing in en_us: %s" % missing_in_en)
elif missing_in_zh:
    fail("lang parity", "missing in zh_cn: %s" % missing_in_zh)
else:
    ok("lang parity — %d keys, identical set" % len(zh_keys))

empty = [k for k, v in zh.items() if not str(v).strip()]
empty += [k for k, v in en.items() if not str(v).strip()]
if empty:
    fail("lang values", "empty values: %s" % sorted(set(empty)))
else:
    ok("lang values — no empty strings")

# zh values should be CJK, en values should be ASCII (sanity)
cjk = re.compile(r"[\u4e00-\u9fff]")
bad_zh = [k for k, v in zh.items() if k.startswith(("item.", "block.", "entity.", "itemGroup.")) and not cjk.search(v)]
bad_en = [k for k, v in en.items() if k.startswith(("item.", "block.", "entity.", "itemGroup.")) and cjk.search(v)]
if bad_zh:
    warn("lang script", "zh value has no CJK: %s" % bad_zh)
else:
    ok("lang script — zh_cn values contain CJK")
if bad_en:
    fail("lang script", "en_us value contains CJK: %s" % bad_en)
else:
    ok("lang script — en_us values are ASCII")

# --------------------------------------------------------- 3. required keys
print("\n[3] Required lang keys")
required = []
for name in ITEMS:
    required.append("item.%s.%s" % (MOD, name))
for name in BLOCKS:
    required.append("block.%s.%s" % (MOD, name))
required.append("itemGroup.%s" % MOD)
for e in ENTITIES:
    required.append("entity.%s.%s" % (MOD, e))
for name in TIP_ITEMS:
    required.append("item.%s.%s.tip" % (MOD, name))
required.append("block.%s.awakened_totem.tip" % MOD)

missing = [k for k in required if k not in zh_keys]
if missing:
    fail("required lang keys", "missing: %s" % missing)
else:
    ok("required lang keys — all %d present" % len(required))

# ------------------------------------------------------ 4. per-name files
print("\n[4] File presence per item / block")
for name in ITEMS:
    p = os.path.join(RES, "models/item", name + ".json")
    if os.path.isfile(p):
        ok("models/item/%s.json" % name)
    else:
        fail("models/item/%s.json" % name, "missing")

for name in BLOCKS:
    for sub, fn in (("models/block", name + ".json"),
                    ("blockstates", name + ".json"),
                    ("models/item", name + ".json")):
        p = os.path.join(RES, sub, fn)
        label = "%s/%s" % (sub, fn)
        if os.path.isfile(p):
            ok(label)
        else:
            fail(label, "missing")

# ------------------------------------------------------- 5. model contents
print("\n[5] Model / blockstate contents")
for name in ITEMS:
    p = os.path.join(RES, "models/item", name + ".json")
    if not os.path.isfile(p):
        continue
    m = load(p)
    label = "models/item/%s.json" % name
    if m.get("parent") != "minecraft:item/generated":
        fail(label, "parent != minecraft:item/generated (%r)" % m.get("parent"))
    tex = (m.get("textures") or {}).get("layer0")
    if tex != "%s:item/%s" % (MOD, name):
        fail(label, "layer0 != %s:item/%s (got %r)" % (MOD, name, tex))
    else:
        ok(label)

for name in BLOCKS:
    p = os.path.join(RES, "models/block", name + ".json")
    if not os.path.isfile(p):
        continue
    m = load(p)
    label = "models/block/%s.json" % name
    if name in COLUMN_BLOCKS:
        if m.get("parent") != "minecraft:block/cube_column":
            fail(label, "parent != minecraft:block/cube_column (%r)" % m.get("parent"))
        t = m.get("textures") or {}
        want = "%s:block/%s" % (MOD, name)
        bad = {k: v for k, v in t.items() if v != want}
        if set(t.keys()) != {"end", "side", "top"}:
            fail(label, "textures keys should be end/side/top, got %s" % sorted(t.keys()))
        elif bad:
            fail(label, "bad textures %s" % bad)
        else:
            ok(label + " (cube_column)")
    else:
        if m.get("parent") != "minecraft:block/cube_all":
            fail(label, "parent != minecraft:block/cube_all (%r)" % m.get("parent"))
        elif (m.get("textures") or {}).get("all") != "%s:block/%s" % (MOD, name):
            fail(label, "textures.all mismatch")
        else:
            ok(label + " (cube_all)")

for name in BLOCKS:
    p = os.path.join(RES, "blockstates", name + ".json")
    if not os.path.isfile(p):
        continue
    s = load(p)
    label = "blockstates/%s.json" % name
    variants = s.get("variants")
    if not isinstance(variants, dict) or "" not in variants:
        fail(label, "missing variants[\"\"]")
    else:
        mdl = variants[""].get("model")
        if mdl != "%s:block/%s" % (MOD, name):
            fail(label, "variants model != %s:block/%s (got %r)" % (MOD, name, mdl))
        else:
            ok(label)

for name in BLOCKS:
    p = os.path.join(RES, "models/item", name + ".json")
    if not os.path.isfile(p):
        continue
    m = load(p)
    label = "models/item/%s.json (block)" % name
    if m.get("parent") != "%s:block/%s" % (MOD, name):
        fail(label, "parent != %s:block/%s (got %r)" % (MOD, name, m.get("parent")))
    else:
        ok(label)

# --------------------------------------------- 6. cross-reference existence
print("\n[6] Cross-references resolve to declared parents")
VANILLA_PARENTS = {
    "minecraft:item/generated", "minecraft:block/cube_all",
    "minecraft:block/cube_column",
}
for dirpath, _d, filenames in os.walk(os.path.join(RES, "models")):
    for fn in sorted(filenames):
        if not fn.endswith(".json"):
            continue
        path = os.path.join(dirpath, fn)
        m = load(path)
        parent = m.get("parent")
        if not parent:
            continue
        label = rel(path)
        if parent in VANILLA_PARENTS:
            ok("%s -> %s" % (label, parent))
        elif parent.startswith(MOD + ":"):
            target = os.path.join(RES, "models", parent.split(":", 1)[1] + ".json")
            if os.path.isfile(target):
                ok("%s -> %s" % (label, parent))
            else:
                fail(label, "parent model missing: %s" % rel(target))
        else:
            fail(label, "unexpected parent %r" % parent)

for name in BLOCKS:
    p = os.path.join(RES, "blockstates", name + ".json")
    if os.path.isfile(p):
        s = load(p)
        for _k, v in (s.get("variants") or {}).items():
            mdl = v.get("model")
            if mdl and mdl.startswith(MOD + ":"):
                target = os.path.join(RES, "models", mdl.split(":", 1)[1] + ".json")
                if not os.path.isfile(target):
                    fail("blockstates/%s.json" % name, "model missing %s" % rel(target))

# ------------------------------------------------------------- 7. summary
print("\n" + "=" * 78)
print("SUMMARY")
print("=" * 78)
print("  json files scanned      : %d" % len(all_json))
print("  items declared          : %d" % len(ITEMS))
print("  blocks declared         : %d" % len(BLOCKS))
print("  lang keys (zh == en)    : %d" % len(zh_keys))
print("  checks passed           : %d" % len(checks))
print("  warnings                : %d" % len(warnings))
print("  errors                  : %d" % len(errors))
if warnings:
    print("\nWARNINGS:")
    for w in warnings:
        print("  - " + w)
if errors:
    print("\nERRORS:")
    for e in errors:
        print("  - " + e)
    print("\nRESULT: FAILED")
    sys.exit(1)
print("\nRESULT: ALL CHECKS PASSED ✅")
