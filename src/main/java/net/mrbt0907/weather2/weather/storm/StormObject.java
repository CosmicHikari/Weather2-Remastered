package net.mrbt0907.weather2.weather.storm;

import net.corosus.coroutillegacy.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.api.weather.AbstractWeatherRenderer;
import net.mrbt0907.weather2.api.weather.IWeatherLayered;
import net.mrbt0907.weather2.api.weather.IWeatherRain;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigSnow;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.entity.EntityHail;
import net.mrbt0907.weather2.entity.EntityLightningEX;
import net.mrbt0907.weather2.network.packets.PacketLightning;
import net.mrbt0907.weather2.registry.StormNames;
import net.mrbt0907.weather2.util.*;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WindManager;

import java.util.Map.Entry;
import java.util.Random;

public class StormObject extends WeatherObject implements IWeatherRain, IWeatherLayered {

    public AbstractWeatherRenderer particleRenderer;
    public ResourceLocation particleRendererId;
    public float angle = 0.0F;

    public int layer = 0;

    public boolean overrideAngle = false;

    public boolean overrideMotion = false;

    public boolean isNatural = true;

    public boolean alwaysProgresses = false;

    public boolean neverDissipate = false;

    public boolean isViolent = false;

    public boolean canProgress;

    public boolean isFirenado = false;

    public boolean isSpout = false;

    public boolean shouldConvert = true;

    public float rain = 0;

    public float rainRate = 0.0F;

    public float hail = 0.0F;

    public float hailRate = 0.0F;

    public boolean shouldBuildHumidity = false;

    public float windSpeed = 0;

    public float temperature = 0;

    public int stageMax = Stage.NORMAL.getStage();

    public int stormType = StormType.LAND.ordinal();

    public int stage = Stage.NORMAL.getStage();

    public float intensity = 0.0F;

    public float intensityRate = 0.03F;

    public int revives = 0;

    public int maxRevives = 0;

    public float lightning = 0.5F;

    public float intensityMax = 0;

    public float funnelSize = 0;

    public float sizeRate = -1.0F;


    public double spin = 0.02D;

    public float formingStrength = 0;

    public float strength = 100;

    public int maxHeight = 60;

    public String name = "";
    public int currentTopYBlock = -1;
    public int updateLCG = (new Random()).nextInt();
    public Vec3 pos_funnel_base = new Vec3(pos.posX, pos.posY, pos.posZ);
    public int flyingBlocks;

    public StormObject(FrontObject front) {
        super(front);

        pos = new Vec3(0, getLayerHeight(), 0);
        size = Maths.random(100, 200) + size;
    }

    public void init() {
        super.init();

        if (isNatural)
            temperature = 0.0F;
        windSpeed = 0.0F;
    }

    public boolean isStorm() {
        return canProgress;
    }

    public boolean isSevere() {
        return stage > Stage.THUNDER.getStage();
    }

    public boolean isDeadly() {
        return stormType == StormType.LAND.ordinal() ? stage > Stage.SEVERE.getStage() : stage > Stage.TROPICAL_DISTURBANCE.getStage();
    }

    public boolean isTornado() {
        return stormType == StormType.LAND.ordinal();
    }

    public boolean isCyclone() {
        return stormType == StormType.WATER.ordinal();
    }

    @Override
    public void readFromNBT() {
        super.readFromNBT();
        stormType = nbt.getInteger("stormType");
        stage = nbt.getInteger("levelCurIntensityStage");
        isSpout = nbt.getBoolean("attrib_waterSpout");
        currentTopYBlock = nbt.getInteger("currentTopYBlock");
        temperature = nbt.getFloat("levelTemperature");
        rain = nbt.getInteger("levelWater");
        hail = nbt.getInteger("hail");
        hailRate = nbt.getInteger("hailRate");
        layer = nbt.getInteger("layer");
        stageMax = nbt.getInteger("levelStormIntensityMax");
        intensity = nbt.getFloat("levelCurStagesIntensity");
        intensityRate = nbt.getFloat("intensityRate");
        funnelSize = nbt.getFloat("levelCurStageSize");
        windSpeed = nbt.getFloat("levelCurStageWind");
        sizeRate = nbt.getFloat("levelCurStageSizeRate");
        lightning = nbt.getFloat("lightning");
        isViolent = nbt.getBoolean("isViolent");
        isFirenado = nbt.getBoolean("isFirenado");
        name = nbt.getString("stormName");
        shouldConvert = nbt.getBoolean("shouldConvert");
        shouldBuildHumidity = nbt.getBoolean("shouldBuildHumidity");
        canProgress = nbt.getBoolean("canProgress");
        neverDissipate = nbt.getBoolean("neverDissipate");
        alwaysProgresses = nbt.getBoolean("alwaysProgresses");
        overrideAngle = nbt.getBoolean("overrideAngle");
        overrideMotion = nbt.getBoolean("overrideMotion");
        maxRevives = nbt.getInteger("maxRevives");
    }

