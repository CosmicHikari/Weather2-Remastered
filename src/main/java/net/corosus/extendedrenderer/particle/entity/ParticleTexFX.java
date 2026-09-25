package net.corosus.extendedrenderer.particle.entity;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;

public class ParticleTexFX extends EntityRotFX {

    public ParticleTexFX(ClientWorld worldIn,
                         double posXIn, double posYIn, double posZIn,
                         double mX, double mY, double mZ,
                         TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY - 0.5, mZ);

        this.setParticleTexture(par8Item);

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;

        this.gravity = 1.0F;

        this.particleScale = 1.0F;

        this.setMaxAge(100);
        this.setCanCollide(false);
    }

    public float getGravity() {
        return this.gravity;
    }
}