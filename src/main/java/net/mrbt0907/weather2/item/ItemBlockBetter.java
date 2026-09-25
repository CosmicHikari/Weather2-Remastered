package net.mrbt0907.weather2.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockBetter extends Item {
    public final Block block;

    public ItemBlockBetter(Block block, Item.Properties properties) {
        super(properties);
        this.block = block;
    }

    private static <T extends Comparable<T>> BlockState updateState(BlockState state, Property<T> property, String valueString) {
        return property.getValue(valueString).map((value) -> {
            return state.setValue(property, value);
        }).orElse(state);
    }

    public static boolean setTileEntityNBT(World world, @Nullable PlayerEntity player, BlockPos pos, ItemStack stack) {
        MinecraftServer minecraftserver = world.getServer();

        if (minecraftserver == null) {
            return false;
        } else {
            CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");

            if (compoundnbt != null) {
                TileEntity tileentity = world.getBlockEntity(pos);

                if (tileentity != null) {
                    if (!world.isClientSide && tileentity.onlyOpCanSetNbt() && (player == null || !player.canUseGameMasterBlocks())) {
                        return false;
                    }

                    CompoundNBT compoundnbt1 = tileentity.save(new CompoundNBT());
                    CompoundNBT compoundnbt2 = compoundnbt1.copy();
                    compoundnbt1.merge(compoundnbt);
                    compoundnbt1.putInt("x", pos.getX());
                    compoundnbt1.putInt("y", pos.getY());
                    compoundnbt1.putInt("z", pos.getZ());

                    if (!compoundnbt1.equals(compoundnbt2)) {
                        tileentity.load(tileentity.getBlockState(), compoundnbt1);
                        tileentity.setChanged();
                        return true;
                    }
                }
            }

            return false;
        }
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        return this.tryPlace(new BlockItemUseContext(context));
    }

    public ActionResultType tryPlace(BlockItemUseContext context) {
        if (!context.canPlace()) {
            return ActionResultType.FAIL;
        } else {
            BlockItemUseContext blockitemusecontext = this.getBlockItemUseContext(context);
            if (blockitemusecontext == null) {
                return ActionResultType.FAIL;
            } else {
                BlockState blockstate = this.getStateForPlacement(blockitemusecontext);
                if (blockstate == null) {
                    return ActionResultType.FAIL;
                } else if (!this.placeBlock(blockitemusecontext, blockstate)) {
                    return ActionResultType.FAIL;
                } else {
                    BlockPos blockpos = blockitemusecontext.getClickedPos();
                    World world = blockitemusecontext.getLevel();
                    PlayerEntity playerentity = blockitemusecontext.getPlayer();
                    ItemStack itemstack = blockitemusecontext.getItemInHand();
                    BlockState blockstate1 = world.getBlockState(blockpos);
                    Block block = blockstate1.getBlock();

                    if (block == this.block) {
                        blockstate1 = this.updateBlockStateFromTag(blockpos, world, itemstack, blockstate1);
                        setTileEntityNBT(world, playerentity, blockpos, itemstack);
                        this.block.setPlacedBy(world, blockpos, blockstate1, playerentity, itemstack);
                    }

                    SoundType soundtype = blockstate1.getSoundType(world, blockpos, playerentity);
                    world.playSound(playerentity, blockpos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    itemstack.shrink(1);
                    return ActionResultType.sidedSuccess(world.isClientSide);
                }
            }
        }
    }

    protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
        return context.getLevel().setBlock(context.getClickedPos(), state, 11);
    }

    @Nullable
    protected BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = this.block.getStateForPlacement(context);
        return blockstate != null && this.canPlace(context, blockstate) ? blockstate : null;
    }

    @Nullable
    protected BlockItemUseContext getBlockItemUseContext(BlockItemUseContext context) {
        return context;
    }

    protected boolean canPlace(BlockItemUseContext context, BlockState state) {
        PlayerEntity playerentity = context.getPlayer();
        return (playerentity == null || playerentity.mayBuild()) && state.canSurvive(context.getLevel(), context.getClickedPos());
    }

    protected BlockState updateBlockStateFromTag(BlockPos pos, World world, ItemStack stack, BlockState state) {
        BlockState blockstate = state;
        CompoundNBT compoundnbt = stack.getTag();

        if (compoundnbt != null) {
            CompoundNBT compoundnbt1 = compoundnbt.getCompound("BlockStateTag");

            for (String s : compoundnbt1.getAllKeys()) {
                Property<?> property = blockstate.getBlock().getStateDefinition().getProperty(s);

                if (property != null) {
                    String s1 = compoundnbt1.get(s).getAsString();
                    blockstate = updateState(blockstate, property, s1);
                }
            }
        }

        if (blockstate != state) {
            world.setBlock(pos, blockstate, 2);
        }

        return blockstate;
    }

    public Block getBlock() {
        return this.block;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        this.block.appendHoverText(stack, world, tooltip, flag);
    }
}