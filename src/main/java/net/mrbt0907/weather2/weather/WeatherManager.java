package net.mrbt0907.weather2.weather;

import net.corosus.coroutillegacy.util.CoroUtilPhysics;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.IWeatherRain;
import net.mrbt0907.weather2.api.weather.IWeatherStaged;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.config.ConfigSimulation;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class WeatherManager {
    public long ticks;
    public WindManager windManager;
    public float cloudIntensity = 1F;
    protected RegistryKey<World> dim;
    protected World world;
    protected HashSet<Long> listWeatherBlockDamageDeflector = new HashSet<>();


    protected FrontObject globalFront;
    protected Map<UUID, FrontObject> fronts = new ConcurrentHashMap<UUID, FrontObject>();
    protected Map<UUID, WeatherObject> systems = new ConcurrentHashMap<UUID, WeatherObject>();


    protected List<VolcanoObject> volcanoObjects = new ArrayList<>();
    protected List<UUID> volcanoUUIDS = new ArrayList<UUID>();

    public WeatherManager(World world) {
        if (world == null)
            Weather2.error("WeatherSystem recieved a null world. Game may crash");
        else
            Weather2.debug("Creating new WeatherSystem for dimension #" + world.dimension().location());

        this.world = world;

        dim = world.dimension();
        windManager = new WindManager(this);
    }

    public void reset(boolean fullReset) {

        fronts.forEach((uuid, front) -> front.reset());
        fronts.clear();
        globalFront = null;


        volcanoObjects.forEach(vo -> vo.reset());
        volcanoObjects.clear();
        volcanoUUIDS.clear();

        if (fullReset) {

            windManager.reset();
        }
    }

    public void tick() {
        if (world != null) {
            fronts.forEach((uuid, front) -> {
                if (!front.isDead) {
                    front.tick();
                }
            });
            volcanoObjects.forEach(vo -> vo.tick());
            windManager.tick();
            ticks++;
        }
    }

    public StormObject createStorm(double posX, double posZ, int layer, int stage, Map<String, Boolean> flags) {
        return globalFront.createStorm(posX, posZ, stage, flags);
    }

    public StormObject createNaturalStorm(int layer) {
        return globalFront.createNaturalStorm();
    }


    public WeatherObject createWeatherObject(Class<? extends WeatherObject> clazz) {
        return globalFront.createWeatherObject(clazz);
    }

    public WeatherObject addWeatherObject(WeatherObject wo) {
        if (!systems.containsKey(wo.getUUID()))
            systems.put(wo.getUUID(), wo);
        return wo;
    }

    public void removeWeatherObject(UUID uuid) {
        WeatherObject system = systems.get(uuid);

        if (system != null) {
            systems.remove(uuid);
            Weather2.debug("Weather " + uuid + " was removed from manager #" + world.dimension().location());
        } else
            Weather2.error("Manager for dimension #" + world.dimension().location() + " tried to remove a non-existent weather object with uuid " + uuid);
    }

    public FrontObject createNaturalFront(int layer, PlayerEntity player) {
        FrontObject front = createFront(layer, player.getX() + Maths.random(-ConfigSimulation.max_storm_spawning_distance, ConfigSimulation.max_storm_spawning_distance), player.getZ() + Maths.random(-ConfigSimulation.max_storm_spawning_distance, ConfigSimulation.max_storm_spawning_distance));
        fronts.put(front.getUUID(), front);
        return front;
    }

    public FrontObject createFront(int layer, double posX, double posZ) {
        FrontObject front = new FrontObject(this, new Vec3(posX, 0, posZ), layer);
        return front;
    }

    public void removeFront(FrontObject front) {
        removeFront(front.getUUID());
    }

    public void removeFront(UUID uuid) {
        FrontObject front = fronts.get(uuid);

        if (front != null) {
            front.reset();
            fronts.remove(uuid);
            Weather2.debug("Front " + uuid.toString() + " was removed from manager #" + world.dimension().location());
        } else
            Weather2.error("Front " + uuid.toString() + " does not exist on this side. Skipping...");
    }

    public FrontObject getFront(UUID uuid) {
        return fronts.get(uuid);
    }

    public List<FrontObject> getFronts() {
        return new ArrayList<FrontObject>(fronts.values());
    }

    public List<FrontObject> getFronts(int layer) {
        List<FrontObject> fronts = new ArrayList<FrontObject>();
        for (FrontObject front : this.fronts.values())
            if (front.layer == layer)
                fronts.add(front);
        return fronts;
    }

    public Map<Integer, List<FrontObject>> getLayeredFronts() {
        Map<Integer, List<FrontObject>> fronts = new HashMap<Integer, List<FrontObject>>();

        for (FrontObject front : this.fronts.values())
            fronts.get(front.layer).add(front);
        return fronts;
    }

    public List<VolcanoObject> getVolcanoObjects() {
        return volcanoObjects;
    }

    public VolcanoObject getVolcanoObjectByID(UUID ID) {
        int size = volcanoUUIDS.size();
        for (int i = 0; i < size; i++)
            if (volcanoUUIDS.get(i).equals(ID))
                return volcanoObjects.get(i);
        return null;
    }

    public void addVolcanoObject(VolcanoObject so) {
        if (!volcanoUUIDS.contains(so.getUUID())) {
            volcanoObjects.add(so);
            volcanoUUIDS.add(so.getUUID());
        } else
            Weather2.warn("Client received new volcano create for an ID that is already active! design bug");
    }

    public void removeVolcanoObject(UUID ID) {
        VolcanoObject vo = getVolcanoObjectByID(ID);
        if (vo != null) {
            vo.setDead();
            volcanoObjects.remove(vo);
            volcanoUUIDS.remove(ID);

            Weather2.debug("removing volcano");
        }
    }

    public boolean hasDownfall() {
        List<WeatherObject> systems = getWeatherObjects();

        for (WeatherObject system : systems)
            if (system instanceof IWeatherRain && ((IWeatherRain) system).hasDownfall())
                return true;
        return false;
    }

    public boolean hasDownfall(BlockPos pos) {
        return hasDownfall(new Vec3(pos));
    }


    public boolean hasDownfall(Vec3 pos) {
        List<WeatherObject> systems = getWeatherObjects();

        for (WeatherObject system : systems)
            if (system instanceof IWeatherRain && ((IWeatherRain) system).hasDownfall(pos))
                return true;

        return false;
    }

    public WeatherObject getClosestWeather(Vec3 pos, double distance) {
        return getClosestWeather(pos, distance, 0, Integer.MAX_VALUE, WeatherEnum.Type.BLIZZARD, WeatherEnum.Type.CLOUD, WeatherEnum.Type.SANDSTORM);
    }

    public WeatherObject getClosestWeather(Vec3 pos, double distance, int minStage, int maxStage, WeatherEnum.Type... excludedTypes) {
        Map<WeatherObject, Integer> list = getWeatherSystems(pos, distance, minStage, maxStage, excludedTypes);
        WeatherObject result = null;
        double dist = Double.MAX_VALUE, curDist;

        for (WeatherObject weather : list.keySet()) {
            curDist = weather.pos.distanceSq(pos) - weather.size;
            if (curDist < dist) {
                dist = curDist;
                result = weather;
            }
        }

        return result;
    }

    public WeatherObject getWorstWeather(Vec3 pos, double distance) {
        return getWorstWeather(pos, distance, 0, Integer.MAX_VALUE, WeatherEnum.Type.BLIZZARD, WeatherEnum.Type.CLOUD, WeatherEnum.Type.SANDSTORM);
    }

    public WeatherObject getWorstWeather(Vec3 pos, double distance, int minStage, int maxStage, WeatherEnum.Type... excludedTypes) {
        Map<WeatherObject, Integer> list = getWeatherSystems(pos, distance, minStage, maxStage, excludedTypes);
        WeatherObject result = null;
        int stage = -1, curStage;

        for (Entry<WeatherObject, Integer> entry : list.entrySet()) {
            curStage = entry.getValue();
            if (curStage > stage) {
                stage = curStage;
                result = entry.getKey();
            }
        }

        return result;
    }

    public Map<WeatherObject, Integer> getWeatherSystems(Vec3 pos, double distance) {
        return getWeatherSystems(pos, distance, 0, Integer.MAX_VALUE, WeatherEnum.Type.BLIZZARD, WeatherEnum.Type.CLOUD, WeatherEnum.Type.SANDSTORM);
    }

    public Map<WeatherObject, Integer> getWeatherSystems(Vec3 pos, double distance, int minStage, int maxStage, WeatherEnum.Type... excludedTypes) {
        boolean truth;
        int stage;
        Map<WeatherObject, Integer> list = new HashMap<WeatherObject, Integer>();
        List<WeatherObject> curList = new ArrayList<WeatherObject>(systems.values());

        for (WeatherObject weather : curList) {
            truth = true;
            stage = 0;

            for (WeatherEnum.Type type : excludedTypes) {
                if (weather.type.equals(type)) {
                    truth = false;
                    break;
                }
            }

            if (truth && weather.pos.distanceSq(pos) - weather.size < distance) {
                if (weather instanceof IWeatherStaged)
                    stage = weather.getStage();

                if (stage >= minStage && stage <= maxStage)
                    list.put(weather, stage);
            }
        }

        return list;
    }


    public SandstormObject getClosestSandstorm(Vec3 parPos, double maxDist) {
        SandstormObject closestStorm = null;
        double closestDist = 9999999;

        List<WeatherObject> listStorms = getWeatherObjects();

        for (int i = 0; i < listStorms.size(); i++) {
            WeatherObject wo = listStorms.get(i);
            if (wo instanceof SandstormObject) {
                SandstormObject storm = (SandstormObject) wo;
                if (storm == null || storm.isDead) continue;
                double dist = storm.pos.distanceSq(parPos);
                if (dist < closestDist && dist <= maxDist) {
                    closestStorm = storm;
                    closestDist = dist;
                }
            }

        }

        return closestStorm;
    }


    public SandstormObject getClosestSandstormByIntensity(Vec3 parPos) {
        SandstormObject bestStorm = null;
        double closestDist = 9999999;
        double mostIntense = 0;

        List<WeatherObject> listStorms = getWeatherObjects();

        for (int i = 0; i < listStorms.size(); i++) {
            WeatherObject wo = listStorms.get(i);
            if (wo instanceof SandstormObject) {
                SandstormObject sandstorm = (SandstormObject) wo;
                if (sandstorm == null || sandstorm.isDead) continue;

                List<net.corosus.coroutillegacy.util.Vec3> points = sandstorm.getSandstormAsShape();

                double scale = sandstorm.getSandstormScale();
                boolean inStorm = CoroUtilPhysics.isInConvexShape(parPos.toVec3Coro(), points);
                double dist = CoroUtilPhysics.getDistanceToShape(parPos.toVec3Coro(), points);

                if (inStorm) {

                    closestDist = 0;
                    if (scale > mostIntense) {
                        mostIntense = scale;
                        bestStorm = sandstorm;
                    }

                } else if (closestDist > 0) {
                    if (dist < closestDist) {
                        closestDist = dist;
                        bestStorm = sandstorm;
                    }
                }
            }

        }

        return bestStorm;
    }

    public List<SandstormObject> getSandstormsAround(Vec3 parPos, double maxDist) {
        List<WeatherObject> systems = getWeatherObjects();
        List<SandstormObject> sandstorms = new ArrayList<SandstormObject>();

        for (WeatherObject system : systems)
            if (system instanceof SandstormObject) {
                SandstormObject storm = (SandstormObject) system;
                if (!storm.isDead && storm.pos.distanceSq(parPos) <= maxDist)
                    sandstorms.add(storm);
            }

        return sandstorms;
    }

    public List<WeatherObject> getWeatherObjects() {
        return new ArrayList<WeatherObject>(systems.values());
    }

    public HashSet<Long> getListWeatherBlockDamageDeflector() {
        return listWeatherBlockDamageDeflector;
    }

    public void setListWeatherBlockDamageDeflector(HashSet<Long> listWeatherBlockDamageDeflector) {
        this.listWeatherBlockDamageDeflector = listWeatherBlockDamageDeflector;
    }

    public World getWorld() {
        return world;
    }

    public FrontObject getGlobalFront() {
        return globalFront;
    }

    public RegistryKey<World> getDimension() {
        return dim;
    }

    public boolean isClient() {
        return world.isClientSide;
    }


    public StormObject getStrongestClosestStorm(Vec3 pos, float maxDistanceSq) {
        return systems.values().stream()
                .filter(s -> s instanceof StormObject)
                .map(s -> (StormObject) s)
                .filter(storm ->
                        storm instanceof IWeatherStaged &&
                                Maths.distanceSq(storm.pos.posX, storm.pos.posZ, pos.posX, pos.posZ) < maxDistanceSq)
                .min((a, b) -> {

                    int stageCompare = Integer.compare(b.stage, a.stage);
                    if (stageCompare != 0) return stageCompare;


                    boolean aPriority = a.isViolent && a.stage > WeatherEnum.Stage.THUNDER.getStage();
                    boolean bPriority = b.isViolent && b.stage > WeatherEnum.Stage.THUNDER.getStage();
                    if (aPriority != bPriority) {
                        return aPriority ? -1 : 1;
                    }


                    double distA = Maths.distanceSq(a.pos.posX, a.pos.posZ, pos.posX, pos.posZ);
                    double distB = Maths.distanceSq(b.pos.posX, b.pos.posZ, pos.posX, pos.posZ);
                    return Double.compare(distA, distB);
                })
                .orElse(null);
    }


    public StormObject getStrongestClosestStormWithRain(Vec3 pos, float maxDistanceSq) {
        return systems.values().stream()
                .filter(s -> s instanceof StormObject)
                .map(s -> (StormObject) s)
                .filter(storm -> storm instanceof IWeatherRain &&
                        Maths.distanceSq(storm.pos.posX, storm.pos.posZ, pos.posX, pos.posZ) < maxDistanceSq
                        && storm.rain >= IWeatherRain.MINIMUM_DRIZZLE)
                .min((a, b) -> {

                    int stageCompare = Integer.compare(b.stage, a.stage);
                    if (stageCompare != 0) return stageCompare;


                    boolean aHasViolentBonus = a.isViolent && a.stage > WeatherEnum.Stage.RAIN.getStage();
                    boolean bHasViolentBonus = b.isViolent && b.stage > WeatherEnum.Stage.RAIN.getStage();
                    if (aHasViolentBonus != bHasViolentBonus) {
                        return aHasViolentBonus ? -1 : 1;
                    }


                    int rainCompare = Integer.compare((int) b.rain, (int) a.rain);
                    if (rainCompare != 0) return rainCompare;


                    double distA = Maths.distanceSq(a.pos.posX, a.pos.posZ, pos.posX, pos.posZ);
                    double distB = Maths.distanceSq(b.pos.posX, b.pos.posZ, pos.posX, pos.posZ);
                    return Double.compare(distA, distB);
                })
                .orElse(null);
    }
}