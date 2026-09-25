package net.mrbt0907.weather2.compat;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mrbt0907.weather2.block.tile.TileComputerRadar;
import net.mrbt0907.weather2.event.ServerTickHandler;
import net.mrbt0907.weather2.weather.WeatherManagerServer;

import java.util.*;

public class OpenComputersManager {
    public static void register() {
        Driver.add(new WeatherRadarDriver());
    }

    public static class WeatherRadarDriver extends DriverSidedTileEntity {
        @Override
        public Class<?> getTileEntityClass() {
            return TileComputerRadar.class;
        }

        @Override
        public ManagedEnvironment createEnvironment(World world, BlockPos pos, Direction side) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof TileComputerRadar)
                return new WeatherRadarEnvironment((TileComputerRadar) te);
            return null;
        }
    }

    public static class WeatherRadarEnvironment extends AbstractManagedEnvironment implements NamedBlock {
        private final TileComputerRadar radar;

        public WeatherRadarEnvironment(TileComputerRadar radar) {
            this.radar = radar;
            setNode(li.cil.oc.api.Network.newNode(this, Visibility.Network)
                    .withComponent("weather_radar")
                    .create());
        }

        @Override
        public String preferredName() {
            return "weather_radar";
        }

        @Override
        public int priority() {
            return 10;
        }

        @Override
        public boolean canUpdate() {
            return false;
        }

        private Map<String, Object> getRotationCorrectedMap(Map<String, Object> original) {
            if (original == null) return null;
            Map<String, Object> storm = new HashMap<>(original);
            double dx = (double) storm.get("dx");
            double dz = (double) storm.get("dz");
            double bearing = (double) storm.get("bearing");

            double relX = dx;
            double relZ = dz;
            double rotationOffset = 0;

            if (radar.getLevel() != null) {
                net.minecraft.block.BlockState bs = radar.getBlockState();
                if (bs.hasProperty(net.mrbt0907.weather2.block.BlockComputerRadar.FACING)) {
                    Direction facing = bs.getValue(net.mrbt0907.weather2.block.BlockComputerRadar.FACING);
                    switch (facing) {
                        case NORTH:
                            relX = dx;
                            relZ = dz;
                            rotationOffset = 0;
                            break;
                        case SOUTH:
                            relX = -dx;
                            relZ = -dz;
                            rotationOffset = 180;
                            break;
                        case WEST:
                            relX = dz;
                            relZ = -dx;
                            rotationOffset = 90;
                            break;
                        case EAST:
                            relX = -dz;
                            relZ = dx;
                            rotationOffset = 270;
                            break;
                    }
                }
            }

            storm.put("relX", relX);
            storm.put("relZ", relZ);
            storm.put("relBearing", (bearing - rotationOffset + 360) % 360);

            return storm;
        }

        @Callback(doc = "function():number -- Returns how far out this radar can detect storms, in blocks.")
        public Object[] getDetectionRange(Context ctx, Arguments args) {
            return new Object[]{(double) radar.getDetectionRange()};
        }

        @Callback(doc = "function(range:number):boolean, string -- Sets how far the radar should scan. Range must be between the minimum and the server's configured maximum.")
        public Object[] setDetectionRange(Context ctx, Arguments args) {
            if (args.count() < 1)
                return new Object[]{false, "Missing range parameter"};

            int newRange;
            try {
                newRange = args.checkInteger(0);
            } catch (Exception e) {
                return new Object[]{false, "Invalid range value — expected an integer"};
            }

            int max = TileComputerRadar.getConfiguredMaxRange();
            if (newRange < TileComputerRadar.MIN_RANGE || newRange > max)
                return new Object[]{false, String.format(
                        "Range must be between %d and %d blocks", TileComputerRadar.MIN_RANGE, max)};

            radar.setDetectionRange(newRange);
            return new Object[]{true, String.format("Detection range set to %d blocks", newRange)};
        }

        @Callback(doc = "function() -- Forces the radar to scan right now instead of waiting for the next automatic scan cycle.")
        public Object[] scan(Context ctx, Arguments args) {
            radar.doScan();
            return new Object[]{true};
        }

        @Callback(doc = "function():table -- Returns a general status table for this radar block, including its position, range settings, and how many storms it currently sees.")
        public Object[] getStatus(Context ctx, Arguments args) {
            Map<String, Object> status = new HashMap<>();
            BlockPos pos = radar.getBlockPos();

            status.put("detectionRange", radar.getDetectionRange());
            status.put("minRange", TileComputerRadar.MIN_RANGE);
            status.put("maxRange", TileComputerRadar.getConfiguredMaxRange());
            status.put("x", pos.getX());
            status.put("y", pos.getY());
            status.put("z", pos.getZ());
            status.put("active", !radar.isRemoved());
            status.put("stormCount", radar.cachedStorms.size());
            status.put("frontCount", radar.cachedFronts.size());
            status.put("scanInterval", TileComputerRadar.SCAN_RATE);

            String facing = "north";
            if (radar.getLevel() != null) {
                net.minecraft.block.BlockState bs = radar.getBlockState();
                if (bs.hasProperty(net.mrbt0907.weather2.block.BlockComputerRadar.FACING))
                    facing = bs.getValue(net.mrbt0907.weather2.block.BlockComputerRadar.FACING)
                            .getSerializedName();
            }
            status.put("facing", facing);

            return new Object[]{status};
        }

        @Callback(doc = "function():string -- Returns the component version string.")
        public Object[] getVersion(Context ctx, Arguments args) {
            return new Object[]{"Weather2 Computer Radar v2.9.11"};
        }

        @Callback(doc = "function():number -- Returns how many storms the radar is currently tracking.")
        public Object[] getStormCount(Context ctx, Arguments args) {
            return new Object[]{radar.cachedStorms.size()};
        }

        @Callback(doc = "function():table -- Returns a list of all storms currently in range. Each entry includes 'relX' and 'relZ' adjusted for block rotation.")
        public Object[] getStorms(Context ctx, Arguments args) {
            Map<Integer, Object> result = new LinkedHashMap<>();
            for (int i = 0; i < radar.cachedStorms.size(); i++)
                result.put(i + 1, getRotationCorrectedMap(radar.cachedStorms.get(i)));
            return new Object[]{result};
        }

        @Callback(doc = "function(id:string):table -- Returns the storm with the given UUID string, or nil if it isn't in range.")
        public Object[] getStorm(Context ctx, Arguments args) {
            if (args.count() < 1)
                return new Object[]{null, "Missing storm ID"};

            String targetId = args.checkString(0);
            for (Map<String, Object> storm : radar.cachedStorms)
                if (targetId.equals(storm.get("id")))
                    return new Object[]{getRotationCorrectedMap(storm)};

            return new Object[]{null, "Storm not found"};
        }

        @Callback(doc = "function(index:number):table -- Returns the storm at the given position in the list (1-based), or nil if the index is out of range.")
        public Object[] getStormByIndex(Context ctx, Arguments args) {
            if (args.count() < 1)
                return new Object[]{null, "Missing index"};

            int index = args.checkInteger(0) - 1;
            if (index < 0 || index >= radar.cachedStorms.size())
                return new Object[]{null, "Index out of range"};

            return new Object[]{getRotationCorrectedMap(radar.cachedStorms.get(index))};
        }

        @Callback(doc = "function():table -- Returns the closest storm to the radar, or nil if there are none in range.")
        public Object[] getClosestStorm(Context ctx, Arguments args) {
            if (radar.cachedStorms.isEmpty())
                return new Object[]{null, "No storms in range"};

            Map<String, Object> closest = null;
            double minDistSq = Double.MAX_VALUE;
            for (Map<String, Object> storm : radar.cachedStorms) {
                double d = (double) storm.get("distanceSq");
                if (d < minDistSq) {
                    minDistSq = d;
                    closest = storm;
                }
            }
            return new Object[]{getRotationCorrectedMap(closest)};
        }

        @Callback(doc = "function():table -- Returns the most severe storm the radar can see, or nil if there are none.")
        public Object[] getMostSevereStorm(Context ctx, Arguments args) {
            if (radar.cachedStorms.isEmpty())
                return new Object[]{null, "No storms in range"};

            Map<String, Object> worst = null;
            int highStage = -1;
            for (Map<String, Object> storm : radar.cachedStorms) {
                int stage = (int) storm.get("stage");
                if (stage > highStage) {
                    highStage = stage;
                    worst = storm;
                }
            }
            return new Object[]{getRotationCorrectedMap(worst)};
        }

        @Callback(doc = "function():table -- Returns all storms sorted from closest to farthest.")
        public Object[] getStormsSortedByDistance(Context ctx, Arguments args) {
            List<Map<String, Object>> sorted = new ArrayList<>(radar.cachedStorms);
            sorted.sort(Comparator.comparingDouble(s -> (double) s.get("distanceSq")));

            Map<Integer, Object> result = new LinkedHashMap<>();
            for (int i = 0; i < sorted.size(); i++)
                result.put(i + 1, getRotationCorrectedMap(sorted.get(i)));
            return new Object[]{result};
        }

        @Callback(doc = "function():table -- Returns all storms sorted from most severe to least severe.")
        public Object[] getStormsSortedByStage(Context ctx, Arguments args) {
            List<Map<String, Object>> sorted = new ArrayList<>(radar.cachedStorms);
            sorted.sort((a, b) -> Integer.compare((int) b.get("stage"), (int) a.get("stage")));

            Map<Integer, Object> result = new LinkedHashMap<>();
            for (int i = 0; i < sorted.size(); i++)
                result.put(i + 1, getRotationCorrectedMap(sorted.get(i)));
            return new Object[]{result};
        }

        @Callback(doc = "function(type:string):table -- Returns only storms that match the given type name, e.g. \"TORNADO\", \"HURRICANE\", \"TROPICAL_STORM\".")
        public Object[] getStormsFiltered(Context ctx, Arguments args) {
            if (args.count() < 1)
                return new Object[]{null, "Missing type filter"};

            String filter = args.checkString(0).toUpperCase();
            Map<Integer, Object> result = new LinkedHashMap<>();
            int idx = 1;
            for (Map<String, Object> storm : radar.cachedStorms)
                if (filter.equals(storm.get("type")))
                    result.put(idx++, getRotationCorrectedMap(storm));
            return new Object[]{result};
        }

        @Callback(doc = "function():table -- Returns only the storms that are deadly (i.e. at tornado, cyclone, or hurricane stage).")
        public Object[] getDeadlyStorms(Context ctx, Arguments args) {
            Map<Integer, Object> result = new LinkedHashMap<>();
            int idx = 1;
            for (Map<String, Object> storm : radar.cachedStorms)
                if ((boolean) storm.get("isDeadly"))
                    result.put(idx++, getRotationCorrectedMap(storm));
            return new Object[]{result};
        }

        @Callback(doc = "function(index:number):... -- Returns all fields of a storm as individual return values instead of a table. Useful if you need specific values without unpacking a table.")
        public Object[] getStormData(Context ctx, Arguments args) {
            int index = args.checkInteger(0) - 1;
            if (index < 0 || index >= radar.cachedStorms.size())
                return new Object[]{null};

            Map<String, Object> s = getRotationCorrectedMap(radar.cachedStorms.get(index));
            return new Object[]{
                    s.get("id"), s.get("type"), s.get("name"),
                    s.get("fullName"), s.get("typeName"),
                    s.get("x"), s.get("y"), s.get("z"),
                    s.get("groundY"),
                    s.get("dx"), s.get("dy"), s.get("dz"),
                    s.get("relX"), s.get("relZ"), s.get("relBearing"),
                    s.get("distanceSq"), s.get("distance"),
                    s.get("bearing"), s.get("heading"), s.get("speed"),
                    s.get("etaTicks"),
                    s.get("ticks"), s.get("size"),
                    s.get("stage"), s.get("stageMax"), s.get("layer"),
                    s.get("stormType"),
                    s.get("intensity"), s.get("intensityRate"), s.get("intensityMax"),
                    s.get("sizeRate"), s.get("formingStrength"),
                    s.get("windSpeed"), s.get("funnelSize"),
                    s.get("rain"), s.get("rainRate"),
                    s.get("hail"), s.get("hailRate"),
                    s.get("temperature"), s.get("lightning"),
                    s.get("angle"), s.get("flyingBlocks"), s.get("currentTopY"),
                    s.get("revives"), s.get("maxRevives"),
                    s.get("isTornado"), s.get("isTropicalStorm"),
                    s.get("isCyclone"), s.get("isHurricane"),
                    s.get("isSevere"), s.get("isDeadly"), s.get("isViolent"),
                    s.get("isFirenado"), s.get("isSpout"), s.get("isNatural"),
                    s.get("isStorm"), s.get("canProgress"),
                    s.get("alwaysProgresses"), s.get("neverDissipate"),
                    s.get("overrideAngle"), s.get("overrideMotion"),
                    s.get("isDrizzling"), s.get("isRaining"),
                    s.get("isHailing"), s.get("hasDownfall")
            };
        }

        @Callback(doc = "function():number -- Returns how many weather fronts the radar can currently see.")
        public Object[] getFrontCount(Context ctx, Arguments args) {
            return new Object[]{radar.cachedFronts.size()};
        }

        @Callback(doc = "function():table -- Returns a list of all weather fronts currently in range. Each entry has the front's position, type, atmospheric readings, and storm counts.")
        public Object[] getFronts(Context ctx, Arguments args) {
            Map<Integer, Object> result = new LinkedHashMap<>();
            for (int i = 0; i < radar.cachedFronts.size(); i++)
                result.put(i + 1, radar.cachedFronts.get(i));
            return new Object[]{result};
        }

        @Callback(doc = "function(id:string):table -- Returns the front with the given UUID string, or nil if it isn't in range.")
        public Object[] getFront(Context ctx, Arguments args) {
            if (args.count() < 1)
                return new Object[]{null, "Missing front ID"};

            String targetId = args.checkString(0);
            for (Map<String, Object> front : radar.cachedFronts)
                if (targetId.equals(front.get("id")))
                    return new Object[]{front};

            return new Object[]{null, "Front not found"};
        }

        @Callback(doc = "function(index:number):table -- Returns the front at the given position in the list (1-based), or nil if the index is out of range.")
        public Object[] getFrontByIndex(Context ctx, Arguments args) {
            if (args.count() < 1)
                return new Object[]{null, "Missing index"};

            int index = args.checkInteger(0) - 1;
            if (index < 0 || index >= radar.cachedFronts.size())
                return new Object[]{null, "Index out of range"};

            return new Object[]{radar.cachedFronts.get(index)};
        }

        @Callback(doc = "function():table -- Returns the current wind conditions for this dimension, including direction, speed, and active gusts.")
        public Object[] getWind(Context ctx, Arguments args) {
            if (radar.getLevel() == null)
                return new Object[]{null, "Level not available"};

            WeatherManagerServer wm = ServerTickHandler.dimensionSystems
                    .get(radar.getLevel().dimension());
            if (wm == null)
                return new Object[]{null, "No weather manager for this dimension"};

            Map<String, Object> wind = new HashMap<>();
            wind.put("angle", (double) wm.windManager.windAngle);
            wind.put("speed", (double) wm.windManager.windSpeed);
            wind.put("angleTarget", (double) wm.windManager.windAngleTarget);
            wind.put("speedTarget", (double) wm.windManager.windSpeedTarget);
            wind.put("gustAngle", (double) wm.windManager.windAngleGust);
            wind.put("gustSpeed", (double) wm.windManager.windSpeedGust);
            wind.put("gustTicks", wm.windManager.windTimeGust);

            double rad = Math.toRadians(wm.windManager.windAngle);
            wind.put("forceX", -Math.sin(rad) * wm.windManager.windSpeed);
            wind.put("forceZ", Math.cos(rad) * wm.windManager.windSpeed);

            return new Object[]{wind};
        }

        @Callback(doc = "function():number -- Returns the current wind direction in degrees (0 = north, 90 = east, 180 = south, 270 = west).")
        public Object[] getWindAngle(Context ctx, Arguments args) {
            if (radar.getLevel() == null)
                return new Object[]{null, "Level not available"};

            WeatherManagerServer wm = ServerTickHandler.dimensionSystems
                    .get(radar.getLevel().dimension());
            if (wm == null)
                return new Object[]{null, "No weather manager for this dimension"};

            return new Object[]{(double) wm.windManager.windAngle};
        }

        @Callback(doc = "function():number -- Returns the current wind speed.")
        public Object[] getWindSpeed(Context ctx, Arguments args) {
            if (radar.getLevel() == null)
                return new Object[]{null, "Level not available"};

            WeatherManagerServer wm = ServerTickHandler.dimensionSystems
                    .get(radar.getLevel().dimension());
            if (wm == null)
                return new Object[]{null, "No weather manager for this dimension"};

            return new Object[]{(double) wm.windManager.windSpeed};
        }
    }
}