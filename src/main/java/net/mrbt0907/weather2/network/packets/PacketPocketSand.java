package net.mrbt0907.weather2.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.mrbt0907.weather2.item.ItemPocketSand;

import java.util.function.Supplier;

public class PacketPocketSand {
    private final String playerName;

    public PacketPocketSand(String playerName) {
        this.playerName = playerName;
    }

    public static void encode(PacketPocketSand packet, PacketBuffer buffer) {
        buffer.writeUtf(packet.playerName);
    }

    public static PacketPocketSand decode(PacketBuffer buffer) {
        return new PacketPocketSand(buffer.readUtf());
    }

    public static void handle(PacketPocketSand packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {

            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet.playerName));
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(String username) {
        World world = Minecraft.getInstance().level;
        if (world != null) {
            PlayerEntity player = world.players().stream()
                    .filter(p -> p.getName().getString().equals(username))
                    .findFirst()
                    .orElse(null);

            if (player != null) {

                ItemPocketSand.particulate(world, player);
            }
        }
    }
}