    @Override
    public CachedNBTTagCompound writeToNBT() {
        super.writeToNBT();
        nbt.setBoolean("attrib_waterSpout", isSpout);
        nbt.setInteger("weatherObjectType", 0);
        nbt.setInteger("currentTopYBlock", currentTopYBlock);
        nbt.setFloat("levelTemperature", temperature);
        nbt.setFloat("levelWater", rain);
        nbt.setFloat("hail", hail);
        nbt.setFloat("hailRate", hailRate);
        nbt.setInteger("layer", layer);
        nbt.setInteger("levelCurIntensityStage", stage);
        nbt.setFloat("levelCurStagesIntensity", intensity);
        nbt.setFloat("intensityRate", intensityRate);
        nbt.setFloat("levelStormIntensityMax", stageMax);
        nbt.setFloat("levelCurStageSize", funnelSize);
        nbt.setFloat("levelCurStageWind", windSpeed);
        nbt.setFloat("levelCurStageSizeRate", sizeRate);
        nbt.setInteger("stormType", stormType);
        nbt.setString("stormName", name);
        nbt.setFloat("lightning", lightning);
        nbt.setBoolean("isViolent", isViolent);
        nbt.setBoolean("shouldConvert", shouldConvert);
        nbt.setBoolean("shouldBuildHumidity", shouldBuildHumidity);
        nbt.setBoolean("isFirenado", isFirenado);
        nbt.setBoolean("canProgress", canProgress);
        nbt.setBoolean("neverDissipate", neverDissipate);
        nbt.setBoolean("alwaysProgresses", alwaysProgresses);
        nbt.setBoolean("overrideAngle", overrideAngle);
        nbt.setBoolean("overrideMotion", overrideMotion);
        nbt.setInteger("maxRevives", maxRevives);
        return nbt;
    }

    @OnlyIn(Dist.CLIENT)
    public void tickRender(float partialTick) {
        super.tickRender(partialTick);
    }

    public void tick() {
        super.tick();

        posGround = new Vec3(pos.posX, pos.posY, pos.posZ);
        posGround.posY = currentTopYBlock;
        if (manager.getWorld().isClientSide) {
            if (!WeatherUtil.isPaused()) {
                tickClient();

                if (isDeadly())
                    NewTornadoHelper.tick(this, world);

                tickMovementClient();
            }
        } else {
            if (isDeadly())
                NewTornadoHelper.tick(this, world);

            tickMovement();
            tickWeatherEvents();
            tickProgressionNormal();
            tickSnowFall();
        }

        if (layer == 0) {

            pos_funnel_base = new Vec3(pos.posX, pos.posY, pos.posZ);

            if (stage >= Stage.TORNADO.getStage()) {
                if (stage > Stage.TORNADO.getStage()) {
                    formingStrength = 1;
                    pos_funnel_base.posY = posGround.posY;
                } else {

                    float intensityAdj = Math.min(1F, intensity - Stage.SEVERE.getStage());

                    float val = (stage + intensityAdj) - Stage.TORNADO.getStage();
                    formingStrength = val;
                    double yDiff = pos.posY - posGround.posY;
                    pos_funnel_base.posY = pos.posY - (yDiff * formingStrength);
                }
            } else if (stage == Stage.SEVERE.getStage()) {
                formingStrength = 0;
                pos_funnel_base.posY = posGround.posY;
            } else {
                formingStrength = 0;
                pos_funnel_base.posY = pos.posY;
            }
        }
    }

    public void tickMovement() {
        if (front.equals(manager.getGlobalFront())) {
            if (!overrideAngle) {


                Random rand = new Random();
                angle += (rand.nextFloat() - rand.nextFloat()) * 0.15F;


                double scanDist = 50;
                double scanX = this.pos.posX + (-Maths.fastSin(Math.toRadians(angle)) * scanDist);
                double scanZ = this.pos.posZ + (Maths.fastCos(Math.toRadians(angle)) * scanDist);
                int height = WeatherUtilBlock.getPrecipitationHeightSafe(this.manager.getWorld(), new BlockPos(scanX, 0, scanZ)).getY();

                if (this.pos.posY < height) {
                    float angleAdj = 45;
                    angle += angleAdj;
                }
            }

            if (!overrideMotion) {
                float finalSpeed;
                double vecX = -Maths.fastSin(Math.toRadians(angle));
                double vecZ = Maths.fastCos(Math.toRadians(angle));
                float cloudSpeedAmp = 0.2F;

                if (stage > Stage.SEVERE.getStage() + 1)
                    finalSpeed = 0.2F;
                else if (stage > Stage.NORMAL.getStage())
                    finalSpeed = 0.05F;
                else
                    finalSpeed = getSpeed() * cloudSpeedAmp;

                if (stage > Stage.SEVERE.getStage() + 1)
                    finalSpeed /= stage - Stage.TORNADO.getStage() + 1F;


                motion.posX = vecX * finalSpeed;
                motion.posZ = vecZ * finalSpeed;
            }

            pos.posX += motion.posX;
            pos.posZ += motion.posZ;
        } else {
            if (!overrideMotion)
                motion = front.motion;

            pos.posX += motion.posX;
            pos.posZ += motion.posZ;
        }
    }

    public void tickMovementClient() {
        pos.posX += motion.posX;
        pos.posZ += motion.posZ;
    }

