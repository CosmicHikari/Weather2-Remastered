package net.mrbt0907.weather2.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
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
import net.mrbt0907.weather2.block.tile.TileWeatherDeflector;
import net.mrbt0907.weather2.registry.TileEntityRegistry;

public class BlockWeatherDeflector extends Block {
    public BlockWeatherDeflector() {
        super(Block.Properties.of(Material.CLAY).strength(0.6F, 10.0F));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileWeatherDeflector(TileEntityRegistry.WEATHER_DEFLECTOR_TILE.get());
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }


    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {

        if (!worldIn.isClientSide && hand == Hand.MAIN_HAND) {
            TileEntity tEnt = worldIn.getBlockEntity(pos);

            if (tEnt instanceof TileWeatherDeflector) {
                ((TileWeatherDeflector) tEnt).rightClicked(worldIn, pos, state, player, hand, hit.getDirection(),
                        (float) hit.getLocation().x, (float) hit.getLocation().y, (float) hit.getLocation().z);
            }
        }

        return ActionResultType.SUCCESS;
    }
}