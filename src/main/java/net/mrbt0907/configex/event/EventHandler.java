package net.mrbt0907.configex.event;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.network.NetworkHandler;

public class EventHandler {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase.equals(Phase.START))
            ClientHandler.onTick();
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerLoggedInEvent event) {
        if (!ConfigManager.isRemote && event.getPlayer() instanceof ServerPlayerEntity)
            NetworkHandler.sendClientPacket(0, ConfigManager.writeNBT(new CompoundNBT()), event.getPlayer());
    }
}