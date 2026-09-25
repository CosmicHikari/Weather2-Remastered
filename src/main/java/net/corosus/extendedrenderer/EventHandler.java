package net.corosus.extendedrenderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.corosus.coroutillegacy.config.ConfigCoroUtilLegacy;
import net.corosus.coroutillegacy.forge.CULog;
import net.corosus.coroutillegacy.util.CoroUtilBlockLightCache;
import net.corosus.extendedrenderer.particle.ParticleRegistry;
import net.corosus.extendedrenderer.particle.ShaderManager;
import net.corosus.extendedrenderer.render.RotatingParticleManager;
import net.corosus.extendedrenderer.shader.ShaderEngine;
import net.corosus.extendedrenderer.shader.ShaderListenerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mrbt0907.weather2.client.foliage.FoliageEnhancerShader;
import org.lwjgl.opengl.GL11;

public class EventHandler {

    private static final ResourceLocation BLOCK_ATLAS = PlayerContainer.BLOCK_ATLAS;
    public static World lastWorld;
    public static int mip_min = GL11.GL_NEAREST;
    public static int mip_mag = GL11.GL_NEAREST;
    public static boolean foliageUseLast;
    public static boolean flagFoliageUpdate = false;
    public static boolean lastLightningBoltLightState = false;

    public static void tickShaderTest() {
    }

    public static boolean queryUseOfShaders() {
        RotatingParticleManager.useShaders = ShaderManager.canUseShadersInstancedRendering();

        if (ConfigCoroUtilLegacy.forceShadersOff) {
            RotatingParticleManager.useShaders = false;
        }

        return RotatingParticleManager.useShaders;
    }

