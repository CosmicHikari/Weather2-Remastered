package net.mrbt0907.weather2.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.corosus.coroutillegacy.config.ConfigCoroUtilLegacy;
import net.corosus.extendedrenderer.EventHandler;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigMisc;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class MixinWorldReciever {

    public static void renderRain(float partialTicks, double camX, double camY, double camZ,
                                  CallbackInfo callback) {
        if (ConfigMisc.proxy_render_override) {
            if (ConfigCoroUtilLegacy.useEntityRenderHookForShaders) {
                EventHandler.hookRenderShaders(partialTicks, new MatrixStack());
            }
            if (!ConfigClient.enable_vanilla_rain) {
                callback.cancel();
            }
        }
    }
}