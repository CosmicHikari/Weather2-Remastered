package net.corosus.extendedrenderer.particle.behavior;

import net.corosus.coroutillegacy.util.Vec3;
import net.corosus.extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class ParticleBehaviorSandstorm extends ParticleBehaviors {

    public int curTick = 0;
    public int ticksMax = 1;

    public ParticleBehaviorSandstorm(Vec3 source) {
        super(source);
    }

    public EntityRotFX initParticle(EntityRotFX particle) {
        super.initParticle(particle);

        particle.rotationYaw = rand.nextInt(360);
        particle.rotationPitch = rand.nextInt(50) - rand.nextInt(50);

        particle.setMaxAge(450 + rand.nextInt(10));
        float randFloat = (rand.nextFloat() * 0.6F);
        float baseBright = 0.7F;
        float finalBright = Math.min(1F, baseBright + randFloat);
        particle.setColor(finalBright, finalBright, finalBright);

        particle.brightness = 1F;
        particle.setAlphaF(1F);

        float sizeBase = (float) (30 + (rand.nextDouble() * 4));

        particle.setScale(sizeBase);
        particle.setCanCollide(true);

        particle.renderRange = 2048;

        particle.setFacePlayer(true);
        particle.setGravity(0.03F);

        return particle;
    }

    @Override
    public void tickUpdateAct(EntityRotFX particle) {

        if (!particle.isAlive()) {
            particles.remove(particle);
        } else {
            if (particle.getEntityId() % 2 == 0) {
                particle.rotationYaw -= 0.1;
            } else {
                particle.rotationYaw += 0.1;
            }

            float ticksFadeInMax = 10;
            float ticksFadeOutMax = 10;

            if (particle.getAge() < ticksFadeInMax) {
                particle.setAlphaF(Math.min(1F, particle.getAge() / ticksFadeInMax));
            } else if (particle.getAge() > particle.getMaxAge() - ticksFadeOutMax) {
                float count = particle.getAge() - (particle.getMaxAge() - ticksFadeOutMax);
                float val = (ticksFadeOutMax - (count)) / ticksFadeOutMax;
                particle.setAlphaF(val);
            } else {
            }


            double moveSpeed = 0.001D;

            BlockPos pos = new BlockPos(particle.getPosX(), particle.getPosY() - particle.aboveGroundHeight, particle.getPosZ());
            BlockState state = particle.getWorld().getBlockState(pos);
            if (!state.isAir()) {
                if (particle.getMotionY() < particle.bounceSpeedMax) {
                    particle.setMotionY(particle.getMotionY() + particle.bounceSpeed);
                }
            } else {
                double aheadMultiplier = 20D;
                BlockPos posAhead = new BlockPos(
                        particle.getPosX() + (particle.getMotionX() * aheadMultiplier),
                        particle.getPosY() - particle.aboveGroundHeight,
                        particle.getPosZ() + (particle.getMotionZ() * aheadMultiplier));
                BlockState stateAhead = particle.getWorld().getBlockState(posAhead);
                if (!stateAhead.isAir()) {
                    if (particle.getMotionY() < particle.bounceSpeedMaxAhead) {
                        particle.setMotionY(particle.getMotionY() + particle.bounceSpeedAhead);
                    }
                }
            }


            double moveSpeedRand = 0.005D;

            particle.setMotionX(particle.getMotionX() + (rand.nextDouble() * moveSpeedRand - rand.nextDouble() * moveSpeedRand));
            particle.setMotionZ(particle.getMotionZ() + (rand.nextDouble() * moveSpeedRand - rand.nextDouble() * moveSpeedRand));

            if (particle.spawnY != -1) {
                particle.setPosX(particle.getPosX());
                particle.setPosY(particle.spawnY);
                particle.setPosZ(particle.getPosZ());
            }

        }
    }
}
