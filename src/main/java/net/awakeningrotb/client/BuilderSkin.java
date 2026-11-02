package net.awakeningrotb.client;

import net.awakeningrotb.item.ModItems;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;

/**
 * 建造师皮肤 —— 客户端侧。
 *
 * <p><b>触发条件</b>：玩家<b>穿着建造师长袍</b>。
 * 剧情开始时 mod 会把长袍发给他并自动穿上，于是他立刻变成远古建造师的样子；
 * 长袍脱下 / 换掉，皮肤就自动变回原来的 —— 不用任何额外的网络协议。
 *
 * <p>皮肤贴图是 mod 自带的资源（{@code assets/awakening/textures/skin/builder_robe.png}，64×64 MC 标准格式），
 * 客户端资源加载器会把它当普通资源贴图注册好，直接用 Identifier 引用即可。
 *
 * <p>本类只在客户端加载（被 client mixin 引用），服务端不会碰。
 */
public final class BuilderSkin {

    /** 建造师皮肤的贴图（mod 资源，64×64）。 */
    public static final Identifier TEXTURE = Identifier.of("awakening", "textures/skin/builder_robe");

    private BuilderSkin() {
    }

    /** 这个玩家现在该显示成建造师吗？（穿着长袍 = 建造师） */
    public static boolean shouldOverride(AbstractClientPlayerEntity player) {
        if (player == null) {
            return false;
        }
        try {
            return player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.BUILDER_ROBE);
        } catch (Throwable t) {
            return false;
        }
    }

    /** 用建造师贴图替换皮肤（保留斗篷 / 鞘翅 / 安全标志等原设置）。 */
    public static SkinTextures apply(SkinTextures base) {
        if (base == null) {
            return null;
        }
        return new SkinTextures(
                TEXTURE,
                base.textureUrl(),
                base.capeTexture(),
                base.elytraTexture(),
                SkinTextures.Model.WIDE,
                base.secure()
        );
    }
}
