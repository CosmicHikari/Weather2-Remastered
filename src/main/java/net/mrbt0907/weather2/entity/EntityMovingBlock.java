package net.mrbt0907.weather2.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.mrbt0907.weather2.api.WeatherDamageSource;
import net.mrbt0907.weather2.config.ConfigGrab;
import net.mrbt0907.weather2.registry.EntityRegistry;
import net.mrbt0907.weather2.util.ChunkUtils;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.weather.storm.StormObject;

import javax.annotation.Nonnull;
import java.util.*;

public class EntityMovingBlock extends Entity implements IEntityAdditionalSpawnData {
    protected static final Map<net.minecraft.util.RegistryKey<World>, List<Entity>> loadedEntities = new HashMap<>();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    public Block block;
    public BlockState state;
    public Class<? extends TileEntity> tileClass;
    public CompoundNBT tileEntityNBT;
    public Material material;
    public StormObject storm;
    public int mode;
    public int age;
    public boolean noCollision;
    public boolean collideFalling = false;
    public double vecX;
    public double vecY;
    public double vecZ;
    public int gravityDelay;

    public EntityMovingBlock(EntityType<?> type, World world) {
        super(type, world);
        this.state = EntityMovingBlock.AIR;
        this.block = state.getBlock();
        this.material = state.getMaterial();
        noCollision = true;
    }

    public EntityMovingBlock(World world) {
        this(EntityRegistry.MOVING_BLOCK.get(), world);
        noCollision = true;
    }

    public EntityMovingBlock(World world, int x, int y, int z, BlockState state, StormObject storm) {
        this(EntityRegistry.MOVING_BLOCK.get(), world);

        this.state = state;
        this.block = state.getBlock();
        this.material = state.getMaterial();
        this.storm = storm;

        if (block.hasTileEntity(state)) {
            TileEntity tile = world.getBlockEntity(new BlockPos(x, y, z));
            if (tile != null) {
                tileClass = tile.getClass();
                tileEntityNBT = tile.save(new CompoundNBT());
            } else {
                tileClass = null;
                tileEntityNBT = null;
            }
        }

        mode = 1;
        age = 0;
        noCollision = false;
        gravityDelay = 60;

        setBoundingBox(new AxisAlignedBB(
                x + 0.05D, y + 0.05D, z + 0.05D,
                x + 0.95D, y + 0.95D, z + 0.95D
        ));
        setPos(x + 0.5D, y + 0.5D, z + 0.5D);
        setDeltaMovement(0.0D, 0.0D, 0.0D);
        xo = x + 0.5D;
        yo = y + 0.5D;
        zo = z + 0.5D;
    }

    public static void updateEntities(World world) {
        if (!(world instanceof net.minecraft.world.server.ServerWorld))
            return;

        net.minecraft.world.server.ServerWorld serverWorld = (net.minecraft.world.server.ServerWorld) world;

        List<Entity> filtered = new ArrayList<>();
        for (Entity entity : serverWorld.getAllEntities()) {
            if (!(entity instanceof EntityMovingBlock)
                    && entity.isPickable()
                    && entity.invulnerableTime == 0) {
                filtered.add(entity);
            }
        }

        EntityMovingBlock.loadedEntities.put(world.dimension(), filtered);
    }

    public static void resetEntities() {
        EntityMovingBlock.loadedEntities.clear();
    }

    @Override
    public boolean fireImmune() {
        return material != null && material.isFlammable();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256D * 256D;
    }

