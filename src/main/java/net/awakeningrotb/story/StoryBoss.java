package net.awakeningrotb.story;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

/**
 * Boss 与敌对 NPC 生成器。
 *
 * <p><b>凋零之子</b> = 原版凋零（三头六翼骨骼蓝皮 —— lore 里就是照凋零写的，
 * 原版凋零的模型和 lore 描述几乎完全一致，是最贴的选择）。
 * <p><b>坚守者</b> = 原版监守者（看不见、靠震动感知 —— lore 原文就是这么描述的：
 * 「它看不见，靠震动感知」）。同样零成本零风险。
 */
public final class StoryBoss {

    public static final String TAG_SENTINEL = "§b坚守者";
    public static final String TAG_WITHER_SON = "§5凋零之子";
    public static final String TAG_GATE_DRAGON = "§6守门龙";

    private StoryBoss() {
    }

    /**
     * 生成凋零之子。
     *
     * @param bossFight true = 第七纪真正的 Boss 战（会攻击玩家、可被击杀）
     *                  false = 第五纪「凋零之罪」的演示（不掉落、不计入击杀）
     */
    public static WitherEntity spawnWitherSon(ServerPlayerEntity player, boolean bossFight) {
        ServerWorld world = player.getServerWorld();
        BlockPos pos = player.getBlockPos();
        WitherEntity wither = EntityType.WITHER.create(world, SpawnReason.COMMAND);
        if (wither == null) {
            return null;
        }
        wither.refreshPositionAndAngles(pos.getX() + 2.5, pos.getY() + 3, pos.getZ() + 2.5, 0f, 0f);
        wither.setCustomName(Text.literal(TAG_WITHER_SON).formatted(Formatting.DARK_PURPLE));
        wither.setCustomNameVisible(true);
        wither.setPersistent();
        wither.setSilent(true);

        EntityAttributeInstance maxHealth = wither.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(bossFight ? 300.0 : 200.0);
        }
        EntityAttributeInstance attr = wither.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (attr != null) {
            attr.setBaseValue(bossFight ? 8.0 : 4.0);
        }

        if (!bossFight) {
            // 演示用：不可被玩家击杀（第五纪「玩家不能阻止」）
            wither.setInvulnerable(true);
        }
        world.spawnEntity(wither);
        return wither;
    }

    /**
     * 生成坚守者 The Echo Sentinel —— 玩家用灵魂与尖啸体召唤的守护者。
     *
     * <p>lore：「他们用凋零战役里死去的同伴的灵魂，加上 Sculk 的共鸣能量，造出了坚守者。」
     * 「它看不见，靠震动感知。它的程序是歼灭一切威胁。」
     *
     * <p>实现：原版监守者（本身就是"靠震动感知、看不见"的生物），
     * 并让它主动锁定凋零之子 —— 这就是 lore 里「凋零 vs 坚守者」的那场死斗。
     */
    public static void spawnSentinel(ServerPlayerEntity player, WitherEntity target) {
        ServerWorld world = player.getServerWorld();
        BlockPos pos = player.getBlockPos();
        net.minecraft.entity.mob.WardenEntity warden =
                EntityType.WARDEN.create(world, SpawnReason.COMMAND);
        if (warden == null) {
            return;
        }
        warden.refreshPositionAndAngles(pos.getX() + 3, pos.getY(), pos.getZ() + 3, 0f, 0f);
        warden.setCustomName(Text.literal(TAG_SENTINEL).formatted(Formatting.AQUA));
        warden.setCustomNameVisible(true);
        warden.setPersistent();
        warden.setSilent(true);
        world.spawnEntity(warden);

        // 锁定凋零之子 —— 坚守者的程序是歼灭一切威胁
        if (target != null) {
            warden.setTarget(target);
            target.setTarget(warden);
        }
        player.playSound(net.minecraft.sound.SoundEvents.ENTITY_WARDEN_EMERGE, 1.0f, 1.0f);
        player.sendMessage(Text.literal("§b地下的深处传来沉重的心跳 —— 坚守者苏醒了。")
                .formatted(Formatting.AQUA), false);
    }

    /** 生成守门龙 —— 第十二纪玩家打败所有末影龙后留下的那一条。 */
    public static void spawnGateDragon(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        BlockPos pos = player.getBlockPos();
        net.minecraft.entity.boss.dragon.EnderDragonEntity dragon =
                EntityType.ENDER_DRAGON.create(world, SpawnReason.COMMAND);
        if (dragon == null) {
            return;
        }
        dragon.refreshPositionAndAngles(pos.getX(), pos.getY() + 8, pos.getZ(), 0f, 0f);
        dragon.setCustomName(Text.literal(TAG_GATE_DRAGON).formatted(Formatting.GOLD));
        dragon.setCustomNameVisible(true);
        dragon.setPersistent();
        world.spawnEntity(dragon);
    }

    /** 生成 N 个暴走的邪术师（第九纪）。 */
    public static void spawnWarlocks(ServerPlayerEntity player, int count) {
        ServerWorld world = player.getServerWorld();
        BlockPos pos = player.getBlockPos();
        for (int i = 0; i < count; i++) {
            EvokerEntity evoker = EntityType.EVOKER.create(world, SpawnReason.COMMAND);
            if (evoker == null) {
                continue;
            }
            double angle = (Math.PI * 2 * i) / count;
            evoker.refreshPositionAndAngles(
                    pos.getX() + Math.cos(angle) * 5,
                    pos.getY(),
                    pos.getZ() + Math.sin(angle) * 5,
                    (float) Math.toDegrees(angle + Math.PI),
                    0f);
            evoker.setCustomName(Text.literal(StoryNpc.TAG_WARLOCK).formatted(Formatting.DARK_PURPLE));
            evoker.setCustomNameVisible(true);
            evoker.setPersistent();
            evoker.setSilent(true);
            world.spawnEntity(evoker);
        }
    }

    /** 清掉场上所有剧情 NPC / Boss（切纪元时用，避免重复生成）。 */
    public static void clearStoryEntities(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        for (net.minecraft.entity.Entity e : world.getEntitiesByClass(
                net.minecraft.entity.Entity.class,
                player.getBoundingBox().expand(128),
                en -> en.getCustomName() != null)) {
            String n = e.getCustomName().getString();
            if (n.contains(StoryNpc.TAG_AWAKENER)
                    || n.contains(StoryNpc.TAG_WARLOCK)
                    || n.contains(StoryBoss.TAG_WITHER_SON)
                    || n.contains(StoryBoss.TAG_GATE_DRAGON)) {
                e.discard();
            }
        }
    }

    /** 清掉场上所有邪术师（第十纪开始时）。 */
    public static void clearWarlocks(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        for (net.minecraft.entity.mob.EvokerEntity evoker : world.getEntitiesByClass(
                EvokerEntity.class,
                player.getBoundingBox().expand(64),
                e -> e.getCustomName() != null
                        && e.getCustomName().getString().contains(StoryNpc.TAG_WARLOCK))) {
            evoker.discard();
        }
    }
}
