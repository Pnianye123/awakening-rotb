package net.awakeningrotb.story;

import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

/**
 * 14 段纪元的定义表：任务文本 + 任务判定 + 进入纪元时的世界布置。
 *
 * <p><b>lore 铁律</b>：纪元顺序、名称、剧情文本都来自 Jimmy 的原始设计
 * （见 {@code data/awakening/function/eraN.mcfunction} 与 STORY.md），
 * 这里只定义「玩家要做什么」，不改动叙事本身。
 */
public final class Era {

    /** 纪元总数。 */
    public static final int COUNT = 14;

    private Era() {
    }

    /** 每段纪元的任务提示（玩家在 action bar 上看到的那行字）。 */
    public static final String[] OBJECTIVE_TEXTS = {
            "与 3 位村民交谈，了解这片土地",           // 第一纪 · 共生
            "采集 8 个黑曜石，造出通往地狱的门",       // 第二纪 · 下界远征
            "与灾厄村民交谈 —— 他们不会伤害你",        // 第三纪 · 灾厄的善意
            "把 3 个灵魂碎片交给灾厄村民",             // 第四纪 · 请求复活
            "在凋零之子的降临中活下来",                // 第五纪 · 凋零之罪
            "躲进地下，撑过凋零病毒的蔓延",            // 第六纪 · 凋零病毒
            "用灵魂与尖啸体召唤坚守者，击败凋零之子",    // 第七纪 · 坚守者
            "在远古古城放置共鸣方块，用下界之星净化世界", // 第八纪 · 信标净化
            "击败 3 个失控的邪术师",                    // 第九纪 · 灾厄的反噬
            "躲进 Y=-40 以下的要塞深处",               // 第十纪 · 再次躲避
            "穿过末地传送门，离开这个世界",            // 第十一纪 · 末地远征
            "击败末影龙，为世界留下一条守门龙",        // 第十二纪 · 龙之战
            "收集 16 个末地石与一副鞘翅，重建文明",     // 第十三纪 · 末地文明
            "使用末影珍珠碎片，走上不归路",            // 第十四纪 · 不归路
    };

    /**
     * 进入某纪元时的世界布置（生成 NPC / 给物品）。
     * 索引 0 = 第一纪。
     */
    public static void enterEra(int era, ServerPlayerEntity player, StoryProgress progress) {
        switch (era) {
            // 第一纪 · 共生 —— 觉醒者登场，作为引路人
            case 0 -> StoryNpc.spawnAwakener(player);
            // 第二纪 · 下界远征 —— 提示去下界
            case 1 -> {
            }
            // 第三纪 · 灾厄的善意 —— 灾厄村民出现
            case 2 -> StoryNpc.spawnWarlock(player, true);
            // 第四纪 · 请求复活 —— 灾厄村民还在，等着灵魂
            case 3 -> StoryItems.give(player, net.awakeningrotb.item.ModItems.SOUL_SHARD, 3);
            // 第五纪 · 凋零之罪 —— 凋零之子降临（玩家无法阻止）
            case 4 -> StoryBoss.spawnWitherSon(player, false);
            // 第六纪 · 凋零病毒 —— 灾厄村民开始异变
            case 5 -> StoryNpc.spawnWarlock(player, false);
            // 第七纪 · 坚守者 —— 真正的 Boss 战：坚守者苏醒，锁定凋零之子
            case 6 -> {
                net.minecraft.entity.boss.WitherEntity wither = StoryBoss.spawnWitherSon(player, true);
                StoryBoss.spawnSentinel(player, wither);
            }
            // 第八纪 · 信标净化 —— 发放共鸣方块 + 下界之星
            // ⚠️ 下界之星是 guardian 才掉的稀有物，玩家不一定有 —— 剧情直接发，
            //    否则第八纪会把人卡死
            case 7 -> {
                StoryItems.give(player, net.awakeningrotb.item.ModItems.RESONANCE_BLOCK_ITEM, 8);
                StoryItems.give(player, Items.NETHER_STAR, 1);
            }
            // 第九纪 · 灾厄的反噬 —— 三个邪术师暴走
            case 8 -> StoryBoss.spawnWarlocks(player, 3);
            // 第十纪 · 再次躲避 —— 清场
            case 9 -> StoryBoss.clearWarlocks(player);
            // 第十一纪 · 末地远征 —— 给末影之眼
            case 10 -> StoryItems.give(player, Items.ENDER_EYE, 12);
            // 第十二纪 · 龙之战 —— 末影龙已在末地等着
            case 11 -> {
            }
            // 第十三纪 · 末地文明 —— 给末地石
            case 12 -> StoryItems.give(player, Items.END_STONE, 16);
            // 第十四纪 · 不归路 —— 给末影珍珠碎片
            case 13 -> StoryItems.give(player, net.awakeningrotb.item.ModItems.ECHO_SHARD, 1);
            default -> {
            }
        }
    }

