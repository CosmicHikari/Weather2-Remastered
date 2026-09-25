package net.mrbt0907.weather2.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.event.ServerTickHandler;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WeatherManagerServer;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.util.Random;

public class BlockSensor extends Block {

    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);

    public BlockSensor() {
        super(Block.Properties.of(Material.CLAY).strength(0.6F, 10.0F));
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {

        updateSensor(worldIn, pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        updateSensor(world, pos, state);
        world.getBlockTicks().scheduleTick(pos, this, 100);
    }

    private void updateSensor(ServerWorld world, BlockPos pos, BlockState state) {
        WeatherManagerServer wms = ServerTickHandler.dimensionSystems.get(world.dimension().location());

        if (wms != null) {
            WeatherObject wo = wms.getWorstWeather(new Vec3(pos.getX(), pos.getY(), pos.getZ()), ConfigMisc.sensor_scan_range, Stage.TORNADO.getStage(), Integer.MAX_VALUE);
            if (wo != null)
                world.setBlock(pos, state.setValue(POWER, 15), 3);
            else
                world.setBlock(pos, state.setValue(POWER, 0), 3);
        }
        world.getBlockTicks().scheduleTick(pos, this, 100);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        context.getLevel().getBlockTicks().scheduleTick(context.getClickedPos(), this, 10);
        return this.defaultBlockState().setValue(POWER, Integer.valueOf(0));
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWER).intValue();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }
}