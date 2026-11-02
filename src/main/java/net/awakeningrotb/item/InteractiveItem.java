package net.awakeningrotb.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.function.BiFunction;

/**
 * 带交互行为的剧情物品。
 *
 * <p>用来把 {@link ModItemBehaviors} 里的逻辑挂到具体物品上，
 * 避免为每个物品都写一个 Item 子类。
 */
public class InteractiveItem extends Item {

    /** 右击时的行为：返回 CONSUME 表示处理完毕。 */
    private final BiFunction<World, PlayerEntity, ActionResult> onUse;

    public InteractiveItem(Settings settings, BiFunction<World, PlayerEntity, ActionResult> onUse) {
        super(settings);
        this.onUse = onUse;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ActionResult result = onUse.apply(world, player);
        return result == ActionResult.PASS ? ActionResult.SUCCESS : result;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        // 记录仪微微发光 —— 提示它能推进剧情
        return false;
    }
}
