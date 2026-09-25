package net.mrbt0907.weather2.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class PacketShader extends PacketBase {
    public static void refreshShaders(ServerPlayerEntity player) {
        PacketBase.send(19, new CompoundNBT(), player);
    }
}