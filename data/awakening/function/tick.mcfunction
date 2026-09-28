# tick.mcfunction — main progression loop
# Runs every server tick via data/minecraft/tags/functions/tick.json

# Initialize scoreboards (idempotent — safe to run every tick)
scoreboard objectives add awakening.current_era dummy
scoreboard objectives add awakening.delay dummy

# Decrement delay timer for all players
scoreboard players remove @a[scores={awakening.delay=1..}] awakening.delay 1

# Initialize new players (no root advancement granted yet)
execute as @a unless @s[advancements={awakening:root=true}] run function awakening:init_player

# Advance to next era when delay reaches 0
execute as @a[scores={awakening.delay=0}] at @s run function awakening:advance_era