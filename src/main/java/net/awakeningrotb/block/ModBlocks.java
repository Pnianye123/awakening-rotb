package net.awakeningrotb.block;

import net.awakeningrotb.AwakeningRotb;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mod 方块注册表 —— 剧情段方块。
 *
 * <p>对应设计稿第四章「剧情段方块」：
 * 古城共鸣方块、觉醒者图腾、建造师石砖、凋零裂隙、Reinforced Echo Stone、灵魂灯。
 *
 * <p><b>注册顺序</b>：方块本体在这里注册，BlockItem 由 {@code ModItems}
 * 从 {@link #BLOCKS} 取 —— 这样避免方块 ↔ 物品互相依赖造成的初始化死锁。
 */
public class ModBlocks {

    /** 方块 id → Block 实例。ModItems 用它创建 BlockItem。 */
    public static final Map<String, Block> BLOCKS = new LinkedHashMap<>();

    // ── 古城共鸣方块：第八纪信标净化的载体 ──
    public static final Block RESONANCE_BLOCK = register(
            "resonance_block",
            AbstractBlock.Settings.create()
                    .strength(3.0f, 6.0f)
                    .sounds(BlockSoundGroup.STONE)
                    .luminance(state -> 5));

    // ── 觉醒者图腾 ──
    public static final Block AWAKENED_TOTEM = register(
            "awakened_totem",
            AbstractBlock.Settings.create()
                    .strength(4.0f, 8.0f)
                    .sounds(BlockSoundGroup.STONE)
                    .luminance(state -> 8));

    // ── 建造师石砖 ──
    public static final Block BUILDER_BRICK = register(
            "builder_brick",
            AbstractBlock.Settings.create()
                    .strength(2.5f, 6.0f)
                    .sounds(BlockSoundGroup.STONE));

    // ── 凋零裂隙 ──
    public static final Block WITHER_RIFT = register(
            "wither_rift",
            AbstractBlock.Settings.create()
                    .strength(-1.0f, 3600000.0f)
                    .noCollision()
                    .nonOpaque()
                    .sounds(BlockSoundGroup.WOOL)
                    .luminance(state -> 2));

    // ── 强化回响石 ──
    public static final Block REINFORCED_ECHO_STONE = register(
            "reinforced_echo_stone",
            AbstractBlock.Settings.create()
                    .strength(8.0f, 12.0f)
                    .sounds(BlockSoundGroup.DEEPSLATE)
                    .luminance(state -> 3));

    // ── 灵魂灯 ──
    public static final Block SOUL_LANTERN = register(
            "soul_lantern",
            AbstractBlock.Settings.create()
                    .strength(1.0f)
                    .sounds(BlockSoundGroup.LANTERN)
                    .luminance(state -> 14));

    // ── 建造师长袍方块（长袍展示方块） ──
    public static final Block BUILDER_ROBE_BLOCK = register(
            "builder_robe_block",
            AbstractBlock.Settings.create()
                    .strength(1.0f)
                    .sounds(BlockSoundGroup.WOOL));

    public static void initialize() {
        // 全部在静态字段初始化时注册
    }

    private static Block register(String name, AbstractBlock.Settings settings) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(AwakeningRotb.MOD_ID, name));
        Block block = new Block(settings.registryKey(key));
        Registry.register(Registries.BLOCK, key, block);
        BLOCKS.put(name, block);
        return block;
    }
}
