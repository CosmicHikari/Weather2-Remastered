package net.corosus.coroutillegacy.config;

import net.mrbt0907.configex.api.ConfigAnnotations.Comment;
import net.mrbt0907.configex.api.ConfigAnnotations.IntegerRange;
import net.mrbt0907.configex.api.ConfigAnnotations.Name;
import net.mrbt0907.configex.api.IConfigEX;

public class ConfigCoroUtilLegacy implements IConfigEX {

    @Name("Disable Particle Renderer")
    @Comment("Disable the custom particle renderer")
    public static boolean disableParticleRenderer = false;

    @Name("Disable Mipmap Fix")
    @Comment("Disable the mipmap fix")
    public static boolean disableMipmapFix = false;

    @Name("Force Shaders Off")
    @Comment("Force all shaders to be disabled")
    public static boolean forceShadersOff = false;

    @Name("Use Entity Render Hook For Shaders")
    @Comment("Provides better context for shaders/particles to work nice with translucent blocks like glass and water")
    public static boolean useEntityRenderHookForShaders = true;

    @Name("Optimized Cloud Rendering")
    @Comment("WIP, more strict transparent cloud usage, better on fps")
    public static boolean optimizedCloudRendering = false;

    @Name("Debug Shaders")
    @Comment("Enable shader debugging output")
    public static boolean debugShaders = false;

    @Name("Foliage Shaders")
    @Comment("Enable foliage shader effects")
    public static boolean foliageShaders = false;

    @Name("Particle Shaders")
    @Comment("Enable particle shader effects")
    public static boolean particleShaders = true;

    @Name("Use Logging Log")
    @Comment("For seldom used but important things to print out in production")
    public static boolean useLoggingLog = true;

    @Name("Use Logging Debug")
    @Comment("For debugging things")
    public static boolean useLoggingDebug = false;

    @Name("Use Logging Error")
    @Comment("For logging warnings/errors")
    public static boolean useLoggingError = true;

    @Name("Ticks To Repair Block")
    @Comment("How many game ticks until a repairing block fully restores to its original block")
    @IntegerRange(min = 1, max = 72000)
    public static int ticksToRepairBlock = 20 * 60 * 5;

    @Override
    public String getName() {
        return "CoroUtilLegacy - General";
    }

    @Override
    public String getDescription() {
        return "General configuration for CoroUtilLegacy";
    }

    @Override
    public String getSaveLocation() {
        return "CoroUtilLegacy/coroutil_general";
    }

    @Override
    public void onConfigChanged(Phase phase, int variables) {
    }

    @Override
    public void onValueChanged(String variable, Object oldValue, Object newValue) {
    }
}