package net.mrbt0907.weather2.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockSESnapshot extends BlockSnapshot {
    public final int type;
    public final BlockPos relativePos;
    public final int rX, rY, rZ;

    public BlockSESnapshot(BlockState state, BlockPos pos, BlockPos relativePos, int type) {
        super(state, pos);
        this.type = type;
        this.relativePos = relativePos;
        if (relativePos != null) {
            rX = relativePos.getX();
            rY = relativePos.getY();
            rZ = relativePos.getZ();
        } else
            rX = rY = rZ = 0;
    }
}