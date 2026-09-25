package net.mrbt0907.weather2.weather.volcano;

import net.corosus.coroutillegacy.util.CoroUtilBlock;
import net.corosus.coroutillegacy.util.Vec3;
import net.corosus.extendedrenderer.ExtendedRenderer;
import net.corosus.extendedrenderer.particle.ParticleRegistry;
import net.corosus.extendedrenderer.particle.behavior.ParticleBehaviors;
import net.corosus.extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.weather.WeatherManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class VolcanoObject {

    public static int staticYPos = 200;
    public UUID ID;
    public WeatherManager manager;
    @OnlyIn(Dist.CLIENT)
    public List<EntityRotFX> listParticlesSmoke = new ArrayList<EntityRotFX>();
    @OnlyIn(Dist.CLIENT)
    public ParticleBehaviors particleBehaviors;
    public int sizeMaxParticles = 300;
    public Vec3 pos = new Vec3(0, staticYPos, 0);

    public int processRateDelay = 20;
    public Block topBlockID = Blocks.AIR;
    public int startYPos = -1;
    public int curRadius = 5;
    public int curHeight = 3;

    public int state = 0;

    public int size = 0;
    public int maxSize = 20;

    public int step = 0;

    public int stepsBuildupMax = 20;

    public int ticksToErupt = 20 * 30;
    public int ticksPerformedErupt = 0;

    public int ticksToCooldown = 20 * 30;
    public int ticksPerformedCooldown = 0;


    public int growthStage = 0;

    public VolcanoObject(WeatherManager parManager) {
        manager = parManager;
        init();
    }

    public void init() {
        ID = UUID.randomUUID();
    }

    public void resetEruption() {
        step = 0;
        ticksPerformedErupt = 0;
        ticksPerformedCooldown = 0;
        state = 2;
        ticksPerformedErupt = 0;
        ticksPerformedCooldown = 0;
    }

    public void readFromNBT(CompoundNBT data) {
        ID = data.getUUID("ID");

        pos = new Vec3(data.getInt("posX"), data.getInt("posY"), data.getInt("posZ"));
        size = data.getInt("size");
        maxSize = data.getInt("maxSize");

        state = data.getInt("state");

        curRadius = data.getInt("curRadius");
        curHeight = data.getInt("curHeight");
        topBlockID = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(data.getString("topBlockID")));
        if (topBlockID == null) topBlockID = Blocks.AIR;
        startYPos = data.getInt("startYPos");

        step = data.getInt("step");
        ticksPerformedErupt = data.getInt("ticksPerformedErupt");
        ticksPerformedCooldown = data.getInt("ticksPerformedCooldown");

    }

    public void writeToNBT(CompoundNBT data) {
        data.putUUID("ID", ID);

        data.putInt("posX", (int) pos.xCoord);
        data.putInt("posY", (int) pos.yCoord);
        data.putInt("posZ", (int) pos.zCoord);

        data.putInt("size", size);
        data.putInt("maxSize", maxSize);

        data.putInt("state", state);

        data.putInt("curRadius", curRadius);
        data.putInt("curHeight", curHeight);
        data.putString("topBlockID", topBlockID.getRegistryName().toString());
        data.putInt("startYPos", startYPos);

        data.putInt("step", step);
        data.putInt("ticksPerformedErupt", ticksPerformedErupt);
        data.putInt("ticksPerformedCooldown", ticksPerformedCooldown);
    }

    public void nbtSyncFromServer(CompoundNBT parNBT) {
        ID = parNBT.getUUID("ID");
        Weather2.debug("VolcanoObject " + ID + " receiving sync");

        pos = new Vec3(parNBT.getInt("posX"), parNBT.getInt("posY"), parNBT.getInt("posZ"));
        size = parNBT.getInt("size");
        maxSize = parNBT.getInt("maxSize");

        state = parNBT.getInt("state");
    }

    public CompoundNBT nbtSyncForClient() {
        CompoundNBT data = new CompoundNBT();

        data.putInt("posX", (int) pos.xCoord);
        data.putInt("posY", (int) pos.yCoord);
        data.putInt("posZ", (int) pos.zCoord);

        data.putUUID("ID", ID);
        data.putInt("size", size);
        data.putInt("maxSize", maxSize);

        data.putInt("state", state);

        return data;
    }

    public void tick() {


        processRateDelay = 10;

        World world = manager.getWorld();
        if (world.isClientSide) {
            if (!WeatherUtil.isPaused()) {
                tickClient();
            }
        } else {

            float res = 5;

            if (state == 0) {

                pos.xCoord = Math.floor(pos.xCoord);
                pos.zCoord = Math.floor(pos.zCoord);

                pos.yCoord = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos((int) pos.xCoord, 0, (int) pos.zCoord)).getY();
                startYPos = (int) pos.yCoord;

                BlockState statez = world.getBlockState(new BlockPos(MathHelper.floor(pos.xCoord), MathHelper.floor(pos.yCoord - 1), MathHelper.floor(pos.zCoord)));
                topBlockID = statez.getBlock();

                if (CoroUtilBlock.isAir(topBlockID) || !statez.getMaterial().isSolid()) {
                    topBlockID = world.getBlockState(new BlockPos((int) pos.xCoord, (int) pos.yCoord - 1, (int) pos.zCoord)).getBlock();
                }

                for (int yy = startYPos + curHeight; yy > 2; yy--) {
                    for (int dist = 0; dist <= curRadius; dist++) {

                        double vecX = dist;
                        double vecZ = 0;

                        if (yy > startYPos) {
                            vecX = dist + (startYPos - yy);
                        }

                        for (double angle = 0; angle <= 360; angle += res) {

                            Vec3 vec = new Vec3(vecX, 0, vecZ);
                            vec.rotateAroundY((float) angle);

                            int posX = (int) Math.floor((pos.xCoord) + vec.xCoord + 0.5);
                            int posZ = (int) Math.floor((pos.zCoord) + vec.zCoord + 0.5);

                            Block blockID = Blocks.OBSIDIAN;

                            if (yy >= startYPos) {
                                blockID = topBlockID;
                            } else if (dist < curRadius) {
                                blockID = Blocks.LAVA;
                            }

                            if (yy != startYPos + curHeight) {
                                Block idScan = world.getBlockState(new BlockPos(posX, yy, posZ)).getBlock();
                                if (CoroUtilBlock.isAir(idScan) || idScan.defaultBlockState().getMaterial() == Material.WATER) {
                                    world.setBlockAndUpdate(new BlockPos(posX, yy, posZ), blockID.defaultBlockState());
                                }
                            }
                        }
                    }
                }

                state++;

                System.out.println("initial volcano created");

            } else if (state == 1) {
                if (this.manager.getWorld().getGameTime() % processRateDelay == 0) {
                    size++;
                    curHeight++;
                    curRadius++;
                    if (size >= maxSize) {
                        state++;
                    }

                    res = 1;

                    for (int yy = 0; yy <= curHeight; yy++) {

                        int radiusForLayer = Math.max(0, curRadius - yy - 2);

                        double vecX = radiusForLayer;
                        double vecZ = 0;

                        for (double angle = 0; angle <= 360; angle += res) {

                            Vec3 vec = new Vec3(vecX, 0, vecZ);
                            vec.rotateAroundY((float) angle);

                            int posX = (int) Math.floor((pos.xCoord) + vec.xCoord + 0.5);
                            int posZ = (int) Math.floor((pos.zCoord) + vec.zCoord + 0.5);

                            Block blockID = topBlockID;

                            Random rand = new Random();

                            if (rand.nextInt(4) == 0) {

                                if (yy != curHeight) {
                                    if (CoroUtilBlock.isAir(world.getBlockState(new BlockPos(posX, startYPos + yy, posZ)).getBlock())) {
                                        world.setBlockAndUpdate(new BlockPos(posX, startYPos + yy, posZ), blockID.defaultBlockState());
                                    }
                                }

                                int underY = startYPos + yy - 1;
                                Block underBlockID = world.getBlockState(new BlockPos(posX, underY, posZ)).getBlock();
                                while ((CoroUtilBlock.isAir(underBlockID) || underBlockID.defaultBlockState().getMaterial() == Material.WATER) && underY > 1) {
                                    world.setBlockAndUpdate(new BlockPos(posX, underY, posZ), Blocks.DIRT.defaultBlockState());
                                    underY--;
                                    underBlockID = world.getBlockState(new BlockPos(posX, underY, posZ)).getBlock();
                                }
                            }
                        }

                    }

                    System.out.println("cur size: " + size + " - " + curHeight + " - " + curRadius);
                }
            } else if (state == 2) {

                if (this.manager.getWorld().getGameTime() % processRateDelay == 0) {

                    if (step <= maxSize) {
                        int posX = (int) Math.floor((pos.xCoord));
                        int posY = (int) Math.floor((startYPos)) + step;
                        int posZ = (int) Math.floor((pos.zCoord));

                        world.setBlockAndUpdate(new BlockPos(posX, posY, posZ), Blocks.LAVA.defaultBlockState());
                        world.setBlockAndUpdate(new BlockPos(posX + 1, posY, posZ), Blocks.LAVA.defaultBlockState());
                        world.setBlockAndUpdate(new BlockPos(posX - 1, posY, posZ), Blocks.LAVA.defaultBlockState());
                        world.setBlockAndUpdate(new BlockPos(posX, posY, posZ + 1), Blocks.LAVA.defaultBlockState());
                        world.setBlockAndUpdate(new BlockPos(posX, posY, posZ - 1), Blocks.LAVA.defaultBlockState());
                    } else {
                        step = 0;
                        state++;
                    }

                    step++;

                }

            } else if (state == 3) {

                if (this.manager.getWorld().getGameTime() % processRateDelay == 0) {
                    step++;
                    if (step > stepsBuildupMax) {
                        step = 0;
                        state++;
                    }
                }

            } else if (state == 4) {


                if (ticksPerformedErupt == 0) {

                    Weather2.debug("volcano " + ID + " is erupting");

                    for (int i = 0; i < 3; i++) {
                        int posX = (int) Math.floor((pos.xCoord));
                        int posY = (int) Math.floor((startYPos)) + maxSize + i;
                        int posZ = (int) Math.floor((pos.zCoord));

                        Block blockID = Blocks.LAVA;

                        world.setBlockAndUpdate(new BlockPos(posX, posY, posZ), blockID.defaultBlockState());
                        world.setBlockAndUpdate(new BlockPos(posX + 1, posY, posZ), blockID.defaultBlockState());
                        world.setBlockAndUpdate(new BlockPos(posX - 1, posY, posZ), blockID.defaultBlockState());
                        world.setBlockAndUpdate(new BlockPos(posX, posY, posZ + 1), blockID.defaultBlockState());
                        world.setBlockAndUpdate(new BlockPos(posX, posY, posZ - 1), blockID.defaultBlockState());
                        world.setBlockAndUpdate(new BlockPos(posX + 1, posY, posZ + 1), blockID.defaultBlockState());
                        world.setBlockAndUpdate(new BlockPos(posX - 1, posY, posZ - 1), blockID.defaultBlockState());
                        world.setBlockAndUpdate(new BlockPos(posX - 1, posY, posZ + 1), blockID.defaultBlockState());
                        world.setBlockAndUpdate(new BlockPos(posX + 1, posY, posZ - 1), blockID.defaultBlockState());
                    }
                }

                ticksPerformedErupt++;
                if (ticksPerformedErupt > ticksToErupt) {
                    state++;
                }

            } else if (state == 5) {

                if (ticksPerformedCooldown == 0) {
                    Weather2.debug("volcano " + ID + " is cooling");
                }

                if (ticksPerformedCooldown % processRateDelay == 0) {
                    int posX = (int) Math.floor((pos.xCoord));
                    int posY = (int) Math.floor((startYPos)) + maxSize - step + 2;
                    int posZ = (int) Math.floor((pos.zCoord));

                    Block blockID = Blocks.STONE;

                    world.setBlockAndUpdate(new BlockPos(posX, posY, posZ), blockID.defaultBlockState());
                    world.setBlockAndUpdate(new BlockPos(posX + 1, posY, posZ), blockID.defaultBlockState());
                    world.setBlockAndUpdate(new BlockPos(posX - 1, posY, posZ), blockID.defaultBlockState());
                    world.setBlockAndUpdate(new BlockPos(posX, posY, posZ + 1), blockID.defaultBlockState());
                    world.setBlockAndUpdate(new BlockPos(posX, posY, posZ - 1), blockID.defaultBlockState());
                    world.setBlockAndUpdate(new BlockPos(posX + 1, posY, posZ + 1), blockID.defaultBlockState());
                    world.setBlockAndUpdate(new BlockPos(posX - 1, posY, posZ - 1), blockID.defaultBlockState());
                    world.setBlockAndUpdate(new BlockPos(posX - 1, posY, posZ + 1), blockID.defaultBlockState());
                    world.setBlockAndUpdate(new BlockPos(posX + 1, posY, posZ - 1), blockID.defaultBlockState());

                    step++;
                }

                ticksPerformedCooldown++;
                if (ticksPerformedCooldown > ticksToCooldown) {
                    state++;
                }

            } else if (state == 6) {

                Weather2.debug("volcano " + ID + " has reset!");

                resetEruption();

            }

        }

    }

    @OnlyIn(Dist.CLIENT)
    public void tickClient() {

        if (particleBehaviors == null) {
            particleBehaviors = new ParticleBehaviors(new Vec3(pos.xCoord, pos.yCoord, pos.zCoord));
        } else {
            if (!Minecraft.getInstance().hasSingleplayerServer() || !(Minecraft.getInstance().screen instanceof IngameMenuScreen)) {
                particleBehaviors.tickUpdateList();
            }
        }

        int delay = 1;
        int loopSize = 1;
        Random rand = new Random();

        if (this.manager.getWorld().getGameTime() % delay == 0) {
            for (int i = 0; i < loopSize; i++) {
                if (listParticlesSmoke.size() < 500) {
                    double spawnRad = size / 48;
                    EntityRotFX particle = spawnSmokeParticle(pos.xCoord + (rand.nextDouble() * spawnRad) - (rand.nextDouble() * spawnRad), pos.yCoord + size + 2, pos.zCoord + (rand.nextDouble() * spawnRad) - (rand.nextDouble() * spawnRad));
                    listParticlesSmoke.add(particle);
                }
            }
        }

        delay = 1;
        loopSize = 2;

        for (int i = 0; i < listParticlesSmoke.size(); i++) {
            EntityRotFX ent = listParticlesSmoke.get(i);
            if (!ent.isAlive()) {
                listParticlesSmoke.remove(ent);
            } else {

                double distt = 300D;
                double curDist = ent.getDistance(pos.xCoord, staticYPos, pos.zCoord);

                double vecX = ent.getPosX() - pos.xCoord;
                double vecZ = ent.getPosZ() - pos.zCoord;
                @SuppressWarnings("unused")
                float angle = (float) (Maths.fastATan2(vecZ, vecX) * 180.0D / Math.PI);
                angle += 50;

                angle -= (ent.getEntityId() % 10) * 3D;

                angle += rand.nextInt(10) - rand.nextInt(10);

                if (curDist > distt) {
                    angle += 20;
                }


            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public EntityRotFX spawnSmokeParticle(double x, double y, double z) {
        double speed = 0D;
        Random rand = new Random();
        EntityRotFX entityfx = particleBehaviors.spawnNewParticleIconFX(Minecraft.getInstance().level, ParticleRegistry.cloud256, x, y, z, (rand.nextDouble() - rand.nextDouble()) * speed, 0.0D, (rand.nextDouble() - rand.nextDouble()) * speed);
        particleBehaviors.initParticle(entityfx);
        ParticleBehaviors.setParticleRandoms(entityfx, true, true);
        ParticleBehaviors.setParticleFire(entityfx);
        entityfx.setCanCollide(false);
        entityfx.callUpdatePB = false;
        entityfx.setMaxAge(400 + rand.nextInt(200));
        entityfx.setScale(50);

        float randFloat = (rand.nextFloat() * 0.6F);
        float baseBright = 0.1F;
        float finalBright = Math.min(1F, baseBright + randFloat);
        entityfx.setColor(finalBright, finalBright, finalBright);

        ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
        particleBehaviors.particles.add(entityfx);
        return entityfx;
    }

    public void reset() {
        setDead();
    }

    public void setDead() {
        Weather2.debug("volcano... killed? NO ONE KILLS A VOLCANO!");
    }

    public UUID getUUID() {
        return ID;
    }
}