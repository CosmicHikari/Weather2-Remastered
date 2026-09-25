package net.mrbt0907.weather2.block.tile;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.mrbt0907.weather2.registry.TileEntityRegistry;

public class TileMachine extends TileEntity implements ITickableTileEntity {
    public TileMachine() {
        super(TileEntityRegistry.MACHINE_CASE_TILE.get());
    }

    public TileMachine(TileEntityType<?> type) {
        super(type);
    }

    @Override
    public void tick() {

    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }
}