    @OnlyIn(Dist.CLIENT)
    public static void hookRenderShaders(float partialTicks, MatrixStack matrixStack) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) return;

        if (lastWorld != mc.level) {
            CULog.log("CoroUtilLegacy: resetting rotating particle renderer");
            ExtendedRenderer.rotEffRenderer.clearEffects(mc.level);
            lastWorld = mc.level;
        }

        ActiveRenderInfo activeRenderInfo = mc.gameRenderer.getMainCamera();

        if (!ConfigCoroUtilLegacy.disableParticleRenderer) {

            mc.gameRenderer.lightTexture().turnOnLightLayer();

            mc.getProfiler().popPush("litParticles");
            ExtendedRenderer.rotEffRenderer.renderLitParticles(
                    mc.getCameraEntity(), activeRenderInfo, partialTicks);

            mc.getProfiler().popPush("particles");

            queryUseOfShaders();

            if (RotatingParticleManager.forceShaderReset) {
                CULog.log("Extended Renderer: Resetting shaders");
                RotatingParticleManager.forceShaderReset = false;
                ShaderEngine.cleanup();
                ShaderListenerRegistry.postReset();
                ExtendedRenderer.foliageRenderer.foliage.clear();
                ShaderEngine.renderer = null;
                ShaderManager.resetCheck();
            }

            if (RotatingParticleManager.useShaders && ShaderEngine.renderer == null) {
                boolean simulateFail = false;
                if (!ShaderEngine.init() || simulateFail) {
                    CULog.log("Extended Renderer: Shaders failed to initialize");
                    ShaderManager.disableShaders();
                    RotatingParticleManager.useShaders = false;
                } else {
                    CULog.log("Extended Renderer: Initialized instanced rendering shaders");
                    ShaderListenerRegistry.postInit();
                }
            }

            preShaderRender(mc.getCameraEntity(), partialTicks, matrixStack, activeRenderInfo);

            if (ConfigCoroUtilLegacy.foliageShaders) {
                ExtendedRenderer.foliageRenderer.render(mc.getCameraEntity(), partialTicks);
            }

            ExtendedRenderer.rotEffRenderer.renderParticles(
                    mc.getCameraEntity(), activeRenderInfo, partialTicks, matrixStack);

            postShaderRender(matrixStack);

            mc.gameRenderer.lightTexture().turnOffLightLayer();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public static void preShaderRender(Entity entityIn, float partialTicks, MatrixStack matrixStack, ActiveRenderInfo activeRenderInfo) {
        Minecraft mc = Minecraft.getInstance();
        matrixStack.pushPose();

        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.pushMatrix();
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(activeRenderInfo.getXRot()));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(activeRenderInfo.getYRot() + 180.0F));
        RenderSystem.loadIdentity();
        RenderSystem.rotatef(activeRenderInfo.getXRot(), 1.0F, 0.0F, 0.0F);
        RenderSystem.rotatef(activeRenderInfo.getYRot() + 180.0F, 0.0F, 1.0F, 0.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.004F);

        if (mc.level instanceof ClientWorld) {
            ActiveRenderInfo ari = mc.gameRenderer.getMainCamera();
            ClientWorld clientWorld = mc.level;
            FogRenderer.setupColor(ari, partialTicks, clientWorld,
                    mc.options.renderDistance,
                    mc.gameRenderer.getDarkenWorldAmount(partialTicks));
            FogRenderer.setupFog(ari, FogRenderer.FogType.FOG_TERRAIN,
                    Math.max(32.0F, mc.options.renderDistance * 16.0F), false, partialTicks);
        }

        RenderSystem.disableCull();

        CoroUtilBlockLightCache.brightnessPlayer = CoroUtilBlockLightCache.getBrightnessFromLightmap(
                mc.level, (float) entityIn.getX(), (float) entityIn.getY(), (float) entityIn.getZ());

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        mip_min = 0;
        mip_mag = 0;

        if (!ConfigCoroUtilLegacy.disableMipmapFix) {
            mc.textureManager.bind(BLOCK_ATLAS);
            mip_min = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
            mip_mag = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public static void postShaderRender(MatrixStack matrixStack) {
        // restore depth mask otherwise we'll see rendering issues, eg block entitiy models leaking
        RenderSystem.depthMask(true);

        if (!ConfigCoroUtilLegacy.disableMipmapFix && mip_min != 0 && mip_mag != 0) {
            Minecraft.getInstance().textureManager.bind(BLOCK_ATLAS);
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mip_min);
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mip_mag);
        }

        RenderSystem.enableCull();
        FogRenderer.setupNoFog();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.popMatrix();

        matrixStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isPaused() {
        return Minecraft.getInstance().isPaused();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void tickRenderScreen(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickShaderTest();
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void tickClient(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {

            Minecraft mc = Minecraft.getInstance();

            if (mc.level != null) {
                if (!isPaused()) {
                    ExtendedRenderer.rotEffRenderer.updateEffects();

                    boolean lightningActive = mc.level.isThundering();

                    if (mc.level.getGameTime() % 2 == 0 || lightningActive != lastLightningBoltLightState) {
                        CoroUtilBlockLightCache.clear();
                    }

                    lastLightningBoltLightState = lightningActive;
                }
            }

            if (ConfigCoroUtilLegacy.foliageShaders != foliageUseLast) {
                foliageUseLast = ConfigCoroUtilLegacy.foliageShaders;
                flagFoliageUpdate = true;
            }

            if (flagFoliageUpdate) {
                CULog.dbg("CoroUtilLegacy detected a need to reload resource packs, initiating");
                flagFoliageUpdate = false;
                mc.reloadResourcePacks();
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void worldRender(RenderWorldLastEvent event) {
        if (!ConfigCoroUtilLegacy.useEntityRenderHookForShaders) {
            EventHandler.hookRenderShaders(event.getPartialTicks(), event.getMatrixStack());
        }
    }

    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(
            modid = ExtendedRenderer.modid,
            bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static class ModBusEvents {

        @SubscribeEvent
        public static void registerIcons(TextureStitchEvent.Pre event) {
            ParticleRegistry.init(event);
        }

        @SubscribeEvent
        public static void registerIconsPost(TextureStitchEvent.Post event) {
            ParticleRegistry.initPost(event);
            if (event.getMap().location().equals(BLOCK_ATLAS)) {
                FoliageEnhancerShader.setupReplacers(event.getMap());
            }
        }

        @SubscribeEvent
        public static void modelBake(ModelBakeEvent event) {
            FoliageEnhancerShader.modelBakeEvent(event);
        }
    }
}