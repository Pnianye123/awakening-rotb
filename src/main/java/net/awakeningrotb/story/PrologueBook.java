package net.awakeningrotb.story;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * 《前情提要》—— 开局发给玩家的那本书。
 *
 * <p>玩家进游戏后拿到这本书，读完才知道自己是谁、发生过什么。
 * 书里最后一段明确告诉玩家：输入 {@code /start} 才会正式开始剧情。
 *
 * <p>书籍正文按玩家语言给（中文 / 英文各一份，见 {@link #PAGES_ZH} / {@link #PAGES_EN}）。
 *
 * <p>lore 铁律参考：设定要求附录 8 条（坚守者 / 尖啸体 / 古城来历 /
 * 灾厄村民初设 / 暗魔法起源 / 凋零病毒 vs 亡灵病毒 / 末影龙"所有" / 末影人由来）。
 */
public final class PrologueBook {

    /** 书名（中文）。 */
    public static final String TITLE_ZH = "前情提要";
    /** 书名（英文）。 */
    public static final String TITLE_EN = "Prologue";
    /** 作者署名。 */
    public static final String AUTHOR = "觉醒者 / The Awakener";

    private PrologueBook() {
    }

    // ── 中文正文 ──
    private static final String[] PAGES_ZH = {
            // ── 封面 ──
            "《前情提要》\n\n—— 致醒来的你\n\n"
                    + "这是一本没有署名的书。\n"
                    + "纸页泛黄，边缘被翻得起了毛。\n"
                    + "每一行字，都是你的笔迹。",

            // ── 你在哪里 ──
            "你在远古古城醒来。\n\n"
                    + "头顶是造了不知多少年的穹顶，\n"
                    + "脚下是你自己铺的石砖。\n"
                    + "石头还记得你的手。\n\n"
                    + "——这座城不是天然的洞穴，\n"
                    + "每一块砖都是建造师亲手垒上去的。",

            // ── 两群人 ──
            "很久以前——也许是几千年，也许更久——\n"
                    + "这片土地上同时出现了两群人。\n\n"
                    + "一群叫村民。\n"
                    + "朴素、保守，世代在平原村庄里种田、交易、繁衍后代。\n"
                    + "村庄有铁傀儡守夜，有钟楼报时，有彼此照应的烟火气。\n\n"
                    + "另一群叫远古建造师。\n"
                    + "跟你长得几乎一模一样，但更聪明、更爱探索、\n"
                    + "更愿意往世界边缘走。",

            // ── 和平时期 ──
            "一开始，村民和建造师井水不犯河水。\n\n"
                    + "村民管建筑，建造师管探索。\n"
                    + "偶尔交换物资——村民给建造师面包，\n"
                    + "建造师给村民红石机关。\n\n"
                    + "两边都觉得这样挺好。",

            // ── 灾厄 ──
            "后来，灾厄来了。\n\n"
                    + "那是一群修炼暗法的村民。\n"
                    + "他们的暗法不是天生带来的，是一次次修行磨出来的。\n"
                    + "起初他们不伤人——不伤村民，也不伤建造师。\n\n"
                    + "他们说：能把死者唤回来。\n"
                    + "你信了。",

            // ── 凋零 ──
            "代价是——世界开始腐烂。\n\n"
                    + "凋零诞生了。\n"
                    + "凋零病毒漫过地表，那不是亡灵作祟，是纯粹的腐坏：\n"
                    + "蓝雾漫到哪里，草就死到哪里，石头都裂开。\n\n"
                    + "你把村民藏进地窖，把孩子推上高塔。\n"
                    + "你自己挡在最后面。",

            // ── 坚守者 ──
            "但有些东西，你一个人扛不住。\n\n"
                    + "你用自己培育的尖啸体——那是你一手种出来的菌类共鸣网络——\n"
                    + "唤醒了坚守者。\n\n"
                    + "它替你守住了第七次凋零。\n"
                    + "代价是从此再也回不去。",

            // ── 末地 ──
            "最后你进了末地。\n\n"
                    + "末影龙有“所有”——打赢它，\n"
                    + "才能在末地留下一条守门龙，让世界还能被敲响。\n\n"
                    + "而在末地待得太久的人，\n"
                    + "会变成末影人。\n\n"
                    + "你就是在那时候，选择走上一条不归路的。",

            // ── 苏醒 ──
            "你不知道自己睡了多久。\n\n"
                    + "醒来时，古城空了，\n"
                    + "尖啸体安静了，\n"
                    + "村民的村庄只剩下断墙。\n\n"
                    + "还有个自称“觉醒者”的老家伙站在旁边，\n"
                    + "说他等了很久了。\n\n"
                    + "你还活着。\n"
                    + "那就把这十四段历史，再走一遍。",

            // ── 怎么玩 ──
            "【这十四段历史】\n\n"
                    + "共生 · 下界远征 · 灾厄的善意 · 请求复活 ·\n"
                    + "凋零之罪 · 凋零病毒 · 坚守者 · 信标净化 ·\n"
                    + "灾厄的反噬 · 再次躲避 · 末地远征 · 龙之战 ·\n"
                    + "末地文明 · 不归路\n\n"
                    + "每一段，你都得亲自做一遍。\n"
                    + "走完十四段，你会看到一首《终末之诗》——\n"
                    + "那是唯一的结局。",

            // ── 开始指令 ──
            "【怎么开始】\n\n"
                    + "读完这本书记得输入：\n\n"
                    + "        /start\n\n"
                    + "输入之后，一切开始。\n\n"
                    + "（书别扔。想重看，就再要一本。）",
    };

    // ── 英文正文 ──
    private static final String[] PAGES_EN = {
            "Prologue\n\n— To you, on waking\n\n"
                    + "A book with no name on the cover.\n"
                    + "The paper has yellowed and the edges are worn soft.\n"
                    + "Every line of it is in your own hand.",

            "You wake in the Ancient City.\n\n"
                    + "Above you is a vault built over years no one counted.\n"
                    + "Underfoot are stone bricks you laid yourself.\n"
                    + "The stone still remembers your hands.\n\n"
                    + "— This city was not a natural cave.\n"
                    + "Every brick was raised by a Builder's own hands.",

            "Long ago — perhaps thousands of years ago, perhaps longer —\n"
                    + "two kinds of people came to share this land.\n\n"
                    + "One kind was the villagers.\n"
                    + "Plain, conservative, farming and trading and raising families\n"
                    + "in their plains villages, with iron golems keeping watch by night\n"
                    + "and a bell tower marking the hours.\n\n"
                    + "The other kind was the ancient Builders.\n"
                    + "They looked almost exactly like you, but cleverer, hungrier to explore,\n"
                    + "and far more willing to walk toward the edges of the world.",

            "At first, villagers and Builders kept to themselves.\n\n"
                    + "Villagers built. Builders explored.\n"
                    + "They traded now and then — bread from the villagers,\n"
                    + "redstone mechanisms from the Builders.\n\n"
                    + "Both sides thought that was a fine arrangement.",

            "Then came the Scourge.\n\n"
                    + "They were villagers who had trained in dark arts —\n"
                    + "not born to it, but ground into it through long practice.\n"
                    + "At first they harmed no one — neither villagers nor Builders.\n\n"
                    + "They said: we can call the dead back.\n"
                    + "You believed them.",

            "The price was that the world began to rot.\n\n"
                    + "The Wither was born.\n"
                    + "The Wither Blight spread across the surface — not the work of the\n"
                    + "undead, but pure decay: wherever the blue haze reached,\n"
                    + "grass died and stone cracked open.\n\n"
                    + "You hid the villagers in cellars and pushed the children up the towers.\n"
                    + "And you put yourself in front, last of all.",

            "But some things you could not carry alone.\n\n"
                    + "Using the Screech Fungus you had cultivated yourself —\n"
                    + "a fungal resonance network you grew by hand —\n"
                    + "you woke the Echo Sentinel.\n\n"
                    + "It held the line against the seventh Wither.\n"
                    + "The price was that you could never go back.",

            "In the end you went to the End.\n\n"
                    + "The Ender Dragon has 'all' of them — defeat it,\n"
                    + "and you may leave one Gate Dragon behind in the End,\n"
                    + "so that the world can still be knocked upon.\n\n"
                    + "And those who stay in the End too long\n"
                    + "become the Ender Folk.\n\n"
                    + "That was the moment you chose the road of no return.",

            "You do not know how long you slept.\n\n"
                    + "When you woke, the Ancient City was empty,\n"
                    + "the Screech Fungus had gone quiet,\n"
                    + "and the villagers' village was nothing but broken walls.\n\n"
                    + "An old fellow calling himself the Awakener stood nearby,\n"
                    + "saying he had waited a long time.\n\n"
                    + "You are still alive.\n"
                    + "So walk these fourteen eras of history one more time.",

            "[These fourteen eras]\n\n"
                    + "Symbiosis · The Nether Expedition · The Scourge's Benevolence ·\n"
                    + "A Plea for Resurrection · The Sin of the Wither · The Wither Blight ·\n"
                    + "The Echo Sentinel · The Beacon's Cleansing · The Scourge's Retaliation ·\n"
                    + "Hiding Again · The Expedition to the End · The War of Dragons ·\n"
                    + "The Civilization of the End · The Road of No Return\n\n"
                    + "You must live each one of them yourself.\n"
                    + "After the fourteenth, you will see a poem — The Final Poem —\n"
                    + "and it is the only ending there is.",

            "[How to begin]\n\n"
                    + "When you have read this far, type:\n\n"
                    + "        /start\n\n"
                    + "After that, everything begins.\n\n"
                    + "(Do not throw the book away. If you want to read it again,\n"
                    + " just ask for another copy.)",
    };

    /** 做一本《前情提要》（按语言）。 */
    public static ItemStack create(String lang) {
        boolean en = Loc.EN.equals(lang);
        String[] pages = en ? PAGES_EN : PAGES_ZH;
        String title = en ? TITLE_EN : TITLE_ZH;

        List<RawFilteredPair<Text>> pageList = new ArrayList<>();
        for (String page : pages) {
            pageList.add(RawFilteredPair.of(Text.literal(page)));
        }
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(
                RawFilteredPair.of(title),   // 标题
                AUTHOR,                        // 作者
                0,                             // generation：0 = 原始版本
                pageList,                      // 页面
                false                          // resolved：false = 未签写
        ));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§6" + title));
        return stack;
    }

    /** 做一本《前情提要》（按玩家语言）。 */
    public static ItemStack create(ServerPlayerEntity player) {
        return create(Loc.langOf(player));
    }

    /** 把书塞进玩家背包（满了就掉在脚下）。 */
    public static void give(ServerPlayerEntity player) {
        player.getInventory().offerOrDrop(create(player));
    }

    /** 页数（测试用）。 */
    public static int pageCount(String lang) {
        return (Loc.EN.equals(lang) ? PAGES_EN : PAGES_ZH).length;
    }
}
