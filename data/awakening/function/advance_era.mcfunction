# advance_era.mcfunction — advance to next era when delay expires

# Increment era counter
scoreboard players add @s awakening.current_era 1

# Display era based on current_era value
execute if score @s awakening.current_era matches 2 run function awakening:era1
execute if score @s awakening.current_era matches 3 run function awakening:era2
execute if score @s awakening.current_era matches 4 run function awakening:era3
execute if score @s awakening.current_era matches 5 run function awakening:era4
execute if score @s awakening.current_era matches 6 run function awakening:era5
execute if score @s awakening.current_era matches 7 run function awakening:era6
execute if score @s awakening.current_era matches 8 run function awakening:era7
execute if score @s awakening.current_era matches 9 run function awakening:era8
execute if score @s awakening.current_era matches 10 run function awakening:era9
execute if score @s awakening.current_era matches 11 run function awakening:era10
execute if score @s awakening.current_era matches 12 run function awakening:era11
execute if score @s awakening.current_era matches 13 run function awakening:era12
execute if score @s awakening.current_era matches 14 run function awakening:era13
execute if score @s awakening.current_era matches 15 run function awakening:era14
execute if score @s awakening.current_era matches 16 run function awakening:ending

# Reset delay (era functions also set this, but safety net)
scoreboard players set @s awakening.delay 200