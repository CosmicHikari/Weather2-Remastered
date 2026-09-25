package net.mrbt0907.weather2.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class PacketRefresh extends PacketBase {
    public static void resetSounds(ServerPlayerEntity player) {
        send(15, new CompoundNBT(), player);
    }

    public static void resetSceneEnhancer(ServerPlayerEntity player) {
        send(17, new CompoundNBT(), player);
    }
}