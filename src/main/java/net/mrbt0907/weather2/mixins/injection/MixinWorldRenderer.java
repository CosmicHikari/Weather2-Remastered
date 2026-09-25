package net.mrbt0907.weather2.mixins.injection;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    private static void drawSkyHemisphereImpl(BufferBuilder bufferBuilderIn, float posY, boolean reverseX) {
        bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);
        int max = 4096;
        for (int k = -max; k <= max; k += 64) {
            for (int l = -max; l <= max; l += 64) {
                float f = (float) k;
                float f1 = (float) (k + 64);
                if (reverseX) {
                    f1 = (float) k;
                    f = (float) (k + 64);
                }
                bufferBuilderIn.vertex(f, posY, l).endVertex();
                bufferBuilderIn.vertex(f1, posY, l).endVertex();
                bufferBuilderIn.vertex(f1, posY, l + 64).endVertex();
                bufferBuilderIn.vertex(f, posY, l + 64).endVertex();
            }
        }
    }

    @Inject(method = "drawSkyHemisphere", at = @At("HEAD"), cancellable = true)
    private void drawSkyHemisphere(BufferBuilder bufferBuilderIn, float posY, boolean reverseX, CallbackInfo ci) {
        ci.cancel();
        drawSkyHemisphereImpl(bufferBuilderIn, posY, reverseX);
    }

    @Redirect(
            method = "createLightSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/WorldRenderer;drawSkyHemisphere(Lnet/minecraft/client/renderer/BufferBuilder;FZ)V"
            )
    )
    private void redirectTopSkyPlaneY(WorldRenderer instance, BufferBuilder bufferBuilderIn, float posY, boolean reverseX) {
        drawSkyHemisphereImpl(bufferBuilderIn, 170.0F, reverseX);
    }

    @Redirect(
            method = "createDarkSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/WorldRenderer;drawSkyHemisphere(Lnet/minecraft/client/renderer/BufferBuilder;FZ)V"
            )
    )
    private void redirectBottomSkyPlaneY(WorldRenderer instance, BufferBuilder bufferBuilderIn, float posY, boolean reverseX) {
        drawSkyHemisphereImpl(bufferBuilderIn, -170.0F, reverseX);
    }
}