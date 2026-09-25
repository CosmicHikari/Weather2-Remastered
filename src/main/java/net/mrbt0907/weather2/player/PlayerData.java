package net.mrbt0907.weather2.player;

import net.corosus.coroutillegacy.util.CoroUtilFile;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.mrbt0907.weather2.Weather2;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

public class PlayerData {

    public static HashMap<UUID, CompoundNBT> playerNBT = new HashMap<UUID, CompoundNBT>();

    public static CompoundNBT getPlayerNBT(UUID playerUUID) {
        if (!playerNBT.containsKey(playerUUID))
            tryLoadPlayerNBT(playerUUID);

        return playerNBT.get(playerUUID);
    }

    public static void tryLoadPlayerNBT(UUID playerUUID) {
        CompoundNBT nbt = new CompoundNBT();

        try {
            String fileURL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + File.separator + "weather2" + File.separator + "PlayerData" + File.separator + playerUUID.toString() + ".dat";

            if ((new File(fileURL)).exists()) {
                nbt = CompressedStreamTools.readCompressed(Files.newInputStream(Paths.get(fileURL)));
            }
        } catch (Exception ex) {
            // not empty
        }

        playerNBT.put(playerUUID, nbt);
    }

    public static void writeAllPlayerNBT(boolean resetData) {
        String fileURL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + File.separator + "weather2" + File.separator + "PlayerData";
        if (!new File(fileURL).exists()) new File(fileURL).mkdirs();

        playerNBT.forEach(PlayerData::writePlayerNBT);

        if (resetData)
            playerNBT.clear();
    }

    public static void writePlayerNBT(UUID playerUUID, CompoundNBT nbt) {
        String fileURL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + File.separator + "weather2" + File.separator + "PlayerData" + File.separator + playerUUID.toString() + ".dat";

        try {
            FileOutputStream fos = new FileOutputStream(fileURL);
            CompressedStreamTools.writeCompressed(nbt, fos);
            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            Weather2.debug("Error writing Weather2 player data for " + playerUUID);
        }
    }

}