package net.mrbt0907.weather2.client.rendering;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WeatherRenderTypes extends RenderState {
    public static final RenderType RADAR_BACKGROUND = RenderType.create(
            "weather2_radar_background",
            DefaultVertexFormats.POSITION_COLOR,
            7,
            256,
            RenderType.State.builder()
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false)
    );

    private WeatherRenderTypes(String name, Runnable setup, Runnable clear) {
        super(name, setup, clear);
    }

    public static RenderType radarIcon(ResourceLocation atlasTexture) {
        return RenderType.create(
                "weather2_radar_icon",
                DefaultVertexFormats.POSITION_TEX_COLOR,
                7,
                256,
                RenderType.State.builder()
                        .setTextureState(new RenderState.TextureState(atlasTexture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .setAlphaState(DEFAULT_ALPHA)
                        .setCullState(NO_CULL)
                        .createCompositeState(false)
        );
    }
}