    public void tickWeatherEvents() {
        World world = manager.getWorld();

        if (stage > Stage.RAIN.getStage() && Maths.random(0, ConfigStorm.lightning_bolt_1_in_x - (int) (ConfigStorm.lightning_bolt_1_in_x * lightning)) == 0) {
            Vec3 pos = this.pos.copy().addVector(Maths.random(-size * 1.25D, size * 1.25D), 0, Maths.random(-size * 1.25D, size * 1.25D));
            BlockPos blockPos = pos.toBlockPos();
            if (world.isLoaded(blockPos))
                createLightning(pos.posX, world.getHeightmapPos(net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, blockPos).getY(), pos.posZ, true);
            else
                createLightning(pos.posX, getLayerHeight(), pos.posZ, false);
        }

        if (isHailing()) {
            PlayerEntity player;
            int amount = (int) Maths.clamp(ConfigStorm.hail_stones_per_tick * (hail - 100.0F) * 0.01F, 1.0F, ConfigStorm.hail_stones_per_tick);

            for (int i = 0; i < world.players().size(); i++) {
                player = world.players().get(Maths.random(0, world.players().size() - 1));
                if (pos.distanceSq(player.getX(), pos.posY, player.getZ()) < size) {
                    for (int ii = 0; ii < amount; ii++) {
                        int x = (int) (player.getX() + Maths.random(-128, 128));
                        int z = (int) (player.getZ() + Maths.random(-128, 128));

                        if (world.isLoaded(new BlockPos(x, getLayerHeight(), z))) {
                            EntityHail hail = new EntityHail(world, Maths.random(0.01F, 0.1F));
                            hail.setPos(x, getLayerHeight(), z);
                            world.addFreshEntity(hail);
                        }
                    }
                }
            }
        }
    }

    public void tickSnowFall() {
        if (!ConfigSnow.Snow_PerformSnowfall || !hasDownfall()) return;

        World world = manager.getWorld();
        int xx = 0;
        int zz = 0;

        for (xx = (int) (pos.posX - size / 2); xx < pos.posX + size / 2; xx += 16) {
            for (zz = (int) (pos.posZ - size / 2); zz < pos.posZ + size / 2; zz += 16) {
                int chunkX = xx / 16;
                int chunkZ = zz / 16;
                int x = chunkX * 16;
                int z = chunkZ * 16;


                if (!world.isLoaded(new BlockPos(x, 128, z)))
                    continue;

                Chunk chunk = world.getChunk(chunkX, chunkZ);
                int i1;
                int xxx;
                int zzz;
                int setBlockHeight;

                if (world.dimensionType().hasSkyLight() && world.getBiome(new BlockPos(x, 128, z)).getPrecipitation() != Biome.RainType.NONE && (ConfigSnow.Snow_RarityOfBuildup == 0 || world.random.nextInt(ConfigSnow.Snow_RarityOfBuildup) == 0)) {
                    updateLCG = updateLCG * 3 + 1013904223;
                    i1 = updateLCG >> 2;
                    xxx = i1 & 15;
                    zzz = i1 >> 8 & 15;
                    double d0 = pos.posX - (xx + xxx);
                    double d2 = pos.posZ - (zz + zzz);
                    if ((double) MathHelper.sqrt(d0 * d0 + d2 * d2) > size)
                        continue;

                    setBlockHeight = world.getHeightmapPos(net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(xxx + x, 0, zzz + z)).getY();
                    if (canSnowAtBody(xxx + x, setBlockHeight, zzz + z) && Blocks.SNOW.canSurvive(Blocks.SNOW.defaultBlockState(), world, new BlockPos(xxx + x, setBlockHeight, zzz + z))) {
                        boolean betterBuildup = true;

                        if (betterBuildup) {
                            WindManager windMan = manager.windManager;
                            float angle = windMan.windAngle;
                            Vec3 vecPos = new Vec3(xxx + x, setBlockHeight, zzz + z);

                            if (!world.isLoaded(vecPos.toBlockPos()))
                                continue;


                            if (!ConfigMisc.overcast_mode)
                                if (world.isEmptyBlock(vecPos.toBlockPos()))
                                    world.setBlockAndUpdate(vecPos.toBlockPos(), Blocks.SNOW.defaultBlockState());


                            WeatherUtilBlock.fillAgainstWallSmoothly(world, vecPos, angle, 15, 2, Blocks.SNOW);
                        }
                    }
                }
            }
        }
    }

    public ChunkCoordinatesBlock getSnowfallEvenOutAdjustCheck(int x, int y, int z, int sourceMeta) {

        ChunkCoordinatesBlock attempt;
        attempt = getSnowfallEvenOutAdjust(x - 1, y, z, sourceMeta);

        if (attempt.posX != 0 || attempt.posZ != 0) return attempt;
        attempt = getSnowfallEvenOutAdjust(x + 1, y, z, sourceMeta);
        if (attempt.posX != 0 || attempt.posZ != 0) return attempt;
        attempt = getSnowfallEvenOutAdjust(x, y, z - 1, sourceMeta);
        if (attempt.posX != 0 || attempt.posZ != 0) return attempt;
        attempt = getSnowfallEvenOutAdjust(x, y, z + 1, sourceMeta);
        if (attempt.posX != 0 || attempt.posZ != 0) return attempt;

        return new ChunkCoordinatesBlock(0, 0, 0, Blocks.AIR, 0);
    }

    public ChunkCoordinatesBlock getSnowfallEvenOutAdjust(int x, int y, int z, int sourceMeta) {
        World world = manager.getWorld();
        Block checkID = world.getBlockState(new BlockPos(x, y, z)).getBlock();

        if (CoroUtilBlock.isAir(checkID)) {
            Block checkID2 = world.getBlockState(new BlockPos(x, y - 1, z)).getBlock();

            if (CoroUtilBlock.isAir(checkID2)) {
            } else
                return new ChunkCoordinatesBlock(x, y, z, Blocks.AIR, 0);
        } else if (checkID == Blocks.SNOW) {
            int checkMeta = world.getBlockState(new BlockPos(x, y, z)).getValue(net.minecraft.block.SnowBlock.LAYERS);
            if (checkMeta < sourceMeta)
                return new ChunkCoordinatesBlock(x, y, z, checkID, checkMeta);
        }

        return new ChunkCoordinatesBlock(0, 0, 0, Blocks.AIR, 0);
    }

