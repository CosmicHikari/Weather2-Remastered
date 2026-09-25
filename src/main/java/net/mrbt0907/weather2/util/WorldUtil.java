package net.mrbt0907.weather2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class WorldUtil {
    private static String cachedWorldName;

    public static boolean isSinglePlayer() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null || server.isSingleplayer();
    }

    public static String getSaveFolder() {
        if (isSinglePlayer()) {
            return getClientGameDir() + File.separator + "saves" + File.separator;
        } else {
            return new File(".").getAbsolutePath() + File.separator;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static String getClientGameDir() {
        return Minecraft.getInstance().gameDirectory.getPath();
    }

    public static String getWorldFolder() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            cachedWorldName = server.getWorldData().getLevelName();
        }
        return cachedWorldName;
    }

    public static String getWorldFile() {
        String worldFolder = getWorldFolder();
        return worldFolder == null ? null : getSaveFolder() + worldFolder + File.separator;
    }

    public static Entity getNearestEntity(World world, double x, double y, double z, double maxDistance) {
        return getNearestEntity(world, x, y, z, maxDistance, null);
    }

    public static Entity getNearestEntity(World world, double x, double y, double z, double maxDistance, Predicate<Entity> predicate) {
        Entity target = null;
        double maxDistanceSq = maxDistance * maxDistance;

        AxisAlignedBB aabb = new AxisAlignedBB(
                x - maxDistance, y - maxDistance, z - maxDistance,
                x + maxDistance, y + maxDistance, z + maxDistance
        );

        List<Entity> entities = world.getEntities((Entity) null, aabb, predicate);

        for (Entity entity : entities) {
            double distSq = entity.distanceToSqr(x, y, z);
            if (distSq < maxDistanceSq) {
                maxDistanceSq = distSq;
                target = entity;
            }
        }
        return target;
    }

    public static List<Entity> getNearestEntities(World world, double x, double y, double z, double maxDistance) {
        return getNearestEntities(world, x, y, z, maxDistance, null);
    }

    public static List<Entity> getNearestEntities(World world, double x, double y, double z, double maxDistance, Predicate<Entity> predicate) {
        AxisAlignedBB aabb = new AxisAlignedBB(
                x - maxDistance, y - maxDistance, z - maxDistance,
                x + maxDistance, y + maxDistance, z + maxDistance
        );

        List<Entity> entities = world.getEntities((Entity) null, aabb, predicate);
        List<Entity> targets = new ArrayList<>();

        double maxDistanceSq = maxDistance * maxDistance;
        for (Entity entity : entities) {
            if (entity.distanceToSqr(x, y, z) < maxDistanceSq) {
                targets.add(entity);
            }
        }
        return targets;
    }
}