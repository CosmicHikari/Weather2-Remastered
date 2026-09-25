package net.mrbt0907.weather2.network;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketNBT {
    private final CompoundNBT nbt;

    public PacketNBT(CompoundNBT nbt) {
        this.nbt = nbt;
    }

    public static PacketNBT decode(PacketBuffer buffer) {
        return new PacketNBT(buffer.readNbt());
    }

    public static void encode(PacketNBT packet, PacketBuffer buffer) {
        buffer.writeNbt(packet.nbt);
    }

    public static void handle(PacketNBT packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.handleClient(packet.nbt));
            } else {
                handleServer(packet.nbt, ctx.get().getSender());
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleServer(CompoundNBT nbt, net.minecraft.entity.player.ServerPlayerEntity player) {
        int command = nbt.getInt("command");

        switch (command) {
            case 8:
                CompoundNBT sendNBT = net.mrbt0907.weather2.config.EZConfigParser.nbtServerData;
                sendNBT.putInt("command", 9);
                sendNBT.putInt("server", 1);
                sendNBT.putBoolean("op", net.minecraftforge.fml.server.ServerLifecycleHooks.getCurrentServer().isSingleplayer() ||
                        net.minecraftforge.fml.server.ServerLifecycleHooks.getCurrentServer().getPlayerList().isOp(player.getGameProfile()));
                net.mrbt0907.weather2.network.packets.PacketEZGUI.syncResponse(sendNBT);
                break;
            case 11:
                net.mrbt0907.weather2.event.ServerTickHandler.playerClientRequestsFullSync(player);
                break;
            case 10:
                if (net.minecraftforge.fml.server.ServerLifecycleHooks.getCurrentServer().isSingleplayer() ||
                        net.minecraftforge.fml.server.ServerLifecycleHooks.getCurrentServer().getPlayerList().isOp(player.getGameProfile()))
                    net.mrbt0907.weather2.config.EZConfigParser.nbtReceiveServer(nbt);
                break;
        }
    }

    public CompoundNBT getNbt() {
        return nbt;
    }

    private static class ClientHandler {
        public static void handleClient(CompoundNBT nbt) {
            net.mrbt0907.weather2.client.event.ClientTickHandler.checkClientWeather();
            int command = nbt.getInt("command");

            switch (command) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
                    break;
                case 9:
                    net.mrbt0907.weather2.config.EZConfigParser.nbtReceiveClient(nbt);
                    break;
                case 10:
                    break;
                case 11:
                case 12:
                case 13:
                case 14:
                    net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
                    break;
                case 17:
                    net.mrbt0907.weather2.client.NewSceneEnhancer.instance().reset();
                    net.mrbt0907.weather2.client.NewSceneEnhancer.instance().enable();
                    break;
                case 18:
                    break;
                case 19:
                    net.mrbt0907.weather2.client.rendering.shaders.VolumetricRenderer.stopShader();
                    if (net.mrbt0907.weather2.config.ConfigClient.enable_volumetrics)
                        net.mrbt0907.weather2.client.rendering.shaders.VolumetricRenderer.startShader();
                    net.mrbt0907.weather2.Weather2.info("Got it");
                    break;
                default:
                    net.mrbt0907.weather2.Weather2.error("Received an invalid network packet from the server");
            }
        }
    }
}