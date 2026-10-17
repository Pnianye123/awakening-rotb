package net.awakeningrotb.item;

import net.awakeningrotb.AwakeningRotb;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Mod item registry — v0.5 玩家专属工具（建造师工具套装）。
 *
 * 三个剧情核心物品：
 * - {@link #BUILDER_ROBE} 建造师长袍（剧情开始自动获得，玩家身份标识）
 * - {@link #RECORDER} 记录仪（剧情触发器）
 * - {@link #RESONANCE_STONE} 共鸣石（听懂尖啸体）
 *
 * v0.5 只做注册 + 基础属性，后续版本补：
 * - 建造师长袍 → ArmorItem（胸甲槽位）
 * - 记录仪 → right-click 推进 era（绑定 v0.1 datapack 的 advance_era）
 * - 共鸣石 → 在 Sculk 附近 passive 触发"听懂"事件
 */
public class ModItems {
    public static final String MOD_ID = AwakeningRotb.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID + "/items");

    public static final Item BUILDER_ROBE = register(
            "builder_robe", Item::new,
            new Item.Settings().maxCount(1)
    );

    public static final Item RECORDER = register(
            "recorder", Item::new,
            new Item.Settings().maxCount(1)
    );

    public static final Item RESONANCE_STONE = register(
            "resonance_stone", Item::new,
            new Item.Settings().maxCount(16)
    );

    public static void initialize() {
        LOGGER.info("[items] registered 3 items: builder_robe, recorder, resonance_stone");
    }

    private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
        Item item = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }
}