    /** 14 段纪元的任务判定。索引 0 = 第一纪。 */
    public static Objective objectiveFor(int era) {
        return switch (era) {
            // 第一纪 · 共生：与 3 位村民交谈
            case 0 -> (p, s) -> s.get("talk_villager") >= 3;
            // 第二纪 · 下界远征：采集 8 个黑曜石
            case 1 -> (p, s) -> countItem(p, Items.OBSIDIAN) >= 8;
            // 第三纪 · 灾厄的善意：与灾厄村民交谈
            case 2 -> (p, s) -> s.get("talk_warlock") >= 1;
            // 第四纪 · 请求复活：交出 3 个灵魂碎片
            case 3 -> (p, s) -> s.get("give_soul") >= 3;
            // 第五纪 · 凋零之罪：在凋零之子手下活过 30 秒
            case 4 -> (p, s) -> s.get("survive_wither") >= 30;
            // 第六纪 · 凋零病毒：地下累计 60 tick
            case 5 -> (p, s) -> s.get("hide_underground") >= 60;
            // 第七纪 · 坚守者：击败凋零之子
            case 6 -> (p, s) -> s.get("kill_wither_son") >= 1;
            // 第八纪 · 信标净化：激活共鸣方块
            case 7 -> (p, s) -> s.hasFlag(StoryManager.FLAG_PURIFIED);
            // 第九纪 · 灾厄的反噬：击败 3 个邪术师
            case 8 -> (p, s) -> s.get("kill_warlock") >= 3;
            // 第十纪 · 再次躲避：在 Y<=-40 累计 100 tick
            case 9 -> (p, s) -> s.get("hide_fortress") >= 100;
            // 第十一纪 · 末地远征：抵达末地
            case 10 -> (p, s) -> p.getWorld().getRegistryKey() == World.END;
            // 第十二纪 · 龙之战：击败末影龙
            case 11 -> (p, s) -> s.get("kill_dragon") >= 1;
            // 第十三纪 · 末地文明：末地石 + 鞘翅
            case 12 -> (p, s) -> countItem(p, Items.END_STONE) >= 16 && hasItem(p, Items.ELYTRA);
            // 第十四纪 · 不归路：使用末影珍珠碎片
            case 13 -> (p, s) -> s.hasFlag(StoryManager.FLAG_NO_RETURN);
            default -> (p, s) -> false;
        };
    }

    /** 统计玩家背包里某个物品的数量。 */
    public static int countItem(ServerPlayerEntity player, net.minecraft.item.Item item) {
        int n = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            net.minecraft.item.ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                n += stack.getCount();
            }
        }
        return n;
    }

    /** 玩家背包里是否有指定物品。 */
    public static boolean hasItem(ServerPlayerEntity player, net.minecraft.item.Item item) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(item)) {
                return true;
            }
        }
        return false;
    }

    /** 纪元序号 → 中文名（第一纪 / 第二纪 ...）。 */
    public static String cnNumber(int era) {
        String[] n = {"第一", "第二", "第三", "第四", "第五", "第六", "第七",
                "第八", "第九", "第十", "第十一", "第十二", "第十三", "第十四"};
        return n[Math.min(era, n.length - 1)];
    }
}
