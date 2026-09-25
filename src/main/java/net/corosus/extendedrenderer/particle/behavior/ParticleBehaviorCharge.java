package net.corosus.extendedrenderer.particle.behavior;

import net.corosus.coroutillegacy.util.Vec3;
import net.corosus.extendedrenderer.particle.entity.EntityRotFX;

public class ParticleBehaviorCharge extends ParticleBehaviors {

    public int curTick = 0;
    public int ticksMax = 1;

    public ParticleBehaviorCharge(Vec3 source) {
        super(source);
    }

    public EntityRotFX initParticle(EntityRotFX particle) {
        super.initParticle(particle);

        particle.rotationYaw = rand.nextInt(360);
        particle.setMaxAge(1 + rand.nextInt(10));
        particle.setGravity(0F);
        particle.setColor(72F / 255F, 239F / 255F, 8F / 255F);
        particle.setColor(0.6F + (rand.nextFloat() * 0.4F), 0.2F + (rand.nextFloat() * 0.7F), 0);
        particle.setScale(0.25F + 0.2F * rand.nextFloat());
        particle.brightness = 1F;
        particle.setScale(0.1F + rand.nextFloat() * 0.5F);
        particle.spawnY = (float) particle.getPosY();
        particle.setCanCollide(false);

        return particle;
    }

    @Override
    public void tickUpdateAct(EntityRotFX particle) {

        if (curTick == 0 || !particle.isAlive()) {
            particles.remove(particle);
        } else {
            double centerX = coordSource.xCoord + 0.0D;
            double centerY = coordSource.yCoord + 0.5D;
            double centerZ = coordSource.zCoord + 0.0D;

            double vecX = centerX - particle.getPosX();
            double vecZ = centerZ - particle.getPosZ();
            double rotYaw = (float) (Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
            rotYaw -= 75D;
            double speed = 0.01D + (0.50D * curTick / ticksMax);
            particle.setMotionX(Math.cos(rotYaw * 0.017453D) * speed);
            particle.setMotionZ(Math.sin(rotYaw * 0.017453D) * speed);
            int cycle = 60;

            if (curTick + 20 < ticksMax) {
                if (particle.getAge() % cycle < cycle / 2) {
                    particle.setGravity(-0.02F);
                } else {
                    particle.setGravity(0.09F);
                }
            } else {
                if (particle.getPosY() > coordSource.yCoord + 1D) {
                    particle.setGravity(0.15F);
                } else {
                    particle.setGravity(-0.15F);
                }

            }
        }
    }
}
