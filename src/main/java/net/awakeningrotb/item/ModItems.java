package net.awakeningrotb.item;

import net.awakeningrotb.AwakeningRotb;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Function;

/**
 * Mod item registry —— 剧情核心物品。
 *
 * <p>清单来自设计稿第四章「剧情段需要的物品」：
 * 建造师长袍、记录仪、共鸣石、灵魂罗盘、古城通行证、凋零核心、
 * 古城钥匙、古老唱片 A/B/C、末影珍珠碎片。
 * 外加剧情任务需要的：灵魂碎片、觉醒者信物。
 */
public class ModItems {

    public static final String MOD_ID = AwakeningRotb.MOD_ID;

    // ── 建造师长袍：玩家身份标识（可装备的胸甲） ──
    public static final ArmorMaterial BUILDER_ROBE_MATERIAL = new ArmorMaterial(
            20,                                              // durability
            Map.of(EquipmentType.CHESTPLATE, 3),              // defense
            15,                                              // enchantmentValue
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
            0.0f,                                            // toughness
            0.0f,                                            // knockbackResistance
            TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "repairable")),
            RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Identifier.of(MOD_ID, "builder_robe"))
    );

    public static final ArmorItem BUILDER_ROBE = register(
            "builder_robe",
            settings -> new ArmorItem(BUILDER_ROBE_MATERIAL, EquipmentType.CHESTPLATE, settings),
            new Item.Settings().maxCount(1).fireproof()
    );

    // ── 记录仪：剧情推进器（右击推进纪元） ──
    public static final Item RECORDER = register(
            "recorder",
            settings -> new InteractiveItem(settings, ModItemBehaviors::useRecorder),
            new Item.Settings().maxCount(1));

    // ── 共鸣石：听懂尖啸体 ──
    public static final Item RESONANCE_STONE = register(
            "resonance_stone",
            settings -> new InteractiveItem(settings, ModItemBehaviors::useResonanceStone),
            new Item.Settings().maxCount(16));

    // ── 灵魂罗盘：指向当前任务 ──
    public static final Item SOUL_COMPASS = register(
            "soul_compass",
            settings -> new InteractiveItem(settings, ModItemBehaviors::useSoulCompass),
            new Item.Settings().maxCount(1));

    // ── 古城通行证 ──
    public static final Item ANCIENT_CITY_PASS = register(
            "ancient_city_pass",
            settings -> new InteractiveItem(settings, ModItemBehaviors::useAncientRelic),
            new Item.Settings().maxCount(1));

    // ── 凋零核心 ──
    public static final Item WITHER_CORE = register(
            "wither_core", Item::new, new Item.Settings().maxCount(16));

    // ── 古城钥匙 ──
    public static final Item ANCIENT_CITY_KEY = register(
            "ancient_city_key",
            settings -> new InteractiveItem(settings, ModItemBehaviors::useAncientRelic),
            new Item.Settings().maxCount(1));

    // ── 觉醒者信物 ──
    public static final Item AWAKENED_TOKEN = register(
            "awakened_token", Item::new, new Item.Settings().maxCount(1));

    // ── 灵魂碎片（第四纪/第七纪任务物） ──
    public static final Item SOUL_SHARD = register(
            "soul_shard", Item::new, new Item.Settings().maxCount(64));

    // ── 末影珍珠碎片（第十四纪） ──
    public static final Item ECHO_SHARD = register(
            "echo_shard", Item::new, new Item.Settings().maxCount(64));

    // ── 古老唱片 A/B/C（可放进点唱机播放） ──
    public static final Item RECORD_A = register(
            "record_a", Item::new,
            new Item.Settings().maxCount(1)
                    .jukeboxPlayable(RegistryKey.of(RegistryKeys.JUKEBOX_SONG,
                            Identifier.of(MOD_ID, "record_a"))));
    public static final Item RECORD_B = register(
            "record_b", Item::new,
            new Item.Settings().maxCount(1)
                    .jukeboxPlayable(RegistryKey.of(RegistryKeys.JUKEBOX_SONG,
                            Identifier.of(MOD_ID, "record_b"))));
    public static final Item RECORD_C = register(
            "record_c", Item::new,
            new Item.Settings().maxCount(1)
                    .jukeboxPlayable(RegistryKey.of(RegistryKeys.JUKEBOX_SONG,
                            Identifier.of(MOD_ID, "record_c"))));

    // ── 方块的 BlockItem（id 与方块同名，MC 惯例） ──
    public static final Item RESONANCE_BLOCK_ITEM = registerBlockItem("resonance_block");
    public static final Item AWAKENED_TOTEM_ITEM = registerBlockItem("awakened_totem");
    public static final Item BUILDER_BRICK_ITEM = registerBlockItem("builder_brick");
    public static final Item WITHER_RIFT_ITEM = registerBlockItem("wither_rift");
    public static final Item REINFORCED_ECHO_STONE_ITEM = registerBlockItem("reinforced_echo_stone");
    public static final Item SOUL_LANTERN_ITEM = registerBlockItem("soul_lantern");
    public static final Item BUILDER_ROBE_BLOCK_ITEM = registerBlockItem("builder_robe_block");

    public static void initialize() {
        // 全部为静态字段初始化，register 在类加载时完成
    }

    private static <T extends Item> T register(
            String name, Function<Item.Settings, T> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
        T item = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    /** 方块的 BlockItem —— 在 ModBlocks 里把方块本体挂上去。 */
    private static Item registerBlockItem(String name) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
        Item.Settings settings = new Item.Settings().registryKey(key);
        BlockItem item = new BlockItem(net.awakeningrotb.block.ModBlocks.BLOCKS.get(name), settings);
        return Registry.register(Registries.ITEM, key, item);
    }
}
