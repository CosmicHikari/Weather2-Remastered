package net.mrbt0907.weather2.client.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.text.TextFormatting;
import net.mrbt0907.weather2.api.weather.IWeatherRain;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.block.tile.TileRadar;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.rendering.WeatherRenderTypes;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.config.ConfigFront;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.registry.ParticleRegistry;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.weather.WeatherManagerServer;
import net.mrbt0907.weather2.weather.storm.StormObject;

public class RenderRadar extends TileEntityRenderer<TileEntity> {
    private static final RenderType ICON_RENDER_TYPE = WeatherRenderTypes.radarIcon(AtlasTexture.LOCATION_BLOCKS);

    public RenderRadar(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TileEntity tile, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        TileRadar radar = (TileRadar) tile;

        matrix.pushPose();
        matrix.translate(0.5D, 1.1D, 0.5D);

        IVertexBuilder bgBuilder = buffer.getBuffer(WeatherRenderTypes.RADAR_BACKGROUND);
        Matrix4f pose = matrix.last().pose();
        float r = radar.renderRange - 0.5F;
        bgBuilder.vertex(pose, -r, 0.002f, -r).color(0f, 0f, 0f, 0.25f).endVertex();
        bgBuilder.vertex(pose, -r, 0.002f, r).color(0f, 0f, 0f, 0.25f).endVertex();
        bgBuilder.vertex(pose, r, 0.002f, r).color(0f, 0f, 0f, 0.25f).endVertex();
        bgBuilder.vertex(pose, r, 0.002f, -r).color(0f, 0f, 0f, 0.25f).endVertex();

        matrix.popPose();

        float playerViewY = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();

        renderLivingLabel("\u00A7" + '6' + "|", matrix, buffer, 0.5D, 1.2D, 0.5D, 1, 10, 10, playerViewY, 1.0F);

        if (ConfigMisc.debug_mode_radar) {
            net.minecraft.entity.player.PlayerEntity player = Minecraft.getInstance().player;
            if (player != null && ClientTickHandler.weatherManager != null) {
                NewSceneEnhancer scene = NewSceneEnhancer.instance();
                WeatherManagerClient wm = ClientTickHandler.weatherManager;
                float precipStr = Math.abs(scene.rain);
                String overcast = Math.round(scene.overcast * 100.0F) + "%";

                renderLivingLabel("\u00A7" + " Fog Strength: " + Maths.clamp(Math.round((scene.fogDensity * 400.0F) * scene.rain), 0, 100) + "%", matrix, buffer, 0.5D, 1.8D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                renderLivingLabel("\u00A7" + " Vanilla Weather Time: " + wm.weatherRainTime, matrix, buffer, 0.5D, 1.9D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                renderLivingLabel("\u00A7" + " Client Weather: " + (player.level.isThundering() ? "Thundering" : precipStr >= 0.5F ? "Heavy Rain" : precipStr >= 0.15F ? "Light Rain" : precipStr > 0.01F ? "Drizzle" : "Clear"), matrix, buffer, 0.5D, 2.0D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                renderLivingLabel("\u00A7" + " Server Weather: " + (wm.weatherID == 2 ? "Thunder" : wm.weatherID == 1 ? "Rain" : "Clear"), matrix, buffer, 0.5D, 2.1D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                renderLivingLabel("\u00A7" + " Precipitation Strength: " + Math.round(precipStr * 100.0F) + "%", matrix, buffer, 0.5D, 2.2D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                renderLivingLabel("\u00A7" + " Overcast Strength: " + overcast, matrix, buffer, 0.5D, 2.3D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                renderLivingLabel("\u00A7" + " Today's Storm Probability: " + WeatherManagerServer.stormChanceToday + "%", matrix, buffer, 0.5D, 1.7D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                renderLivingLabel("\u00A7" + " -------------------------", matrix, buffer, 0.5D, 2.4D, 0.5D, 1, 10, 10, playerViewY, 1.0F);

                if (radar.system != null && radar.system instanceof StormObject) {
                    StormObject system = (StormObject) radar.system;
                    renderLivingLabel("\u00A7" + " Rain/Hail: " + Maths.clamp(Math.round((system.rain - IWeatherRain.MINIMUM_DRIZZLE) * 10.0F / 3.0F) * 0.1F, 0.0F, 100.0F) + "%/" + Maths.clamp(system.hail - 100.0F, 0.0F, 100.0F) + "%", matrix, buffer, 0.5D, 2.5D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                    renderLivingLabel("\u00A7" + " Stage Complete: " + (((system.intensity - system.stage + 1)) * 100.0F) + "%", matrix, buffer, 0.5D, 2.6D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                    renderLivingLabel("\u00A7" + " Current Funnel Wind Speed: " + (long) WeatherUtil.toMph(system.windSpeed) + " MPH", matrix, buffer, 0.5D, 2.7D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                    renderLivingLabel("\u00A7" + " Current Funnel Size: " + (long) system.funnelSize + " Blocks", matrix, buffer, 0.5D, 2.8D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                    renderLivingLabel("\u00A7" + " Current Stage/MaxStage: " + system.stage + "/" + system.stageMax, matrix, buffer, 0.5D, 2.9D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                    renderLivingLabel("\u00A7" + " Lifespan Multiplier: " + ((ConfigStorm.storm_lifespan_min / system.intensityRate) * 1000.0F) + "%", matrix, buffer, 0.5D, 3.0D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                    renderLivingLabel("\u00A7" + " Size Multiplier: " + (system.sizeRate * 100.0F) + "%", matrix, buffer, 0.5D, 3.1D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                    renderLivingLabel("\u00A7" + " Is Violent: " + system.isViolent, matrix, buffer, 0.5D, 3.2D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                    renderLivingLabel("\u00A7" + " UUID: " + system.getUUID(), matrix, buffer, 0.5D, 3.3D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                    String stage = radar.system.getName();
                    if (system.isDying)
                        stage += "  (Dying)";
                    renderLivingLabel("\u00A7" + " " + TextFormatting.BOLD + stage, matrix, buffer, 0.5D, 3.4D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
                }

                renderLivingLabel("\u00A7" + " Radar Tier " + radar.getTier() + " (R:" + radar.pingRange + ")", matrix, buffer, 0.5D, 3.5D, 0.5D, 1, 10, 10, playerViewY, 1.0F);
            }
        }

        BlockPos pos = radar.getBlockPos();
        radar.systems.forEach(so ->
        {
            matrix.pushPose();

            Vec3 posRenderOffset = so.pos.copy();
            posRenderOffset.posX -= pos.getX();
            posRenderOffset.posZ -= pos.getZ();
            posRenderOffset.posX /= radar.pingRange;
            posRenderOffset.posZ /= radar.pingRange;
            posRenderOffset.posX *= radar.renderRange - 0.5D;
            posRenderOffset.posZ *= radar.renderRange - 0.5D;

            matrix.translate(posRenderOffset.posX, 0, posRenderOffset.posZ);

            if (radar.showRating) {
                FontRenderer font = Minecraft.getInstance().font;
                renderLivingLabel(so.type == 0 && !ConfigFront.ShowFrontsOnRadar ? "" : so.typeName, matrix, buffer, 0.5D, so.type == 0 ? 1.54D : 1.5D, 0.5D, 1, font.width(so.typeName), 5, playerViewY, radar.renderAlpha);
            }

            if (so.type == 1 || so.type == 2) {
                switch (so.stage) {
                    case 0:
                        renderIconNew(matrix, buffer, 0.5D, 1.4D, 0.5D, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha * 0.5F, ParticleRegistry.radarIconCloud);
                        break;
                    case 1:
                        if (so.isRaining)
                            if (so.name.toLowerCase().contains("snowstorm"))
                                renderIconNew(matrix, buffer, 0.5D, 1.4D, 0.5D, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconSnow);
                            else
                                renderIconNew(matrix, buffer, 0.5D, 1.4D, 0.5D, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconRain);
                        else
                            renderIconNew(matrix, buffer, 0.5D, 1.4D, 0.5D, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconCloud);
                        break;
                    case 2:
                        renderIconNew(matrix, buffer, 0.5D, 1.4D, 0.5D, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconLightning);
                        break;
                    case 3:
                        renderIconNew(matrix, buffer, 0.5D, 1.4D, 0.5D, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconLightning);
                        renderIconNew(matrix, buffer, 0.5D, 1.4D, 0.5D, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconWind);
                        break;
                    default:
                        if (so.type == 1)
                            renderIconNew(matrix, buffer, 0.5D, 1.4D, 0.5D, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconTornado);
                        else
                            renderIconNew(matrix, buffer, 0.5D, 1.4D, 0.5D, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconCyclone);
                }

                if (so.isHailing)
                    renderIconNew(matrix, buffer, 0.5D, 1.4D, 0.5D, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconHail);

                if (ConfigMisc.debug_mode_radar && radar.system != null && so.uuid.equals(radar.system.getUUID()))
                    renderLivingLabel(TextFormatting.GOLD + "" + TextFormatting.BOLD + "|", matrix, buffer, 0.5D, 1.2D, 0.5D, 1, 5, 5, playerViewY, radar.renderAlpha);
                else {
                    if (so.stage == Stage.NORMAL.getStage())
                        renderLivingLabel(TextFormatting.GRAY + "|", matrix, buffer, 0.5D, 1.2D, 0.5D, 1, 5, 5, playerViewY, radar.renderAlpha * 0.35F);
                    else if (so.isDying)
                        renderLivingLabel(TextFormatting.RED + "|", matrix, buffer, 0.5D, 1.2D, 0.5D, 1, 5, 5, playerViewY, radar.renderAlpha);
                    else
                        renderLivingLabel(TextFormatting.GREEN + "|", matrix, buffer, 0.5D, 1.2D, 0.5D, 1, 5, 5, playerViewY, radar.renderAlpha);
                }
            } else if (so.type == 0 && ConfigFront.ShowFrontsOnRadar) {
                int type = so.name.toLowerCase().contains("stationary") ? 0 : so.name.toLowerCase().contains("warm") ? 2 : so.name.toLowerCase().contains("cold") ? 1 : 3;
                renderIconNew(matrix, buffer, 0.5D, 1.12D, 0.5D, (int) (64 * radar.renderRange), (int) (64 * radar.renderRange), 90.0F, 0.0F, so.angle, radar.renderAlpha,
                        type == 0 ? ParticleRegistry.radarIconStationaryFront :
                                type == 1 ? ParticleRegistry.radarIconColdFront :
                                        type == 2 ? ParticleRegistry.radarIconWarmFront :
                                                ParticleRegistry.radarIconOccludedFront);
                if (!so.isDying)
                    renderLivingLabel(TextFormatting.BOLD + "" + TextFormatting.DARK_GREEN + "|", matrix, buffer, 0.5D, 1.22D, 0.5D, 1, 5, 5, playerViewY, radar.renderAlpha);
                else
                    renderLivingLabel(TextFormatting.BOLD + "" + TextFormatting.DARK_RED + "|", matrix, buffer, 0.5D, 1.22D, 0.5D, 1, 5, 5, playerViewY, radar.renderAlpha);
            } else if (so.type > 0) {
                renderIconNew(matrix, buffer, 0.5D, 1.4D, 0.5D, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconSandstorm);
                if (!so.isDying)
                    renderLivingLabel("\u00A7" + '2' + "|", matrix, buffer, 0.5D, 1.2D, 0.5D, 1, 5, 5, playerViewY, radar.renderAlpha);
                else
                    renderLivingLabel("\u00A7" + '4' + "|", matrix, buffer, 0.5D, 1.2D, 0.5D, 1, 5, 5, playerViewY, radar.renderAlpha);
            }

            matrix.translate(-posRenderOffset.posX, 0, -posRenderOffset.posZ);
            matrix.popPose();
        });
    }

    protected void renderLivingLabel(String text, MatrixStack matrix, IRenderTypeBuffer buffer, double x, double y, double z, int par9, float width, float height, float angle, float alpha) {
        if (alpha <= 0.01F || text.isEmpty()) return;

        int hexAlpha = (int) (255 * alpha) << 24;
        int color = 0xFFFFFF + hexAlpha;
        int borderSize = 2;

        FontRenderer font = Minecraft.getInstance().font;
        float scale = 0.6F;
        float var13 = 0.016666668F * scale;

        matrix.pushPose();
        matrix.translate(x, y, z);
        matrix.mulPose(new Quaternion(0, -angle, 0, true));
        matrix.scale(-var13, -var13, var13);

        Matrix4f pose = matrix.last().pose();

        if (par9 == 0) {
            IVertexBuilder bgBuilder = buffer.getBuffer(WeatherRenderTypes.RADAR_BACKGROUND);
            bgBuilder.vertex(pose, -width / 2 - borderSize, -borderSize, 0).color(0f, 0f, 0f, 0.25f).endVertex();
            bgBuilder.vertex(pose, -width / 2 - borderSize, height, 0).color(0f, 0f, 0f, 0.25f).endVertex();
            bgBuilder.vertex(pose, width / 2 + borderSize, height, 0).color(0f, 0f, 0f, 0.25f).endVertex();
            bgBuilder.vertex(pose, width / 2 + borderSize, -borderSize, 0).color(0f, 0f, 0f, 0.25f).endVertex();
        }

        font.drawInBatch(text, -width / 2 + borderSize, 0, color, false, pose, buffer, false, 0, 0xF000F0);

        matrix.popPose();
    }

    public void renderIconNew(MatrixStack matrix, IRenderTypeBuffer buffer, double x, double y, double z, int width, int height, float angleX, float angleY, float angleZ, float alpha, TextureAtlasSprite sprite) {
        if (alpha <= 0.01F) return;

        float f6 = sprite.getU0();
        float f7 = sprite.getU1();
        float f9 = sprite.getV0();
        float f8 = sprite.getV1();

        float scale = 0.6F;
        float var13 = 0.016666668F * scale;
        int borderSize = 2;

        matrix.pushPose();
        matrix.translate(x, y, z);
        matrix.mulPose(new Quaternion(angleX, 0, 0, true));
        matrix.mulPose(new Quaternion(0, -angleY, 0, true));
        matrix.mulPose(new Quaternion(0, 0, angleZ, true));
        matrix.scale(-var13, -var13, var13);

        Matrix4f pose = matrix.last().pose();

        IVertexBuilder builder = buffer.getBuffer(ICON_RENDER_TYPE);
        builder.vertex(pose, -width / 2 - borderSize, -borderSize, 0).uv(f6, f9).color(1f, 1f, 1f, alpha).endVertex();
        builder.vertex(pose, (float) -width / 2 - borderSize, height, 0).uv(f6, f8).color(1f, 1f, 1f, alpha).endVertex();
        builder.vertex(pose, width / 2 + borderSize, height, 0).uv(f7, f8).color(1f, 1f, 1f, alpha).endVertex();
        builder.vertex(pose, width / 2 + borderSize, -borderSize, 0).uv(f7, f9).color(1f, 1f, 1f, alpha).endVertex();

        matrix.popPose();
    }
}