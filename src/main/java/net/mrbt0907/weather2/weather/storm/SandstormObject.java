package net.mrbt0907.weather2.weather.storm;

import net.corosus.extendedrenderer.particle.ParticleRegistry;
import net.corosus.extendedrenderer.particle.behavior.ParticleBehaviorSandstorm;
import net.corosus.extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.client.entity.particle.ParticleSandstorm;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigSand;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.util.CachedNBTTagCompound;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.weather.WeatherManager;
import net.mrbt0907.weather2.weather.WindManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class SandstormObject extends WeatherObject {

    public int height = 0;

    public Vec3 posSpawn = new Vec3(0, 0, 0);
    public float angle = 0.0F;

    @OnlyIn(Dist.CLIENT)
    public List<EntityRotFX> listParticlesCloud;

    public ParticleBehaviorSandstorm particleBehavior;

    public int age = 0;
    public int sizePeak = 1;
    public int ageFadeout = 0;
    public int ageFadeoutMax = 20 * 60 * 5;
    public boolean isFrontGrowing = true;
    public Random rand = new Random();
    private float maxSize = 100.0F;

    public SandstormObject(WeatherManager parManager) {
        super(parManager.getGlobalFront());

        this.type = WeatherEnum.Type.SANDSTORM;

        if (parManager.getWorld().isClientSide) {
            listParticlesCloud = new ArrayList<EntityRotFX>();

        }
    }

    public static boolean isDesert(Biome biome) {
        return isDesert(biome, false);
    }

    public static boolean isDesert(Biome biome, boolean forSpawn) {
        ResourceLocation biomeId = biome.getRegistryName();

        return biomeId.equals(Biomes.DESERT.location()) ||
                biomeId.equals(Biomes.DESERT_HILLS.location()) ||
                (!forSpawn && biomeId.equals(Biomes.RIVER.location())) ||
                (biomeId.toString().contains("desert") && biome.getBaseTemperature() >= 2.0F);
    }

    public void initSandstormSpawn(Vec3 pos) {
        this.pos = pos.copy();

        size = 1;
        sizePeak = 1;
        maxSize = 100;


        World world = manager.getWorld();
        int yy = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(pos.posX, 0, pos.posZ)).getY();
        pos.posY = yy;

        posGround = pos.copy();

        this.posSpawn = this.pos.copy();


    }

    public float getSandstormScale() {
        if (isFrontGrowing) {
            return (float) size / maxSize;
        } else {
            return 1F - ((float) ageFadeout / (float) ageFadeoutMax);
        }
    }

    public void tickProgressionAndMovement() {

        World world = manager.getWorld();
        WindManager windMan = manager.windManager;

        angle = windMan.windAngle;
        float speedWind = windMan.windSpeed;


        if (!world.isClientSide) {
            age++;


            BlockPos posBlock = pos.toBlockPos();


            if (isFrontGrowing && world.isLoaded(posBlock)) {
                Biome biomeIn = world.getBiome(posBlock);

                isFrontGrowing = isDesert(biomeIn);
            } else {
                isFrontGrowing = false;
            }

            int sizeAdjRate = 10;

            if (isFrontGrowing) {
                if (world.getGameTime() % sizeAdjRate == 0) {
                    if (size < maxSize) {
                        size++;

                    }
                }
            } else {
                if (world.getGameTime() % sizeAdjRate == 0) {
                    if (size > 0) {
                        size--;

                    }
                }


                if (ageFadeout < ageFadeoutMax) {
                    ageFadeout++;
                } else {

                    this.setDead();
                }
            }

            if (size > sizePeak) {
                sizePeak = size;
            }

        }


        double vecX = -Maths.fastSin(Math.toRadians(angle));
        double vecZ = Maths.fastCos(Math.toRadians(angle));
        double speed = speedWind * 0.3D;


        if (size > 0) {
            this.pos.posX += vecX * speed;
            this.pos.posZ += vecZ * speed;
        }


        int yy = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(pos.posX, 0, pos.posZ)).getY();

        this.pos.posY = yy + 1;
    }

    @SuppressWarnings("unused")
    public void tickBlockSandBuildup() {

        World world = manager.getWorld();
        WindManager windMan = manager.windManager;

        float angle = windMan.windAngle;


        int delay = ConfigSand.buildup_tick_delay;
        int loop = (int) ((float) ConfigSand.max_buildup_loop_ammount * getSandstormScale());

        int count = 0;


        if (!world.isClientSide) {
            if (world.getGameTime() % delay == 0) {

                for (int i = 0; i < loop; i++) {


                    if (rand.nextDouble() >= getSandstormScale()) continue;

                    Vec3 vecPos = getRandomPosInSandstorm();

                    int y = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(vecPos.posX, 0, vecPos.posZ)).getY();
                    vecPos.posY = y;


                    if (!world.isLoaded(vecPos.toBlockPos())) continue;

                    Biome biomeIn = world.getBiome(vecPos.toBlockPos());

                    if (ConfigSand.enable_buildup_outside_desert || isDesert(biomeIn)) {
                        WeatherUtilBlock.fillAgainstWallSmoothly(world, vecPos, angle, 15, 2, BlockRegistry.sand_layer.get());
                    }

                    count++;


                }


            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (manager == null) {
            System.out.println("WeatherManager is null for " + this + ", why!!!");
            return;
        }

        World world = manager.getWorld();


        if (world == null) {
            System.out.println("world is null for " + this + ", why!!!");
            return;
        }

        if (WeatherUtil.isPausedSideSafe(world)) return;


        tickProgressionAndMovement();

        int yy = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(pos.posX, 0, pos.posZ)).getY();


        if (world.isClientSide) {
            tickClient();
        }


        if (getSandstormScale() > 0.2D) {
            tickBlockSandBuildup();
        }

        this.posGround.posX = pos.posX;
        this.posGround.posY = yy;
        this.posGround.posZ = pos.posZ;

    }

    @OnlyIn(Dist.CLIENT)
    public void tickClient() {
        Minecraft mc = Minecraft.getInstance();
        World world = manager.getWorld();
        WindManager windMan = manager.windManager;

        if (particleBehavior == null) {
            particleBehavior = new ParticleBehaviorSandstorm(pos.toVec3Coro());
        }

        double distBetweenParticles = 3;


        Random rand = mc.level.random;

        this.height = this.size / 4;
        int heightLayers = Math.max(1, this.height / (int) distBetweenParticles);


        double distFromSpawn = this.posSpawn.distanceSq(this.pos);

        double xVec = this.posSpawn.posX - this.pos.posX;
        double zVec = this.posSpawn.posZ - this.pos.posZ;

        double directionAngle = Maths.fastATan2(zVec, xVec);


        double directionAngleDeg = Math.toDegrees(directionAngle);

        int spawnedThisTick = 0;


        float sandstormScale = getSandstormScale();

        double sandstormParticleRateDust = ConfigClient.sandstorm_dust_particle_rate;
        if (size > 0) {
            for (int heightLayer = 0; heightLayer < heightLayers && spawnedThisTick < 500; heightLayer++) {


                double i = directionAngleDeg + (rand.nextDouble() * 180D);
                if ((mc.level.getGameTime()) % 2 == 0) {

                    if (rand.nextDouble() >= sandstormParticleRateDust) continue;

                    double sizeSub = heightLayer * 2D;
                    double sizeDyn = size - sizeSub;
                    double inwardsAdj = rand.nextDouble() * 5D;

                    double sizeRand = (sizeDyn + -inwardsAdj);
                    double x = pos.posX + (-Maths.fastSin(Math.toRadians(i)) * (sizeRand));
                    double z = pos.posZ + (Maths.fastCos(Math.toRadians(i)) * (sizeRand));
                    double y = pos.posY + (heightLayer * distBetweenParticles * 2);

                    TextureAtlasSprite sprite = ParticleRegistry.cloud256;
                    if (WeatherUtil.isAprilFoolsDay()) {
                        sprite = ParticleRegistry.chicken;
                    }

                    ParticleSandstorm part = new ParticleSandstorm(mc.level, x, y, z
                            , 0, 0, 0, sprite);
                    particleBehavior.initParticle(part);

                    part.angleToStorm = i;
                    part.distAdj = sizeRand;
                    part.heightLayer = heightLayer;
                    part.lockPosition = true;

                    part.setFacePlayer(false);
                    part.isTransparent = true;
                    part.rotationYaw = (float) i + rand.nextInt(20) - 10;
                    part.rotationPitch = 0;
                    part.setMaxAge(300);
                    part.setGravity(0.09F);
                    part.setAlphaF(1F);
                    float brightnessMulti = 1F - (rand.nextFloat() * 0.5F);
                    part.setColor(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
                    part.setScale(100);


                    part.setKillOnCollide(true);
                    part.renderOrder = 0;
                    particleBehavior.particles.add(part);
                    part.spawnAsWeatherEffect();

                    spawnedThisTick++;


                }

            }
        }


        if (spawnedThisTick > 0) {

            spawnedThisTick = 0;
        }

        if ((mc.level.getGameTime()) % 20 == 0) {

        }


        double spawnAngle = Maths.fastATan2(this.sizePeak, distFromSpawn);


        spawnAngle *= 1.2D;

        double spawnDistInc = 10;

        double extraDistSpawnIntoWall = sizePeak / 2D;


        if ((mc.level.getGameTime()) % 3 == 0) {


            for (double spawnDistTick = 0; spawnDistTick < distFromSpawn + (extraDistSpawnIntoWall) && spawnedThisTick < 500; spawnDistTick += spawnDistInc) {


                if (rand.nextDouble() >= sandstormScale) continue;

                if (rand.nextDouble() >= sandstormParticleRateDust) continue;


                double randAngle = directionAngle + (Math.PI / 2D) - (spawnAngle) + (rand.nextDouble() * spawnAngle * 2D);

                double randHeight = (spawnDistTick / distFromSpawn) * height * 1.2D * rand.nextDouble();


                double x = posSpawn.posX + (-Maths.fastSin(randAngle) * (spawnDistTick));
                double z = posSpawn.posZ + (Maths.fastCos(randAngle) * (spawnDistTick));


                x += (rand.nextDouble() - rand.nextDouble()) * 30D;
                z += (rand.nextDouble() - rand.nextDouble()) * 30D;

                int yy = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(x, 0, z)).getY();
                double y = yy + 2 + randHeight;

                TextureAtlasSprite sprite = ParticleRegistry.cloud256;
                if (WeatherUtil.isAprilFoolsDay()) {
                    sprite = ParticleRegistry.chicken;
                }

                ParticleSandstorm part = new ParticleSandstorm(mc.level, x, y, z
                        , 0, 0, 0, sprite);
                particleBehavior.initParticle(part);

                part.setFacePlayer(false);
                part.isTransparent = true;
                part.rotationYaw = (float) rand.nextInt(360);
                part.rotationPitch = (float) rand.nextInt(360);
                part.setMaxAge(100);
                part.setGravity(0.09F);
                part.setAlphaF(1F);
                float brightnessMulti = 1F - (rand.nextFloat() * 0.5F);
                part.setColor(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
                part.setScale(100);
                part.renderOrder = 0;

                part.setKillOnCollide(true);

                part.windWeight = 1F;

                particleBehavior.particles.add(part);

                part.spawnAsWeatherEffect();

                spawnedThisTick++;
            }


        }

        if (spawnedThisTick > 0) {


        }

        float angle = windMan.windAngle;
        float speedWind = windMan.windSpeed;

        double vecX = -Maths.fastSin(Math.toRadians(angle));
        double vecZ = Maths.fastCos(Math.toRadians(angle));
        double speed = 0.8D;


        particleBehavior.coordSource = pos.toVec3Coro();
        particleBehavior.tickUpdateList();


        for (int i = 0; i < particleBehavior.particles.size(); i++) {
            ParticleSandstorm particle = (ParticleSandstorm) particleBehavior.particles.get(i);


            if (particle.lockPosition) {
                if (size > 0) {
                    double x = pos.posX + (-Maths.fastSin(Math.toRadians(particle.angleToStorm)) * (particle.distAdj));
                    double z = pos.posZ + (Maths.fastCos(Math.toRadians(particle.angleToStorm)) * (particle.distAdj));
                    double y = pos.posY + (particle.heightLayer * distBetweenParticles);

                    moveToPosition(particle, x, y, z, 0.01D);
                } else {

                    particle.setMotionX((vecX * speedWind * 0.3F));
                    particle.setMotionZ((vecZ * speedWind * 0.3F));
                }
            } else {
                particle.setMotionX((vecX * speed));
                particle.setMotionZ((vecZ * speed));
            }


        }

    }

    public Vec3 getRandomPosInSandstorm() {

        double extraDistSpawnIntoWall = sizePeak / 2D;
        double distFromSpawn = this.posSpawn.distanceSq(this.pos);

        double randDist = rand.nextDouble() * (distFromSpawn + extraDistSpawnIntoWall);

        double xVec = this.posSpawn.posX - this.pos.posX;
        double zVec = this.posSpawn.posZ - this.pos.posZ;

        double spawnAngle = Maths.fastATan2(this.sizePeak, distFromSpawn);


        double directionAngle = Maths.fastATan2(zVec, xVec);

        double randAngle = directionAngle + (Math.PI / 2D) - (spawnAngle) + (rand.nextDouble() * spawnAngle * 2D);

        double x = posSpawn.posX + (-Maths.fastSin(randAngle) * (randDist));
        double z = posSpawn.posZ + (Maths.fastCos(randAngle) * (randDist));

        return new Vec3(x, 0, z);
    }

    public List<net.corosus.coroutillegacy.util.Vec3> getSandstormAsShape() {
        List<net.corosus.coroutillegacy.util.Vec3> listPoints = new ArrayList<>();

        double extraDistSpawnIntoWall = sizePeak / 2D;
        double distFromSpawn = this.posSpawn.distanceSq(this.pos);


        listPoints.add(new net.corosus.coroutillegacy.util.Vec3(this.posSpawn.posX, 0, this.posSpawn.posZ));

        double xVec = this.posSpawn.posX - this.pos.posX;
        double zVec = this.posSpawn.posZ - this.pos.posZ;

        double spawnAngle = Maths.fastATan2(this.sizePeak, distFromSpawn);

        double directionAngle = Maths.fastATan2(zVec, xVec);

        double angleLeft = directionAngle + (Math.PI / 2D) - (spawnAngle);
        double angleRight = directionAngle + (Math.PI / 2D) - (spawnAngle) + (spawnAngle * 2D);

        double xLeft = posSpawn.posX + (-Maths.fastSin(angleLeft) * (distFromSpawn + extraDistSpawnIntoWall));
        double zLeft = posSpawn.posZ + (Maths.fastCos(angleLeft) * (distFromSpawn + extraDistSpawnIntoWall));

        double xRight = posSpawn.posX + (-Maths.fastSin(angleRight) * (distFromSpawn + extraDistSpawnIntoWall));
        double zRight = posSpawn.posZ + (Maths.fastCos(angleRight) * (distFromSpawn + extraDistSpawnIntoWall));

        listPoints.add(new net.corosus.coroutillegacy.util.Vec3(xLeft, 0, zLeft));
        listPoints.add(new net.corosus.coroutillegacy.util.Vec3(xRight, 0, zRight));

        return listPoints;
    }

    public void moveToPosition(ParticleSandstorm particle, double x, double y, double z, double maxSpeed) {
        if (particle.getPosX() > x) {
            particle.setMotionX(particle.getMotionX() + -maxSpeed);
        } else {
            particle.setMotionX(particle.getMotionX() + maxSpeed);
        }

        if (particle.getPosZ() > z) {
            particle.setMotionZ(particle.getMotionZ() + -maxSpeed);
        } else {
            particle.setMotionZ(particle.getMotionZ() + maxSpeed);
        }


        double distXZ = Math.sqrt((particle.getPosX() - x) * 2 + (particle.getPosZ() - z) * 2);
        if (distXZ < 5D) {
            particle.setMotionX(particle.getMotionX() * 0.8D);
            particle.setMotionZ(particle.getMotionZ() * 0.8D);
        }
    }

    @Override
    public int getNetRate() {
        return 1;
    }


    @Override
    public void readFromNBT() {
        super.readFromNBT();
        posSpawn = new Vec3(nbt.getDouble("posSpawnX"), nbt.getDouble("posSpawnY"), nbt.getDouble("posSpawnZ"));

        this.ageFadeout = nbt.getInteger("ageFadeout");
        this.ageFadeoutMax = nbt.getInteger("ageFadeoutMax");

        this.sizePeak = nbt.getInteger("sizePeak");
        this.age = nbt.getInteger("age");

        this.isFrontGrowing = nbt.getBoolean("isFrontGrowing");


        motion = new Vec3(nbt.getDouble("vecX"), nbt.getDouble("vecY"), nbt.getDouble("vecZ"));
    }

    @Override
    public CachedNBTTagCompound writeToNBT() {
        super.writeToNBT();
        nbt.setDouble("posSpawnX", posSpawn.posX);
        nbt.setDouble("posSpawnY", posSpawn.posY);
        nbt.setDouble("posSpawnZ", posSpawn.posZ);
        nbt.setInteger("ageFadeout", this.ageFadeout);
        nbt.setInteger("ageFadeoutMax", this.ageFadeoutMax);
        nbt.setInteger("sizePeak", sizePeak);
        nbt.setInteger("age", age);
        nbt.setBoolean("isFrontGrowing", isFrontGrowing);
        nbt.setDouble("vecX", motion.posX);
        nbt.setDouble("vecY", motion.posY);
        nbt.setDouble("vecZ", motion.posZ);
        return nbt;
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void cleanupClient(boolean wipe) {
        listParticlesCloud.forEach(particle -> particle.remove());
        listParticlesCloud.clear();
        if (particleBehavior != null) particleBehavior.particles.clear();
        particleBehavior = null;
    }

    @Override
    public float getWindSpeed() {
        return 7.0F;
    }

    @Override
    public int getStage() {

        return 1;
    }

    @Override
    public void setStage(int stage) {
    }

    @Override
    public String getName() {
        return "Sandstorm";
    }

    @Override
    public String getTypeName() {
        return "SS";
    }

    @Override
    public float getAngle() {
        return angle;
    }

    @Override
    public float getSpeed() {
        return (float) motion.speedSq();
    }

    public int getParticleCount() {
        return particleBehavior == null ? 0 : particleBehavior.particles.size();
    }

    public boolean canSpawnParticle() {
        return ConfigClient.max_particles < 0 || ClientTickHandler.weatherManager.getParticleCount() < ConfigClient.max_particles;
    }

}