    public boolean canSnowAtBody(int par1, int par2, int par3) {
        World world = manager.getWorld();
        Biome biomegenbase = world.getBiome(new BlockPos(par1, 0, par3));
        BlockPos pos = new BlockPos(par1, par2, par3);

        if (biomegenbase == null) return false;
        float temperature = WeatherUtil.getTemperature(world, pos);

        if (temperature > 0.15F)
            return false;
        else {
            if (par2 >= 0 && par2 < 256 && world.getBrightness(LightType.BLOCK, pos) < 10) {
                BlockState iblockstate1 = ChunkUtils.getBlockState(world, pos);
                return (iblockstate1.isAir(world, pos) || iblockstate1.getBlock() == Blocks.SNOW) && Blocks.SNOW.canSurvive(Blocks.SNOW.defaultBlockState(), world, pos);
            }

            return false;
        }
    }

    public void tickProgressionNormal() {
        World world = manager.getWorld();

        if (ticks % ConfigStorm.storm_tick_delay == 0) {
            Biome biome = world.getBiome(new BlockPos(MathHelper.floor(pos.posX), 0, MathHelper.floor(pos.posZ)));
            float tempAdjustRate = (float) ConfigStorm.temperature_adjust_rate;
            boolean hasWater, hasOcean = false;

            if (stage > Stage.TORNADO.getStage() || stormType == StormType.WATER.ordinal() && stage > Stage.TROPICAL_DEPRESSION.getStage())
                funnelSize = (float) Math.min(Math.pow((intensity - 3.0F) * 14, ConfigStorm.storm_size_curve_mult) * (stormType == StormType.LAND.ordinal() ? sizeRate : sizeRate * 1.5F), ConfigStorm.max_funnel_size);
            else if (funnelSize != 14.0F)
                funnelSize = 14.0F;

            size = (int) Maths.clamp((funnelSize + ConfigStorm.min_storm_size) * (stormType == StormType.LAND.ordinal() ? 1.5F : 3.0F), ConfigStorm.min_storm_size, ConfigStorm.max_storm_size);
            windSpeed = Math.max(6.73F + (stormType == StormType.WATER.ordinal() ? 1.7F : 2.8F) * (intensity - 3.0F), 0.0F);


            if (biome != null) {

                String biomeName = biome.getRegistryName() != null ? biome.getRegistryName().getPath() : "";
                hasOcean = biomeName.toLowerCase().contains("ocean");
                float biomeTempAdj = getTemperatureMCToWeatherSys(CoroUtilCompatibility.getAdjustedTemperature(manager.getWorld(), biome, new BlockPos(MathHelper.floor(pos.posX), 64, MathHelper.floor(pos.posZ))));
                if (temperature > biomeTempAdj)
                    temperature -= tempAdjustRate;
                else if (temperature < biomeTempAdj)
                    temperature += tempAdjustRate;
            }
            BlockState blockID = world.getBlockState(new BlockPos(MathHelper.floor(pos.posX), currentTopYBlock - 1, MathHelper.floor(pos.posZ)));
            hasWater = blockID.getMaterial().isLiquid();

            if (isStorm()) {
                if (shouldBuildHumidity) {
                    if (!isDying) {
                        rain += ConfigStorm.humidity_buildup_rate * WeatherUtil.getHumidity(world, pos.toBlockPos());
                        if (rain > ConfigStorm.max_rain_buildup) rain = ConfigStorm.max_rain_buildup;
                        if (hailRate > 0.0F && hailRate < 200.0F)
                            hail += hailRate * WeatherUtil.getHumidity(world, pos.toBlockPos()) * 2.5F;
                    } else {
                        if (rain > 0.0F)
                            rain -= ConfigStorm.humidity_spend_rate * WeatherUtil.getHumidity(world, pos.toBlockPos());
                        if (hailRate > 0.0F)
                            hail -= hailRate * WeatherUtil.getHumidity(world, pos.toBlockPos()) * 2.0F;
                    }
                    if (stage < WeatherEnum.Stage.SEVERE.getStage() && hail > 125.0F)
                        hail = 125.0F;
                    if (rain < 0.0F) {
                        Weather2.debug("Storm " + getUUID().toString() + " has stopped raining");
                        rain = 0.0F;
                        shouldBuildHumidity = false;
                    }
                }

                if (rain < IWeatherRain.MINIMUM_DRIZZLE && stage > 0)
                    rain = IWeatherRain.MINIMUM_DRIZZLE;


                if (!isDying)
                    if (ConfigMisc.overcast_mode && isNatural && !neverDissipate && !manager.getWorld().isRaining()) {
                        Weather2.debug("Storm " + getUUID().toString() + " was forced to dissipate because of overcast mode at stage " + stage + " and is now dying");
                        isDying = true;
                    } else if (ConfigStorm.disable_tornados && stormType == StormType.LAND.ordinal() || ConfigStorm.disable_cyclones && stormType == StormType.WATER.ordinal()) {
                        Weather2.debug("Storm " + getUUID().toString() + " was forced to dissipate because it was disabled at stage " + stage + " and is now dying");
                        isDying = true;
                    }

                if (stage == Stage.SEVERE.getStage() && hasWater) {
                    if (ConfigStorm.high_wind_waterspout_10_in_x != 0 && Maths.random(ConfigStorm.high_wind_waterspout_10_in_x) == 0)
                        isSpout = true;
                } else
                    isSpout = false;

                float intensityRate = isDeadly() ? this.intensityRate : 0.03F;
                boolean intensify = intensity - (stage - 1) > 1.0F;


                if (stage >= Stage.TORNADO.getStage())
                    intensityRate *= 3;

                if (!isDying) {
                    if (!neverDissipate || (!intensify && stage <= stageMax || alwaysProgresses))
                        intensity += intensityRate;

                    if (intensify && (stage < stageMax || alwaysProgresses)) {
                        stageNext();
                        Weather2.debug("Storm " + getUUID().toString() + " has intensified to stage " + stage);

                        if (ConfigStorm.storms_aim_at_player && front.isGlobal() && stage == Stage.TORNADO.getStage()) {
                            lightning = Math.max(Maths.random(0.01F, 0.95F), lightning);
                            aimStormAtPlayer(null);
                        }

                        if (shouldConvert && !ConfigStorm.disable_cyclones && (stage < WeatherEnum.Stage.SEVERE.getStage() && hasOcean || ConfigStorm.disable_tornados)) {
                            Weather2.debug("Storm " + getUUID().toString() + " was converted into a tropical cyclone");
                            lightning = Maths.random(0.01F, 0.30F);
                            stormType = StormType.WATER.ordinal();
                            updateType();
                        }
                    } else if (!(neverDissipate || alwaysProgresses) && stage >= stageMax && intensify) {
                        Weather2.debug("Storm " + getUUID().toString() + " has peaked at stage " + stage + " and is now dying");
                        isDying = true;
                    }
                } else {
                    if (ConfigMisc.overcast_mode && manager.getWorld().isRaining())
                        intensity -= intensityRate * 0.5F;
                    else
                        intensity -= intensityRate * 0.2F;

                    if (intensity - (stage - 1) <= 0) {
                        stagePrev();
                        Weather2.debug("Storm " + getUUID().toString() + " has weakened to stage " + stage);
                        if (stage == 2 && revives < maxRevives) {
                            isDying = false;
                            revives++;
                            resetStorm();
                        } else if (stage <= 0)
                            setNoStorm();
                    }
                }
            }
        }
    }

