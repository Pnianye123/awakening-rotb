package net.awakeningrotb.mixin.client;

import net.awakeningrotb.client.BuilderSkin;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 玩家皮肤覆盖 —— 穿着建造师长袍的人显示为远古建造师。
 *
 * <p>注入点：{@code AbstractClientPlayerEntity#getSkinTextures()}（本地玩家和其他玩家都走这里）。
 *
 * <p>整段包在 try/catch 里：任何异常都直接放行，保留原皮肤 —— 绝不因为皮肤功能把客户端搞崩。
 */
@Mixin(AbstractClientPlayerEntity.class)
public abstract class BuilderSkinMixin {

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void awakening$builderSkin(CallbackInfoReturnable<SkinTextures> cir) {
        try {
            AbstractClientPlayerEntity self = (AbstractClientPlayerEntity) (Object) this;
            if (!BuilderSkin.shouldOverride(self)) {
                return;
            }
            SkinTextures base = cir.getReturnValue();
            if (base == null) {
                return;
            }
            SkinTextures custom = BuilderSkin.apply(base);
            if (custom != null) {
                cir.setReturnValue(custom);
            }
        } catch (Throwable ignored) {
            // 皮肤功能出问题也绝不影响游戏 —— 保留原皮肤
        }
    }
}
