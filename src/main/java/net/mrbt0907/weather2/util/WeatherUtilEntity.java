package net.mrbt0907.weather2.util;

import net.corosus.coroutillegacy.api.weather.IWindHandler;
import net.corosus.coroutillegacy.util.CoroUtilEntOrParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.api.WeatherUtilData;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.entity.EntityMovingBlock;
import net.mrbt0907.weather2.mixins.accessor.GoalSelectorAccessor;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WindManager;


public class WeatherUtilEntity {

    private WeatherUtilEntity() {
    }

    public static float getWeight(Object obj) {
        World world = CoroUtilEntOrParticle.getWorld(obj);
        if (world == null)
            return -1.0F;

        if (obj instanceof IWindHandler)
            return ((IWindHandler) obj).getWindWeight();

        if (world.isClientSide && obj instanceof Particle)
            return WeatherUtilEntityClient.getParticleWeight(obj);

        if (obj instanceof EntityMovingBlock) {
            EntityMovingBlock block = (EntityMovingBlock) obj;
            float hardness = block.state.getDestroySpeed(block.level, block.blockPosition());
            if (block.block.isToolEffective(block.state, net.minecraftforge.common.ToolType.AXE))
                return 12F + hardness;
            if (block.block.isToolEffective(block.state, net.minecraftforge.common.ToolType.SHOVEL))
                return 12F + hardness * 6F;
            return 12F + hardness * 13F;
        }

        if (obj instanceof SquidEntity)
            return 400F;

        if (obj instanceof PlayerEntity)
            return getPlayerWeight((PlayerEntity) obj);

        if (obj instanceof LivingEntity)
            return getLivingEntityWeight((LivingEntity) obj);

        if (obj instanceof BoatEntity || obj instanceof ItemEntity || obj instanceof FishingBobberEntity)
            return 4000F;

        if (obj instanceof AbstractMinecartEntity)
            return 80F;

        if (obj instanceof Entity) {
            Entity ent = (Entity) obj;
            if (WeatherUtilData.isWindWeightSet(ent))
                return WeatherUtilData.getWindWeight(ent);
        }

        return 1F;
    }

    private static float getPlayerWeight(PlayerEntity player) {
        int airTime = player.getPersistentData().getInt("timeInAir");

        if (player.isOnGround() || player.isInWater())
            airTime = 0;
        else
            airTime++;

        player.getPersistentData().putInt("timeInAir", airTime);

        if (player.isCreative() || player.isSpectator())
            return -1.0F;

        float armorWeight = 0.0F;
        if (player.inventory != null) {
            for (ItemStack stack : player.inventory.armor) {
                if (!stack.isEmpty() && stack.getMaxDamage() > 0)
                    armorWeight += stack.getMaxDamage() * 0.0025F;
            }
        }

        return 5.0F + armorWeight + airTime * 0.0025F;
    }

    private static float getLivingEntityWeight(LivingEntity entity) {
        int airTime = entity.getPersistentData().getInt("timeInAir");

        if (entity.isOnGround() || entity.isInWater())
            airTime = 0;
        else
            airTime++;

        entity.getPersistentData().putInt("timeInAir", airTime);
        return 5.0F + airTime * 0.0025F;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean canPushEntity(Entity ent) {
        WindManager windMan = ClientTickHandler.weatherManager.windManager;

        double speed = 10.0D;
        float windRad = windMan.windAngle / 180.0F * (float) Math.PI;
        int startX = (int) (ent.getX() - speed * (-Maths.fastSin(windRad) * Maths.fastCos(0F)));
        int startZ = (int) (ent.getZ() - speed * (Maths.fastCos(windRad) * Maths.fastCos(0F)));

        Vector3d start = new Vector3d(ent.getX(), ent.getY() + ent.getEyeHeight(), ent.getZ());
        Vector3d end = new Vector3d(startX, ent.getY() + ent.getEyeHeight(), startZ);

        BlockRayTraceResult result = ent.level.clip(
                new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, ent));
        return result.getType() == RayTraceResult.Type.MISS;
    }

    public static boolean isEntityOutside(Entity entity) {
        return isPosOutside(entity.level, new Vec3(entity.getX(), entity.getY(), entity.getZ()), false);
    }

    public static boolean isEntityOutside(Entity entity, boolean cheapCheck) {
        return isPosOutside(entity.level, new Vec3(entity.getX(), entity.getY(), entity.getZ()), cheapCheck);
    }

    public static boolean isPosOutside(World world, Vec3 pos) {
        return isPosOutside(world, pos, false);
    }

    public static boolean isPosOutside(World world, Vec3 pos, boolean cheapCheck) {
        int rangeCheck = 5;
        int yOffset = 1;

        BlockPos groundPos = new BlockPos(MathHelper.floor(pos.posX), 0, MathHelper.floor(pos.posZ));
        if (WeatherUtilBlock.getPrecipitationHeightSafe(world, groundPos).getY() < pos.posY + 1)
            return true;

        if (cheapCheck)
            return false;

        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            Vec3 target = new Vec3(
                    pos.posX + dir.getStepX() * rangeCheck,
                    pos.posY + yOffset,
                    pos.posZ + dir.getStepZ() * rangeCheck);
            if (checkVecOutside(world, pos, target))
                return true;
        }

        return false;
    }

    public static boolean checkVecOutside(World world, Vec3 pos, Vec3 checkPos) {
        Vector3d start = new Vector3d(pos.posX, pos.posY, pos.posZ);
        Vector3d end = new Vector3d(checkPos.posX, checkPos.posY, checkPos.posZ);

        BlockRayTraceResult result = world.clip(
                new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null));

        BlockPos groundPos = new BlockPos(MathHelper.floor(checkPos.posX), 0, MathHelper.floor(checkPos.posZ));
        return result.getType() == RayTraceResult.Type.MISS
                && WeatherUtilBlock.getPrecipitationHeightSafe(world, groundPos).getY() < checkPos.posY;
    }

    public static PlayerEntity getClosestPlayer(World world, double posX, double posY, double posZ, double radius) {
        PlayerEntity closest = null;
        double minDist = Double.MAX_VALUE;

        for (PlayerEntity player : world.players()) {
            double dist = FartsyUtil.sqrtf((float) player.distanceToSqr(posX, posY, posZ));
            if (dist <= radius && dist < minDist) {
                closest = player;
                minDist = dist;
            }
        }

        return closest;
    }

    public static boolean hasAITask(CreatureEntity creature, Class<? extends Goal> clazz) {
        GoalSelectorAccessor accessor = (GoalSelectorAccessor) creature.goalSelector;
        for (PrioritizedGoal entry : accessor.getAvailableGoals()) {
            if (clazz.isAssignableFrom(entry.getGoal().getClass()))
                return true;
        }
        return false;
    }
}