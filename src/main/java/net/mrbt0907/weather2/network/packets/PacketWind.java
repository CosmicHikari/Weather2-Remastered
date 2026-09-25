package net.mrbt0907.weather2.network.packets;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.fml.InterModComms;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.weather.WindManager;

public class PacketWind extends PacketBase {
    public static void update(RegistryKey<World> dimension, WindManager wm) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("manager", wm.nbtSyncForClient());
        send(6, nbt, dimension);
        InterModComms.sendTo(Weather2.MODID, "weather.wind", () -> nbt);
    }
}