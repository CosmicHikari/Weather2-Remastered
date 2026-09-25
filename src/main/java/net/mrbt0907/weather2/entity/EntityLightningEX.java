package net.mrbt0907.weather2.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.ConfigVolume;
import net.mrbt0907.weather2.mixins.accessor.LightningBoltEntityAccessor;
import net.mrbt0907.weather2.registry.EntityRegistry;
import net.mrbt0907.weather2.registry.SoundRegistry;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WorldUtil;

public class EntityLightningEX extends LightningBoltEntity implements IEntityAdditionalSpawnData {
    public int fireLifeTime;
    public int fireChance;

    public EntityLightningEX(EntityType<? extends LightningBoltEntity> type, World world) {
        super(type, world);
        fireLifeTime = ConfigStorm.lightning_bolt_fire_lifetime;
        fireChance = ConfigStorm.lightning_bolt_sets_fire_10_in_x;
    }

    public EntityLightningEX(World world) {
        this(EntityRegistry.LIGHTNING_BOLT.get(), world);
    }

    public EntityLightningEX(World world, double x, double y, double z) {
        this(EntityRegistry.LIGHTNING_BOLT.get(), world);
        this.moveTo(x, y, z);
    }

    @Override
    public void tick() {
        if (!level.isClientSide)
            setSharedFlag(6, isGlowing());

        baseTick();

        LightningBoltEntityAccessor accessor = (LightningBoltEntityAccessor) this;
        int life = accessor.getLife();
        int flashes = accessor.getFlashes();

        if (level.isClientSide && life == 2)
            onSoundTick();

        life--;
        accessor.setLife(life);

        if (life < 0) {
            if (flashes == 0)
                this.remove();
            else if (life < -random.nextInt(10)) {
                flashes--;
                accessor.setFlashes(flashes);
                accessor.setLife(1);

                this.seed = this.random.nextLong();

                if (!level.isClientSide && ConfigStorm.enable_lightning_bolt_fires && level.getGameRules().getBoolean(net.minecraft.world.GameRules.RULE_DOFIRETICK) && Maths.chance(fireChance)) {
                    BlockPos blockpos = this.blockPosition();

                    if (level.isLoaded(blockpos)) {
                        BlockState fireState = net.minecraft.block.AbstractFireBlock.getState(level, blockpos);

                        if (fireState.hasProperty(FireBlock.AGE)) {
                            fireState = fireState.setValue(FireBlock.AGE, fireLifeTime);
                        }

                        if (level.getBlockState(blockpos).isAir() && fireState.canSurvive(level, blockpos)) {
                            level.setBlockAndUpdate(blockpos, fireState);
                        }
                    }
                }
            }
        } else {
            if (level.isClientSide) {
                if (ConfigClient.enable_sky_lightning)
                    onLightSky();
            } else {
                WorldUtil.getNearestEntities(level, this.getX(), this.getY(), this.getZ(), 3.0D).forEach(entity -> {
                    if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, this))
                        entity.thunderHit((net.minecraft.world.server.ServerWorld) level, this);
                });
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void onLightSky() {
        net.minecraft.client.Minecraft MC = net.minecraft.client.Minecraft.getInstance();
        if (MC.player != null && MC.player.distanceTo(this) < ConfigStorm.max_lightning_bolt_distance)
            level.setSkyFlashTime(2);
    }

    @OnlyIn(Dist.CLIENT)
    protected void onSoundTick() {
        net.minecraft.client.Minecraft MC = net.minecraft.client.Minecraft.getInstance();
        if (MC.player == null) return;
        double distance = MC.player.distanceTo(this);
        if (distance < ConfigStorm.max_lightning_bolt_distance) {
            if (distance > 200.0D)
                level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundRegistry.thunderNear.get(), SoundCategory.WEATHER, 10000.0F * ConfigVolume.lightning, 0.8F + random.nextFloat() * 0.2F, true);
            else {
                level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 64.0F * ConfigVolume.lightning, 0.8F + random.nextFloat() * 0.2F, true);
                level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundRegistry.thunderDangerouslyClose.get(), SoundCategory.WEATHER, (float) (150.0D / distance) * ConfigVolume.lightning, 0.5F + random.nextFloat() * 0.2F, false);
            }
        } else if (distance < ConfigStorm.max_lightning_bolt_distance * 1.5D) {
            level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundRegistry.thunderFar.get(), SoundCategory.WEATHER, 10000.0F * ConfigVolume.lightning, 0.8F + random.nextFloat() * 0.2F, false);
        }
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(fireLifeTime);
        buffer.writeInt(fireChance);
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.fireLifeTime = buffer.readInt();
        this.fireChance = buffer.readInt();
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}