    @Override
    public boolean isPickable() {
        return !this.removed && !this.noCollision;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public boolean isPushable() {
        return !this.removed;
    }

    @Override
    public void tick() {
        if (block.equals(Blocks.AIR)) {
            this.remove();
            return;
        }

        Vector3d motion = this.getDeltaMovement();
        double motionX = motion.x * 0.98D;
        double motionY = motion.y - 0.05000000074505806D;
        double motionZ = motion.z * 0.98D;

        xo = this.getX();
        yo = this.getY();
        zo = this.getZ();

        this.setPos(this.getX() + motionX, this.getY() + motionY, this.getZ() + motionZ);
        this.setDeltaMovement(motionX, motionY, motionZ);

        ++age;
        vecX++;
        vecY++;
        vecZ++;

        if (this.getY() < -64.0D) {
            outOfWorld();
            return;
        }

        if (age > gravityDelay) {
            mode = 0;
            if (!level.isClientSide
                    && tileEntityNBT == null
                    && (ConfigGrab.Storm_Tornado_rarityOfDisintegrate < 0
                    || random.nextInt((ConfigGrab.Storm_Tornado_rarityOfDisintegrate + 1) * 20) == 0)) {
                this.remove();
                return;
            }
        }

        if (mode == 1) {
            fallDistance = 0.0F;
            horizontalCollision = false;
        }

        if (!level.isClientSide) {
            BlockPos pos = this.blockPosition();

            if (tickCount % 20 == 0 && tileEntityNBT == null && !level.isLoaded(pos)) {
                this.remove();
                return;
            }

            if (!noCollision) {
                motion = this.getDeltaMovement();
                motionX = motion.x;
                motionY = motion.y;
                motionZ = motion.z;

                float speed = (float) Maths.speedSq(motionX, motionY, motionZ);
                float dampening;

                Vector3d start_point = new Vector3d(this.getX(), this.getY(), this.getZ());
                Vector3d end_point = new Vector3d(
                        this.getX() + motionX * 1.3D,
                        this.getY() + motionY * 1.3D,
                        this.getZ() + motionZ * 1.3D);

                net.minecraft.util.RegistryKey<World> dimKey = level.dimension();
                if (tickCount % 5 == 0 && EntityMovingBlock.loadedEntities.containsKey(dimKey)) {
                    for (Entity entity : EntityMovingBlock.loadedEntities.get(dimKey)) {
                        AxisAlignedBB expandedBox = entity.getBoundingBox().inflate(this.getBbWidth());
                        Optional<Vector3d> hitPoint = expandedBox.clip(start_point, end_point);

                        if (hitPoint.isPresent()) {
                            if (ConfigGrab.grabbed_blocks_hurt) {
                                if (this.isOnFire() || Material.LAVA.equals(material))
                                    entity.setSecondsOnFire(15);

                                if (block == Blocks.CACTUS)
                                    try {
                                        entity.hurt(DamageSource.CACTUS, 1);
                                    } catch (Exception ignored) {
                                    }

                                try {
                                    float hardness = state.getDestroySpeed(level, pos);
                                    entity.hurt(
                                            WeatherDamageSource.FLYING_BLOCK,
                                            speed * (hardness >= 0.0F ? hardness * 3.0F : 10.0F));

                                    if (state != null)
                                        block.entityInside(state, level, pos, entity);
                                } catch (Exception ignored) {
                                }
                            }

                            if (entity.isPushable()) {
                                dampening = 1.0F / ((Math.max(entity.getBbHeight(), entity.getBbWidth())) * 0.25F + 1);
                                dampening = Math.min(dampening, 1.0F);

                                Vector3d entityMotion = entity.getDeltaMovement();
                                entity.setDeltaMovement(
                                        entityMotion.x + motionX,
                                        entityMotion.y + motionY,
                                        entityMotion.z + motionZ);

                                motionX *= dampening;
                                motionY *= dampening;
                                motionZ *= dampening;
                                this.setDeltaMovement(motionX, motionY, motionZ);
                                speed = (float) Maths.speedSq(motionX, motionY, motionZ);

                                SoundType sound = block.getSoundType(state, level, pos, null);
                                if (sound != null)
                                    level.playSound(null, this.getX(), this.getY(), this.getZ(),
                                            sound.getFallSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                                break;
                            }
                        }
                    }
                }

                if (mode == 0) {
                    end_point = new Vector3d(
                            this.getX() + motionX * 1.3D,
                            this.getY() + motionY * 1.3D,
                            this.getZ() + motionZ * 1.3D);

                    BlockRayTraceResult raytrace = level.clip(new RayTraceContext(
                            new Vector3d(this.getX(), this.getY(), this.getZ()),
                            end_point,
                            RayTraceContext.BlockMode.COLLIDER,
                            RayTraceContext.FluidMode.NONE,
                            this));

                    if (raytrace != null && RayTraceResult.Type.BLOCK.equals(raytrace.getType())) {

                        if (raytrace.isInside()) {
                            firstTick = false;
                            return;
                        }

                        BlockPos target_pos = raytrace.getBlockPos();
                        BlockState target = level.getBlockState(target_pos);
                        Block target_block = target.getBlock();

                        net.minecraftforge.common.ToolType tool_type = block.getHarvestTool(state);
                        float speed_penalty = (tool_type != null && target_block.isToolEffective(target, tool_type))
                                ? 1.0F : 0.3F;
                        float target_hardness = target.getDestroySpeed(level, target_pos);

                        if (target_hardness >= 0.0F && target_hardness < speed * speed_penalty) {
                            dampening = Math.min(1.0F / (target_hardness + 1.0F), 1.0F);
                            motionX *= dampening;
                            motionY *= dampening;
                            motionZ *= dampening;
                            this.setDeltaMovement(motionX, motionY, motionZ);
                            speed = (float) Maths.speedSq(motionX, motionY, motionZ);

                            if (speed < 0.01F)
                                blockify(target_pos.getX(), target_pos.getY(), target_pos.getZ());
                            else {
                                level.setBlock(target_pos, EntityMovingBlock.AIR, 2 | 16);
                                level.levelEvent(2001, target_pos, Block.getId(state));
                            }

                        } else if (Direction.DOWN.equals(raytrace.getDirection())) {
                            motionY = 0.0D;
                            this.setDeltaMovement(motionX, motionY, motionZ);
                            SoundType sound = block.getSoundType(state, level, pos, null);
                            if (sound != null)
                                level.playSound(null, this.getX(), this.getY(), this.getZ(),
                                        sound.getHitSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);

                        } else if (Direction.UP.equals(raytrace.getDirection())) {
                            BlockPos landPos = target_pos.above();
                            BlockState landState = level.getBlockState(landPos);

                            if (WeatherUtilBlock.isReplacable(landState, true) || tileEntityNBT != null) {
                                blockify(landPos.getX(), landPos.getY(), landPos.getZ());
                            }
                        }
                    }
                }
            }
        }

        firstTick = false;
    }

    public boolean canEntityBeSeen(Entity entity) {
        BlockRayTraceResult result = this.level.clip(new RayTraceContext(
                new Vector3d(this.getX(), this.getY() + this.getEyeHeight(), this.getZ()),
                new Vector3d(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ()),
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                this));
        return result == null || result.getType() == RayTraceResult.Type.MISS;
    }

    private void blockify(int x, int y, int z) {
        try {
            if (ConfigGrab.Storm_Tornado_rarityOfBreakOnFall < 0
                    || random.nextInt(ConfigGrab.Storm_Tornado_rarityOfBreakOnFall + 1) != 0) {
                if (ChunkUtils.isValidPos(level, y)) {
                    BlockPos pos = new BlockPos(x, y, z);
                    ChunkUtils.setBlockState(level, pos, state);
                    level.levelEvent(2001, pos, Block.getId(state));

                    if (tileEntityNBT != null) {
                        TileEntity tile = block.createTileEntity(state, level);
                        if (tile != null) {
                            tile.load(state, tileEntityNBT);
                            level.setBlockEntity(pos, tile);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.remove();
    }

    @Override
    public void remove() {
        super.remove();
        if (!level.isClientSide && storm != null)
            storm.flyingBlocks = Math.max(storm.flyingBlocks - 1, 0);
    }

    @Override
    public boolean hurt(@Nonnull DamageSource source, float amount) {
        this.remove();
        return false;
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundNBT nbt) {
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        nbt.putString("Tile", blockId != null ? blockId.toString() : "minecraft:air");
        nbt.put("BlockState", NBTUtil.writeBlockState(state));

        if (tileClass != null)
            nbt.putString("TileClass", tileClass.getName());
        if (tileEntityNBT != null)
            nbt.put("TileEntity", tileEntityNBT);
    }

    @Override
    public boolean save(@Nonnull CompoundNBT compound) {
        return tileEntityNBT == null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundNBT nbt) {
        block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("Tile")));

        if (nbt.contains("BlockState", 10))
            state = NBTUtil.readBlockState(nbt.getCompound("BlockState"));
        else
            state = block != null ? block.defaultBlockState() : AIR;

        material = state.getMaterial();

        if (nbt.contains("TileClass"))
            try {
                tileClass = (Class<? extends TileEntity>) Class.forName(nbt.getString("TileClass"));
            } catch (Exception e) {
                tileClass = null;
            }
        else
            tileClass = null;

        tileEntityNBT = nbt.contains("TileEntity") ? nbt.getCompound("TileEntity") : null;
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        ResourceLocation id = block != null ? ForgeRegistries.BLOCKS.getKey(block) : null;
        String tName = tileClass == null ? "" : tileClass.getName();
        buffer.writeUtf(id == null ? "" : id.toString());
        buffer.writeUtf(tName);
        buffer.writeNbt(NBTUtil.writeBlockState(state));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readSpawnData(PacketBuffer buffer) {
        String str = buffer.readUtf();
        String tileStr = buffer.readUtf();
        CompoundNBT stateNBT = buffer.readNbt();

        if (!str.isEmpty())
            block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(str));
        else
            block = EntityMovingBlock.AIR.getBlock();

        if (!tileStr.isEmpty())
            try {
                this.tileClass = (Class<? extends TileEntity>) Class.forName(tileStr);
            } catch (Exception e) {
                this.tileClass = null;
            }

        state = stateNBT != null ? NBTUtil.readBlockState(stateNBT) :
                (block != null ? block.defaultBlockState() : AIR);
        material = state.getMaterial();
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}