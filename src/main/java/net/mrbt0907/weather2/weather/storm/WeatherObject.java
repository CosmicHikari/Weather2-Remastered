package net.mrbt0907.weather2.weather.storm;

import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.api.weather.AbstractWeatherLogic;
import net.mrbt0907.weather2.api.weather.IWeatherDetectable;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.util.CachedNBTTagCompound;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WeatherManager;

import java.util.UUID;

public abstract class WeatherObject implements IWeatherDetectable {
    public FrontObject front;
    public WeatherManager manager;
    public CachedNBTTagCompound nbt;
    public WeatherEnum.Type type = WeatherEnum.Type.CLOUD;
    public Vec3 pos = new Vec3(0, 0, 0);
    public Vec3 posGround = new Vec3(0, 0, 0);
    public Vec3 motion = new Vec3(0, 0, 0);
    public boolean isDying = false;
    public boolean isDead = false;
    public long ticks = 0L;
    public int size = ConfigStorm.min_storm_size;
    public AbstractWeatherLogic weatherLogic;
    public int ticksSinceNoNearPlayer = 0;
    protected World world;
    private UUID id;

    public WeatherObject(FrontObject front) {
        this.front = front;
        manager = front.getWeatherManager();
        nbt = new CachedNBTTagCompound();
        world = front.world;
        init();
    }

    public void init() {
        id = UUID.randomUUID();
    }

    public void tick() {
        ticks++;

        if (ticks < 0)
            ticks = 0;
    }

    @OnlyIn(Dist.CLIENT)
    public void tickRender(float partialTick) {
    }

    public void setDead() {
        isDead = true;


        if (world != null && world.isClientSide)
            cleanupClient(true);

        cleanup();
    }

    public void reset() {
        setDead();
    }

    public void cleanup() {
        manager = null;
    }

    @OnlyIn(Dist.CLIENT)
    public void cleanupClient(boolean wipe) {
    }

    public void readFromNBT() {
        if (nbt.getNewNBT().hasUUID("ID")) {
            UUID loadedId = nbt.getUUID("ID");
            if (loadedId != null) {
                id = loadedId;
            }
        }

        pos = new Vec3(nbt.getDouble("posX"), nbt.getDouble("posY"), nbt.getDouble("posZ"));
        motion = new Vec3(nbt.getDouble("vecX"), nbt.getDouble("vecY"), nbt.getDouble("vecZ"));
        size = nbt.getInteger("size");
        type = WeatherEnum.Type.get(nbt.getInteger("weatherType"));
        isDying = nbt.getBoolean("isDying");
        isDead = nbt.getBoolean("isDead");
        ticksSinceNoNearPlayer = 0;
    }

    public CachedNBTTagCompound writeToNBT() {
        nbt.setDouble("posX", pos.posX);
        nbt.setDouble("posY", pos.posY);
        nbt.setDouble("posZ", pos.posZ);
        nbt.setDouble("vecX", motion.posX);
        nbt.setDouble("vecY", motion.posY);
        nbt.setDouble("vecZ", motion.posZ);
        nbt.setUUID("ID", id);
        nbt.setUUID("frontUUID", front.getUUID());

        nbt.getNewNBT().putUUID("ID", id);
        nbt.setInteger("size", size);
        nbt.setInteger("weatherType", type.ordinal());
        nbt.setBoolean("isDying", isDying);
        nbt.setBoolean("isDead", isDead);
        nbt.setInteger("ticksSinceNoNearPlayer", ticksSinceNoNearPlayer);
        return nbt;
    }

    public int getNetRate() {
        return 40;
    }

    public boolean isDangerous() {
        return type.isDangerous();
    }

    public UUID getUUID() {
        return id;
    }

    @Override
    public Vec3 getPos() {
        return pos;
    }

    @Override
    public boolean isDying() {
        return isDying;
    }
}