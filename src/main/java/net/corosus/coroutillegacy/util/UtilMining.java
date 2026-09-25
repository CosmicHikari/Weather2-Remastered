package net.corosus.coroutillegacy.util;

import net.corosus.coroutillegacy.forge.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class UtilMining {

    public static List<BlockState> listBlocksBlacklistedRepairing = new ArrayList<>();

    public static boolean canConvertToRepairingBlockNew(World world, BlockPos pos, boolean isClient) {
        boolean isTileEntity = world.getBlockEntity(pos) != null;

        if (!isTileEntity) {
            return !isBlockBlacklistedFromRepairingBlockNonTileEntity(world, pos, isClient);
        }

        return false;
    }

    public static boolean isBlockBlacklistedFromRepairingBlockNonTileEntity(World world, BlockPos pos, boolean client) {
        BlockState state = world.getBlockState(pos);
        return CoroUtilBlockState.partialStateInListMatchesFullState(
                state,
                client ? ClientData.listBlocksBlacklistedRepairing : listBlocksBlacklistedRepairing
        );
    }

    @Deprecated
    public static boolean canConvertToRepairingBlock(World world, BlockState state) {
        if (state.getMaterial() == Material.GLASS) {
            return true;
        }

        return state.isCollisionShapeFullBlock(world, BlockPos.ZERO);
    }

    @Deprecated
    public static boolean canMineBlock(World world, BlockCoord pos, Block block) {
        return canMineBlock(world, pos.toBlockPos(), block);
    }

    @Deprecated
    public static boolean canMineBlock(World world, BlockPos pos, Block block) {
        return canMineBlock(world, pos);
    }

    public static boolean canMineBlock(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block.isAir(state, world, pos) || block == CommonProxy.blockRepairingBlock.get()) {
            return false;
        }

        if (world.getBlockEntity(pos) != null) {
            return false;
        }

        return !state.getMaterial().isLiquid();
    }

    public static class ClientData {
        public static List<BlockState> listBlocksBlacklistedRepairing = new ArrayList<>();
    }
}