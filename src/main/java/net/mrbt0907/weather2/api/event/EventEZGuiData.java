package net.mrbt0907.weather2.api.event;

import net.minecraftforge.eventbus.api.Event;

public class EventEZGuiData extends Event {
    public final String id;
    public final int oldValue;
    public final int newValue;


    public EventEZGuiData(String id, int oldValue, int newValue) {
        this.id = id;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}