package net.mrbt0907.weather2.network.packets;

import net.corosus.coroutillegacy.packet.PacketHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.weather2.Weather2;

public class PacketBase {
    protected static void send(int command, CompoundNBT nbt, Object... target) {
        if (nbt == null) {
            Weather2.error("Network command #" + command + " returned null nbt data");
            return;
        }

        nbt.putInt("command", command);

        if (target.length == 0 && !isClientSide()) {
            Weather2.PACKET_HANDLER.send(
                    PacketDistributor.ALL.noArg(),
                    PacketHelper.getNBTPacket(nbt, Weather2.MODID)
            );
        } else if (target.length > 0) {
            if (target[0] instanceof RegistryKey) {
                Weather2.PACKET_HANDLER.send(
                        PacketDistributor.DIMENSION.with(() -> (RegistryKey<World>) target[0]),
                        PacketHelper.getNBTPacket(nbt, Weather2.MODID)
                );
            } else if (target[0] instanceof ServerPlayerEntity) {
                Weather2.PACKET_HANDLER.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) target[0]),
                        PacketHelper.getNBTPacket(nbt, Weather2.MODID)
                );
            } else {
                Weather2.error("Network packet #" + command + " returned an invalid target");
            }
        } else {
            Weather2.PACKET_HANDLER.sendToServer(PacketHelper.getNBTPacket(nbt, Weather2.MODID));
        }
    }

    private static boolean isClientSide() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return true;
        return !server.isSameThread();
    }
}