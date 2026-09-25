package net.corosus.extendedrenderer.foliage;

import net.corosus.extendedrenderer.particle.entity.ParticleTexLeafColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;

public class ParticleTallGrassTemp extends ParticleTexLeafColor {

    public int height = 0;

    public ParticleTallGrassTemp(ClientWorld worldIn, double posXIn, double posYIn, double posZIn,
                                 double mX, double mY, double mZ, TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
    }

    @Override
    public void tick() {
        super.tick();
        float windSpeed = 1F;
        this.rotationPitch = windSpeed * 60F;
        this.rotationPitch = (float) Math.toDegrees(Math.sin(this.getAge() * 0.1F) * 0.2F);
    }
}