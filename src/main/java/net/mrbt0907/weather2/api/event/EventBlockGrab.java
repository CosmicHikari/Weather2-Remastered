package net.mrbt0907.weather2.api.event;

import net.minecraft.block.BlockState;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class EventBlockGrab extends Event {
    private final BlockState state;


    public EventBlockGrab(BlockState state) {
        this.state = state;
    }

    public BlockState getBlockState() {
        return state;
    }
}