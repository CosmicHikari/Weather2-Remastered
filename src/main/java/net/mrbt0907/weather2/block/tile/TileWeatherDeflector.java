package net.mrbt0907.weather2.block.tile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.event.ServerTickHandler;
import net.mrbt0907.weather2.network.packets.PacketWeatherObject;
import net.mrbt0907.weather2.registry.TileEntityRegistry;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WeatherManagerServer;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.util.ArrayList;
import java.util.List;

public class TileWeatherDeflector extends TileEntity implements ITickableTileEntity {


    public static final int MODE_KILLSTORMS = 0;
    public static final int MODE_NOBLOCKDAMAGE = 1;
    public int mode = 0;

    public TileWeatherDeflector() {
        this(TileEntityRegistry.WEATHER_DEFLECTOR_TILE.get());
    }

    public TileWeatherDeflector(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        maintainBlockDamageDeflect();
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            if (mode == MODE_KILLSTORMS && level.getGameTime() % 100L == 0L) {
                WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(level.dimension().location());
                if (wm != null) {
                    List<WeatherObject> storms = new ArrayList<WeatherObject>(wm.getWeatherSystems(new Vec3(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()), ConfigStorm.storm_deflector_range, ConfigStorm.storm_deflector_minimum_stage, Integer.MAX_VALUE).keySet());
                    WeatherObject wo;
                    int size = storms.size();

                    for (int i = 0; i < size; i++) {
                        wo = storms.get(i);
                        wo.front.removeWeatherObject(wo.getUUID());
                        PacketWeatherObject.remove(wm.getDimension(), wo);
                    }
                    storms.clear();
                }
            }

            if (level.getGameTime() % 20 == 0)
                maintainBlockDamageDeflect();
        }
    }

    public void maintainBlockDamageDeflect() {
        WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(level.dimension().location());

        if (wm != null) {
            long pos = worldPosition.asLong();
            switch (mode) {
                case 1:
                    wm.getListWeatherBlockDamageDeflector().remove(pos);
                    break;
                default:
                    wm.getListWeatherBlockDamageDeflector().add(pos);
            }
        }
    }

    public void rightClicked(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        cycleMode();

        switch (mode) {
            case MODE_NOBLOCKDAMAGE:
                playerIn.sendMessage(new StringTextComponent("Deflection Mode: Protect Blocks"), playerIn.getUUID());
                maintainBlockDamageDeflect();
                break;
            default:
                playerIn.sendMessage(new StringTextComponent("Deflection Mode: Destroy Storms"), playerIn.getUUID());
        }
    }

    public void cycleMode() {
        mode = (mode + 1) % 2;
    }

    @Override
    public CompoundNBT save(CompoundNBT var1) {
        var1.putInt("mode", mode);
        return super.save(var1);
    }

    @Override
    public void load(BlockState state, CompoundNBT var1) {
        super.load(state, var1);
        mode = var1.getInt("mode");
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (!level.isClientSide) {

            WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(level.dimension().location());
            if (wm != null)
                wm.getListWeatherBlockDamageDeflector().remove(worldPosition.asLong());
        }
    }
}