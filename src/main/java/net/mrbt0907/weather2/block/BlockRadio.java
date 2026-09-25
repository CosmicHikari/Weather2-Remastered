package net.mrbt0907.weather2.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.mrbt0907.weather2.api.interfaces.ITileInteractable;
import net.mrbt0907.weather2.block.tile.TileRadioTransmitter;
import net.mrbt0907.weather2.registry.TileEntityRegistry;

public class BlockRadio extends Block {
    public BlockRadio(Material materialIn) {
        super(Block.Properties.of(materialIn));
    }

    public BlockRadio(Block.Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileRadioTransmitter(TileEntityRegistry.RADIO_TRANSMITTER_TILE.get());
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
        if (world.isClientSide && hand == Hand.MAIN_HAND) {
            TileEntity tEnt = world.getBlockEntity(pos);

            if (tEnt instanceof ITileInteractable)
                ((ITileInteractable) tEnt).onTileActivated(world, pos, state, playerIn, hand, hit.getDirection(),
                        (float) hit.getLocation().x, (float) hit.getLocation().y, (float) hit.getLocation().z);
        }

        return ActionResultType.SUCCESS;
    }
}