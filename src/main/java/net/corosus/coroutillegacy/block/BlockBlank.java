package net.corosus.coroutillegacy.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockBlank extends Block {

    public BlockBlank() {
        super(Block.Properties.of(Material.AIR)
                .noCollission()
                .noOcclusion()
                .air());
    }
}