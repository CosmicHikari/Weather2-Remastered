package net.corosus.extendedrenderer.particle.behavior;

import net.corosus.coroutillegacy.util.Vec3;
import net.corosus.extendedrenderer.particle.entity.EntityRotFX;

import java.util.Random;

public class ParticleBehaviorTrail extends ParticleBehaviors {

    public ParticleBehaviorTrail(Vec3 source) {
        super(source);
    }


    public EntityRotFX initParticle(EntityRotFX particle) {
        super.initParticle(particle);

        particle.rotationYaw = rand.nextInt(360);
        particle.setMaxAge(5 + rand.nextInt(10));
        particle.setGravity(0F);
        particle.setColor(72F / 255F, 239F / 255F, 8F / 255F);
        particle.setColor(0.6F + (rand.nextFloat() * 0.4F), 0.2F + (rand.nextFloat() * 0.7F), 0);
        particle.setScale(0.25F + 0.2F * rand.nextFloat());
        particle.brightness = 1F;
        particle.setScale(0.1F + rand.nextFloat() * 0.9F);
        particle.spawnY = (float) particle.getPosY();
        particle.setCanCollide(false);

        return particle;
    }

    @Override
    public void tickUpdateAct(EntityRotFX particle) {

        if (!particle.isAlive()) {
            particles.remove(particle);
        } else {
            double centerX = coordSource.xCoord + 0.0D;
            double centerY = coordSource.yCoord + 0.0D;
            double centerZ = coordSource.zCoord + 0.0D;

            double vecX = centerX - particle.getPosX();
            double vecY = centerY - particle.getPosY();
            double vecZ = centerZ - particle.getPosZ();
            Vec3 vec = new Vec3(vecX, vecY, vecZ);

            double radius = 1.6D;
            double amp = 0.005D * Math.max(1, (vec.lengthVector() * 0.01D));

            double rotSpeed = (particle.getAge()) * amp;


            Vec3 vec2 = new Vec3(vecX, vecY, vecZ);
            vec = vec.normalize();
            vec2 = vec2.normalize();

            vec = new Vec3(vec.xCoord * amp, vec.yCoord * amp, vec.zCoord * amp);

            Random rand = new Random();

            double randRange = 0.01D;

            particle.setMotionX(particle.getMotionX() + (rand.nextDouble() * randRange - randRange / 2));
            particle.setMotionY(particle.getMotionY() + (rand.nextDouble() * randRange - randRange / 2));
            particle.setMotionZ(particle.getMotionZ() + (rand.nextDouble() * randRange - randRange / 2));

            double rotYaw = (float) (Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
            double rotPitch = (float) (Math.atan2(vecY, vecX) * 180.0D / Math.PI);
            double rotRoll = (float) (Math.atan2(vecY, vecZ) * 180.0D / Math.PI);

            double rot = 30D;

            rotYaw -= rot;
            rotPitch -= rot;
            rotRoll -= rot;

            amp = 0.01D;
            double ampYaw = amp + amp * Math.sin(Math.toRadians(90 + ((particle.getWorld().getGameTime() * 5) % 180)));
            double ampPitch = 0.1D * amp * Math.min(10, particle.getAge() * 0.1D);
            double ampRoll = 0.1D * amp * Math.min(10, particle.getAge() * 0.1D);

        }
    }
}
