package net.awakeningrotb.story;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 剧情进度存档 —— 挂在世界（overworld）上的 PersistentState。
 *
 * <p>用 world 级 PersistentState 而不是玩家 NBT 的原因：
 * 剧情状态量不小（每玩家十几个字段 + 计数器表），
 * 塞进玩家 NBT 会在每次进出世界时来回读写，且多玩家的世界不好管理。
 */
public class StoryState extends PersistentState {

    private static final String ID = "awakening_story";

    public static final Type<StoryState> TYPE = new Type<>(
            StoryState::new,
            StoryState::fromNbt,
            net.minecraft.datafixer.DataFixTypes.LEVEL
    );

    /** uuid(String) → 进度 */
    public final Map<String, StoryProgress> players = new HashMap<>();

    public StoryProgress get(UUID uuid) {
        return players.computeIfAbsent(uuid.toString(), k -> new StoryProgress());
    }

    public StoryProgress get(net.minecraft.entity.player.PlayerEntity player) {
        return get(player.getUuid());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound map = new NbtCompound();
        for (Map.Entry<String, StoryProgress> e : players.entrySet()) {
            map.put(e.getKey(), e.getValue().writeNbt(new NbtCompound()));
        }
        nbt.put("players", map);
        return nbt;
    }
    public static StoryState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        StoryState state = new StoryState();
        NbtCompound map = nbt.getCompound("players");
        for (String key : map.getKeys()) {
            state.players.put(key, StoryProgress.fromNbt(map.getCompound(key)));
        }
        return state;
    }

    /** 取（并按需创建）世界级存档。 */
    public static StoryState of(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE, ID);
    }}
