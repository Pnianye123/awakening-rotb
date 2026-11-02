package net.awakeningrotb.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.awakeningrotb.story.Era;
import net.awakeningrotb.story.Loc;
import net.awakeningrotb.story.PrologueBook;
import net.awakeningrotb.story.StoryBoss;
import net.awakeningrotb.story.StoryEvents;
import net.awakeningrotb.story.StoryItems;
import net.awakeningrotb.story.StoryManager;
import net.awakeningrotb.story.StoryProgress;
import net.awakeningrotb.story.StoryRewards;
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
 * 命令注册 —— {@code /start} 与 {@code /awakening ...}。
 *
 * <p><b>/start</b>：玩家唯一需要记的命令。开局拿到《前情提要》后输入它，
 * 剧情才正式开始。（/awakening start 仍然保留，等价，方便调试。）
 *
 * <p><b>/awakening</b>：调试面板 —— 跳纪元、看进度、改语言、重置。
 */
public class AwakeningCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // ── /start —— 正式开始剧情 ──
        dispatcher.register(literal("start").executes(ctx -> start(ctx)));

        // ── /awakening —— 调试面板 ──
        dispatcher.register(literal("awakening")
                .then(literal("start").executes(ctx -> start(ctx)))
                .then(literal("status").executes(ctx -> status(ctx)))
                .then(literal("book").executes(ctx -> book(ctx)))
                .then(literal("lang")
                        .then(literal("zh").executes(ctx -> lang(ctx, Loc.ZH)))
                        .then(literal("en").executes(ctx -> lang(ctx, Loc.EN)))
                        .then(literal("auto").executes(ctx -> lang(ctx, ""))))
                .then(literal("skip")
                        .then(argument("era", IntegerArgumentType.integer(1, Era.COUNT))
                                .executes(ctx -> skip(ctx, IntegerArgumentType.getInteger(ctx, "era")))))
                .then(literal("reset")
                        .executes(ctx -> reset(ctx)))
                .then(literal("freeplay")
                        .executes(ctx -> freeplay(ctx)))
                .then(literal("debug").executes(ctx -> debug(ctx))));
    }

    /**
     * 开始剧情 —— {@code /start} 与 {@code /awakening start} 共用。
     *
     * <p>这是玩家侧的正式入口：先发《前情提要》，再进入第一纪。
     */
    private static int start(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Loc.text(Loc.ZH, "msg.start.not_player")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        ServerWorld world = player.getServerWorld();
        StoryProgress progress = StoryState.of(world).get(player);

        if (progress.started && !progress.freePlay) {
            player.sendMessage(Loc.msg(player, "msg.start.already").formatted(Formatting.GRAY), false);
            return 0;
        }

        // 清掉上一局残留的剧情 NPC / Boss（否则重复 start 会叠加生成）
        StoryBoss.clearStoryEntities(player);

        progress.reset();
        progress.started = true;
        progress.era = 0;
        progress.cutsceneLine = 0;
        progress.cutsceneDone = false;
        progress.objectiveDone = false;

        player.sendMessage(Loc.msg(player, "msg.start.done").formatted(Formatting.GOLD, Formatting.BOLD), false);
        player.sendMessage(Loc.msg(player, "msg.start.wake").formatted(Formatting.GRAY), false);

        // 发建造师长袍 + 记录仪 + 共鸣石 —— 玩家身份标识与剧情触发器
        // 先清掉旧的同类物品，避免重复 start 时叠加
        StoryRewards.clearRewardItems(player);
        StoryItems.give(player, net.awakeningrotb.item.ModItems.BUILDER_ROBE, 1);
        StoryItems.give(player, net.awakeningrotb.item.ModItems.RECORDER, 1);
        StoryItems.give(player, net.awakeningrotb.item.ModItems.RESONANCE_STONE, 1);

        Era.enterEra(0, player, progress);
        return 1;
    }

    /** 再要一本《前情提要》。 */
    private static int book(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Loc.text(Loc.ZH, "msg.start.not_player")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        PrologueBook.give(player);
        player.sendMessage(Loc.msg(player, "msg.start.book").formatted(Formatting.GOLD), false);
        return 1;
    }

    /** 切换语言：zh / en / auto（跟随客户端）。 */
    private static int lang(CommandContext<ServerCommandSource> ctx, String lang) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Loc.text(Loc.ZH, "msg.start.not_player")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        Loc.setLang(player, lang);
        String label = lang.isEmpty() ? "auto" : lang;
        player.sendMessage(Loc.msg(player, "msg.lang.set", label).formatted(Formatting.GRAY), false);
        return 1;
    }

    /** 调试：报告剧情 NPC / Boss 的实际状态。 */
    private static int debug(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Loc.text(Loc.ZH, "msg.start.not_player")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        ServerWorld world = player.getServerWorld();
        StoryProgress progress = StoryState.of(world).get(player);
        String lang = Loc.langOf(player);

        ctx.getSource().sendFeedback(() -> Text.literal("§6=== 剧情调试 ===").formatted(Formatting.GOLD), false);
        ctx.getSource().sendFeedback(() -> Text.literal("§7纪元：§f" + StoryManager.fullTitleOf(lang, progress.era))
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
        int nearbyBlocks = StoryEvents.countResonanceBlocks(world, player.getBlockPos());
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
            ctx.getSource().sendFeedback(() -> Loc.text(Loc.ZH, "msg.start.not_player")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        ServerWorld world = player.getServerWorld();
        StoryProgress progress = StoryState.of(world).get(player);
        String lang = Loc.langOf(player);

        if (!progress.started) {
            player.sendMessage(Loc.msg(player, "msg.not_started").formatted(Formatting.GRAY), false);
            return 0;
        }
        if (progress.freePlay) {
            player.sendMessage(Loc.msg(player, "msg.freeplay.title").formatted(Formatting.GOLD), false);
            return 0;
        }

        player.sendMessage(Loc.msg(player, "msg.progress.title").formatted(Formatting.GOLD, Formatting.BOLD), false);
        player.sendMessage(Text.literal(Loc.str(lang, "msg.era") + StoryManager.fullTitleOf(lang, progress.era))
                .formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal(Loc.str(lang, "msg.task") + Loc.objective(lang, progress.era))
                .formatted(Formatting.GRAY), false);
        return 0;
    }

    private static int skip(CommandContext<ServerCommandSource> ctx, int era) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Loc.text(Loc.ZH, "msg.start.not_player")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        ServerWorld world = player.getServerWorld();
        StoryProgress progress = StoryState.of(world).get(player);
        String lang = Loc.langOf(player);

        int target = Math.max(0, Math.min(era - 1, Era.COUNT - 1));
        progress.started = true;
        progress.era = target;
        progress.cutsceneLine = 0;
        progress.cutsceneDone = false;
        progress.objectiveDone = false;
        progress.counter = 0;

        player.sendMessage(Text.literal(Loc.str(lang, "msg.skipped") + Loc.eraNumber(lang, target))
                .formatted(Formatting.GOLD), false);
        // 先清掉上一纪元留下的 NPC / Boss，避免重复生成
        StoryBoss.clearStoryEntities(player);
        Era.enterEra(target, player, progress);
        return 1;
    }

    private static int reset(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Loc.text(Loc.ZH, "msg.start.not_player")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        StoryState.of(player.getServerWorld()).get(player).reset();
        StoryBoss.clearStoryEntities(player);
        // 重置也要把剧情物品收回去 —— 否则旧的长袍/唱片/方块会残留在背包里
        StoryRewards.clearRewardItems(player);
        player.sendMessage(Loc.msg(player, "msg.reset").formatted(Formatting.GRAY), false);
        return 1;
    }

    private static int freeplay(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Loc.text(Loc.ZH, "msg.start.not_player")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        StoryProgress progress = StoryState.of(player.getServerWorld()).get(player);
        progress.started = true;
        progress.freePlay = true;
        progress.era = Era.COUNT - 1;
        StoryRewards.grantFreePlayRewards(player);
        player.sendMessage(Loc.msg(player, "msg.freeplay").formatted(Formatting.GOLD), false);
        return 1;
    }
}