    public WeatherEntityConfig getWeatherEntityConfigForStorm() {
        return WeatherTypes.weatherEntTypes.get(Maths.clamp(stage - Stage.TORNADO.getStage(), 0, 6));
    }

    public void updateType() {
        switch (stage) {
            case 0:
                type = WeatherEnum.Type.CLOUD;
                break;
            case 1:
                type = WeatherEnum.Type.RAIN;
                break;
            case 2:
                if (stormType == 1)
                    type = WeatherEnum.Type.TROPICAL_DISTURBANCE;
                else
                    type = WeatherEnum.Type.THUNDER;
                break;
            case 3:
                if (stormType == 1)
                    type = WeatherEnum.Type.TROPICAL_DEPRESSION;
                else
                    type = WeatherEnum.Type.SUPERCELL;
                break;
            default:
                if (stormType == 1)
                    type = stage == Stage.TROPICAL_STORM.getStage() ? WeatherEnum.Type.TROPICAL_STORM : WeatherEnum.Type.HURRICANE;
                else
                    type = WeatherEnum.Type.TORNADO;
        }

        if (stormType == 1 && name.length() == 0)
            name = StormNames.get();
    }

    public void stageNext() {
        stage += 1;
        updateType();
    }

    public void stagePrev() {
        stage -= 1;
        updateType();
    }

    public void resetStorm() {
        shouldBuildHumidity = true;
        sizeRate = Maths.random(0.75F, 1.35F);
        isViolent = Maths.chance(ConfigStorm.chance_for_violent_storm * 0.01D * 0.25D);
        stageMax = Math.max(rollDiceOnMaxIntensity(), WeatherEnum.Stage.TORNADO.getStage());
        intensityRate = Maths.random(ConfigStorm.storm_lifespan_min <= 0.0D ? 0.003F : (float) ConfigStorm.storm_lifespan_min, ConfigStorm.storm_lifespan_max <= 0.0D ? 0.06F : (float) ConfigStorm.storm_lifespan_max);

        Biome biome = world.getBiome(new BlockPos(MathHelper.floor(pos.posX), 0, MathHelper.floor(pos.posZ)));
        if (shouldConvert) {
            String biomeName = biome != null && biome.getRegistryName() != null ? biome.getRegistryName().getPath() : "";
            stormType = biome != null && biomeName.toLowerCase().contains("ocean") ? StormType.WATER.ordinal() : StormType.LAND.ordinal();
        }

        if (isViolent) {
            sizeRate += Maths.random(0.25F, 1.65F);

            if (stageMax < 9)
                stageMax += 1;
        }

        updateType();
        Weather2.debug("Revived Into Deadly Storm: \nIs Violent: " + isViolent + "\nMax Stage: " + stageMax + " (EF" + (stageMax - 4) + ")\nSize Multiplier: " + sizeRate * 100 + "%");
    }

