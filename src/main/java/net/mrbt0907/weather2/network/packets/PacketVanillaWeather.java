package net.mrbt0907.weather2.network.packets;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

public class PacketVanillaWeather extends PacketBase {
    public static void send(RegistryKey<World> dimension, int weatherID, int weatherRainTime) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("weatherID", weatherID);
        nbt.putInt("weatherRainTime", weatherRainTime);
        send(0, nbt, dimension);
    }
}