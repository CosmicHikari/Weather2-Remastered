package net.corosus.coroutillegacy.block;

import net.corosus.coroutillegacy.item.ItemRepairingGel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockRepairingBlock extends Block {
    public static final VoxelShape SHAPE = VoxelShapes.block();
    public static final VoxelShape NO_COLLIDE_SHAPE = VoxelShapes.empty();

    public BlockRepairingBlock(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null && (player.getItemInHand(Hand.MAIN_HAND).getItem() instanceof ItemRepairingGel)) {
            return SHAPE;
        } else {
            return NO_COLLIDE_SHAPE;
        }
    }
}