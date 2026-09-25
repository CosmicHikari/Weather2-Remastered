package net.mrbt0907.weather2.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class PacketEZGUI extends PacketBase {
    public static void sync() {
        CompoundNBT nbt = new CompoundNBT();
        send(8, nbt);
    }

    public static void syncResponse(CompoundNBT nbt) {
        send(9, nbt);
    }

    public static void apply(CompoundNBT nbt) {
        send(10, nbt);
    }

    public static void apply(CompoundNBT nbt, ServerPlayerEntity player) {
        send(10, nbt, player);
    }
}