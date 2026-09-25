package net.corosus.coroutillegacy.block;

import net.corosus.coroutillegacy.config.ConfigCoroUtilLegacy;
import net.corosus.coroutillegacy.forge.CULog;
import net.corosus.coroutillegacy.forge.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class TileEntityRepairingBlock extends TileEntity implements ITickableTileEntity {
    private BlockState orig_blockState;
    private float orig_hardness = 1;
    private float orig_explosionResistance = 1;

    private long timeToRepairAt = 0;

    public TileEntityRepairingBlock(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public static TileEntityRepairingBlock replaceBlockAndBackup(World world, BlockPos pos) {
        return replaceBlockAndBackup(world, pos, ConfigCoroUtilLegacy.ticksToRepairBlock);
    }

    public static TileEntityRepairingBlock replaceBlockAndBackup(World world, BlockPos pos, int ticksToRepair) {
        BlockState oldState = world.getBlockState(pos);
        float oldHardness = oldState.getDestroySpeed(world, pos);
        float oldExplosionResistance = 1;

        try {
            oldExplosionResistance = oldState.getBlock().getExplosionResistance(oldState, world, pos, null);
        } catch (Exception ex) {
        }

        world.setBlockAndUpdate(pos, CommonProxy.blockRepairingBlock.get().defaultBlockState());
        TileEntity tEnt = world.getBlockEntity(pos);

        if (tEnt instanceof TileEntityRepairingBlock) {
            TileEntityRepairingBlock repairing = (TileEntityRepairingBlock) tEnt;
            repairing.setBlockData(oldState);
            repairing.setOrig_hardness(oldHardness);
            repairing.setOrig_explosionResistance(oldExplosionResistance);
            repairing.timeToRepairAt = world.getGameTime() + ticksToRepair;

            return repairing;
        } else {
            CULog.dbg("failed to set repairing block for pos: " + pos);
            return null;
        }
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            updateScheduledTick();
        }
    }

    public void updateScheduledTick() {
        if (!level.isClientSide) {
            if (orig_blockState == null || orig_blockState == this.getBlockState().getBlock().defaultBlockState()) {
                CULog.dbg("invalid state for repairing block, removing, orig_blockState: " + orig_blockState + " vs " + this.getBlockState().getBlock().defaultBlockState());
                level.setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
            } else {
                if (level.getGameTime() > timeToRepairAt) {
                    VoxelShape shape = this.getBlockState().getShape(level, worldPosition);
                    AxisAlignedBB aabb = shape.bounds().move(worldPosition);

                    List<LivingEntity> listTest = level.getEntitiesOfClass(LivingEntity.class, aabb);

                    if (listTest.size() == 0) {
                        restoreBlock();
                    }
                }
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public void restoreBlock() {
        level.setBlockAndUpdate(this.worldPosition, orig_blockState);

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos posFix = worldPosition.offset(x, y, z);
                    BlockState state = level.getBlockState(posFix);
                    if (state.getBlock() instanceof LeavesBlock) {
                        try {
                            level.setBlock(posFix, state.setValue(LeavesBlock.PERSISTENT, true), 4);
                        } catch (Exception ex) {
                            if (ConfigCoroUtilLegacy.useLoggingDebug) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public void setBlockData(BlockState state) {
        this.orig_blockState = state;
    }

    public BlockState getOrig_blockState() {
        return orig_blockState;
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        if (orig_blockState != null) {
            ResourceLocation blockName = orig_blockState.getBlock().getRegistryName();
            if (blockName != null) {
                compound.putString("orig_blockName", blockName.toString());
                compound.putInt("orig_blockMeta", Block.getId(orig_blockState));
            }
        }
        compound.putLong("timeToRepairAt", timeToRepairAt);
        compound.putFloat("orig_hardness", orig_hardness);
        compound.putFloat("orig_explosionResistance", orig_explosionResistance);

        return super.save(compound);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);

        timeToRepairAt = compound.getLong("timeToRepairAt");

        try {
            String blockNameStr = compound.getString("orig_blockName");
            if (!blockNameStr.isEmpty()) {
                ResourceLocation blockName = new ResourceLocation(blockNameStr);
                Block block = ForgeRegistries.BLOCKS.getValue(blockName);
                if (block != null) {
                    int meta = compound.getInt("orig_blockMeta");
                    this.orig_blockState = Block.stateById(meta);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            this.orig_blockState = Blocks.AIR.defaultBlockState();
        }

        orig_hardness = compound.getFloat("orig_hardness");
        orig_explosionResistance = compound.getFloat("orig_explosionResistance");
    }

    public float getOrig_hardness() {
        return orig_hardness;
    }

    public void setOrig_hardness(float orig_hardness) {
        this.orig_hardness = orig_hardness;
    }

    public float getOrig_explosionResistance() {
        return orig_explosionResistance;
    }

    public void setOrig_explosionResistance(float orig_explosionResistance) {
        this.orig_explosionResistance = orig_explosionResistance;
    }
}