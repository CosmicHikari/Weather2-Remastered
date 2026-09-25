package net.mrbt0907.weather2.client.entity.particle;

import net.corosus.coroutillegacy.api.weather.IWindHandler;
import net.corosus.extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.util.Maths;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class EntityWaterfallFX extends EntityRotFX implements IWindHandler {
    public int age;
    public float brightness;

    public EntityWaterfallFX(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, double lifetimeMult, int colorType) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);

        this.xd = velocityX + (double) ((float) (Math.random() * 2.0D - 1.0D) * 0.05F);
        this.yd = velocityY + (double) ((float) (Math.random() * 2.0D - 1.0D) * 0.05F);
        this.zd = velocityZ + (double) ((float) (Math.random() * 2.0D - 1.0D) * 0.05F);

        Color colorObj = null;

        if (colorType == 0) {
            this.rCol = this.gCol = this.bCol = this.random.nextFloat() * 0.3F;
        } else if (colorType == 1) {
            colorObj = new Color(0xFF5000);
        } else if (colorType == 2) {
            colorObj = new Color(0x0000FF);
        } else if (colorType == 3) {
            colorObj = new Color(0x6666FF);
        } else if (colorType == 4) {
            colorObj = new Color(0xFFFFFF);
        } else if (colorType == 5) {
            colorObj = new Color(7951674);
        }

        this.brightness = 1.0F;

        if (colorObj != null && colorType != 0) {
            this.rCol = (float) colorObj.getRed() / 255.0F;
            this.gCol = (float) colorObj.getGreen() / 255.0F;
            this.bCol = (float) colorObj.getBlue() / 255.0F;
        }

        this.lifetime = 18;
        this.lifetime = (int) ((double) ((float) this.lifetime) * lifetimeMult);

        this.gravity = 0.2F;
        this.setScale(0.5F);
        setParticleTextureIndex(0);
    }


    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (rCol < 255) rCol += 0.01F;
        if (gCol < 255) gCol += 0.01F;
        if (bCol < 255) bCol += 0.01F;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        this.setParticleTextureIndex(7 - this.age * 8 / this.lifetime);

        BlockPos pos = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        BlockState state = this.level.getBlockState(pos);

        if (state.getMaterial() == Material.WATER) {
            Vector3d vec3 = Blocks.WATER.getFluidState(state).getFlow(level, pos);
            double dir = -1000;

            if (vec3.x != 0 && vec3.z != 0) {
                dir = Maths.fastATan2(vec3.z, vec3.x) - (Math.PI / 2D);
            }

            if (dir != -1000) {
                float speed = 0.005F;
                this.xd -= Maths.fastSin(dir) * speed;
                this.zd += Maths.fastCos(dir) * speed;
            }

            float range = 0.03F;
            this.xd += (random.nextFloat() * range) - (range / 2);
            this.zd += (random.nextFloat() * range) - (range / 2);

            if (!state.getFluidState().isSource()) {
                this.yd -= 0.05000000074505806D * this.gravity;
            } else {
                this.yd += (0.05F * this.gravity * 0.2F);
            }
        } else {
            this.yd -= 0.05000000074505806D * this.gravity * 1.5F;
        }

        if (this.yd > 0.03F) this.yd = 0.03F;

        float friction = 0.98F;
        this.xd *= friction;
        this.yd *= friction;
        this.zd *= friction;

        this.move(this.xd, this.yd, this.zd);

        if (state.getMaterial() == Material.WATER && yd > 0F) {
            float waterLevel = state.getFluidState().getHeight(level, pos);
            if (this.y > ((int) Math.floor(this.y)) + waterLevel) {
                this.yd = -0.05F;
            }
        }
    }

    @Override
    public float getWindWeight() {
        return 60F;
    }

    @Override
    public int getParticleDecayExtra() {
        return 0;
    }
}