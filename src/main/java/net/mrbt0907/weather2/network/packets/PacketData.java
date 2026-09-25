package net.mrbt0907.weather2.network.packets;

import net.minecraft.nbt.CompoundNBT;

public class PacketData extends PacketBase {
    public static void sync() {
        CompoundNBT nbt = new CompoundNBT();
        send(11, nbt);
    }
}