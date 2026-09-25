package net.mrbt0907.weather2.network;

import java.util.ArrayList;
import java.util.List;

/**
 * Useful as a guide for the netcode. Could be used directly.
 * 0 - Update Vanilla Weather<br>
 * 1 - Create Weather Object<br>
 * 2 - Update Weather Object<br>
 * 3 - Remove Weather Object<br>
 * 4 - Create Volcano Object<br>
 * 5 - Update Volcano Object<br>
 * 6 - Update Wind Manager<br>
 * 7 - Create Lightning Bolt
 */
public enum EnumNetworkID {
    UPDATE_VANILLA_WEATHER("weatherID", "weatherRainTime"),
    CREATE_WEATHER_OBJECT("weatherObject"),
    UPDATE_WEATHER_OBJECT("weatherObject"),
    REMOVE_WEATHER_OBJECT("uuid"),
    CREATE_VOLCANO_OBJECT("volcanoObject"),
    UPDATE_VOLCANO_OBJECT("volcanoObject"),
    UPDATE_WIND_MANAGER("manager"),
    CREATE_LIGHTNING_BOLT("posX", "posY", "posZ", "entityID", "useCustomLightning"),
    EZGUI_SYNC_REQUEST,
    EZGUI_SYNC_RECIEVE,
    EZGUI_APLLY,
    UPDATE_ALL;


    private final List<String> keys = new ArrayList<String>();

    EnumNetworkID(String... nbtKeys) {
        for (String key : nbtKeys)
            if (nbtKeys.length > 0)
                keys.add(key);
    }

    public String getKey(int index) {
        return keys.get(index);
    }

    public List<String> getKeys(int index) {
        return keys;
    }
}