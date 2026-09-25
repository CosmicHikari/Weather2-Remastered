package net.mrbt0907.weather2.block;

import net.corosus.coroutillegacy.util.CoroUtilMisc;
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
import net.mrbt0907.weather2.block.tile.TileWeatherConstructor;
import net.mrbt0907.weather2.registry.TileEntityRegistry;

public class BlockWeatherConstructor extends Block {
    public BlockWeatherConstructor() {
        super(Block.Properties.of(Material.CLAY).strength(0.6F, 10.0F));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileWeatherConstructor(TileEntityRegistry.WEATHER_MACHINE_TILE.get());
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {

        if (!world.isClientSide && hand == Hand.MAIN_HAND) {
            TileEntity tile = world.getBlockEntity(pos);

            if (tile instanceof TileWeatherConstructor) {
                TileWeatherConstructor constructor = (TileWeatherConstructor) tile;
                constructor.cycleWeatherType(player.isCrouching());
                String msg = "Off";

                switch (constructor.stage) {
                    case 1:
                        msg = "Cloud";
                        break;
                    case 2:
                        msg = "Rainstorm";
                        break;
                    case 3:
                        msg = "Thunderstorm";
                        break;
                    case 4:
                        msg = "Supercell";
                        break;
                    case 5:
                        msg = "Hailing Supercell";
                        break;
                    case 6:
                        msg = "EF1 Tornado";
                        break;
                    case 7:
                        msg = "Category 1 Hurricane";
                        break;
                }

                CoroUtilMisc.sendCommandSenderMsg(player, "Weather Machine is now spawning a " + msg);
                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void attack(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        super.attack(state, worldIn, pos, player);
    }


    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }
}