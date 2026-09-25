package net.mrbt0907.weather2.entity.AI;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.server.ServerWorld;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.event.ServerTickHandler;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WeatherManager;

import java.util.EnumSet;
import java.util.Optional;

public class EntityAITakeCover extends Goal {
    private static final double SHELTER_REACH_DIST_SQ = 6.25D;
    private final CreatureEntity mob;
    public boolean isAlert = false;
    private BlockPos shelterPos = null;
    private int pathRetryTimer = 0;

    public EntityAITakeCover(CreatureEntity entity) {
        this.mob = entity;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean hasReachedShelter() {
        if (shelterPos == null) return false;
        return mob.distanceToSqr(
                shelterPos.getX() + 0.5D,
                shelterPos.getY(),
                shelterPos.getZ() + 0.5D) <= SHELTER_REACH_DIST_SQ;
    }

    private boolean isStormActive() {
        if (isAlert) return true;
        WeatherManager weatherManager = ServerTickHandler.getWeatherSystemForDim(mob.level.dimension());
        if (weatherManager == null) return false;

        double rangeSq = ConfigStorm.villager_detection_range;
        Vec3 pos = new Vec3(mob.blockPosition());
        return weatherManager.getWorstWeather(
                pos, rangeSq, Stage.SEVERE.getStage(),
                Integer.MAX_VALUE, WeatherEnum.Type.CLOUD) != null;
    }

    private boolean findShelter() {
        if (!(mob.level instanceof ServerWorld)) return false;
        ServerWorld serverWorld = (ServerWorld) mob.level;

        Optional<BlockPos> nearestPOI = serverWorld.getPoiManager().findClosest(
                PointOfInterestType.HOME.getPredicate(),
                mob.blockPosition(),
                64,
                PointOfInterestManager.Status.ANY
        );

        if (nearestPOI.isPresent()) {
            shelterPos = nearestPOI.get();
            return true;
        }
        return false;
    }

    @Override
    public boolean canUse() {
        if (!isStormActive()) return false;
        if (hasReachedShelter()) return false;
        return findShelter();
    }

    @Override
    public boolean canContinueToUse() {
        if (!isStormActive()) return false;
        if (hasReachedShelter()) return false;
        return !mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        pathRetryTimer = 0;
        pathToShelter();
    }

    @Override
    public void tick() {
        if (shelterPos == null) return;
        if (hasReachedShelter()) {
            mob.getNavigation().stop();
            return;
        }

        pathRetryTimer--;
        if (pathRetryTimer <= 0 || mob.getNavigation().isDone()) {
            pathRetryTimer = 20;
            pathToShelter();
        }
    }

    private void pathToShelter() {
        if (shelterPos == null) return;

        double targetX = shelterPos.getX() + 0.5D;
        double targetY = shelterPos.getY();
        double targetZ = shelterPos.getZ() + 0.5D;

        if (mob.distanceToSqr(targetX, targetY, targetZ) > 256.0D) {
            Vector3d waypoint = RandomPositionGenerator.getPosTowards(
                    mob, 14, 3, new Vector3d(targetX, targetY, targetZ));
            if (waypoint != null) {
                mob.getNavigation().moveTo(waypoint.x, waypoint.y, waypoint.z, 1.0D);
                return;
            }
        }

        mob.getNavigation().moveTo(targetX, targetY, targetZ, 1.0D);
    }

    @Override
    public void stop() {
        isAlert = false;
        shelterPos = null;
        pathRetryTimer = 0;
        mob.getNavigation().stop();
    }
}