package net.awakeningrotb.story;

import net.awakeningrotb.AwakeningRotb;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 剧情调度器 —— mod 的心脏。
 *
 * <p>职责：
 * <ol>
 *   <li>按 tick 推进每个玩家的 cutscene（逐行推文本）</li>
 *   <li>cutscene 播完后进入「任务阶段」，轮询 {@link Era#OBJECTIVES}</li>
 *   <li>任务完成 → 推进到下一纪元，播放下一段 cutscene</li>
 *   <li>第十四纪结束后播放终末之诗，进入自由段</li>
 * </ol>
 *
 * <p>所有状态都落在 {@link StoryState}（世界存档），掉线重连不丢进度。
 */
public final class StoryManager {

    /** 每行 cutscene 之间的间隔（tick）。20 tick = 1 秒。 */
    private static final int LINE_INTERVAL = 22;

    /** 剧情音效注册表 —— 后面 ModSounds 补齐后接上。 */
    public static final Map<Integer, net.minecraft.registry.entry.RegistryEntry<net.minecraft.sound.SoundEvent>> CUES = new HashMap<>();

    /** flag: 世界已被信标净化 */
    public static final int FLAG_PURIFIED = 0;
    /** flag: 玩家已走上不归路 */
    public static final int FLAG_NO_RETURN = 1;
    /** flag: 终末之诗已播放 */
    public static final int FLAG_POEM_DONE = 2;

    private StoryManager() {
    }

    /** 每 server tick 调用一次。 */
    public static void tick(MinecraftServer server) {        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerWorld world = player.getServerWorld();
            StoryState state = StoryState.of(world);
            StoryProgress progress = state.get(player);

            if (!progress.started) {
                continue;
            }
            if (progress.freePlay) {
                continue;
            }

            // ── cutscene 阶段：逐行推文本 ──
            if (!progress.cutsceneDone) {
                progress.counter++;
                if (progress.counter >= LINE_INTERVAL) {
                    progress.counter = 0;
                    advanceCutscene(player, progress);
                }
                continue;
            }

            // ── 任务阶段：轮询完成条件 ──
            if (!progress.objectiveDone) {
                trackSurvival(player, progress);
                Objective objective = Era.objectiveFor(progress.era);
                if (objective.isComplete(player, progress)) {
                    progress.objectiveDone = true;
                    onObjectiveComplete(player, progress);
                } else {
                    // 任务提示常驻 action bar
                    player.networkHandler.sendPacket(new OverlayMessageS2CPacket(
                            Text.literal("§e⏳ ").append(Text.literal(
                                            StoryProgress.objectiveTextFor(progress.era)).formatted(Formatting.GRAY))));
                }
            }
        }
    }

    /**
     * 持续性任务计数：需要"待够时间"才能完成的任务。
     * <ul>
     *   <li>第五纪：在凋零之子的威胁下活过 30 秒</li>
     *   <li>第六纪：躲进地下累计 60 tick</li>
     *   <li>第十纪：在要塞深处（Y&lt;=-40）累计 100 tick</li>
     * </ul>
     */
    private static void trackSurvival(ServerPlayerEntity player, StoryProgress progress) {
        double y = player.getY();
        switch (progress.era) {
            // 第五纪 · 凋零之罪 —— 只要还活着就在计时
            case 4 -> {
                if (player.isAlive()) {
                    progress.increment("survive_wither");
                }
            }
            // 第六纪 · 凋零病毒 —— 躲进地下（Y < 0）
            case 5 -> {
                if (y < 0) {
                    progress.increment("hide_underground");
                }
            }
            // 第十纪 · 再次躲避 —— 要塞深处（Y <= -40）
            case 9 -> {
                if (y <= -40) {
                    progress.increment("hide_fortress");
                }
            }
            default -> {
            }
        }
    }

    /** 推进 cutscene 文本行。 */
    private static void advanceCutscene(ServerPlayerEntity player, StoryProgress progress) {
        String[] lines = StoryProgress.linesFor(progress.era);
        String[] colors = StoryProgress.colorsFor(progress.era);

        if (progress.cutsceneLine == 0) {
            // 纪元标题卡
            showTitle(player,
                    Text.literal(Era.cnNumber(progress.era) + "纪").formatted(Formatting.GOLD, Formatting.BOLD),
                    Text.literal(subtitleOf(progress.era)).formatted(Formatting.YELLOW));
            player.playSound(net.minecraft.sound.SoundEvents.BLOCK_BEACON_ACTIVATE, 0.7f, 1.4f);
        }

        if (progress.cutsceneLine >= lines.length) {
            // cutscene 结束 → 进入任务阶段
            progress.cutsceneDone = true;
            progress.counter = 0;
            showObjectiveTitle(player, progress);
            return;
        }

        String line = lines[progress.cutsceneLine];
        String color = progress.cutsceneLine < colors.length ? colors[progress.cutsceneLine] : "white";
        player.sendMessage(Text.literal("§" + colorCode(color) + line), false);
        progress.cutsceneLine++;
    }

    /** 任务完成 → 推进纪元。 */
    private static void onObjectiveComplete(ServerPlayerEntity player, StoryProgress progress) {
        player.playSound(net.minecraft.sound.SoundEvents.ENTITY_PLAYER_LEVELUP, 0.6f, 1.2f);
        player.sendMessage(Text.literal("§a✔ 任务完成").formatted(Formatting.GREEN, Formatting.BOLD), false);

        if (progress.era >= Era.COUNT - 1) {
            // 最后一纪完成 → 终末之诗 → 自由段
            progress.setFlag(FLAG_POEM_DONE);
            progress.freePlay = true;
            playEnding(player, progress);
            StoryRewards.grantFreePlayRewards(player);
            return;
        }

        progress.era++;
        progress.cutsceneLine = 0;
        progress.cutsceneDone = false;
        progress.objectiveDone = false;
        progress.counter = 0;

        // 进入新纪元的世界布置（先清掉上一纪元的 NPC / Boss）
        StoryBoss.clearStoryEntities(player);
        Era.enterEra(progress.era, player, progress);
    }

    /** 终末之诗 —— 唯一结局。 */
    private static void playEnding(ServerPlayerEntity player, StoryProgress progress) {
        showTitle(player,
                Text.literal("终末之诗").formatted(Formatting.DARK_PURPLE, Formatting.BOLD),
                Text.literal("The Final Poem").formatted(Formatting.LIGHT_PURPLE));

        String[] lines = StoryText.ENDING;
        String[] colors = StoryText.ENDING_COLOR;
        for (int i = 0; i < lines.length; i++) {
            String color = i < colors.length ? colors[i] : "white";
            player.sendMessage(Text.literal("§" + colorCode(color) + lines[i]), false);
        }
    }

    /** 任务阶段开始时给一个提示标题。 */
    private static void showObjectiveTitle(ServerPlayerEntity player, StoryProgress progress) {
        showTitle(player,
                Text.literal("⛏ " + Era.cnNumber(progress.era) + "纪 · 任务").formatted(Formatting.YELLOW, Formatting.BOLD),
                Text.literal(StoryProgress.objectiveTextFor(progress.era)).formatted(Formatting.GRAY));
    }

    /** 发送标题 + 副标题。 */
    public static void showTitle(ServerPlayerEntity player, Text title, Text subtitle) {
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 60, 15));
        player.networkHandler.sendPacket(new TitleS2CPacket(title));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
    }

    /** 清除标题。 */
    public static void clearTitle(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new ClearTitleS2CPacket(true));
    }

    /** 纪元副标题（共生 / 下界远征 ...）—— 去掉"第N纪 · "前缀，只留名字。 */
    public static String subtitleOf(int era) {
        String title = StoryProgress.titleFor(era);
        int idx = title.indexOf("·");
        if (idx >= 0) {
            return title.substring(idx + 1).trim();
        }
        return title;
    }

    /** 纪元完整标题（第一纪 · 共生），用于进度显示。 */
    public static String fullTitleOf(int era) {
        return Era.cnNumber(era) + "纪 · " + subtitleOf(era);
    }

    /** MC 颜色名 → 格式代码。 */
    public static String colorCode(String name) {
        return switch (name) {
            case "gold" -> "6";
            case "yellow" -> "e";
            case "gray" -> "7";
            case "dark_gray" -> "8";
            case "white" -> "f";
            case "red" -> "c";
            case "dark_red" -> "4";
            case "green" -> "a";
            case "dark_green" -> "2";
            case "aqua" -> "b";
            case "dark_aqua" -> "3";
            case "blue" -> "9";
            case "dark_blue" -> "1";
            case "light_purple" -> "d";
            case "dark_purple" -> "5";
            default -> "f";
        };
    }
}
