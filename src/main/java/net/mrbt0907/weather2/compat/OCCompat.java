package net.mrbt0907.weather2.compat;

import net.mrbt0907.weather2.Weather2;

public class OCCompat {
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        try {
            Class.forName("li.cil.oc.api.network.ManagedEnvironment");
            Class.forName("net.mrbt0907.weather2.compat.OpenComputersManager")
                    .getMethod("register")
                    .invoke(null);
            Weather2.info("OpenComputers detected — Computerized Weather Radar is available.");
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            Weather2.info("OpenComputers not detected — Computerized Weather Radar unavailable.");
        } catch (Exception e) {
            Weather2.info("OpenComputers detected but failed to register driver.");
            e.printStackTrace();
        }
    }

}