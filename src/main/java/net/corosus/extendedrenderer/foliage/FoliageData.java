package net.corosus.extendedrenderer.foliage;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;

import java.util.concurrent.ConcurrentHashMap;

public class FoliageData {

    public static ConcurrentHashMap<BlockState, IBakedModel> backupBakedModelStore = new ConcurrentHashMap<>();
    public BlockState originalBlockState;

}