package net.awakeningrotb.item;

import net.awakeningrotb.story.Loc;
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
            sp.sendMessage(Loc.msg(sp, "msg.recorder.idle").formatted(Formatting.GRAY), false);
            return ActionResult.PASS;
        }
        if (progress.freePlay) {
            sp.sendMessage(Loc.msg(sp, "msg.recorder.dead").formatted(Formatting.GRAY), false);
            return ActionResult.PASS;
        }
        if (!progress.cutsceneDone) {
            sp.sendMessage(Loc.msg(sp, "msg.recorder.recording").formatted(Formatting.GRAY), false);
            return ActionResult.PASS;
        }
        if (progress.objectiveDone) {
            sp.sendMessage(Loc.msg(sp, "msg.recorder.done").formatted(Formatting.GRAY), false);
            return ActionResult.PASS;
        }

        // 强制完成当前任务 → 推进纪元
        progress.objectiveDone = true;
        sp.sendMessage(Loc.msg(sp, "msg.recorder.next").formatted(Formatting.YELLOW), false);
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
            sp.sendMessage(Loc.msg(sp, "msg.stone.hot")
                    .formatted(Formatting.AQUA), false);
            sp.sendMessage(Loc.msg(sp, "msg.stone.heard")
                    .formatted(Formatting.GRAY), false);
        } else {
            sp.sendMessage(Loc.msg(sp, "msg.stone.cold").formatted(Formatting.DARK_GRAY), false);
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

        String lang = Loc.langOf(sp);
        if (!progress.started || progress.freePlay) {
            sp.sendMessage(Loc.msg(sp, "msg.compass.stopped")
                    .formatted(Formatting.GRAY), false);
            return ActionResult.SUCCESS;
        }
        sp.sendMessage(Loc.msg(sp, "msg.compass.points").formatted(Formatting.GOLD), false);
        sp.sendMessage(Text.literal(Loc.objective(lang, progress.era))
                .formatted(Formatting.WHITE), false);
        return ActionResult.SUCCESS;
    }

    /** 古城钥匙/通行证：提示远古古城的相关信息。 */
    public static ActionResult useAncientRelic(World world, PlayerEntity player) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) {
            return ActionResult.PASS;
        }
        sp.sendMessage(Loc.msg(sp, "msg.relic.owner").formatted(Formatting.GOLD), false);
        sp.sendMessage(Loc.msg(sp, "msg.relic.city")
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
