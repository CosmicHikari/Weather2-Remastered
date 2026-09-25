package net.corosus.coroutillegacy.forge;

import net.corosus.coroutillegacy.config.ConfigCoroUtilLegacy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CULog {

    private static final Logger LOGGER = LogManager.getLogger(CoroUtilLegacy.modID);


    public static void log(String string) {
        if (ConfigCoroUtilLegacy.useLoggingLog) {
            LOGGER.info(string);
        }
    }


    public static void err(String string) {
        if (ConfigCoroUtilLegacy.useLoggingError) {
            LOGGER.error(string);
        }
    }


    public static void dbg(String string) {
        if (ConfigCoroUtilLegacy.useLoggingDebug) {
            LOGGER.info(string);
        }
    }

}