package net.corosus.coroutillegacy.item;

import net.corosus.coroutillegacy.block.TileEntityRepairingBlock;
import net.corosus.coroutillegacy.util.UtilMining;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemRepairingGel extends Item {
    public ItemRepairingGel(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World worldIn = context.getLevel();
        BlockPos pos = context.getClickedPos();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        Direction facing = context.getClickedFace();
        ItemStack itemstack = context.getItemInHand();

        if (player == null || !player.mayUseItemAt(pos.relative(facing), facing, itemstack)) {
            return ActionResultType.FAIL;
        } else {
            if (!worldIn.isClientSide) {
                if (player.isCreative() && player.isCrouching()) {
                    BlockState state = worldIn.getBlockState(pos);
                    if (UtilMining.canMineBlock(worldIn, pos, state.getBlock())) {
                        TileEntityRepairingBlock.replaceBlockAndBackup(worldIn, pos);
                        return ActionResultType.SUCCESS;
                    }
                } else {
                    TileEntity tEnt = worldIn.getBlockEntity(pos);
                    if (tEnt instanceof TileEntityRepairingBlock) {
                        ((TileEntityRepairingBlock) tEnt).restoreBlock();

                        if (!player.abilities.instabuild) {
                            itemstack.shrink(1);
                        }

                        return ActionResultType.SUCCESS;
                    } else {
                        return ActionResultType.PASS;
                    }
                }
            }
        }

        return ActionResultType.PASS;
    }
}