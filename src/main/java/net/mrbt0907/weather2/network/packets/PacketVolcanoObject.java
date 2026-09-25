package net.mrbt0907.weather2.network.packets;

import net.minecraft.nbt.CompoundNBT;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;

public class PacketVolcanoObject extends PacketBase {
    public static void create(Object target, VolcanoObject vo) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("volcanoObject", vo.nbtSyncForClient());
        send(4, nbt, target);
    }

    public static void update(Object target, VolcanoObject vo) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("volcanoObject", vo.nbtSyncForClient());
        send(5, nbt, target);
    }
}