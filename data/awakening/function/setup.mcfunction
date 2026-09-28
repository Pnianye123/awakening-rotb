# setup.mcfunction — 序：醒来 (intro cutscene)

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

# Set spawn point
spawnpoint @s ~ ~ ~