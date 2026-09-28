# init_player.mcfunction — called for new players (no root advancement yet)

# Grant root advancement (fires root reward → shows intro title)
advancement grant @s only awakening:root

# Set initial era state (current_era=1 means "intro shown, era 1 next")
scoreboard players set @s awakening.current_era 1
scoreboard players set @s awakening.delay 100

# Show intro cutscene
function awakening:setup