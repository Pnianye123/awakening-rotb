package net.awakeningrotb.item;

import net.awakeningrotb.story.StoryManager;
import net.awakeningrotb.story.StoryProgress;
import net.awakeningrotb.story.StoryState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

/**
 * 剧情物品的交互行为。
 *
 * <p>设计稿里这些物品不只是"战利品"，各有实际用途：
 * <ul>
 *   <li><b>记录仪</b> —— 右击推进到下一纪元（剧情触发器）</li>
 *   <li><b>共鸣石</b> —— 在尖啸体（sculk）附近会发烫，帮你"听懂"它</li>
 *   <li><b>灵魂罗盘</b> —— 指向当前纪元的任务目标方向</li>
 *   <li><b>古城钥匙 / 通行证</b> —— 远古古城的钥匙（自由段可用）</li>
 * </ul>
 */
public class ModItemBehaviors {

    private ModItemBehaviors() {
    }

    /** 记录仪：右击推进纪元。 */
    public static ActionResult useRecorder(World world, PlayerEntity player) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) {
            return ActionResult.PASS;
        }
        ServerWorld sw = (ServerWorld) world;
        StoryProgress progress = StoryState.of(sw).get(sp);

        if (!progress.started) {
            sp.sendMessage(Text.literal("§7记录仪一片死寂 —— 剧情还没开始。").formatted(Formatting.GRAY), false);
            return ActionResult.PASS;
        }
        if (progress.freePlay) {
            sp.sendMessage(Text.literal("§7记录仪的指针不再指向任何地方。").formatted(Formatting.GRAY), false);
            return ActionResult.PASS;
        }
        if (!progress.cutsceneDone) {
            sp.sendMessage(Text.literal("§7记录仪正在记录 —— 等这段历史讲完。").formatted(Formatting.GRAY), false);
            return ActionResult.PASS;
        }
        if (progress.objectiveDone) {
            sp.sendMessage(Text.literal("§7这一纪已经记下了 —— 任务还没完成呢。").formatted(Formatting.GRAY), false);
            return ActionResult.PASS;
        }

        // 强制完成当前任务 → 推进纪元
        progress.objectiveDone = true;
        sp.sendMessage(Text.literal("§e记录仪翻到了下一页。").formatted(Formatting.YELLOW), false);
        return ActionResult.SUCCESS;
    }

    /** 共鸣石：在潜影之声（sculk）附近发烫。 */
    public static ActionResult useResonanceStone(World world, PlayerEntity player) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) {
            return ActionResult.PASS;
        }
        int sculk = 0;
        BlockPosHelper helper = new BlockPosHelper();
        for (int r = 1; r <= 6 && sculk == 0; r++) {
            for (int dx = -r; dx <= r && sculk == 0; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        var pos = player.getBlockPos().add(dx, dy, dz);
                        var state = world.getBlockState(pos);
                        String id = net.minecraft.registry.Registries.BLOCK.getId(state.getBlock()).toString();
                        if (id.contains("sculk") || id.contains("shrieker") || id.contains("sensor")) {
                            sculk++;
                            helper.set(pos);
                            break;
                        }
                    }
                    if (sculk > 0) break;
                }
                if (sculk > 0) break;
            }
        }

        if (sculk > 0) {
            sp.sendMessage(Text.literal("§b共鸣石开始发热 —— 尖啸体就在附近。")
                    .formatted(Formatting.AQUA), false);
            sp.sendMessage(Text.literal("§7你听懂了：那是死去的同伴在用震动说话。")
                    .formatted(Formatting.GRAY), false);
        } else {
            sp.sendMessage(Text.literal("§8共鸣石冰凉 —— 附近没有尖啸体。").formatted(Formatting.DARK_GRAY), false);
        }
        return ActionResult.SUCCESS;
    }

    /** 灵魂罗盘：提示当前任务。 */
    public static ActionResult useSoulCompass(World world, PlayerEntity player) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) {
            return ActionResult.PASS;
        }
        ServerWorld sw = (ServerWorld) world;
        StoryProgress progress = StoryState.of(sw).get(sp);

        if (!progress.started || progress.freePlay) {
            sp.sendMessage(Text.literal("§7罗盘的指针缓缓转了一圈，然后停住了。")
                    .formatted(Formatting.GRAY), false);
            return ActionResult.SUCCESS;
        }
        sp.sendMessage(Text.literal("§6灵魂罗盘指向：").formatted(Formatting.GOLD), false);
        sp.sendMessage(Text.literal("§f" + StoryProgress.objectiveTextFor(progress.era))
                .formatted(Formatting.WHITE), false);
        return ActionResult.SUCCESS;
    }

    /** 古城钥匙/通行证：提示远古古城的相关信息。 */
    public static ActionResult useAncientRelic(World world, PlayerEntity player) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) {
            return ActionResult.PASS;
        }
        sp.sendMessage(Text.literal("§6它属于远古建造师。").formatted(Formatting.GOLD), false);
        sp.sendMessage(Text.literal("§7考古学家在主世界地下能找到他们留下的城市 —— 规则化的方形大厅，柱子上嵌着共鸣石。")
                .formatted(Formatting.GRAY), false);
        return ActionResult.SUCCESS;
    }

    /** 简单的位置持有者（避免一堆 out 变量）。 */
    static class BlockPosHelper {
        net.minecraft.util.math.BlockPos pos;

        void set(net.minecraft.util.math.BlockPos p) {
            this.pos = p;
        }
    }
}
