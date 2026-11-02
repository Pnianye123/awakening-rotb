package net.awakeningrotb.story;

import net.awakeningrotb.item.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

/**
 * 剧情事件监听 —— 把玩家的实际行为翻译成任务进度。
 *
 * <p>各纪元的任务判定分散在这里：
 * <ul>
 *   <li>与村民交谈 → 第一纪</li>
 *   <li>与灾厄村民交谈 → 第三纪</li>
 *   <li>交出灵魂碎片 → 第四纪</li>
 *   <li>击杀凋零之子 → 第七纪</li>
 *   <li>用下界之星净化 → 第八纪</li>
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

        // ── 玩家进世界 → 发《前情提要》 ──
        // 开局第一件事是拿到那本书；书里告诉玩家输入 /start 才开始剧情。
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (player == null) {
                return;
            }
            // 已经玩过（进度里有 started）就不重复塞书了
            StoryProgress progress = StoryState.of(player.getServerWorld()).get(player);
            if (progress.started) {
                return;
            }
            PrologueBook.give(player);
            player.sendMessage(Loc.msg(player, "msg.prologue.given").formatted(Formatting.GRAY), false);
        });

        // ── 与 NPC 交谈 ──
        UseEntityCallback.EVENT.register((PlayerEntity player, World world, Hand hand,
                                          Entity entity, EntityHitResult hitResult) -> {
            if (!(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return ActionResult.PASS;
            }
            StoryProgress progress = StoryState.of(sw).get(sp);
            if (!progress.inStory()) {
                return ActionResult.PASS;
            }

            String name = entity.getCustomName() == null ? "" : entity.getCustomName().getString();

            if (entity.getType() == EntityType.VILLAGER
                    && !name.contains(StoryNpc.TAG_AWAKENER)) {
                // 普通村民 —— 第一纪：与 3 位村民交谈
                progress.increment("talk_villager");
                hint(sp, Loc.str(Loc.langOf(sp), "msg.talk.villager",
                        Math.min(progress.get("talk_villager"), 3)));
            } else if (name.contains(StoryNpc.TAG_WARLOCK)) {
                // 灾厄村民 —— 第三纪
                progress.increment("talk_warlock");
                hint(sp, Loc.str(Loc.langOf(sp), "msg.talk.warlock"));
                if (progress.get("talk_warlock") >= 1 && progress.era == 2) {
                    sp.sendMessage(Loc.msg(sp, "msg.talk.warlock.ask").formatted(Formatting.GRAY), false);
                }
            }
            return ActionResult.PASS;
        });

        // ── 交出灵魂碎片 —— 第四纪 ──
        UseEntityCallback.EVENT.register((PlayerEntity player, World world, Hand hand,
                                          Entity entity, EntityHitResult hitResult) -> {
            if (!(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return ActionResult.PASS;
            }
            StoryProgress progress = StoryState.of(sw).get(sp);
            if (!progress.inStory() || progress.era != 3) {
                return ActionResult.PASS;
            }
            String name = entity.getCustomName() == null ? "" : entity.getCustomName().getString();
            if (!name.contains(StoryNpc.TAG_WARLOCK)) {
                return ActionResult.PASS;
            }
            ItemStack held = player.getStackInHand(hand);
            if (!held.isOf(ModItems.SOUL_SHARD)) {
                return ActionResult.PASS;
            }
            held.decrement(1);
            progress.increment("give_soul");
            hint(sp, Loc.str(Loc.langOf(sp), "msg.give.soul", progress.get("give_soul")));
            if (progress.get("give_soul") >= 3) {
                sp.sendMessage(Loc.msg(sp, "msg.give.soul.done").formatted(Formatting.DARK_GRAY), false);
            }
            return ActionResult.PASS;
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
                    player.sendMessage(Loc.msg(player, "msg.kill.wither")
                            .formatted(Formatting.DARK_PURPLE), false);
                    // 坚守者 —— 第七纪 Boss 战的胜利者
                    StoryBoss.spawnGateDragon(player);
                } else if (isWarlock) {
                    progress.increment("kill_warlock");
                    player.sendMessage(Loc.msg(player, "msg.kill.warlock")
                            .formatted(Formatting.DARK_PURPLE), false);
                } else if (isDragon) {
                    progress.increment("kill_dragon");
                    player.sendMessage(Loc.msg(player, "msg.kill.dragon")
                            .formatted(Formatting.GOLD), false);
                }
            }
        });

        // ── 第八纪 · 信标净化 —— 右击共鸣方块 + 下界之星 ──
        UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand,
                                        BlockHitResult hitResult) -> {
            if (!(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return ActionResult.PASS;
            }
            StoryProgress progress = StoryState.of(sw).get(sp);
            if (!progress.inStory() || progress.era != 7) {
                return ActionResult.PASS;
            }
            // 找共鸣方块：先看直接点中的，再看附近的（8 格内）
            // —— 玩家瞄歪一格不该卡住剧情
            BlockPos pos = hitResult.getBlockPos();
            if (!isResonanceBlock(sw, pos)) {
                BlockPos found = findResonanceBlock(sw, sp, pos);
                if (found == null) {
                    return ActionResult.PASS;
                }
                pos = found;
            }
            tryPurify(sp, sw, progress, pos, hand);
            return ActionResult.PASS;
        });

        // ── 第八纪 · 信标净化 —— 拿着下界之星「使用」也能净化 ──
        // lore：「你用下界之星触碰它，世界安静了。」
        // 这样玩家不需要精准瞄准方块，符合"碰一下"的直觉，也更宽容。
        UseItemCallback.EVENT.register((PlayerEntity player, World world, Hand hand) -> {
            if (!(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return ActionResult.PASS;
            }
            StoryProgress progress = StoryState.of(sw).get(sp);
            if (!progress.inStory()) {
                return ActionResult.PASS;
            }
            // 第十四纪 · 使用末影珍珠碎片 ——「不归路」
            if (progress.era == 13 && player.getStackInHand(hand).isOf(ModItems.ECHO_SHARD)) {
                player.getStackInHand(hand).decrement(1);
                progress.setFlag(StoryManager.FLAG_NO_RETURN);
                sp.sendMessage(Loc.msg(sp, "msg.use.echo")
                        .formatted(Formatting.DARK_PURPLE), false);
                return ActionResult.SUCCESS;
            }
            // 第八纪 · 净化
            if (progress.era == 7 && player.getStackInHand(hand).isOf(Items.NETHER_STAR)) {
                BlockPos found = findResonanceBlock(sw, sp, player.getBlockPos());
                if (found != null) {
                    tryPurify(sp, sw, progress, found, hand);
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
    }

    /** 真正执行净化（两条触发路径共用）。 */
    private static void tryPurify(ServerPlayerEntity sp, ServerWorld sw, StoryProgress progress,
                                  BlockPos pos, Hand hand) {
        if (progress.hasFlag(StoryManager.FLAG_PURIFIED)) {
            return;
        }
        // 必须有下界之星 —— 手持优先，没有就扫快捷栏
        // （玩家选错槽位不该卡住剧情，这是刻意的宽容）
        ItemStack held = sp.getStackInHand(hand);
        if (!held.isOf(Items.NETHER_STAR)) {
            held = null;
            for (int i = 0; i < sp.getInventory().size(); i++) {
                ItemStack stack = sp.getInventory().getStack(i);
                if (stack.isOf(Items.NETHER_STAR)) {
                    held = stack;
                    break;
                }
            }
            if (held == null) {
                return;
            }
        }

        held.decrement(1);
        progress.setFlag(StoryManager.FLAG_PURIFIED);

        // 净化表现 —— 金色光柱 + 全场音效
        sw.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_BEACON_ACTIVATE,
                net.minecraft.sound.SoundCategory.BLOCKS, 3.0f, 0.6f);
        sw.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_BEACON_DEACTIVATE,
                net.minecraft.sound.SoundCategory.BLOCKS, 3.0f, 1.6f);
        sw.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                60, 0.3, 0.0, 1.0, 0.04);
        // 净化范围：把附近的凋零清掉
        for (Entity e : sw.getEntitiesByClass(MobEntity.class,
                sp.getBoundingBox().expand(32),
                en -> en instanceof net.minecraft.entity.boss.WitherEntity)) {
            e.discard();
        }

        sp.sendMessage(Loc.msg(sp, "msg.purify.beam")
                .formatted(Formatting.GOLD), false);
        sp.sendMessage(Loc.msg(sp, "msg.purify.blight")
                .formatted(Formatting.GREEN), false);
    }

    /** 判断某位置的方块是不是共鸣方块 —— 用注册表 id 比对，不依赖 Block 实例。 */
    private static boolean isResonanceBlock(ServerWorld world, BlockPos pos) {
        var block = world.getBlockState(pos).getBlock();
        return net.minecraft.registry.Registries.BLOCK.getId(block).toString()
                .equals("awakening:resonance_block");
    }

    /** 统计玩家附近的共鸣方块数量（玩家可见的调试信息 + 测试断言用）。 */
    public static int countResonanceBlocks(ServerWorld world, BlockPos origin) {
        int n = 0;
        for (int dx = -8; dx <= 8; dx++) {
            for (int dy = -8; dy <= 8; dy++) {
                for (int dz = -8; dz <= 8; dz++) {
                    if (isResonanceBlock(world, origin.add(dx, dy, dz))) {
                        n++;
                    }
                }
            }
        }
        return n;
    }

    /** 在玩家附近找共鸣方块（8 格内，取离瞄准点最近的）。 */
    private static BlockPos findResonanceBlock(ServerWorld world, ServerPlayerEntity player,
                                               BlockPos near) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        BlockPos origin = player.getBlockPos();
        for (int dx = -8; dx <= 8; dx++) {
            for (int dy = -8; dy <= 8; dy++) {
                for (int dz = -8; dz <= 8; dz++) {
                    BlockPos p = origin.add(dx, dy, dz);
                    if (!isResonanceBlock(world, p)) {
                        continue;
                    }
                    double d = p.getSquaredDistance(near);
                    if (d < bestDist) {
                        bestDist = d;
                        best = p.toImmutable();
                    }
                }
            }
        }
        return best;
    }

    /** 小提示（action bar）。 */
    private static void hint(ServerPlayerEntity player, String text) {
        player.sendMessage(Text.literal(text).formatted(Formatting.GRAY), true);
    }
}
