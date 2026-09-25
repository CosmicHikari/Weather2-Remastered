package net.mrbt0907.weather2.mixins.injection;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.weather.storm.StormObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(GameRenderer.class)
public abstract class MixinEntityRenderer {
    private static final Minecraft MC = Minecraft.getInstance();

    @Shadow
    private float renderDistance;

    @Inject(method = "getProjectionMatrix(Lnet/minecraft/client/renderer/ActiveRenderInfo;FZ)Lnet/minecraft/util/math/vector/Matrix4f;",
            at = @At("HEAD"))
    private void extendFarPlane(ActiveRenderInfo activeRenderInfo, float partialTicks,
                                boolean useFov, CallbackInfoReturnable<Matrix4f> cir) {

        float sceneDist = NewSceneEnhancer.instance().renderDistance;

        if (sceneDist <= 0.0F) {
            sceneDist = MC.options.renderDistance * 16.0F;
        }

        this.renderDistance = sceneDist * 1.3F;
    }

    @Inject(
            method = "renderLevel(FJLcom/mojang/blaze3d/matrix/MatrixStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V",
                    shift = At.Shift.AFTER
            )
    )
    private void orientCamera(float partialTicks, long nanoTime,
                              MatrixStack matrixStack, CallbackInfo callback) {
        if (!MC.isPaused() && ConfigClient.camera_shake_mult > 0.0D) {
            NewSceneEnhancer scene = NewSceneEnhancer.instance();
            float tornadoStrength = 0.0F;
            float windStrength = WeatherUtilEntity.isEntityOutside(MC.player, true)
                    ? 0.1F * Maths.clamp((scene.cachedWindSpeed - 4.0F) * 0.2F, 0.0F, 1.0F)
                    : 0.0F;

            if (scene.cachedSystem instanceof StormObject) {
                StormObject storm = (StormObject) scene.cachedSystem;
                if (storm.type.equals(Type.TORNADO))
                    tornadoStrength = (1.0F - (float) Math.min(
                            ((scene.cachedFunnelDistance - storm.funnelSize) / (storm.funnelSize + 64.0F)), 1.0F))
                            * Math.min(storm.stage * 0.1F, 1.0F);
            }

            float strength = (tornadoStrength + windStrength) * 0.025F
                    * ConfigClient.camera_shake_mult;

            if (strength > 0.0F) {
                matrixStack.translate(
                        Maths.random(-strength, strength),
                        Maths.random(-strength, strength),
                        Maths.random(-strength, strength));
            }
        }
    }
}