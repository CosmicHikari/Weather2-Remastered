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
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtilEntity;

public class TileWindVane extends TileEntity implements ITickableTileEntity {


    public float smoothAngle = 0;
    public float smoothSpeed = 0;

    public float smoothAngleRotationalVel = 0;
    public float smoothAngleRotationalVelAccel = 0;

    public float smoothAngleAdj = 0.1F;
    public float smoothSpeedAdj = 0.1F;

    public boolean isOutsideCached = false;

    public TileWindVane() {
        this(TileEntityRegistry.WIND_VANE_TILE.get());
    }

    public TileWindVane(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void tick() {
        if (level.isClientSide) {

            if (level.getGameTime() % 40 == 0) {
                isOutsideCached = WeatherUtilEntity.isPosOutside(level, new Vec3(worldPosition.getX() + 0.5F, worldPosition.getY() + 0.5F, worldPosition.getZ() + 0.5F));
            }

            if (isOutsideCached) {
                float targetAngle = WindReader.getWindAngle(level, new Vec3(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()));
                float windSpeed = WindReader.getWindSpeed(level, new Vec3(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()));

                if (smoothAngle > 180) smoothAngle -= 360;
                if (smoothAngle < -180) smoothAngle += 360;

                float bestMove = Maths.wrapDegrees(targetAngle - smoothAngle);


                smoothAngleAdj = windSpeed;

                if (Math.abs(bestMove) < 180) {
                    float realAdj = smoothAngleAdj;

                    if (realAdj * 2 > windSpeed) {
                        if (bestMove > 0) smoothAngleRotationalVelAccel -= realAdj;
                        if (bestMove < 0) smoothAngleRotationalVelAccel += realAdj;
                    }

                    if (smoothAngleRotationalVelAccel > 0.3 || smoothAngleRotationalVelAccel < -0.3) {
                        smoothAngle += smoothAngleRotationalVelAccel;
                    } else {

                    }


                    smoothAngleRotationalVelAccel *= 0.80F;


                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 3, worldPosition.getZ() + 1);
    }

    @Override
    public CompoundNBT save(CompoundNBT var1) {
        return super.save(var1);
    }

    @Override
    public void load(BlockState state, CompoundNBT var1) {
        super.load(state, var1);
    }
}