    public void initRealStorm() {
        shouldBuildHumidity = true;

        if (stage != Stage.RAIN.getStage()) {
            stage = Stage.RAIN.getStage();
            intensity = 0.0F;
        }

        lightning = Maths.random(0.01F, 0.5F);

        if (stageMax < 1)
            stageMax = rollDiceOnMaxIntensity();

        if (sizeRate < 0.0F)
            sizeRate = (float) Maths.random(ConfigStorm.min_size_growth, ConfigStorm.max_size_growth);

        if (isViolent || Maths.chance(ConfigStorm.chance_for_violent_storm / 100.0D)) {
            isViolent = true;
            sizeRate += Maths.random(ConfigStorm.min_violent_size_growth, ConfigStorm.max_violent_size_growth);
            if (stageMax < Stage.TORNADO.getStage() + 4)
                stageMax += 1;
        }

        while (Maths.chance(ConfigStorm.chance_for_storm_revival * 0.01D) && revives < ConfigStorm.max_storm_revives)
            revives++;

        if (Maths.chance(ConfigStorm.chance_for_hail * 0.01D))
            hailRate = (float) Maths.random(ConfigStorm.hail_max_buildup_rate);

        if (stageMax > Stage.SEVERE.getStage()) {
            intensityRate = Maths.random(ConfigStorm.storm_lifespan_min <= 0.0D ? 0.003F : (float) ConfigStorm.storm_lifespan_min, ConfigStorm.storm_lifespan_max <= 0.0D ? 0.06F : (float) ConfigStorm.storm_lifespan_max);
            Weather2.debug("New Deadly Storm: \nIs Violent: " + isViolent + "\nMax Stage: " + stageMax + " (EF" + (stageMax - 4) + ")\nSize Multiplier: " + sizeRate * 100 + "%\nLifespan Multiplier: " + intensityRate * 100);
        } else
            Weather2.debug("New Normal Storm: \nIs Violent: " + isViolent + "\nMax Stage: " + stageMax + "\nSize Multiplier: " + sizeRate * 100 + "%");
        canProgress = true;
        updateType();
    }

    public int rollDiceOnMaxIntensity() {
        if (!Maths.chance(ConfigStorm.chance_for_thunderstorm * 0.01D)) return Stage.RAIN.getStage();
        else if (!Maths.chance(ConfigStorm.chance_for_supercell * 0.01D)) return Stage.THUNDER.getStage();


        ConfigList list = new ConfigList();
        if (stormType == StormType.LAND.ordinal()) {
            if (!ConfigStorm.disable_tornados)
                list = WeatherAPI.getTornadoStageList();
        } else {
            if (!ConfigStorm.disable_cyclones)
                list = WeatherAPI.getHurricaneStageList();
        }

        for (Entry<String, Object[]> entry : list.toMap().entrySet()) {
            String key = entry.getKey();

            if (entry.getValue().length > 0) {
                double value = entry.getValue()[0] instanceof String && ((String) entry.getValue()[0]).matches("^[\\d\\.]+$") ? Double.parseDouble((String) entry.getValue()[0]) : entry.getValue()[0] instanceof Double ? (double) entry.getValue()[0] : 0.0D;
                boolean chance = Maths.chance(value * 0.01D);
                if (key.matches("^\\d+$") && chance)
                    return Integer.parseInt(key) + 4;
            }
        }

        return Stage.SEVERE.getStage();
    }

    public void setNoStorm() {
        Weather2.debug("Storm " + this.getUUID().toString() + " was terminated");
        stage = Stage.NORMAL.getStage();
        intensity = 0;
        isDead = true;
    }

    @OnlyIn(Dist.CLIENT)
    public void tickClient() {
        double spinSpeedMax = 0.4D;
        spin = Math.min(spinSpeedMax, Math.max(0.007D * stage, 0.03D));

        if (stormType == StormType.WATER.ordinal())
            spin += 0.025D;

        if (size == 0) size = 1;

        ResourceLocation id = WeatherAPI.getParticleRendererId();
        if (particleRendererId != id) {
            if (particleRenderer != null) {
                particleRenderer.cleanup();
                particleRenderer = null;
            }
            particleRendererId = id;
            particleRenderer = WeatherAPI.getParticleRenderer(this);
        }

        if (particleRenderer != null)
            particleRenderer.tick();
    }

    @Override
    public float getSpeed() {
        return overrideMotion ? (float) motion.speedSq() : manager.windManager.windSpeed;
    }

    public void setSpeed(float speed) {
        overrideMotion = true;
        motion.posX = -Maths.fastSin(Math.toRadians(angle)) * speed;
        motion.posZ = Maths.fastCos(Math.toRadians(angle)) * speed;
    }

    @Override
    public float getAngle() {
        if (overrideAngle) return angle;

        float angle = manager.windManager.windAngle;

        float angleAdjust = Math.max(10, Math.min(45, 45F * temperature * 0.2F));
        float targetYaw = 0;


        if (temperature > 0)
            targetYaw = 180;
        else
            targetYaw = 0;

        float bestMove = Maths.wrapDegrees(targetYaw - angle);

        if (Math.abs(bestMove) < 180) {
            if (bestMove > 0) angle -= angleAdjust;
            if (bestMove < 0) angle += angleAdjust;
        }

        return angle;
    }

    public void setAngle(float angle) {
        overrideAngle = true;
        this.angle = angle % 360.0F;
    }

