package net.mrbt0907.weather2.network.packets;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.InterModComms;
import net.mrbt0907.weather2.Weather2;

public class PacketLightning extends PacketBase {
    public static void spawnLightning(RegistryKey<World> dimension, Entity entity) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("posX", MathHelper.floor(entity.getX()));
        nbt.putInt("posY", MathHelper.floor(entity.getY()));
        nbt.putInt("posZ", MathHelper.floor(entity.getZ()));
        nbt.putInt("entityID", entity.getId());
        send(7, nbt, dimension);
        InterModComms.sendTo(Weather2.MODID, "weather.lightning", () -> nbt);
    }

    public static void spawnInvisibleLightning(RegistryKey<World> dimension, double x, double y, double z) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("posX", MathHelper.floor(x));
        nbt.putInt("posY", MathHelper.floor(y));
        nbt.putInt("posZ", MathHelper.floor(z));
        send(7, nbt, dimension);
        InterModComms.sendTo(Weather2.MODID, "weather.lightning", () -> nbt);
    }
}