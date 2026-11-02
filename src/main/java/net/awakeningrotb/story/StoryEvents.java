package net.awakeningrotb.story;

import net.awakeningrotb.item.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * 剧情事件监听 —— 把玩家的实际行为翻译成任务进度。
 *
 * <p>各纪元的任务判定分散在这里：
 * <ul>
 *   <li>与村民交谈 → 第一纪</li>
 *   <li>与灾厄村民交谈 → 第三纪</li>
 *   <li>交出灵魂碎片 → 第四纪</li>
 *   <li>击杀凋零之子 → 第七纪</li>
 *   <li>击杀邪术师 → 第九纪</li>
 *   <li>击杀末影龙 → 第十二纪</li>
 *   <li>使用末影珍珠碎片 → 第十四纪</li>
 * </ul>
 * 另外还有持续性检测（藏在地下 / 躲在要塞 / 在凋零手下存活），
 * 这些在 {@link StoryManager#tick} 里轮询。
 */
public final class StoryEvents {

    private StoryEvents() {
    }

    public static void register() {
        // ── 每 tick 推进剧情 ──
        ServerTickEvents.END_SERVER_TICK.register(StoryManager::tick);

        // ── 与 NPC 交谈 ──
        UseEntityCallback.EVENT.register((net.minecraft.entity.player.PlayerEntity player,
                                          net.minecraft.world.World world,
                                          net.minecraft.util.Hand hand,
                                          net.minecraft.entity.Entity entity,
                                          net.minecraft.util.hit.EntityHitResult hitResult) -> {
            if (!(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return net.minecraft.util.ActionResult.PASS;
            }
            StoryState state = StoryState.of(sw);
            StoryProgress progress = state.get(sp);
            if (!progress.inStory()) {
                return net.minecraft.util.ActionResult.PASS;
            }

            String name = entity.getCustomName() == null ? "" : entity.getCustomName().getString();

            if (entity.getType() == EntityType.VILLAGER
                    && !name.contains(StoryNpc.TAG_AWAKENER)) {
                // 普通村民 —— 第一纪：与 3 位村民交谈
                progress.increment("talk_villager");
                hint(sp, "§7你和村民聊了聊。§7（§f"
                        + Math.min(progress.get("talk_villager"), 3) + "§7/§f3§7）");
            } else if (name.contains(StoryNpc.TAG_WARLOCK)) {
                // 灾厄村民 —— 第三纪
                progress.increment("talk_warlock");
                hint(sp, "§7灾厄村民抬起头看你。§7它的眼神很凶，但它没有动手。");
                if (progress.get("talk_warlock") >= 1 && progress.era == 2) {
                    // 第三纪完成后，把灵魂碎片给玩家的提示
                    sp.sendMessage(Text.literal("§e灾厄村民：§7「我们可以帮你们复活他们……但首先，我们需要灵魂碎片。」")
                            .formatted(Formatting.GRAY), false);
                }
            }
            return net.minecraft.util.ActionResult.PASS;
        });

        // ── 交出灵魂碎片 —— 第四纪 ──
        UseEntityCallback.EVENT.register((net.minecraft.entity.player.PlayerEntity player,
                                          net.minecraft.world.World world,
                                          net.minecraft.util.Hand hand,
                                          net.minecraft.entity.Entity entity,
                                          net.minecraft.util.hit.EntityHitResult hitResult) -> {
            if (!(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return net.minecraft.util.ActionResult.PASS;
            }
            StoryState state = StoryState.of(sw);
            StoryProgress progress = state.get(sp);
            if (!progress.inStory()) {
                return net.minecraft.util.ActionResult.PASS;
            }
            String name = entity.getCustomName() == null ? "" : entity.getCustomName().getString();
            if (!name.contains(StoryNpc.TAG_WARLOCK)) {
                return net.minecraft.util.ActionResult.PASS;
            }
            ItemStack held = player.getStackInHand(hand);
            if (!held.isOf(ModItems.SOUL_SHARD)) {
                return net.minecraft.util.ActionResult.PASS;
            }
            if (progress.era != 3) {
                return net.minecraft.util.ActionResult.PASS;
            }
            held.decrement(1);
            progress.increment("give_soul");
            hint(sp, "§7你把一枚灵魂碎片放进灾厄村民的掌心。§7（§f"
                    + progress.get("give_soul") + "§7/§f3§7）");
            if (progress.get("give_soul") >= 3) {
                sp.sendMessage(Text.literal("§8灾厄村民们低下头，开始了复活术。").formatted(Formatting.DARK_GRAY), false);
            }
            return net.minecraft.util.ActionResult.PASS;
        });

        // ── 击杀统计 —— 第七纪（凋零之子）/ 第九纪（邪术师）/ 第十二纪（末影龙） ──
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity.getWorld() instanceof ServerWorld sw)) {
                return;
            }
            String name = entity.getCustomName() == null ? "" : entity.getCustomName().getString();
            boolean isWitherSon = name.contains(StoryBoss.TAG_WITHER_SON);
            boolean isWarlock = name.contains(StoryNpc.TAG_WARLOCK);
            boolean isDragon = entity.getType() == EntityType.ENDER_DRAGON;

            if (!isWitherSon && !isWarlock && !isDragon) {
                return;
            }
            for (ServerPlayerEntity player : sw.getServer().getPlayerManager().getPlayerList()) {
                StoryProgress progress = StoryState.of(sw).get(player);
                if (!progress.inStory()) {
                    continue;
                }
                if (isWitherSon) {
                    progress.increment("kill_wither_son");
                    player.sendMessage(Text.literal("§5凋零之子被撕碎了。").formatted(Formatting.DARK_PURPLE), false);
                    // 坚守者 —— 第七纪 Boss 战的胜利者
                    StoryBoss.spawnGateDragon(player);
                } else if (isWarlock) {
                    progress.increment("kill_warlock");
                    player.sendMessage(Text.literal("§5一个邪术师倒下了。").formatted(Formatting.DARK_PURPLE), false);
                } else if (isDragon) {
                    progress.increment("kill_dragon");
                    player.sendMessage(Text.literal("§6末影龙坠落。最后一条守门龙留了下来。")
                            .formatted(Formatting.GOLD), false);
                }
            }
        });

        // ── 使用末影珍珠碎片 —— 第十四纪「不归路」 ──
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return net.minecraft.util.ActionResult.PASS;
            }
            StoryProgress progress = StoryState.of(sw).get(sp);
            if (!progress.inStory() || progress.era != 13) {
                return net.minecraft.util.ActionResult.PASS;
            }
            ItemStack held = player.getStackInHand(hand);
            if (!held.isOf(ModItems.ECHO_SHARD)) {
                return net.minecraft.util.ActionResult.PASS;
            }
            held.decrement(1);
            progress.setFlag(StoryManager.FLAG_NO_RETURN);
            sp.sendMessage(Text.literal("§5你握紧末影珍珠碎片。§5世界在你眼前碎成了像素。")
                    .formatted(Formatting.DARK_PURPLE), false);
            return net.minecraft.util.ActionResult.PASS;
        });
    }

    /** 小提示（action bar）。 */
    private static void hint(ServerPlayerEntity player, String text) {
        player.sendMessage(Text.literal(text).formatted(Formatting.GRAY), true);
    }
}
