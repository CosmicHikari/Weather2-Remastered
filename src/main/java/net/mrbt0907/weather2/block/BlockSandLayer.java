package net.mrbt0907.weather2.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.registry.ItemRegistry;

import javax.annotation.Nullable;
import java.util.List;

public class BlockSandLayer extends Block {
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 8);
    protected static final VoxelShape[] SAND_SHAPE = new VoxelShape[]{
            VoxelShapes.empty(),
            VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D),
            VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D),
            VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D),
            VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D),
            VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D),
            VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D),
            VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D),
            VoxelShapes.block()};

    public BlockSandLayer() {
        super(Block.Properties.of(Material.SAND).sound(SoundType.SAND));
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(8)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SAND_SHAPE[state.getValue(LAYERS).intValue()];
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SAND_SHAPE[state.getValue(LAYERS).intValue()];
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getBlockSupportShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return SAND_SHAPE[state.getValue(LAYERS).intValue()];
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getVisualShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        return SAND_SHAPE[state.getValue(LAYERS).intValue()];
    }


    @Override
    @SuppressWarnings("deprecation")
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return state.getValue(LAYERS).intValue() < 8;
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState iblockstate = worldIn.getBlockState(pos.below());
        Block block = iblockstate.getBlock();

        if (block == this && iblockstate.getValue(LAYERS).intValue() >= 7)
            return true;


        return Block.isFaceFull(iblockstate.getCollisionShape(worldIn, pos.below()), Direction.UP);
    }


    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isClientSide) {
            if (!state.canSurvive(worldIn, pos)) {
                worldIn.destroyBlock(pos, true);
            }
        }
    }

    @Override
    public void playerDestroy(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.playerDestroy(worldIn, player, pos, state, te, stack);
        worldIn.removeBlock(pos, false);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> drops = new java.util.ArrayList<>();
        int layers = state.getValue(LAYERS);

        if (layers >= 8) {
            drops.add(new ItemStack(Blocks.SAND));
        } else {
            for (int i = 0; i < layers; i++) {
                drops.add(new ItemStack(ItemRegistry.itemSandLayer.get()));
            }
        }

        return drops;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (adjacentBlockState.getBlock() == this) {
            return adjacentBlockState.getValue(LAYERS).intValue() >= state.getValue(LAYERS).intValue();
        }
        return super.skipRendering(state, adjacentBlockState, side);
    }


    @Override
    public boolean canBeReplaced(BlockState state, net.minecraft.item.BlockItemUseContext useContext) {
        int layers = state.getValue(LAYERS);
        if (useContext.getItemInHand().getItem() == this.asItem() && layers < 8) {
            if (useContext.replacingClickedOnBlock()) {
                return useContext.getClickedFace() == Direction.UP;
            } else {
                return true;
            }
        }
        return layers == 1;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(net.minecraft.item.BlockItemUseContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.getBlock() == this) {
            int i = blockstate.getValue(LAYERS);
            return blockstate.setValue(LAYERS, Math.min(8, i + 1));
        } else {

            return this.defaultBlockState().setValue(LAYERS, 1);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
}