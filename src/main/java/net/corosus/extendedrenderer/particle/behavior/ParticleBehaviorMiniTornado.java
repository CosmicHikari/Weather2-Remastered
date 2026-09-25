package net.corosus.extendedrenderer.particle.behavior;

import net.corosus.coroutillegacy.util.Vec3;
import net.corosus.extendedrenderer.particle.entity.EntityRotFX;

public class ParticleBehaviorMiniTornado extends ParticleBehaviors {

    public int curTick = 0;
    public int ticksMax = 1;

    public ParticleBehaviorMiniTornado(Vec3 source) {
        super(source);
    }

    public EntityRotFX initParticle(EntityRotFX particle) {
        super.initParticle(particle);

        particle.rotationYaw = rand.nextInt(360);
        particle.setMaxAge(1 + rand.nextInt(10));
        particle.setGravity(0F);
        particle.setColor(72F / 255F, 239F / 255F, 8F / 255F);
        particle.setColor(0.6F + (rand.nextFloat() * 0.4F), 0.2F + (rand.nextFloat() * 0.7F), 0);
        float greyScale = 0.5F + (rand.nextFloat() * 0.3F);
        particle.setColor(greyScale, greyScale, greyScale);

        particle.setScale(0.25F + 0.2F * rand.nextFloat());
        particle.brightness = 1F;
        particle.setScale(0.5F + rand.nextFloat() * 0.5F);
        particle.spawnY = (float) particle.getPosY();
        particle.setCanCollide(false);
        particle.isTransparent = false;

        particle.setMaxAge(100);

        return particle;
    }

    @Override
    public void tickUpdateAct(EntityRotFX particle) {

        if (!particle.isAlive()) {
            particles.remove(particle);
        } else {

            particle.setMotionX(0);
            particle.setMotionY(0);
            particle.setMotionZ(0);

            double x = particle.getPosX();
            double y = particle.getPosY();
            double z = particle.getPosZ();

            double age = particle.getAge();
            double ageOffset = age + particle.getEntityId();

            double yAdj = age * 0.01D;

            double ageScale;

            double distFromCenter = 0.2D + (yAdj * 0.3D);

            ageScale = (Math.PI / 45) * ageOffset * 3D;

            double timeAdj = Math.toRadians(particle.getWorld().getGameTime() % 360);

            double twistScale = 0.035D * Math.sin(timeAdj);

            double centerX = coordSource.xCoord;
            double centerZ = coordSource.zCoord;

            x = centerX + (Math.sin(ageScale) * distFromCenter);
            z = centerZ + (Math.cos(ageScale) * distFromCenter);

            particle.setPosX(x);
            particle.setPosY(coordSource.yCoord + yAdj);
            particle.setPosZ(z);

            double var16 = centerX - x;
            double var18 = centerZ - z;
            particle.rotationYaw = (float) Math.toDegrees(Math.atan2(var18, var16)) + 90;

        }
    }
}
