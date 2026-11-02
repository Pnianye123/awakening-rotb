package net.awakeningrotb.story;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 剧情物品发放小工具。 */
public final class StoryItems {

    private StoryItems() {
    }

    /** 给玩家 N 个物品。 */
    public static void give(ServerPlayerEntity player, Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        player.getInventory().offerOrDrop(stack);
    }

    /** 给玩家一个带自定义名字的物品。 */
    public static void giveNamed(ServerPlayerEntity player, Item item, int count, String name) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, net.minecraft.text.Text.literal(name));
        player.getInventory().offerOrDrop(stack);
    }

    /** 扣除玩家背包里的 N 个物品，返回是否扣够。 */
    public static boolean consume(ServerPlayerEntity player, Item item, int count) {
        int have = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                have += stack.getCount();
            }
        }
        if (have < count) {
            return false;
        }
        int left = count;
        for (int i = 0; i < player.getInventory().size() && left > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isOf(item)) {
                continue;
            }
            int take = Math.min(left, stack.getCount());
            left -= take;
            if (stack.getCount() - take <= 0) {
                player.getInventory().setStack(i, ItemStack.EMPTY);
            } else {
                stack.decrement(take);
            }
        }
        return true;
    }
}
