package net.mrbt0907.weather2.mixins.injection;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.util.MixinWorldReciever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(WorldRenderer.class)
public abstract class MixinLevelRenderer {

    @Inject(method = "renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V",
            at = @At("HEAD"), cancellable = true)
    private void renderRain(LightTexture lightTexture, float partialTicks,
                            double x, double y, double z, CallbackInfo callback) {
        MixinWorldReciever.renderRain(partialTicks, x, y, z, callback);
    }

    @Inject(method = "tickRain(Lnet/minecraft/client/renderer/ActiveRenderInfo;)V",
            at = @At("HEAD"), cancellable = true)
    private void renderSplash(ActiveRenderInfo activeRenderInfo, CallbackInfo callback) {
        if (ConfigMisc.proxy_render_override && !ConfigClient.enable_vanilla_rain)
            callback.cancel();
    }

}