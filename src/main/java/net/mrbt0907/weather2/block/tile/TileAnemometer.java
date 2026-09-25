package net.mrbt0907.weather2.block.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.api.WindReader;
import net.mrbt0907.weather2.registry.TileEntityRegistry;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtilEntity;

public class TileAnemometer extends TileEntity implements ITickableTileEntity {

    public float smoothAngle = 0;
    public float smoothAnglePrev = 0;
    public float smoothSpeed = 0;

    public float smoothAngleRotationalVel = 0;
    public float smoothAngleRotationalVelAccel = 0;

    public float smoothAngleAdj = 0.1F;
    public float smoothSpeedAdj = 0.1F;

    public boolean isOutsideCached = false;

    public TileAnemometer() {
        this(TileEntityRegistry.ANEMOMETER_TILE.get());
    }

    public TileAnemometer(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void tick() {
        if (level.isClientSide) {

            if (level.getGameTime() % 40 == 0)
                isOutsideCached = WeatherUtilEntity.isPosOutside(level, new Vec3(worldPosition.getX() + 0.5F, worldPosition.getY() + 0.5F, worldPosition.getZ() + 0.5F));

            if (isOutsideCached) {
                float windSpeed = WindReader.getWindSpeed(level, new Vec3(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()));

                smoothAngleRotationalVel += windSpeed * 0.35F;

                if (smoothAngleRotationalVel > 100F) smoothAngleRotationalVel = 100F;
                if (smoothAngle >= 180) smoothAngle -= 360;
                if (smoothAnglePrev >= 180) smoothAnglePrev -= 360;

            }

            smoothAnglePrev = smoothAngle;
            smoothAngle += smoothAngleRotationalVel;
            smoothAngleRotationalVel -= 0.1F;

            smoothAngleRotationalVel *= 0.97F;

            if (smoothAngleRotationalVel <= 0) smoothAngleRotationalVel = 0;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 3, worldPosition.getZ() + 1);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        return super.save(compound);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
    }
}