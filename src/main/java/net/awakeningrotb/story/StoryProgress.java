package net.awakeningrotb.story;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 单个玩家的剧情进度。
 *
 * <p>挂在 {@link StoryState}（世界级 PersistentState）里，用 UUID 做 key。
 * 之所以不放玩家 NBT：剧情状态需要在玩家离线时也保留（世界存档里更稳），
 * 而且 mod 自己的存储格式比塞玩家 NBT 更可控。
 */
public class StoryProgress {

    /** 当前纪元序号（0-based）。0 = 第一纪，13 = 第十四纪。 */
    public int era;

    /** 当前 cutscene 已播放到第几行（用于断线续播）。 */
    public int cutsceneLine;

    /** 当前纪元的剧情文本是否已播放完（进入任务阶段）。 */
    public boolean cutsceneDone;

    /** 该纪元任务是否已完成（完成即推进到下一纪元）。 */
    public boolean objectiveDone;

    /** 玩家是否已进入自由段（终末之诗播放完毕）。 */
    public boolean freePlay;

    /** 是否已经开始过剧情（避免重进世界重复触发）。 */
    public boolean started;

    /** 通用计数器 —— 不同纪元复用它存自己的计数（如"已收集灵魂数"）。 */
    public int counter;

    /** 通用标记位（bit flag）。 */
    public int flags;

    /** 该玩家身上「已完成的 NPC 击杀数」等按实体类型分的统计。 */
    public final Map<String, Integer> counters = new HashMap<>();

    public void reset() {
        era = 0;
        cutsceneLine = 0;
        cutsceneDone = false;
        objectiveDone = false;
        freePlay = false;
        started = false;
        counter = 0;
        flags = 0;
        counters.clear();
    }

    public void increment(String key) {
        counters.merge(key, 1, Integer::sum);
    }

    public int get(String key) {
        return counters.getOrDefault(key, 0);
    }

    public boolean hasFlag(int bit) {
        return (flags & (1 << bit)) != 0;
    }

    public void setFlag(int bit) {
        flags |= (1 << bit);
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("era", era);
        nbt.putInt("line", cutsceneLine);
        nbt.putBoolean("cutsceneDone", cutsceneDone);
        nbt.putBoolean("objectiveDone", objectiveDone);
        nbt.putBoolean("freePlay", freePlay);
        nbt.putBoolean("started", started);
        nbt.putInt("counter", counter);
        nbt.putInt("flags", flags);
        NbtList list = new NbtList();
        counters.forEach((k, v) -> {
            NbtCompound e = new NbtCompound();
            e.putString("k", k);
            e.putInt("v", v);
            list.add(e);
        });
        nbt.put("counters", list);
        return nbt;
    }

    public static StoryProgress fromNbt(NbtCompound nbt) {
        StoryProgress p = new StoryProgress();
        p.era = nbt.getInt("era");
        p.cutsceneLine = nbt.getInt("line");
        p.cutsceneDone = nbt.getBoolean("cutsceneDone");
        p.objectiveDone = nbt.getBoolean("objectiveDone");
        p.freePlay = nbt.getBoolean("freePlay");
        p.started = nbt.getBoolean("started");
        p.counter = nbt.getInt("counter");
        p.flags = nbt.getInt("flags");
        NbtElement listEl = nbt.get("counters");
        if (listEl instanceof NbtList list) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof NbtCompound e) {
                    p.counters.put(e.getString("k"), e.getInt("v"));
                }
            }
        }
        return p;
    }

    /** 便捷：把当前纪元的剧情文本取出来。 */
    public static String[] linesFor(int era) {
        return switch (era) {
            case 0 -> StoryText.ERA1;
            case 1 -> StoryText.ERA2;
            case 2 -> StoryText.ERA3;
            case 3 -> StoryText.ERA4;
            case 4 -> StoryText.ERA5;
            case 5 -> StoryText.ERA6;
            case 6 -> StoryText.ERA7;
            case 7 -> StoryText.ERA8;
            case 8 -> StoryText.ERA9;
            case 9 -> StoryText.ERA10;
            case 10 -> StoryText.ERA11;
            case 11 -> StoryText.ERA12;
            case 12 -> StoryText.ERA13;
            case 13 -> StoryText.ERA14;
            default -> new String[0];
        };
    }

    /** 便捷：当前纪元的每行颜色。 */
    public static String[] colorsFor(int era) {
        return switch (era) {
            case 0 -> StoryText.ERA1_COLOR;
            case 1 -> StoryText.ERA2_COLOR;
            case 2 -> StoryText.ERA3_COLOR;
            case 3 -> StoryText.ERA4_COLOR;
            case 4 -> StoryText.ERA5_COLOR;
            case 5 -> StoryText.ERA6_COLOR;
            case 6 -> StoryText.ERA7_COLOR;
            case 7 -> StoryText.ERA8_COLOR;
            case 8 -> StoryText.ERA9_COLOR;
            case 9 -> StoryText.ERA10_COLOR;
            case 10 -> StoryText.ERA11_COLOR;
            case 11 -> StoryText.ERA12_COLOR;
            case 12 -> StoryText.ERA13_COLOR;
            case 13 -> StoryText.ERA14_COLOR;
            default -> new String[0];
        };
    }

    /** 便捷：当前纪元的中文标题（如"第七纪 · 坚守者"）。 */
    public static String titleFor(int era) {
        String[] lines = linesFor(era);
        return lines.length > 0 ? lines[0] : "";
    }

    /** 便捷：当前纪元的任务描述。 */
    public static String objectiveTextFor(int era) {
        return Era.OBJECTIVE_TEXTS[Math.min(era, Era.OBJECTIVE_TEXTS.length - 1)];
    }

    /** 便捷：玩家是否在末地。 */
    public static boolean inEnd(net.minecraft.server.network.ServerPlayerEntity player) {
        return player.getWorld().getRegistryKey() == net.minecraft.world.World.END;
    }

    /** 便捷：判断该玩家是否在跑剧情（而非自由段）。 */
    public boolean inStory() {
        return started && !freePlay;
    }

    /** 便捷：UUID 形式（给 StoryState 用）。 */
    public static String keyOf(UUID uuid) {
        return uuid.toString();
    }

    /** 便捷：拿玩家进度，没有就新建。 */
    public static StoryProgress of(Map<String, StoryProgress> map, ServerPlayerEntity player) {
        return map.computeIfAbsent(keyOf(player.getUuid()), k -> new StoryProgress());
    }
}
