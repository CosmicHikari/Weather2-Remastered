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
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.weather.WeatherManagerServer;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.util.List;
import java.util.Random;

public class BlockNewSensor extends BlockMachine {
    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);


    private final int scanType;

    public BlockNewSensor(Material material, int scanType) {
        super(material);

        this.scanType = scanType;
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {

        updateSensor(worldIn, pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        updateSensor(worldIn, pos, state);
        worldIn.getBlockTicks().scheduleTick(pos, this, 100);
    }

    private void updateSensor(ServerWorld world, BlockPos pos, BlockState state) {
        int power = 0;
        WeatherManagerServer manager = ServerTickHandler.dimensionSystems.get(world.dimension().location());

        if (manager != null) {
            WeatherObject so = manager.getWorstWeather(new Maths.Vec3(pos.getX(), pos.getY(), pos.getZ()), ConfigMisc.sensor_scan_range, Stage.RAIN.getStage(), Integer.MAX_VALUE);

            switch (scanType) {
                case 0:
                    if (so != null) {
                        if (so instanceof StormObject)
                            power = Maths.clamp(((StormObject) so).stage, 0, 15);
                        else if (so instanceof SandstormObject)
                            power = 5;


                    }
                    break;
                case 1:
                    power = (int) (WeatherUtil.getHumidity(world, pos) * 15.0F);
                    break;
                case 2:
                    List<WeatherObject> wos = manager.getWeatherObjects();
                    power = 0;

                    for (WeatherObject wo : wos) {
                        if (wo instanceof StormObject && ((StormObject) wo).hasDownfall() && wo.pos.distanceSq(new Maths.Vec3(pos)) < wo.size) {
                            power = 15;
                            break;
                        }
                    }
                    break;
                case 3:
                    power = (int) Maths.clamp(WeatherUtil.getTemperature(world, pos) * 15.0F * 0.7F, 0.0F, 15.0F);
                    break;
                case 4:
                    power = (int) Math.min(((manager.windManager.windSpeed > manager.windManager.windSpeedGust ? manager.windManager.windSpeed : manager.windManager.windSpeedGust) * 0.072F) * 15.0F, 15.0F);
                    break;
                case 5:
                    power = (int) Maths.clamp((WeatherUtil.getPressure(world, pos) - 900.0F) * 0.12F, 0.0F, 15.0F);
                    break;
            }
            world.setBlock(pos, state.setValue(POWER, Maths.clamp(power, 0, 15)), 3);
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