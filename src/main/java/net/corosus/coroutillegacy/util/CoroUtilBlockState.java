package net.corosus.coroutillegacy.util;

import net.minecraft.block.BlockState;
import net.minecraft.state.Property;

import java.util.List;

public class CoroUtilBlockState {

    public static boolean partialStateInListMatchesFullState(BlockState fullState, List<BlockState> listStates) {
        for (BlockState partialState : listStates) {
            if (partialStateMatchesFullState(partialState, fullState)) {
                return true;
            }
        }
        return false;
    }

    public static boolean partialStateMatchesFullState(BlockState partialState, BlockState fullState) {
        if (partialState.getBlock() != fullState.getBlock()) return false;

        for (Property<?> prop : partialState.getProperties()) {
            if (partialState.getValue(prop) != fullState.getValue(prop)) {
                return false;
            }
        }
        return true;
    }

}