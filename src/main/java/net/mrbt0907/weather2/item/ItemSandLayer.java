package net.mrbt0907.weather2.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.mrbt0907.weather2.block.BlockSandLayer;
import net.mrbt0907.weather2.registry.BlockRegistry;

import javax.annotation.Nullable;

public class ItemSandLayer extends ItemBlockBetter {
    public ItemSandLayer(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Direction facing = context.getClickedFace();

        if (player != null && !stack.isEmpty() && player.mayUseItemAt(pos, facing, stack)) {
            BlockPos blockpos = pos;

            if ((facing != Direction.UP || block != this.block) && !state.canBeReplaced(new BlockItemUseContext(context))) {
                blockpos = pos.relative(facing);
                state = world.getBlockState(blockpos);
                block = state.getBlock();
            }

            if (block == this.block) {
                int layers = state.getValue(BlockSandLayer.LAYERS);

                if (layers < 8) {
                    BlockState newState = state.setValue(BlockSandLayer.LAYERS, layers + 1);
                    VoxelShape voxelshape = newState.getCollisionShape(world, blockpos);

                    if (!voxelshape.isEmpty() && world.isUnobstructed(newState, blockpos, ISelectionContext.of(player)) &&
                            world.setBlock(blockpos, newState, 10)) {
                        SoundType soundtype = this.block.getSoundType(newState, world, blockpos, player);
                        world.playSound(player, blockpos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                        if (!player.abilities.instabuild) {
                            stack.shrink(1);
                        }
                        return ActionResultType.sidedSuccess(world.isClientSide);
                    }
                }
            }

            BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(
                    context.getClickLocation(),
                    facing,
                    blockpos,
                    false
            );
            return super.useOn(new ItemUseContext(player, context.getHand(), rayTraceResult));
        } else {
            return ActionResultType.FAIL;
        }
    }

    @Nullable
    @Override
    protected BlockItemUseContext getBlockItemUseContext(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() == BlockRegistry.sand_layer.get() && state.getValue(BlockSandLayer.LAYERS) < 8) {
            return context;
        }

        return super.getBlockItemUseContext(context);
    }

    @Override
    protected boolean canPlace(BlockItemUseContext context, BlockState state) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState existingState = world.getBlockState(pos);

        if (existingState.getBlock() == BlockRegistry.sand_layer.get() && existingState.getValue(BlockSandLayer.LAYERS) < 8) {
            return true;
        }

        return super.canPlace(context, state);
    }
}