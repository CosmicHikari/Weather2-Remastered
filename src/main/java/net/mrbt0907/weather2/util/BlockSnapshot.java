package net.mrbt0907.weather2.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class BlockSnapshot {
    public final BlockState state;
    public final Block block;
    public final BlockPos pos;
    public final int x, y, z;

    public BlockSnapshot(@Nonnull BlockState state, BlockPos pos) {
        this.state = state;
        this.pos = pos;
        block = state.getBlock();
        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();
    }

    public double distance(BlockSnapshot snapshot) {
        return distance(snapshot.x, snapshot.y, snapshot.z);
    }

    public double distance(double x, double y, double z) {
        return Maths.distanceSq(this.x, this.y, this.z, x, y, z);
    }

    @Override
    public int hashCode() {
        return x + z << 8 + y << 16;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof BlockSnapshot && object.hashCode() == hashCode();
    }

    @Override
    public String toString() {
        return "BlockSnapshot{block=" + block.getRegistryName() + ", x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}