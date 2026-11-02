package net.awakeningrotb.story;

import net.awakeningrotb.item.ModItems;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * 自由段奖励 —— 终末之诗播放完毕后解锁。
 *
 * <p>对应设计稿第三章：剧情结束后玩家变成正常 Minecraft 玩家，
 * 但保留这些奖励（物品 + 特殊皮肤 + 3 张唱片）。
 */
public final class StoryRewards {

    private StoryRewards() {
    }

    /** 发放自由段全部奖励。 */
    public static void grantFreePlayRewards(ServerPlayerEntity player) {
        // 先清掉同名旧物品，避免重复发放（比如玩家已拿过又重进自由段）
        clearRewardItems(player);

        // 物品
        StoryItems.give(player, ModItems.BUILDER_ROBE, 1);
        StoryItems.give(player, ModItems.RESONANCE_STONE, 1);
        StoryItems.give(player, ModItems.SOUL_COMPASS, 1);
        StoryItems.give(player, ModItems.ANCIENT_CITY_PASS, 1);
        StoryItems.give(player, ModItems.WITHER_CORE, 1);
        StoryItems.give(player, ModItems.AWAKENED_TOKEN, 1);
        StoryItems.give(player, ModItems.ANCIENT_CITY_KEY, 1);
        StoryItems.give(player, ModItems.RECORD_A, 1);
        StoryItems.give(player, ModItems.RECORD_B, 1);
        StoryItems.give(player, ModItems.RECORD_C, 1);

        player.sendMessage(Text.literal("§6✦ 终末之诗已奏响 —— 你获得了自由段奖励").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("§7建造师长袍 · 共鸣石 · 灵魂罗盘 · 古城通行证 · 凋零核心 · 觉醒者信物 · 古城钥匙 · 古老唱片 A/B/C")
                .formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("§7这个世界从此是你的了 —— 拆古城、杀村民、退出游戏、玩创造模式，什么都行。")
                .formatted(Formatting.DARK_GRAY), false);
        player.playSound(net.minecraft.sound.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    /** 清掉已有的剧情物品（避免重复发放）。 */
    public static void clearRewardItems(ServerPlayerEntity player) {
        net.minecraft.item.Item[] rewards = {
                ModItems.BUILDER_ROBE, ModItems.RESONANCE_STONE, ModItems.SOUL_COMPASS,
                ModItems.ANCIENT_CITY_PASS, ModItems.WITHER_CORE, ModItems.AWAKENED_TOKEN,
                ModItems.ANCIENT_CITY_KEY, ModItems.RECORD_A, ModItems.RECORD_B, ModItems.RECORD_C,
                ModItems.RECORDER, ModItems.SOUL_SHARD, ModItems.ECHO_SHARD
        };
        for (int i = 0; i < player.getInventory().size(); i++) {
            net.minecraft.item.ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            for (net.minecraft.item.Item reward : rewards) {
                if (stack.isOf(reward)) {
                    player.getInventory().setStack(i, net.minecraft.item.ItemStack.EMPTY);
                    break;
                }
            }
        }
    }
}
