package net.awakeningrotb.sound;

import net.awakeningrotb.AwakeningRotb;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mod 音效注册表。
 *
 * <p>包含：古城环境音、尖啸体共鸣、纪元推进音、终末之诗主题。
 * 音乐（古老唱片 A/B/C）在 {@link ModMusic} 里作为 jukebox song 注册。
 */
public class ModSounds {

    /** 古城环境音（ambient） */
    public static final RegistryEntry<SoundEvent> ANCIENT_CITY_AMBIENT = register("ancient_city_ambient");
    /** 尖啸体共鸣 */
    public static final RegistryEntry<SoundEvent> RESONANCE = register("resonance");
    /** 纪元推进 */
    public static final RegistryEntry<SoundEvent> ERA_ADVANCE = register("era_advance");
    /** 凋零诞生 */
    public static final RegistryEntry<SoundEvent> WITHER_BORN = register("wither_born");
    /** 坚守者苏醒 */
    public static final RegistryEntry<SoundEvent> SENTINEL_AWAKEN = register("sentinel_awaken");
    /** 终末之诗 */
    public static final RegistryEntry<SoundEvent> FINAL_POEM = register("final_poem");
    /** 自由段解锁 */
    public static final RegistryEntry<SoundEvent> FREEPLAY_UNLOCK = register("freeplay_unlock");

    /** 古老唱片 A —— 远古时代（共生） */
    public static final RegistryEntry<SoundEvent> RECORD_A = register("music/record_a");
    /** 古老唱片 B —— 末地远征 */
    public static final RegistryEntry<SoundEvent> RECORD_B = register("music/record_b");
    /** 古老唱片 C —— 终末之诗 */
    public static final RegistryEntry<SoundEvent> RECORD_C = register("music/record_c");

    public static void initialize() {
        // 全部在静态字段初始化时注册
    }

    private static RegistryEntry<SoundEvent> register(String name) {
        Identifier id = Identifier.of(AwakeningRotb.MOD_ID, name);
        RegistryKey<SoundEvent> key = RegistryKey.of(RegistryKeys.SOUND_EVENT, id);
        return Registry.registerReference(Registries.SOUND_EVENT, key, SoundEvent.of(id));
    }
}
