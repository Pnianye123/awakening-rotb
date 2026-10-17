package net.awakeningrotb;

import net.awakeningrotb.item.ModItemGroups;
import net.awakeningrotb.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Awakening: Return of the Builder
 *
 * v0.5 骨架入口。当前已就位：
 * - Fabric 项目骨架（gradle 9.0.0, MC 1.21.4, fabric-loom 1.8.13）
 * - 三个剧情物品注册（建造师长袍、记录仪、共鸣石）
 * - 自定义创造栏「方块之下」
 * - lang en_us / zh_cn
 *
 * 后续版本补：
 * - 14 段 cutscene 调度
 * - 古城结构生成
 * - 坚守者 Boss (The Echo Sentinel)
 * - 终末之诗播放系统
 * - 唱片音乐（古老唱片 A/B/C）
 */
public class AwakeningRotb implements ModInitializer {
    public static final String MOD_ID = "awakening";
    public static final String MOD_NAME = "Awakening: Return of the Builder";
    public static final String MOD_VERSION = "0.5.0";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[{}] v{} initializing...", MOD_NAME, MOD_VERSION);

        // 注册顺序：先 items，再 item groups（group 引用 items）
        ModItems.initialize();
        ModItemGroups.initialize();

        LOGGER.info("[{}] v0.5: items + creative tab registered", MOD_ID);
    }
}
