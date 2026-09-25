package net.mrbt0907.weather2.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.mrbt0907.weather2.weather.storm.StormObject;

import javax.annotation.Nonnull;

public class BlockReplaceSnapshot extends BlockSnapshot {
    public final StormObject storm;
    public final BlockState newState;

    public BlockReplaceSnapshot(@Nonnull StormObject storm, BlockState state, @Nonnull BlockState oldState, @Nonnull BlockPos pos) {
        super(oldState, pos);
        this.storm = storm;
        newState = state;
    }

    @Override
    public String toString() {
        return "BlockReplaceSnapshot{block=" + block.getRegistryName() + ", replacement=" + (state == null ? "grab" : state.getBlock().getRegistryName()) + ", x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}