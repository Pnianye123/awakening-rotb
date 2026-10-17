package net.awakeningrotb;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Awakening: Return of the Builder
 *
 * v0.5 骨架入口。当前只做基础初始化，后续版本会在这里注册：
 * - 玩家专属物品（建造师长袍、记录仪、共鸣石）
 * - 古城结构生成
 * - 14 段 cutscene 调度
 * - 终末之诗播放器
 */
public class AwakeningRotb implements ModInitializer {
    public static final String MOD_ID = "awakening";
    public static final String MOD_NAME = "Awakening: Return of the Builder";
    public static final String MOD_VERSION = "0.5.0";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[{}] v{} initializing...", MOD_NAME, MOD_VERSION);
        LOGGER.info("[{}] v0.5 骨架就位 — 物品/cutscene/古城/Boss 留待后续版本", MOD_ID);
    }
}
