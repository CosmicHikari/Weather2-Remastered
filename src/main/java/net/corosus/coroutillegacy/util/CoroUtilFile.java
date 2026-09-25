package net.corosus.coroutillegacy.util;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class CoroUtilFile {
    public static String lastWorldFolder = "";


    public static String getWorldFolderName() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server != null) {
            try {
                Path worldPath = server.getWorldPath(FolderName.ROOT);
                Path worldFolder = worldPath.getParent();

                if (worldFolder != null) {
                    lastWorldFolder = worldFolder.getFileName().toString();
                    return lastWorldFolder + File.separator;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return lastWorldFolder + File.separator;
    }


    public static String getMinecraftSaveFolderPath() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && !server.isSingleplayer()) {
            return new File(".").getAbsolutePath() + File.separator + "config" + File.separator;
        }
        return DistExecutor.unsafeRunForDist(
                () -> () -> getClientSidePath() + File.separator + "config" + File.separator,
                () -> () -> new File(".").getAbsolutePath() + File.separator + "config" + File.separator
        );
    }

    public static String getWorldSaveFolderPath() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && !server.isSingleplayer()) {
            return new File(".").getAbsolutePath() + File.separator;
        }
        return DistExecutor.unsafeRunForDist(
                () -> () -> getClientSidePath() + File.separator + "saves" + File.separator,
                () -> () -> new File(".").getAbsolutePath() + File.separator
        );
    }

    @OnlyIn(Dist.CLIENT)
    public static String getClientSidePath() {
        return Minecraft.getInstance().gameDirectory.getPath();
    }


    @OnlyIn(Dist.CLIENT)
    public static String getContentsFromResourceLocation(ResourceLocation resourceLocation) {
        try {
            IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            IResource iresource = resourceManager.getResource(resourceLocation);
            String contents = IOUtils.toString(iresource.getInputStream(), StandardCharsets.UTF_8);
            return contents;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
}