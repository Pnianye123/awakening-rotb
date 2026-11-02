package net.awakeningrotb.story;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * 剧情 NPC 生成器。
 *
 * <p><b>设计取舍</b>：不写自定义实体类（那需要自定义渲染器 + 贴图 + 模型，
 * 客户端崩一次整局就没了）。这里统一用原版实体 + 自定义名字 + 属性调整：
 * <ul>
 *   <li><b>觉醒者</b> = 村民（引导 NPC，不参与战斗）</li>
 *   <li><b>邪术师</b> = 唤魔者 / 卫道士（第三纪友善 → 第九纪暴走）</li>
 *   <li><b>末影守护者</b> = 末影人（认亲不攻击）</li>
 * </ul>
 * 这样零客户端风险，且完全符合 lore（这些角色本来就像村民/末影人）。
 */
public final class StoryNpc {

    /** NPC 名字标记 —— 用于识别"这个 NPC 是剧情 NPC"。 */
    public static final String TAG_AWAKENER = "§6觉醒者";
    public static final String TAG_WARLOCK = "§5邪术师";
    public static final String TAG_GUARDIAN = "§3末影守护者";

    private StoryNpc() {
    }

    /** 在玩家附近生成觉醒者。 */
    public static void spawnAwakener(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        BlockPos pos = near(player);
        VillagerEntity villager = EntityType.VILLAGER.create(world, net.minecraft.entity.SpawnReason.COMMAND);
        if (villager == null) {
            return;
        }
        villager.refreshPositionAndAngles(pos.getX() + 1.5, pos.getY(), pos.getZ() + 1.5, 180f, 0f);
        villager.setCustomName(Text.literal(TAG_AWAKENER).formatted(Formatting.GOLD));
        villager.setCustomNameVisible(true);
        villager.setPersistent();
        villager.setAiDisabled(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        world.spawnEntity(villager);
    }

    /**
     * 生成邪术师。
     *
     * @param friendly true = 第三纪（善良的灾厄村民，不攻击玩家）
     *                 false = 第六/九纪（暴走，敌对）
     */
    public static void spawnWarlock(ServerPlayerEntity player, boolean friendly) {
        ServerWorld world = player.getServerWorld();
        BlockPos pos = near(player);
        net.minecraft.entity.mob.EvokerEntity evoker = EntityType.EVOKER.create(world, net.minecraft.entity.SpawnReason.COMMAND);
        if (evoker == null) {
            return;
        }
        evoker.refreshPositionAndAngles(pos.getX() - 1.5, pos.getY(), pos.getZ() + 1.5, 0f, 0f);
        evoker.setCustomName(Text.literal(TAG_WARLOCK).formatted(Formatting.DARK_PURPLE));
        evoker.setCustomNameVisible(true);
        evoker.setPersistent();
        evoker.setSilent(true);
        if (friendly) {
            // 善良期：不主动攻击，且不还手
            evoker.setAiDisabled(true);
            evoker.setInvulnerable(true);
        } else {
            evoker.setAiDisabled(false);
        }
        world.spawnEntity(evoker);
    }

    /** 生成末影守护者（认亲不攻击）。 */
    public static void spawnEnderGuardian(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        BlockPos pos = near(player);
        net.minecraft.entity.mob.EndermanEntity enderman = EntityType.ENDERMAN.create(world, net.minecraft.entity.SpawnReason.COMMAND);
        if (enderman == null) {
            return;
        }
        enderman.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0f, 0f);
        enderman.setCustomName(Text.literal(TAG_GUARDIAN).formatted(Formatting.DARK_AQUA));
        enderman.setCustomNameVisible(true);
        enderman.setPersistent();
        enderman.setSilent(true);
        // 末影人不攻击：给它一个无效目标并降低攻击性
        enderman.setTarget(null);
        enderman.setAiDisabled(true);
        world.spawnEntity(enderman);
    }

    /** 玩家附近的落脚点。 */
    private static BlockPos near(ServerPlayerEntity player) {
        BlockPos base = player.getBlockPos();
        return new BlockPos(base.getX(), base.getY(), base.getZ());
    }
}
