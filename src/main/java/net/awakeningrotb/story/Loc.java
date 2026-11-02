package net.awakeningrotb.story;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

/**
 * 语言运行时 —— 按玩家客户端语言选择中文 / 英文。
 *
 * <p>语言判定优先级：
 * <ol>
 *   <li>玩家用 {@code /awakening lang zh|en} 手动指定过 → 用指定的</li>
 *   <li>否则看客户端语言（{@code SyncedClientOptions.language()}）：en_* → 英文，其余 → 中文</li>
 * </ol>
 *
 * <p>手动指定存在 {@link StoryProgress#lang} 里（跟剧情进度一起存档）。
 * 默认中文是 Jimmy 的母语环境，英文留给 Modrinth 的海外玩家。
 */
public final class Loc {

    /** 语言代码 → 玩家档案里的标记。 */
    public static final String ZH = "zh";
    public static final String EN = "en";

    private static final Map<String, Integer> KEY_INDEX = new HashMap<>();

    static {
        for (int i = 0; i < L10n.KEYS.length; i++) {
            KEY_INDEX.put(L10n.KEYS[i], i);
        }
    }

    private Loc() {
    }

    /** 玩家当前该用哪种语言。 */
    public static String langOf(ServerPlayerEntity player) {
        StoryProgress progress = StoryState.of(player.getServerWorld()).get(player);
        if (ZH.equals(progress.lang) || EN.equals(progress.lang)) {
            return progress.lang;
        }
        // 客户端语言：en_us / en_gb ... → 英文
        try {
            var options = player.getClientOptions();
            if (options != null) {
                String code = options.language();
                if (code != null && code.toLowerCase(java.util.Locale.ROOT).startsWith("en")) {
                    return EN;
                }
            }
        } catch (Throwable ignored) {
            // 某些服务端实现拿不到客户端设置 —— 落回中文
        }
        return ZH;
    }

    /** 玩家是不是在看英文。 */
    public static boolean isEnglish(ServerPlayerEntity player) {
        return EN.equals(langOf(player));
    }

    /** 手动设置语言（null = 自动）。 */
    public static void setLang(ServerPlayerEntity player, String lang) {
        StoryProgress progress = StoryState.of(player.getServerWorld()).get(player);
        progress.lang = (lang == null || lang.isBlank()) ? "" : lang;
    }

    /** 取一条界面文本（按玩家语言），并带上原本的颜色。 */
    public static MutableText msg(ServerPlayerEntity player, String key, Object... args) {
        return text(langOf(player), key, args);
    }

    /** 取一条界面文本（指定语言）。 */
    public static MutableText text(String lang, String key, Object... args) {
        Integer idx = KEY_INDEX.get(key);
        if (idx == null) {
            return Text.literal(key);
        }
        String raw = EN.equals(lang) ? L10n.EN[idx] : L10n.ZH[idx];
        if (args != null && args.length > 0) {
            raw = formatArgs(raw, args);
        }
        return Text.literal(raw).formatted(colorOf(L10n.COLOR[idx]));
    }

    /** 取一条界面文本的纯字符串形式（拼接用）。 */
    public static String str(String lang, String key, Object... args) {
        Integer idx = KEY_INDEX.get(key);
        if (idx == null) {
            return key;
        }
        String raw = EN.equals(lang) ? L10n.EN[idx] : L10n.ZH[idx];
        if (args != null && args.length > 0) {
            raw = formatArgs(raw, args);
        }
        return raw;
    }

    /** 极简 %s 替换（只处理 %s，不走正则，省事也安全）。 */
    private static String formatArgs(String raw, Object[] args) {
        StringBuilder sb = new StringBuilder(raw.length() + 16);
        int argIndex = 0;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '%' && i + 1 < raw.length() && raw.charAt(i + 1) == 's') {
                sb.append(argIndex < args.length ? String.valueOf(args[argIndex++]) : "%s");
                i++;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 颜色名 → Formatting。 */
    public static Formatting colorOf(String name) {
        if (name == null) {
            return Formatting.WHITE;
        }
        return switch (name) {
            case "gold" -> Formatting.GOLD;
            case "yellow" -> Formatting.YELLOW;
            case "gray" -> Formatting.GRAY;
            case "dark_gray" -> Formatting.DARK_GRAY;
            case "white" -> Formatting.WHITE;
            case "red" -> Formatting.RED;
            case "dark_red" -> Formatting.DARK_RED;
            case "green" -> Formatting.GREEN;
            case "dark_green" -> Formatting.DARK_GREEN;
            case "aqua" -> Formatting.AQUA;
            case "dark_aqua" -> Formatting.DARK_AQUA;
            case "blue" -> Formatting.BLUE;
            case "dark_blue" -> Formatting.DARK_BLUE;
            case "light_purple" -> Formatting.LIGHT_PURPLE;
            case "dark_purple" -> Formatting.DARK_PURPLE;
            default -> Formatting.WHITE;
        };
    }

    /** 纪元编号（"第一纪" / "Era I"）。 */
    public static String eraNumber(String lang, int era) {
        int i = Math.max(0, Math.min(era, L10n.ERA_NUMBER_ZH.length - 1));
        return EN.equals(lang) ? L10n.ERA_NUMBER_EN[i] : L10n.ERA_NUMBER_ZH[i];
    }

    /** 纪元副标题（"共生" / "Symbiosis"）。 */
    public static String eraName(String lang, int era) {
        int i = Math.max(0, Math.min(era, L10n.ERA_NAME_ZH.length - 1));
        return EN.equals(lang) ? L10n.ERA_NAME_EN[i] : L10n.ERA_NAME_ZH[i];
    }

    /** 任务提示文本。 */
    public static String objective(String lang, int era) {
        int i = Math.max(0, Math.min(era, L10n.OBJECTIVE_ZH.length - 1));
        return EN.equals(lang) ? L10n.OBJECTIVE_EN[i] : L10n.OBJECTIVE_ZH[i];
    }

    /** 纪元剧情文本（按语言取对应的那一份）。 */
    public static String[] lines(String lang, int era) {
        boolean en = EN.equals(lang);
        return switch (era) {
            case 0 -> en ? StoryTextEn.ERA1 : StoryText.ERA1;
            case 1 -> en ? StoryTextEn.ERA2 : StoryText.ERA2;
            case 2 -> en ? StoryTextEn.ERA3 : StoryText.ERA3;
            case 3 -> en ? StoryTextEn.ERA4 : StoryText.ERA4;
            case 4 -> en ? StoryTextEn.ERA5 : StoryText.ERA5;
            case 5 -> en ? StoryTextEn.ERA6 : StoryText.ERA6;
            case 6 -> en ? StoryTextEn.ERA7 : StoryText.ERA7;
            case 7 -> en ? StoryTextEn.ERA8 : StoryText.ERA8;
            case 8 -> en ? StoryTextEn.ERA9 : StoryText.ERA9;
            case 9 -> en ? StoryTextEn.ERA10 : StoryText.ERA10;
            case 10 -> en ? StoryTextEn.ERA11 : StoryText.ERA11;
            case 11 -> en ? StoryTextEn.ERA12 : StoryText.ERA12;
            case 12 -> en ? StoryTextEn.ERA13 : StoryText.ERA13;
            case 13 -> en ? StoryTextEn.ERA14 : StoryText.ERA14;
            default -> new String[0];
        };
    }

    /** 终末之诗。 */
    public static String[] ending(String lang) {
        return EN.equals(lang) ? StoryTextEn.ENDING : StoryText.ENDING;
    }
}
