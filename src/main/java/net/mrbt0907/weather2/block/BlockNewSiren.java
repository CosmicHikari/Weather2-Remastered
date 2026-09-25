package net.mrbt0907.weather2.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.mrbt0907.weather2.block.tile.TileSiren;
import net.mrbt0907.weather2.registry.TileEntityRegistry;

public class BlockNewSiren extends BlockMachine {

    public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");

    public BlockNewSiren() {
        this(Material.CLAY);
        this.registerDefaultState(this.stateDefinition.any().setValue(ENABLED, Boolean.valueOf(true)));
    }

    public BlockNewSiren(Material mat) {
        super(mat, 0.6F, 10.0F);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileSiren(TileEntityRegistry.TORNADO_SIREN_TILE.get());
    }


    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(ENABLED, Boolean.valueOf(true));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isClientSide) {
            this.updateState(worldIn, pos, state);
        }
    }

    public void updateState(World world, BlockPos pos, BlockState state) {
        boolean flag = !world.hasNeighborSignal(pos);

        if (flag != state.getValue(ENABLED).booleanValue()) {
            world.setBlock(pos, state.setValue(ENABLED, Boolean.valueOf(flag)), 3);

            if (world instanceof ServerWorld) {
                ((ServerWorld) world).getBlockTicks().scheduleTick(pos, this, 100);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            this.updateState(worldIn, pos, state);
        }
    }
}