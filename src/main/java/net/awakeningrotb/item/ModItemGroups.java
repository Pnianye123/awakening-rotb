package net.awakeningrotb.item;

import net.awakeningrotb.AwakeningRotb;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * 自定义创造栏「方块之下」—— 把三个剧情物品集中放一起。
 * 1.21.4 API: ItemGroup.create(Row, column) 返回 Builder，链式配置。
 */
public class ModItemGroups {
    public static final ItemGroup AWAKENING = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of(AwakeningRotb.MOD_ID, "awakening"),
            ItemGroup.create(ItemGroup.Row.TOP, 0)
                    .displayName(Text.translatable("itemGroup." + AwakeningRotb.MOD_ID))
                    .icon(() -> new ItemStack(ModItems.BUILDER_ROBE))
                    .entries((context, entries) -> {
                        entries.add(ModItems.BUILDER_ROBE);
                        entries.add(ModItems.RECORDER);
                        entries.add(ModItems.RESONANCE_STONE);
                    })
                    .build()
    );

    public static void initialize() {
        // ItemGroup 在 static 字段初始化时已 register，这里只是给主 mod 一个调用点
    }
}
