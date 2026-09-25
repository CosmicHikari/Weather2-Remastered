package net.mrbt0907.configex.network;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketNBT {
    private int index;
    private CompoundNBT nbt;

    public PacketNBT() {
    }

    public PacketNBT(int index, CompoundNBT nbt) {
        this.index = index;
        this.nbt = nbt;
    }

    public static PacketNBT decode(PacketBuffer buffer) {
        PacketNBT packet = new PacketNBT();
        packet.index = buffer.readInt();
        packet.nbt = buffer.readNbt();
        return packet;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(index);
        buffer.writeNbt(nbt);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
        {
            if (ctx.get().getDirection().getReceptionSide().isClient())
                handleClient(this.index, this.nbt);
            else
                handleServer(this.index, this.nbt, ctx.get().getSender());
        });
        ctx.get().setPacketHandled(true);
    }

    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    private void handleClient(int index, CompoundNBT nbt) {
        NetworkHandler.onClientMessage(index, nbt);
    }

    private void handleServer(int index, CompoundNBT nbt, net.minecraft.entity.player.ServerPlayerEntity player) {
        NetworkHandler.onServerMessage(index, nbt, player);
    }
}