package net.mrbt0907.weather2.client.foliage;

import net.corosus.coroutillegacy.util.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;


public class FoliageReplacerCross extends FoliageReplacerBase {

    public FoliageReplacerCross(BlockState state) {
        super(state);
    }

    public FoliageReplacerCross(BlockState state, int expectedHeight) {
        super(state);
        this.expectedHeight = expectedHeight;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public boolean validFoliageSpot(World world, BlockPos pos) {
        if (baseMaterial == null || world.getBlockState(pos).getMaterial() == baseMaterial) {
            if (stateSensitive) {
                BlockState stateScan = world.getBlockState(pos.above());
                if (stateScan.getBlock() == state.getBlock()) {
                    boolean fail = false;
                    for (Map.Entry<Property<?>, Comparable<?>> entrySet : lookupPropertiesToComparable.entrySet()) {
                        if (stateScan.getValue((Property) entrySet.getKey()) != entrySet.getValue()) {
                            fail = true;
                            break;
                        }
                    }
                    return !fail;
                } else {
                    return false;
                }
            } else {
                return world.getBlockState(pos.above()).getBlock() == state.getBlock();
            }
        } else {
            return false;
        }
    }

    @Override
    public void addForPos(World world, BlockPos pos) {

        int height = expectedHeight;
        if (height == -1) {
            Block block = state.getBlock();

            height = 0;
            while (block == state.getBlock()) {
                height++;
                block = world.getBlockState(pos.above(height)).getBlock();
            }
        }
        FoliageEnhancerShader.addForPos(this, height, pos, randomizeCoord ? new Vec3(0.4, 0, 0.4) : null, biomeColorize);
    }
}