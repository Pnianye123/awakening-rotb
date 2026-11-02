package net.awakeningrotb;

import net.awakeningrotb.block.ModBlocks;
import net.awakeningrotb.item.ModItemGroups;
import net.awakeningrotb.item.ModItems;
import net.awakeningrotb.sound.ModSounds;
import net.awakeningrotb.command.AwakeningCommand;
import net.awakeningrotb.story.StoryEvents;
import net.awakeningrotb.world.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Awakening: Return of the Builder ——《方块之下》
 *
 * <p>v1.0：完整剧情段。
 * <p>v1.1：/start 门槛 + 前情提要书 + 中英双语。
 * <ul>
 *   <li>14 段连续纪元 cutscene（逐行叙事 + 标题卡）</li>
 *   <li>每段纪元附带一个必须亲手完成的任务</li>
 *   <li>觉醒者 / 邪术师 / 末影守护者 / 坚守者 / 凋零之子 / 守门龙</li>
 *   <li>终末之诗 —— 唯一结局，之后进入自由段</li>
 *   <li>自由段奖励：建造师长袍、共鸣石、灵魂罗盘、古城通行证、凋零核心、
 *       觉醒者信物、古城钥匙、古老唱片 A/B/C</li>
 * </ul>
 */
public class AwakeningRotb implements ModInitializer {

    public static final String MOD_ID = "awakening";
    public static final String MOD_NAME = "Awakening: Return of the Builder";
    public static final String MOD_VERSION = "1.1.0";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[{}] v{} initializing...", MOD_NAME, MOD_VERSION);

        // 注册顺序：先 items/blocks，再 item groups（group 引用 items）
        ModItems.initialize();
        ModBlocks.initialize();
        ModSounds.initialize();
        ModItemGroups.initialize();
        ModWorldGen.initialize();

        // 事件监听（剧情任务判定）
        StoryEvents.register();

        // 命令注册
        // 用 v1 的 CommandRegistrationCallback（1.21.4 标准用法）
        net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicatedServer) -> {
                    LOGGER.info("[{}] registering /awakening command...", MOD_ID);
                    AwakeningCommand.register(dispatcher);
                });

        // 服务器启动完成 → 打印状态
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
                LOGGER.info("[{}] v{} ready — 14 eras, one ending.", MOD_NAME, MOD_VERSION));
    }
}