    public float getAvoidAngleIfTerrainAtOrAheadOfPosition(float angle, Vec3 pos) {
        double scanDistMax = 120;
        for (int scanAngle = -20; scanAngle < 20; scanAngle += 10) {
            for (double scanDistRange = 20; scanDistRange < scanDistMax; scanDistRange += 10) {
                double scanX = pos.posX + (-Maths.fastSin(Math.toRadians(angle + scanAngle)) * scanDistRange);
                double scanZ = pos.posZ + (Maths.fastCos(Math.toRadians(angle + scanAngle)) * scanDistRange);

                int height = WeatherUtilBlock.getPrecipitationHeightSafe(this.manager.getWorld(), new BlockPos(scanX, 0, scanZ)).getY();

                if (pos.posY < height) {
                    if (scanAngle <= 0) {
                        return 90;
                    } else {
                        return -90;
                    }
                }
            }
        }
        return 0;
    }

    public void spinEntity(Object obj) {
        float weight = WeatherUtilEntity.getWeight(obj);
        if (weight <= 0.0F) return;


        WeatherEntityConfig config = getWeatherEntityConfigForStorm();
        Entity entity = obj instanceof Entity ? (Entity) obj : null;
        World world = CoroUtilEntOrParticle.getWorld(obj);
        boolean is_particle = world.isClientSide && obj instanceof net.minecraft.client.particle.Particle;
        float height_mult = getLayerHeight() * (is_particle ? 0.0075F : 0.0034F);
        float rotation_mult = height_mult * 0.5F * ((isViolent ? 3.1F : 1.45F) + Math.min((stage - 5.0F) / 3.0F, 2.0F));
        double radius = 10D, scale = config.tornadoWidthScale * 10D;
        double dx = pos.posX - CoroUtilEntOrParticle.getPosX(obj);
        double dy = pos.posY - CoroUtilEntOrParticle.getPosY(obj);
        double dz = pos.posZ - CoroUtilEntOrParticle.getPosZ(obj);


        float center_direction = (float) ((Maths.fastATan2(dz, dx) * 180D) / Math.PI) - 90F;
        for (; center_direction < -180F; center_direction += 360F) ;
        for (; center_direction >= 180F; center_direction -= 360F) ;

        double disty = Maths.clamp(pos.posY - dy, 0, pos.posY);
        double dist = Maths.distanceSq(dx, dz, pos.posX, pos.posZ);

        double pull_direction = (10D / weight) * ((Math.abs((maxHeight - disty)) / maxHeight));
        float lift_force = 0.0F;

        if (dist > 5D)
            pull_direction = pull_direction * (radius / dist);

        lift_force += (float) (is_particle ? config.tornadoLiftRate * 4.0D : Maths.clamp(windSpeed * 0.1F / weight, 0.05D, 0.2D));
        double adjPull = 0.005D / ((weight * ((dist + 1D) / radius)));
        if (is_particle)
            lift_force *= 0.15F;
        lift_force += adjPull;
        pull_direction += config.relTornadoSize;

        double profileAngle = Math.max(1, (75D + pull_direction - (scale)));

        center_direction = (float) ((double) center_direction + profileAngle);
        float pullX = (float) Maths.fastCos(-center_direction * 0.01745329F - (float) Math.PI);
        float pullZ = (float) Maths.fastSin(-center_direction * 0.01745329F - (float) Math.PI);
        float pull_force = is_particle ? config.tornadoPullRate : (float) Maths.clamp(windSpeed * 0.04F / weight, 0.010D, 0.05D);

        if (entity != null) {
            if (entity instanceof LivingEntity) {
                pull_force *= Maths.random(1.25D, 1.5D);
                lift_force *= 0.65D;
            }

            pull_force *= 2.0D;
        }

        if (config.type == 0)
            lift_force *= 0.25F;
        float moveX = pullX * pull_force;
        float moveZ = pullZ * pull_force;
        lift_force *= 1.0F - Maths.clamp(-dy / pos.posY, 0.0F, 1.0F);
        setVel(obj, -moveX * rotation_mult * 2.0F, lift_force * height_mult, moveZ * rotation_mult * 2.0F);
    }

    public void setVel(Object entity, float f, float f1, float f2) {
        CoroUtilEntOrParticle.setMotionX(entity, CoroUtilEntOrParticle.getMotionX(entity) + f);
        CoroUtilEntOrParticle.setMotionY(entity, CoroUtilEntOrParticle.getMotionY(entity) + f1);
        CoroUtilEntOrParticle.setMotionZ(entity, CoroUtilEntOrParticle.getMotionZ(entity) + f2);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void cleanupClient(boolean wipe) {
        if (wipe && particleRenderer != null) {
            particleRenderer.cleanup();
            particleRenderer = null;
        }
    }

    public void aimStormAtPlayer(PlayerEntity entP) {
        if (entP == null)
            entP = manager.getWorld().getNearestPlayer(pos.posX, pos.posY, pos.posZ, -1, false);

        if (entP != null) {
            float yaw = -(float) (Maths.fastATan2(entP.getX() - pos.posX, entP.getZ() - pos.posZ) * 180.0D / Math.PI);
            int size = ConfigStorm.storm_aim_accuracy_in_angle;
            if (size > 0)
                yaw += Maths.random(size) - (size / 2);

            overrideAngle = true;
            angle = yaw;

            Weather2.debug("Storm " + getUUID() + " was aimed at player " + CoroUtilEntity.getName(entP));
        }
    }

    public float getTemperatureMCToWeatherSys(float parOrigVal) {
        return parOrigVal - 0.3F;
    }

    public void createLightning(double x, double y, double z, boolean spawnBolt) {
        if (world.isClientSide) return;

        if (spawnBolt) {
            EntityLightningEX lightning = new EntityLightningEX(world, x, y, z);
            world.addFreshEntity(lightning);
            PacketLightning.spawnLightning(manager.getDimension(), lightning);
        } else
            PacketLightning.spawnInvisibleLightning(manager.getDimension(), x, y, z);
    }

    @Override
    public int getNetRate() {
        if (stage >= Stage.SEVERE.getStage()) {
            return 2;
        } else {
            return super.getNetRate();
        }
    }

    public boolean isDrizzling() {
        return rain >= IWeatherRain.MINIMUM_DRIZZLE && rain < IWeatherRain.MINIMUM_RAIN;
    }

    public boolean isRaining() {
        return rain >= IWeatherRain.MINIMUM_RAIN;
    }

    public boolean hasDownfall() {
        return rain >= IWeatherRain.MINIMUM_DRIZZLE;
    }

    @Override
    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
        updateType();
    }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public int getLayerHeight() {
        switch (layer) {
            case 1:
                return ConfigStorm.cloud_layer_1_height;
            case 2:
                return ConfigStorm.cloud_layer_2_height;
            default:
                return ConfigStorm.cloud_layer_0_height;
        }
    }

