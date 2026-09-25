package net.mrbt0907.weather2.api.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.Map;

@Cancelable
public class EventRegisterDimension extends Event {
    public final Map<Integer, Boolean> weatherList;
    public final Map<Integer, Boolean> effectList;


    public EventRegisterDimension(Map<Integer, Boolean> weatherList, Map<Integer, Boolean> effectList) {
        this.weatherList = weatherList;
        this.effectList = effectList;
    }
}