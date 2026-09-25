package net.mrbt0907.weather2.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

public class PacketWeatherObject extends PacketBase {
    public static void create(Object target, WeatherObject wo) {
        CompoundNBT nbt = new CompoundNBT();
        wo.nbt.setUpdateForced(true);
        wo.writeToNBT();
        wo.nbt.setUpdateForced(false);
        nbt.put("weatherObject", wo.nbt.getNewNBT());
        send(1, nbt, target);
    }

    public static void update(Object target, WeatherObject wo) {
        CompoundNBT nbt = new CompoundNBT();
        wo.nbt.setNewNBT(new CompoundNBT());
        wo.writeToNBT();
        nbt.put("weatherObject", wo.nbt.getNewNBT());
        send(2, nbt, target);
    }

    public static void remove(RegistryKey<World> dimension, WeatherObject wo) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUUID("frontObject", wo.front.getUUID());
        nbt.putUUID("weatherObject", wo.getUUID());
        send(3, nbt, dimension);
    }

    public static void clientCleanup(ServerPlayerEntity player) {
        send(14, new CompoundNBT(), player);
    }
}