    public boolean isHailing() {
        return hail > 100.0F;
    }

    @Override
    public float getDownfall() {
        return rain;
    }

    @Override
    public float getDownfall(Vec3 pos) {
        float distance = Math.max((float) Maths.distanceSq(pos.posX, pos.posZ, this.pos.posX, this.pos.posZ) - size * 0.75F, 0.0F);
        float mult = 1.0F - Math.min(distance / (size * 0.25F), 1.0F);
        return getDownfall() * mult;
    }

    @Override
    public float getDownfall(BlockPos pos) {
        float distance = Math.max((float) Math.sqrt(pos.distSqr(
                (int) this.pos.posX, pos.getY(), (int) this.pos.posZ, false))
                - size * 0.75F, 0.0F);
        float mult = 1.0F - Math.min(distance / (size * 0.25F), 1.0F);
        return getDownfall() * mult;
    }

    @Override
    public boolean hasDownfall(Vec3 pos) {
        return hasDownfall() && this.pos.distanceSq(pos.posX, this.pos.posY, pos.posZ) <= size && world.canSeeSky(pos.toBlockPos());
    }

    @Override
    public boolean hasDownfall(BlockPos pos) {
        return hasDownfall() && this.pos.distanceSq(pos.getX(), this.pos.posY, pos.getZ()) <= size && world.canSeeSky(pos);
    }

    @Override
    public float getWindSpeed() {
        return windSpeed;
    }

    @Override
    public String getName() {
        return getName(false);
    }

    public String getName(boolean getEF) {
        boolean truth = name.length() == 0, isHailing = isHailing();

        switch (type) {
            case CLOUD:
                return (truth ? "" : name + " ") + (isHailing ? "Hailing " : "") + "Cloud";
            case RAIN:
                return (truth ? "" : name + " ") + (isHailing ? "Hailing " : "") + (hasDownfall() ? temperature <= 0.0F ? "Snowstorm" : "Rainstorm" : "Cloud");
            case THUNDER:
                return (truth ? "" : name + " ") + (isHailing ? "Hailing " : "") + "Thunderstorm";
            case SUPERCELL:
                return (truth ? "" : name + " ") + (isHailing ? "Hailing " : "") + "Supercell";
            case TROPICAL_DISTURBANCE:
                return (isHailing ? "Hailing " : "") + "Tropical Disturbance" + (truth ? "" : " " + name);
            case TROPICAL_DEPRESSION:
                return (isHailing ? "Hailing " : "") + "Tropical Depression" + (truth ? "" : " " + name);
            case TROPICAL_STORM:
                return (isHailing ? "Hailing " : "") + "Tropical Storm " + name;
            case TORNADO:
                return (truth ? "" : name + " ") + (ConfigStorm.enable_ef_scale || getEF ? "EF" + (stage - Stage.TORNADO.getStage()) : "F" + (int) Maths.clamp(Math.floor(funnelSize * 0.0206611570247933884297520661157F), 0, Integer.MAX_VALUE)) + " " + (isHailing ? "Hailing " : "") + "Tornado";
            case HURRICANE:
                return (isHailing ? "Hailing " : "") + "Hurricane " + name + " - Category " + (stage - Stage.TORNADO.getStage());
            default:
                return (isHailing ? "Hailing " : "") + "Unknown Storm";
        }
    }

    @Override
    public String getTypeName() {
        boolean truth = name.length() == 0;

        switch (type) {
            case TORNADO:
                return (truth ? "" : name + " ") + (ConfigStorm.enable_ef_scale ? "EF" + (stage - Stage.TORNADO.getStage()) : "F" + (int) Maths.clamp(Math.floor(funnelSize * 0.0206611570247933884297520661157F), 0, stageMax - Stage.TORNADO.getStage()));
            case HURRICANE:
                return name + " C" + (stage - Stage.TORNADO.getStage());
            case TROPICAL_DISTURBANCE:
                return "TD1";
            case TROPICAL_DEPRESSION:
                return "TD2";
            case TROPICAL_STORM:
                return "TS";
            default:
                return "";
        }
    }
    public enum StormType {LAND, WATER}
}