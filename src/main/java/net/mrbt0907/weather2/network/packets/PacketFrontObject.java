package net.mrbt0907.weather2.network.packets;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.mrbt0907.weather2.weather.storm.FrontObject;

public class PacketFrontObject extends PacketBase {
    public static void create(Object target, FrontObject front) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("frontObject", front.writeNBT());
        nbt.putUUID("uuid", front.getUUID());
        send(11, nbt, target);
    }

    public static void update(Object target, FrontObject front) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("frontObject", front.writeNBT());
        send(12, nbt, target);
    }

    public static void remove(RegistryKey<World> dimension, FrontObject front) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUUID("frontUUID", front.getUUID());
        send(13, nbt, dimension);
    }
}