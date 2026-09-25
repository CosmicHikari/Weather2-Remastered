package net.mrbt0907.weather2.util;

import net.corosus.coroutillegacy.util.CoroUtilCompatibility;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Calendar;

public class WeatherUtil {

    public static boolean isPaused() {
        return Minecraft.getInstance().isPaused();
    }

    public static boolean isPausedSideSafe(World world) {
        return world.isClientSide && isPaused();
    }

    public static boolean isAprilFoolsDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        return calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

    public static float toFahrenheit(float temperature) {
        return temperature * 80.0F;
    }

    public static float toCelsius(float temperature) {
        return ((temperature * 80.0F) - 32.0F) * 5.0F / 9.0F;
    }

    public static float toMph(float windSpeed) {
        return windSpeed * 9.657718F;
    }

    public static float toKph(float windSpeed) {
        return windSpeed * 15.5412F;
    }

    public static float toMps(float windSpeed) {
        return windSpeed * 4.317F;
    }

    public static float getDewpoint(World world, BlockPos pos) {
        float a = toCelsius(getTemperature(world, pos)) - ((100.0F - (getHumidity(world, pos) * 100.0F)) * 0.2F);
        return a;
    }

    public static float getPressure(World world, BlockPos pos) {
        return 1031.0F - (51.0F * getTemperature(world, pos) * 0.5F);
    }

    public static float getTemperature(World world, BlockPos pos) {
        float temp = CoroUtilCompatibility.getAdjustedTemperature(world, world.getBiome(pos), pos);
        float time_bonus = 0.0F;
        float[] time_table = {0.18F, 0.16F, 0.14F, 0.12F, 0.09F, 0.07F, 0.05F, 0.02F, 0.0F, 0.01F, 0.015F, 0.2F, 0.3F, 0.5F, 0.75F, 0.9F, 0.12F, 0.14F, 0.16F, 0.175F, 0.2F, 0.215F, 0.2F, 0.19F};
        long time = world.getDayTime() % 24000L;

        time_bonus = (time_table[(int) (time * 0.0001)]);
        return temp - time_bonus;
    }

    public static float getHumidity(World world, BlockPos pos) {
        float a = getTemperature(world, pos);
        float b = CoroUtilCompatibility.getAdjustedTemperature(world, world.getBiome(pos), pos);
        return Math.max((a / b) - Maths.clamp(a - 1.0F, 0.0F, 1.0F), 0.0F);
    }
}