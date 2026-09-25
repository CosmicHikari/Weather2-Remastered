package net.corosus.coroutillegacy.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.mrbt0907.weather2.network.PacketNBT;

import java.io.IOException;

public class PacketHelper {


    public static PacketNBT getModConfigPacketForClientToServer(String command) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("command", "modconfig");
        nbt.putString("data", command);
        return new PacketNBT(nbt);
    }


    public static CompoundNBT readNBTTagCompound(ByteBuf fullBuffer) throws IOException {
        PacketBuffer packetBuffer = new PacketBuffer(fullBuffer);
        return packetBuffer.readNbt();
    }


    public static PacketNBT getNBTPacket(CompoundNBT parNBT, String parChannel) {
        return new PacketNBT(parNBT);
    }
}