package net.mrbt0907.weather2.util;

import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChunkUtils {
    public static BlockState getBlockState(World world, int x, int y, int z) {
        return ChunkUtils.getBlockState(world, new BlockPos(x, y, z));
    }

    public static BlockState getBlockState(World world, BlockPos pos) {
        return world.getBlockState(pos);
    }

    public static void setBlockState(World world, int x, int y, int z, BlockState newState) {
        ChunkUtils.setBlockState(world, new BlockPos(x, y, z), newState);
    }

    public static void setBlockState(World world, BlockPos pos, BlockState newState) {
        if (newState.getBlock() instanceof AirBlock) {
            BlockState state = ChunkUtils.getBlockState(world, pos.above());
            if (state.getMaterial().isLiquid() || !WeatherUtilBlock.isReplacable(state, false) || newState.hasTileEntity()) {
                world.setBlock(pos, newState, 3);
                return;
            }
        }

        world.setBlock(pos, newState, 2 | 16);
    }

    public static boolean isValidPos(World world, int y) {
        return world != null && y >= 0 && y < world.getHeight();
    }
}