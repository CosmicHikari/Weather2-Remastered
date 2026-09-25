package net.corosus.coroutillegacy.forge;

import net.corosus.coroutillegacy.config.ConfigCoroUtilLegacy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.mrbt0907.configex.ConfigModEX;

@Mod(CoroUtilLegacy.modID)
public class CoroUtilLegacy {

    public static final String modID = "coroutillegacy";
    public static final String version = "1.16.5-1.2.37";
    public static CoroUtilLegacy instance;
    public static ConfigCoroUtilLegacy configCoroUtillegacy = new ConfigCoroUtilLegacy();

    public CoroUtilLegacy() {
        instance = this;

        CommonProxy.register(FMLJavaModLoadingContext.get().getModEventBus());

        ConfigModEX.register(configCoroUtillegacy);
    }

}