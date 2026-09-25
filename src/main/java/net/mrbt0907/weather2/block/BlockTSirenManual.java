package net.mrbt0907.weather2.block;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.mrbt0907.weather2.block.tile.TileEntityTSirenManual;
import net.mrbt0907.weather2.registry.TileEntityRegistry;

public class BlockTSirenManual extends BlockSiren {

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntityTSirenManual(TileEntityRegistry.TORNADO_SIREN_MANUAL_TILE.get());
    }

    @Override
    public void updateState(World worldIn, BlockPos pos, BlockState state) {
        boolean flag = worldIn.hasNeighborSignal(pos);

        if (flag != state.getValue(ENABLED).booleanValue()) {
            worldIn.setBlock(pos, state.setValue(ENABLED, Boolean.valueOf(flag)), 3);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(ENABLED, Boolean.valueOf(false));
    }
}