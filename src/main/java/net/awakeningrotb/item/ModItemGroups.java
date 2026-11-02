package net.awakeningrotb.item;

import net.awakeningrotb.AwakeningRotb;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * 自定义创造栏「方块之下」—— 把剧情物品集中放一起。
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
                        // 剧情核心物品
                        entries.add(ModItems.BUILDER_ROBE);
                        entries.add(ModItems.RECORDER);
                        entries.add(ModItems.RESONANCE_STONE);
                        entries.add(ModItems.SOUL_COMPASS);
                        entries.add(ModItems.ANCIENT_CITY_PASS);
                        entries.add(ModItems.ANCIENT_CITY_KEY);
                        entries.add(ModItems.AWAKENED_TOKEN);
                        entries.add(ModItems.WITHER_CORE);
                        entries.add(ModItems.SOUL_SHARD);
                        entries.add(ModItems.ECHO_SHARD);
                        // 古老唱片
                        entries.add(ModItems.RECORD_A);
                        entries.add(ModItems.RECORD_B);
                        entries.add(ModItems.RECORD_C);
                        // 剧情方块
                        entries.add(ModItems.RESONANCE_BLOCK_ITEM);
                        entries.add(ModItems.AWAKENED_TOTEM_ITEM);
                        entries.add(ModItems.BUILDER_BRICK_ITEM);
                        entries.add(ModItems.WITHER_RIFT_ITEM);
                        entries.add(ModItems.REINFORCED_ECHO_STONE_ITEM);
                        entries.add(ModItems.SOUL_LANTERN_ITEM);
                        entries.add(ModItems.BUILDER_ROBE_BLOCK_ITEM);
                    })
                    .build()
    );

    public static void initialize() {
        // ItemGroup 在 static 字段初始化时已 register
    }
}
