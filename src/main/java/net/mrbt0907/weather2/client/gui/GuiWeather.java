package net.mrbt0907.weather2.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mrbt0907.weather2.api.WindReader;
import net.mrbt0907.weather2.api.weather.AbstractWeatherRenderer;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.item.ItemRadar;
import net.mrbt0907.weather2.item.ItemSensor;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.util.WeatherUtilGui;

public class GuiWeather extends WeatherUtilGui {
    private final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {

        if (!ElementType.HOTBAR.equals(event.getType()) || mc.level == null) return;

        MatrixStack matrixStack = event.getMatrixStack();
        matrixStack.pushPose();


        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (mc.player != null) {
            WeatherManagerClient manager = ClientTickHandler.weatherManager;
            if (manager != null) {
                FontRenderer font = mc.font;
                ItemStack stack = mc.player.getItemInHand(Hand.MAIN_HAND);

                if (ConfigClient.enable_debug_renderer) {
                    for (int i = 0; i < AbstractWeatherRenderer.renderDebugInfo.size(); i++)
                        drawString(matrixStack, font,
                                AbstractWeatherRenderer.renderDebugInfo.get(i),
                                0, 2 + 10 * i, 0xFFFFFF00);
                }

                if (stack.getItem() instanceof ItemSensor)
                    renderSensorData(matrixStack, stack);
                else if (stack.getItem() instanceof ItemRadar)
                    renderRadar(matrixStack, stack);
            }
        }

        color();
        matrixStack.popPose();
    }

    private void renderSensorData(MatrixStack matrixStack, ItemStack stack) {
        ItemSensor item = (ItemSensor) stack.getItem();
        CompoundNBT nbt = stack.getTag();
        boolean enabled = nbt != null && nbt.getBoolean("enabled");

        if (!enabled) return;


        World world = mc.level;
        BlockPos bPos = new BlockPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        Maths.Vec3 pos = new Maths.Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());

        switch (item.getType()) {
            case 1:
                drawString(matrixStack, mc.font,
                        String.format("Temperature: %.2f\u00B0F,  %.02f\u00B0C",
                                WeatherUtil.toFahrenheit(WeatherUtil.getTemperature(world, bPos)),
                                WeatherUtil.toCelsius(WeatherUtil.getTemperature(world, bPos))),
                        0, 2, 0xFFFFFFFF);
                break;
            case 2:
                drawString(matrixStack, mc.font,
                        String.format("Humidity: %.02f%%",
                                WeatherUtil.getHumidity(world, bPos) * 100.0F),
                        0, 2, 0xFFFFFFFF);
                break;
            case 3:
                float windAngle = WindReader.getWindAngle(world, pos);
                float windSpeed = WindReader.getWindSpeed(world, pos);
                String direction = windAngle >= 315 ? "South"
                        : windAngle >= 225 ? "East"
                        : windAngle >= 135 ? "North"
                        : windAngle >= 45 ? "West" : "South";
                drawString(matrixStack, mc.font,
                        String.format("Wind Speed: %.2f Mph, %.2f Kph, %.2f M/s  (%.2f) (%s)",
                                WeatherUtil.toMph(windSpeed),
                                WeatherUtil.toKph(windSpeed),
                                WeatherUtil.toMps(windSpeed),
                                windAngle, direction),
                        0, 2, 0xFFFFFFFF);
                break;
        }
    }

    private void renderRadar(MatrixStack matrixStack, ItemStack stack) {

    }
}