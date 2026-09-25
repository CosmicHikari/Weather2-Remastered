package net.mrbt0907.weather2.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.mrbt0907.weather2.block.tile.TileAnemometer;
import net.mrbt0907.weather2.registry.TileEntityRegistry;

import java.util.Random;

@SuppressWarnings("unused")
public class BlockAnemometer extends Block {
    public static final VoxelShape SHAPE = VoxelShapes.box(0.4, 0, 0.4, 0.6, 0.3, 0.6);

    public BlockAnemometer(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isRandomlyTicking(BlockState state) {
        return false;
    }

    @Override
    public void randomTick(BlockState state, net.minecraft.world.server.ServerWorld worldIn, BlockPos pos, Random random) {
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileAnemometer(TileEntityRegistry.ANEMOMETER_TILE.get());
    }
}