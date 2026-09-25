package net.mrbt0907.weather2.api.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.mrbt0907.weather2.util.ConfigList;

@Cancelable
public class EventRegisterGrabLists extends Event {
    public final ConfigList grabList;
    public final ConfigList replaceList;
    public final ConfigList entityList;
    public final ConfigList windResistanceList;


    public EventRegisterGrabLists(ConfigList grabList, ConfigList replaceList, ConfigList entityList, ConfigList windResistanceList) {
        this.grabList = grabList;
        this.replaceList = replaceList;
        this.entityList = entityList;
        this.windResistanceList = windResistanceList;
    }
}