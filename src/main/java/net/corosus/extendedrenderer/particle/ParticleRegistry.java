package net.corosus.extendedrenderer.particle;

import net.corosus.extendedrenderer.ExtendedRenderer;
import net.corosus.extendedrenderer.render.RotatingParticleManager;
import net.corosus.extendedrenderer.shader.MeshBufferManagerFoliage;
import net.corosus.extendedrenderer.shader.MeshBufferManagerParticle;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import java.util.ArrayList;
import java.util.List;

public class ParticleRegistry {

    private static final ResourceLocation BLOCK_ATLAS = AtlasTexture.LOCATION_BLOCKS;

    public static TextureAtlasSprite squareGrey;
    public static TextureAtlasSprite smoke;
    public static TextureAtlasSprite smokeTest;
    public static TextureAtlasSprite cloud;
    public static TextureAtlasSprite cloud256;
    public static TextureAtlasSprite cloud256_fire;
    public static TextureAtlasSprite cloud256_test;
    public static TextureAtlasSprite cloud256_2;
    public static TextureAtlasSprite cloud256_6;
    public static TextureAtlasSprite downfall2;
    public static TextureAtlasSprite downfall3;
    public static TextureAtlasSprite downfall4;
    public static TextureAtlasSprite cloud256_7;
    public static TextureAtlasSprite chicken;
    public static TextureAtlasSprite potato;
    public static TextureAtlasSprite leaf;
    public static TextureAtlasSprite rain;
    public static TextureAtlasSprite rain_white;
    public static TextureAtlasSprite rain_white_trans;
    public static TextureAtlasSprite rain_white_2;
    public static TextureAtlasSprite snow;
    public static TextureAtlasSprite cloud256dark;
    public static TextureAtlasSprite cloudDownfall;
    public static TextureAtlasSprite tumbleweed;
    public static TextureAtlasSprite debris_1;
    public static TextureAtlasSprite debris_2;
    public static TextureAtlasSprite debris_3;
    public static TextureAtlasSprite test_texture;
    public static TextureAtlasSprite white_square;
    public static List<TextureAtlasSprite> listFish = new ArrayList<>();
    public static List<TextureAtlasSprite> listSeaweed = new ArrayList<>();
    public static TextureAtlasSprite grass;

    public static void init(TextureStitchEvent.Pre event) {

        if (!event.getMap().location().equals(BLOCK_ATLAS))
            return;


        MeshBufferManagerParticle.clearMapOnly();
        MeshBufferManagerFoliage.clearMapOnly();

        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_fire"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_test"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_6"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall3"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/chicken"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/potato"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/leaf"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/test_texture"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white_square"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain_white"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/snow"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/tumbleweed"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_1"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_2"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_3"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/grass"));
        event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/smoke"));
    }

    public static void initPost(TextureStitchEvent.Post event) {

        if (!event.getMap().location().equals(BLOCK_ATLAS))
            return;

        squareGrey = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white"));
        cloud256 = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256"));
        cloud256_fire = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_fire"));
        cloud256_test = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_test"));
        cloud256_6 = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_6"));
        downfall3 = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall3"));
        chicken = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/chicken"));
        potato = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/potato"));
        leaf = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/leaf"));
        test_texture = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/test_texture"));
        white_square = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white_square"));
        rain_white = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain_white"));
        snow = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/snow"));
        tumbleweed = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/tumbleweed"));
        debris_1 = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_1"));
        debris_2 = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_2"));
        debris_3 = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_3"));
        grass = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/grass"));
        smoke = event.getMap().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/smoke"));


        RotatingParticleManager.forceShaderReset = true;
    }

}