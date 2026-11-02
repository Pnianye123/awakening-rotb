package net.awakeningrotb.story;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 一个纪元的「可玩任务」判定器。
 *
 * <p>每个纪元 = 剧情文本（cutscene）+ 一个必须亲手完成的任务。
 * 任务完成才推进到下一纪元 —— 这是《方块之下》区别于纯文字 mod 的核心：
 * 玩家不是"看历史"，是"重演历史"。
 *
 * <p>实现方式：每 tick 由 {@link StoryManager} 轮询 {@link #isComplete}，
 * 因此判定必须是纯函数式的（只读世界/玩家状态），不能有副作用。
 */
@FunctionalInterface
public interface Objective {

    /**
     * @param player  当前玩家
     * @param state   该玩家的剧情进度（用于读取计数器等）
     * @return 任务是否已完成
     */
    boolean isComplete(ServerPlayerEntity player, StoryProgress progress);
}
