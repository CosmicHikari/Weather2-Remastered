package net.mrbt0907.weather2.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.event.EventRegisterGrabLists;
import net.mrbt0907.weather2.api.event.EventRegisterParticleRenderer;
import net.mrbt0907.weather2.api.event.EventRegisterStages;
import net.mrbt0907.weather2.api.weather.AbstractWeatherRenderer;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.client.rendering.April24StormRenderer;
import net.mrbt0907.weather2.client.rendering.LegacyStormRenderer;
import net.mrbt0907.weather2.client.rendering.NormalStormRenderer;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigGrab;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.EZConfigParser;
import net.mrbt0907.weather2.util.ConfigList;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WeatherManager;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.util.*;
import java.util.Map.Entry;

public class WeatherAPI {
    private static final ConfigList tornadoStageList = new ConfigList();
    private static final ConfigList hurricaneStageList = new ConfigList();
    private static final ConfigList grabList = new ConfigList();
    private static final ConfigList replaceList = new ConfigList();
    private static final ConfigList entityList = new ConfigList();
    private static final ConfigList windResistanceList = new ConfigList().setReplaceOnly();

    private static final Map<ResourceLocation, Class<?>> particleRenderers = new LinkedHashMap<>();
    private static ResourceLocation currentParticleRenderer;
    private static ResourceLocation currentWeatherLogic;
    private static ResourceLocation currentWeatherManager;

    public static WeatherManager getManager(World world) {
        if (world == null) return null;
        if (world.isClientSide)
            return getManager();
        return net.mrbt0907.weather2.event.ServerTickHandler.dimensionSystems.get(world.dimension());
    }

    @OnlyIn(Dist.CLIENT)
    public static WeatherManager getManager() {
        return net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager;
    }

    public static WeatherManager getManager(RegistryKey<World> dimension) {
        return net.mrbt0907.weather2.event.ServerTickHandler.dimensionSystems.get(dimension);
    }

    @OnlyIn(Dist.CLIENT)
    public static WeatherObject getClosestWeather(Vec3 pos, double maxDist, int minStage, int maxStage, WeatherEnum.Type... excludedTypes) {
        return net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager != null
                ? net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager.getClosestWeather(pos, maxDist, minStage, maxStage, excludedTypes)
                : null;
    }

    public static WeatherObject getClosestWeather(RegistryKey<World> dimension, Vec3 pos, double maxDist, int minStage, int maxStage, WeatherEnum.Type... excludedTypes) {
        WeatherManager manager = getManager(dimension);
        return manager != null ? manager.getClosestWeather(pos, maxDist, minStage, maxStage, excludedTypes) : null;
    }

    public static boolean isPrecipitatingAt(World world, BlockPos position) {
        WeatherManager manager = getManager(world);
        return manager != null && manager.hasDownfall(position);
    }

    public static ConfigList getTornadoStageList() {
        return tornadoStageList;
    }

    public static ConfigList getHurricaneStageList() {
        return hurricaneStageList;
    }

    public static ConfigList getWRList() {
        return windResistanceList;
    }

    public static ConfigList getGrabList() {
        return grabList;
    }

    public static ConfigList getReplaceList() {
        return replaceList;
    }

    public static ConfigList getEntityGrabList() {
        return entityList;
    }

    public static float getEFWindSpeed(float stageMultiplier) {
        return 65.0F + 27.0F * stageMultiplier;
    }

    public static float getEFWindSpeed(int stage) {
        return 65.0F + 27.0F * stage;
    }

    public static ResourceLocation getWeatherLogicId() {
        return currentWeatherLogic;
    }

    public static ResourceLocation getWeatherManagerId() {
        return currentWeatherManager;
    }

    @OnlyIn(Dist.CLIENT)
    public static ResourceLocation getParticleRendererId() {
        return currentParticleRenderer;
    }

