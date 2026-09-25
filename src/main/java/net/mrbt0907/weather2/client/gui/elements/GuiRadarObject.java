package net.mrbt0907.weather2.client.gui.elements;

import net.mrbt0907.weather2.api.weather.IWeatherDetectable;
import net.mrbt0907.weather2.api.weather.IWeatherStaged;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.storm.StormObject;

import java.util.UUID;

public class GuiRadarObject {
    public String name;
    public String typeName;
    public UUID uuid;
    public Vec3 pos;
    public int stage;
    public int type;
    public float size;
    public float angle;
    public float motion;
    public boolean isDying;
    public boolean isRaining;
    public boolean isHailing;

    public GuiRadarObject(IWeatherDetectable wo) {
        name = wo.getName();
        typeName = wo.getTypeName();
        uuid = wo.getUUID();
        pos = wo.getPos();
        isDying = wo.isDying();
        size = 0.0F;
        angle = wo.getAngle();
        motion = wo.getSpeed();

        if (wo instanceof IWeatherStaged) {
            stage = wo.getStage();
        }

        if (wo instanceof FrontObject)
            type = 0;
        else if (wo instanceof StormObject) {
            type = ((StormObject) wo).stormType == 0 ? 1 : 2;
            isRaining = ((StormObject) wo).hasDownfall();
            isHailing = ((StormObject) wo).isHailing();
            size = ((StormObject) wo).funnelSize;
        } else if (wo instanceof SandstormObject) {
            SandstormObject sso = ((SandstormObject) wo);
            isDying = !sso.isFrontGrowing;
            type = 3;
        }
    }
}