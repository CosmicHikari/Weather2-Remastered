package net.corosus.coroutillegacy.util;

import net.minecraft.block.Block;

public class ChunkCoordinatesBlock extends BlockCoord {

    public Block block;
    public int meta;


    public ChunkCoordinatesBlock(int par1, int par2, int par3, Block parBlockID, int parMeta) {
        super(par1, par2, par3);
        block = parBlockID;
        meta = parMeta;
    }

}