    @OnlyIn(Dist.CLIENT)
    public static AbstractWeatherRenderer getParticleRenderer(WeatherObject storm) {
        if (currentParticleRenderer == null || storm == null) return null;
        Class<?> renderer = particleRenderers.get(currentParticleRenderer);
        try {
            return renderer == null ? null : (AbstractWeatherRenderer) renderer.getConstructor(WeatherObject.class).newInstance(storm);
        } catch (Exception e) {
            Weather2.error(e);
            return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void refreshRenders(boolean fullRefresh) {
        currentParticleRenderer = null;
        if (fullRefresh) {
            Weather2.debug("Refreshing all renderers...");
            particleRenderers.clear();
            Weather2.debug("Registering particle renderers...");
            particleRenderers.put(new ResourceLocation(Weather2.MODID, "normal"), NormalStormRenderer.class);
            Weather2.debug("Registered particle renderer " + Weather2.MODID + ":normal");
            particleRenderers.put(new ResourceLocation(Weather2.MODID, "legacy"), LegacyStormRenderer.class);
            Weather2.debug("Registered particle renderer " + Weather2.MODID + ":legacy");
            particleRenderers.put(new ResourceLocation(Weather2.MODID, "april24"), April24StormRenderer.class);
            Weather2.debug("Registered particle renderer " + Weather2.MODID + ":april24");
            EventRegisterParticleRenderer event = new EventRegisterParticleRenderer();

            MinecraftForge.EVENT_BUS.post(event);

            particleRenderers.putAll(event.getRegistry());
            Weather2.debug("All weather renderers updated: " + particleRenderers.size() + " total");
        }

        if (ConfigClient.particle_renderer.matches("^\\d$")) {
            try {
                int i = 0, ii = Integer.parseInt(ConfigClient.particle_renderer);
                for (ResourceLocation id : particleRenderers.keySet()) {
                    if (i == ii) currentParticleRenderer = id;
                    i++;
                }
            } catch (Exception e) {
                Weather2.error(e);
                ConfigClient.particle_renderer = Weather2.MODID + ":normal";
                currentParticleRenderer = new ResourceLocation(Weather2.MODID, "normal");
            }
        } else
            for (ResourceLocation id : particleRenderers.keySet())
                if (id.toString().equals(ConfigClient.particle_renderer))
                    currentParticleRenderer = id;

        Weather2.debug("Set particle renderer to " + (currentParticleRenderer == null ? "none" : currentParticleRenderer.toString()));
    }

    public static void refreshDimensionRules() {
        EZConfigParser.refreshDimensionRules();
    }

    public static void refreshStages() {
        EventRegisterStages event = new EventRegisterStages(tornadoStageList, hurricaneStageList);
        event.tornadoStageList.clear();
        event.hurricaneStageList.clear();
        MinecraftForge.EVENT_BUS.post(event);

        String stagesA = ConfigStorm.chances_for_tornados.replaceAll("[^\\d\\.\\s\\,\\=]*", "");
        String stagesB = ConfigStorm.chances_for_hurricanes.replaceAll("[^\\d\\.\\s\\,\\=]*", "");
        event.tornadoStageList.parse(stagesA);
        event.hurricaneStageList.parse(stagesB);

        Weather2.debug("Cyclonic stages updated:\n- Tornado Stage List = " + tornadoStageList.size()
                + "\n- Hurricane Stage List = " + hurricaneStageList.size());
    }

    public static void refreshGrabRules() {
        ConfigList eventGrabList = new ConfigList();
        ConfigList eventReplaceList = new ConfigList();
        ConfigList eventEntityList = new ConfigList();
        ConfigList eventWindResistanceList = new ConfigList().setReplaceOnly();

        EventRegisterGrabLists event = new EventRegisterGrabLists(
                eventGrabList, eventReplaceList, eventEntityList, eventWindResistanceList);
        MinecraftForge.EVENT_BUS.post(event);

        event.grabList.parse(ConfigGrab.grab_list_entries);
        event.replaceList.parse(ConfigGrab.replace_list_entries);
        event.windResistanceList.parse(ConfigGrab.wind_resistance_entries);
        event.entityList.parse(ConfigGrab.entity_blacklist_entries);

        Set<ResourceLocation> blockEntries = ForgeRegistries.BLOCKS.getKeys();
        Set<ResourceLocation> entityEntries = ForgeRegistries.ENTITIES.getKeys();

        ConfigList list = processGrabList(blockEntries, event.grabList, ConfigGrab.grab_list_partial_match, 0);
        grabList.clear();
        grabList.addAll(list);

        list = processGrabList(blockEntries, event.replaceList, ConfigGrab.replace_list_partial_match, 1);
        replaceList.clear();
        replaceList.addAll(list);

        list = processGrabList(blockEntries, event.windResistanceList, ConfigGrab.wind_resistance_partial_matches, 2);

        for (ResourceLocation id : blockEntries) {
            String strID = id.toString();
            if (!list.containsKey(strID)) {
                Block block = ForgeRegistries.BLOCKS.getValue(id);
                if (block == null) continue;

                BlockState state = block.defaultBlockState();
                float hardness = state.getDestroySpeed(net.minecraft.world.EmptyBlockReader.INSTANCE, BlockPos.ZERO);
                if (hardness >= 0.0F) {
                    boolean easyToBreak = !state.requiresCorrectToolForDrops()
                            || state.getHarvestTool() == ToolType.AXE;
                    list.add(strID, easyToBreak
                            ? WeatherAPI.getEFWindSpeed(hardness)
                            : WeatherAPI.getEFWindSpeed(hardness * 2.0F));
                }
            }
        }
        windResistanceList.clear();
        windResistanceList.addAll(list);

        list = processGrabList(entityEntries, event.entityList, ConfigGrab.entity_blacklist_partial_match, 0);
        entityList.clear();
        entityList.addAll(list);

        Weather2.debug("Grab Rules updated:\n- Grab List = " + WeatherAPI.getGrabList().size()
                + " Entry(s)\n- Replace List = " + WeatherAPI.getReplaceList().size()
                + " Entry(s)\n- Wind Resistance List = " + WeatherAPI.getWRList().size()
                + " Entry(s)\n- Blacklisted Entity List = " + WeatherAPI.getEntityGrabList().size() + " Entry(s)");
    }

    private static ConfigList processGrabList(Set<ResourceLocation> entries, ConfigList cfg, boolean partialMatches, int type) {
        ConfigList list = new ConfigList();
        String keyA, keyB, keyC;
        List<String> keys;
        List<Object> values;
        boolean usePartialMatch;
        int meta;

        if (cfg.isReplaceOnly())
            list.setReplaceOnly();

        for (Entry<String, Object[]> entry : cfg.toMap().entrySet()) {
            if (type > 0 && entry.getValue().length == 0) continue;

            keyA = entry.getKey();

            if (keyA == null || keyA.isEmpty()) continue;

            if (keyA.contains("#")) {
                try {
                    meta = Integer.parseInt(keyA.replaceAll(".*\\#", ""));
                } catch (Exception e) {
                    meta = -1;
                }
                keyA = keyA.replaceAll("\\#.*", "");
            } else
                meta = -1;

            if (keyA.contains(":")) {
                keyB = keyA;
                usePartialMatch = false;
            } else {
                keyB = "minecraft:" + keyA;
                usePartialMatch = partialMatches;
            }

            keys = new ArrayList<>();
            values = new ArrayList<>();

            for (ResourceLocation block : entries) {
                keyC = block.toString();
                if (keyC.equals(keyB) || (usePartialMatch && keyC.toLowerCase().contains(keyA.toLowerCase())))
                    keys.add(keyC + (meta > -1 ? "#" + meta : ""));
            }

            for (Object str : entry.getValue()) {
                switch (type) {
                    case 1:
                        if (str instanceof String) {
                            usePartialMatch = false;
                            keyA = (String) str;
                            if (keyA.contains("#")) {
                                try {
                                    meta = Integer.parseInt(keyA.replaceAll(".*\\#", ""));
                                } catch (Exception e) {
                                    meta = -1;
                                }
                                keyA = keyA.replaceAll("\\#.*", "");
                            } else
                                meta = -1;

                            if (keyA.contains(":"))
                                keyB = keyA;
                            else {
                                keyB = "minecraft:" + keyA;
                                usePartialMatch = partialMatches;
                            }

                            boolean foundInRegistry = false;
                            for (ResourceLocation block : entries) {
                                keyC = block.toString();
                                if (keyC.equals(keyB) || (usePartialMatch && keyC.toLowerCase().contains(keyA.toLowerCase()))) {
                                    values.add(keyC + (meta > -1 ? "#" + meta : ""));
                                    foundInRegistry = true;
                                }
                            }

                            if (!foundInRegistry) {
                                String fallback = keyB + (meta > -1 ? "#" + meta : "");
                                Weather2.debug("processGrabList: replacement target '" + fallback
                                        + "' not found in block registry, using verbatim");
                                values.add(fallback);
                            }
                        }
                        break;
                    case 2:
                        if (str instanceof Float)
                            values.add(str);
                        else if (str instanceof String) {
                            try {
                                values.add(Float.parseFloat((String) str));
                            } catch (Exception e) {
                            }
                        }
                        break;
                    default:
                        values.add(str);
                        break;
                }
            }

            if (type > 0 && values.isEmpty()) continue;

            for (String key : keys)
                list.add(key, values.toArray());
        }

        return list;
    }
}