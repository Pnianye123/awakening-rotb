# setup.mcfunction — 序：醒来 (intro cutscene)

# === SCENE SETUP ===

# Teleport player to ancient city (deep underground)
execute in minecraft:overworld run tp @s ~ -52 ~

# CRITICAL: Clear an air bubble around the player at the new location
# Otherwise the player is embedded in solid deepslate and suffocates
# (clears 5x5x4 box centered on player feet)
execute at @s run fill ~-2 ~ ~-2 ~2 ~3 ~2 minecraft:air

# Build a small ancient city set piece (10x10 platform + sculk atmosphere)
# Floor (polished_deepslate, one block below player's feet)
execute at @s run fill ~-5 ~-1 ~-5 ~5 ~-1 ~5 minecraft:polished_deepslate
# Sculk patches on the floor
execute at @s run setblock ~-3 ~-1 ~3 minecraft:sculk
execute at @s run setblock ~3 ~-1 ~-3 minecraft:sculk
execute at @s run setblock ~-2 ~-1 ~2 minecraft:sculk_vein
execute at @s run setblock ~2 ~-1 ~-2 minecraft:sculk_vein
# Sculk shriekers (the lore "shrinking" / "echo")
execute at @s run setblock ~-3 ~ ~3 minecraft:sculk_shrieker
execute at @s run setblock ~3 ~ ~-3 minecraft:sculk_shrieker
# Reinforced deepslate pillars (4 corners — the Builder architecture)
execute at @s run setblock ~-5 ~-1 ~-5 minecraft:reinforced_deepslate
execute at @s run setblock ~5 ~-1 ~-5 minecraft:reinforced_deepslate
execute at @s run setblock ~-5 ~-1 ~5 minecraft:reinforced_deepslate
execute at @s run setblock ~5 ~-1 ~5 minecraft:reinforced_deepslate

# Give night vision so the player can see in the dark
effect give @s minecraft:night_vision 99999 0 true

# Spawn the Awakener NPC next to the player (2 blocks east)
# - Custom name "觉醒者" (plain string, no JSON text wrapper)
# - Nitwit profession (no profession display, only custom name shown)
# - Invulnerable + NoAI + Silent (can't be hurt, doesn't move, makes no sound)
# - Rotation:[90f,0f] = face west (-X direction) = toward player at +0 X
execute at @s run summon minecraft:villager ~2 ~ ~2 {CustomName:'觉醒者',CustomNameVisible:1b,Invulnerable:1b,NoAI:1b,Silent:1b,PersistenceRequired:1b,Offers:{},VillagerData:{profession:"minecraft:nitwit",level:1,type:"minecraft:plains"},Rotation:[90f,0f]}

# Set spawn point at current (teleported) location
execute in minecraft:overworld run spawnpoint @s ~ -52 ~

# === INTRO CUTSCENE (lore) ===

title @s times 10 100 20
title @s title {"text":"方块之下","color":"gold","bold":true}
title @s subtitle {"text":"序：醒来","color":"yellow"}

tellraw @s {"text":"═══════════════════════════════════","color":"gold"}
tellraw @s {"text":"《方块之下》 / Awakening: Return of the Builder","color":"gold","bold":true}
tellraw @s {"text":"═══════════════════════════════════","color":"gold"}
tellraw @s ""
tellraw @s {"text":"序 · 醒来","color":"light_purple","bold":true}
tellraw @s ""
tellraw @s {"text":"你不是这个世界的人。","color":"white"}
tellraw @s {"text":"但你已经在这片土地上行走了很久。","color":"white"}
tellraw @s {"text":"你劈过树、杀过僵尸、挖过矿、烧过玻璃、给末影龙最后一刀。","color":"gray"}
tellraw @s ""
tellraw @s {"text":"然后——一段长长的黑屏文字滚过屏幕。","color":"gray"}
tellraw @s {"text":"两位声音在讨论你，说你是「宇宙做了个关于自己的梦」。","color":"gray"}
tellraw @s ""
tellraw @s {"text":"你以为游戏结束了。","color":"white"}
tellraw @s {"text":"其实故事才刚刚开始。","color":"yellow","bold":true}
tellraw @s ""