package net.mrbt0907.weather2.event;

import net.corosus.coroutillegacy.packet.PacketHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.rendering.shaders.VolumetricRenderer;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.EZConfigParser;
import net.mrbt0907.weather2.network.packets.PacketEZGUI;

public class EventHandlerPacket {

    @SubscribeEvent
    public void onPacketFromServer(NetworkEvent.ClientCustomPayloadEvent event) {
        try {
            CompoundNBT nbt = PacketHelper.readNBTTagCompound(event.getPayload());
            int command = nbt.getInt("command");

            event.getSource().get().enqueueWork(() ->
            {
                switch (command) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        ClientTickHandler.checkClientWeather();
                        ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
                        break;
                    case 9:
                        EZConfigParser.nbtReceiveClient(nbt);
                        break;
                    case 10:
                        break;
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                        ClientTickHandler.checkClientWeather();
                        ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
                        break;
                    case 17:
                        NewSceneEnhancer.instance().reset();
                        NewSceneEnhancer.instance().enable();
                        break;
                    case 18:
                        break;
                    case 19:
                        VolumetricRenderer.stopShader();
                        if (ConfigClient.enable_volumetrics)
                            VolumetricRenderer.startShader();
                        Weather2.info("Got it");
                        break;
                    default:
                        Weather2.error("Recieved an invalid network packet from the server");
                }
            });
            event.getSource().get().setPacketHandled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onPacketFromClient(NetworkEvent.ServerCustomPayloadEvent event) {
        final ServerPlayerEntity entP = event.getSource().get().getSender();

        if (entP == null) return;

        try {
            CompoundNBT nbt = PacketHelper.readNBTTagCompound(event.getPayload());
            int command = nbt.getInt("command");

            event.getSource().get().enqueueWork(() ->
            {
                switch (command) {
                    case 8:
                        CompoundNBT sendNBT = EZConfigParser.nbtServerData;
                        sendNBT.putInt("command", 9);
                        sendNBT.putInt("server", 1);
                        sendNBT.putBoolean("op", ServerLifecycleHooks.getCurrentServer().isSingleplayer() || entP.hasPermissions(2));
                        PacketEZGUI.syncResponse(sendNBT);
                        break;
                    case 11:
                        ServerTickHandler.playerClientRequestsFullSync(entP);
                        break;
                    case 10:
                        if (ServerLifecycleHooks.getCurrentServer().isSingleplayer() || entP.hasPermissions(2))
                            EZConfigParser.nbtReceiveServer(nbt);
                        break;
                }
            });
            event.getSource().get().setPacketHandled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}