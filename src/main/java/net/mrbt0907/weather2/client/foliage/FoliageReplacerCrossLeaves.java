package net.mrbt0907.weather2.client.foliage;

import net.corosus.coroutillegacy.util.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class FoliageReplacerCrossLeaves extends FoliageReplacerCross {

    public FoliageReplacerCrossLeaves(BlockState state) {
        super(state);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public boolean validFoliageSpot(World world, BlockPos pos) {
        if (stateSensitive) {
            BlockState stateScan = world.getBlockState(pos);
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
            return world.getBlockState(pos).getBlock() == state.getBlock();
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

        Vec3 vec = new Vec3(0.2, 0, 0.2);

        FoliageEnhancerShader.addForPos(this, height, pos, vec, biomeColorize, -1, new Vec3(0, 0, 0));

    }
}