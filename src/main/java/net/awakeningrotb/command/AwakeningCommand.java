package net.awakeningrotb.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.awakeningrotb.story.Era;
import net.awakeningrotb.story.StoryManager;
import net.awakeningrotb.story.StoryProgress;
import net.awakeningrotb.story.StoryState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * /awakening 命令 —— 剧情控制台。
 *
 * <p>给 Jimmy 调试用：开始剧情、跳到指定纪元、查看进度、重置。
 * 玩家自己也能用 /awakening status 查看当前任务。
 */
public class AwakeningCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("awakening")
                .then(literal("start").executes(ctx -> start(ctx)))
                .then(literal("status").executes(ctx -> status(ctx)))
                .then(literal("skip")
                        .then(argument("era", IntegerArgumentType.integer(1, Era.COUNT))
                                .executes(ctx -> skip(ctx, IntegerArgumentType.getInteger(ctx, "era")))))
                .then(literal("reset")
                        .executes(ctx -> reset(ctx)))
                .then(literal("freeplay")
                        .executes(ctx -> freeplay(ctx)))
                .then(literal("debug").executes(ctx -> debug(ctx))));
    }

    private static int start(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("§7/awakening 需要在游戏内由玩家执行。")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        ServerWorld world = player.getServerWorld();
        StoryProgress progress = StoryState.of(world).get(player);

        if (progress.started && !progress.freePlay) {
            player.sendMessage(Text.literal("§7剧情已经在进行中了。§f/awakening status §7查看当前任务。")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }

        // 清掉上一局残留的剧情 NPC / Boss（否则重复 start 会叠加生成）
        net.awakeningrotb.story.StoryBoss.clearStoryEntities(player);

        progress.reset();
        progress.started = true;
        progress.era = 0;
        progress.cutsceneLine = 0;
        progress.cutsceneDone = false;
        progress.objectiveDone = false;

        player.sendMessage(Text.literal("§6《方块之下》—— 剧情开始。").formatted(Formatting.GOLD, Formatting.BOLD), false);
        player.sendMessage(Text.literal("§7你在这个世界上醒来……但你好像已经走了很久。").formatted(Formatting.GRAY), false);

        // 发建造师长袍 + 记录仪 + 共鸣石 —— 玩家身份标识与剧情触发器
        // 先清掉旧的同类物品，避免重复 /awakening start 时叠加
        net.awakeningrotb.story.StoryRewards.clearRewardItems(player);
        net.awakeningrotb.story.StoryItems.give(player, net.awakeningrotb.item.ModItems.BUILDER_ROBE, 1);
        net.awakeningrotb.story.StoryItems.give(player, net.awakeningrotb.item.ModItems.RECORDER, 1);
        net.awakeningrotb.story.StoryItems.give(player, net.awakeningrotb.item.ModItems.RESONANCE_STONE, 1);

        Era.enterEra(0, player, progress);
        return 1;
    }

    /** 调试：报告剧情 NPC / Boss 的实际状态。 */
    private static int debug(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("§7需要在游戏内执行。").formatted(Formatting.GRAY), false);
            return 0;
        }
        ServerWorld world = player.getServerWorld();
        StoryProgress progress = StoryState.of(world).get(player);

        ctx.getSource().sendFeedback(() -> Text.literal("§6=== 剧情调试 ===").formatted(Formatting.GOLD), false);
        ctx.getSource().sendFeedback(() -> Text.literal("§7纪元：§f" + StoryManager.fullTitleOf(progress.era))
                .formatted(Formatting.GRAY), false);
        ctx.getSource().sendFeedback(() -> Text.literal("§7切scene完成：§f" + progress.cutsceneDone)
                .formatted(Formatting.GRAY), false);

        int awakener = 0, warlock = 0, sentinel = 0, witherSon = 0;
        for (net.minecraft.entity.Entity e : world.getEntitiesByClass(net.minecraft.entity.Entity.class,
                player.getBoundingBox().expand(64), en -> true)) {
            String name = e.getCustomName() == null ? "" : e.getCustomName().getString();
            if (name.contains(net.awakeningrotb.story.StoryNpc.TAG_AWAKENER)) {
                awakener++;
            } else if (name.contains(net.awakeningrotb.story.StoryNpc.TAG_WARLOCK)) {
                warlock++;
            } else if (name.contains(net.awakeningrotb.story.StoryBoss.TAG_SENTINEL)) {
                sentinel++;
            } else if (name.contains(net.awakeningrotb.story.StoryBoss.TAG_WITHER_SON)) {
                witherSon++;
            }
        }
        int fA = awakener, fW = warlock, fS = sentinel, fWither = witherSon;
        ctx.getSource().sendFeedback(() -> Text.literal("§7觉醒者：§f" + fA + " §7邪术师：§f" + fW
                + " §7坚守者：§f" + fS + " §7凋零之子：§f" + fWither).formatted(Formatting.GRAY), false);

        ctx.getSource().sendFeedback(() -> Text.literal("§7计数器：§f" + progress.counters)
                .formatted(Formatting.GRAY), false);

        // 附近的共鸣方块（第八纪净化用；玩家自己也能看到「方块放对没」）
        int nearbyBlocks = net.awakeningrotb.story.StoryEvents.countResonanceBlocks(world, player.getBlockPos());
        ctx.getSource().sendFeedback(() -> Text.literal("§7共鸣方块：§f" + nearbyBlocks)
                .formatted(Formatting.GRAY), false);

        // 背包明细（用注册表 id，不依赖客户端能否识别 mod 物品名）
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < player.getInventory().size(); i++) {
            net.minecraft.item.ItemStack st = player.getInventory().getStack(i);
            if (st.isEmpty()) {
                continue;
            }
            String id = net.minecraft.registry.Registries.ITEM.getId(st.getItem()).toString();
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(st.getCount()).append("x ").append(id);
        }
        final String inv = sb.toString();
        ctx.getSource().sendFeedback(() -> Text.literal("§7背包：§f" + (inv.isEmpty() ? "(空)" : inv))
                .formatted(Formatting.GRAY), false);
        return 0;
    }

    private static int status(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("§7/awakening 需要在游戏内由玩家执行。")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        ServerWorld world = player.getServerWorld();
        StoryProgress progress = StoryState.of(world).get(player);

        if (!progress.started) {
            player.sendMessage(Text.literal("§7剧情还没开始 —— 输入 §f/awakening start").formatted(Formatting.GRAY), false);
            return 0;
        }
        if (progress.freePlay) {
            player.sendMessage(Text.literal("§6自由段 §7—— 剧情已结束，世界是你的了。").formatted(Formatting.GOLD), false);
            return 0;
        }

        player.sendMessage(Text.literal("§6《方块之下》进度").formatted(Formatting.GOLD, Formatting.BOLD), false);
        player.sendMessage(Text.literal("§7纪元：§f" + StoryManager.fullTitleOf(progress.era))
                .formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("§7任务：§f" + StoryProgress.objectiveTextFor(progress.era))
                .formatted(Formatting.GRAY), false);
        return 0;
    }

    private static int skip(CommandContext<ServerCommandSource> ctx, int era) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("§7/awakening 需要在游戏内由玩家执行。")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        ServerWorld world = player.getServerWorld();
        StoryProgress progress = StoryState.of(world).get(player);

        int target = Math.max(0, Math.min(era - 1, Era.COUNT - 1));
        progress.started = true;
        progress.era = target;
        progress.cutsceneLine = 0;
        progress.cutsceneDone = false;
        progress.objectiveDone = false;
        progress.counter = 0;

        player.sendMessage(Text.literal("§6已跳转到：§f" + Era.cnNumber(target) + "纪").formatted(Formatting.GOLD), false);
        // 先清掉上一纪元留下的 NPC / Boss，避免重复生成
        net.awakeningrotb.story.StoryBoss.clearStoryEntities(player);
        Era.enterEra(target, player, progress);
        return 1;
    }

    private static int reset(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("§7/awakening 需要在游戏内由玩家执行。")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        ServerWorld world = player.getServerWorld();
        StoryState.of(world).get(player).reset();
        net.awakeningrotb.story.StoryBoss.clearStoryEntities(player);
        player.sendMessage(Text.literal("§7剧情进度已重置。").formatted(Formatting.GRAY), false);
        return 1;
    }

    private static int freeplay(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("§7/awakening 需要在游戏内由玩家执行。")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        ServerWorld world = player.getServerWorld();
        StoryProgress progress = StoryState.of(world).get(player);
        progress.started = true;
        progress.freePlay = true;
        progress.era = Era.COUNT - 1;
        net.awakeningrotb.story.StoryRewards.grantFreePlayRewards(player);
        player.sendMessage(Text.literal("§6已直接进入自由段。").formatted(Formatting.GOLD), false);
        return 1;
    }
}
