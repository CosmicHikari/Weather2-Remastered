package net.mrbt0907.weather2.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockBase extends Block {
    public BlockBase(Material material) {
        this(Block.Properties.of(material).strength(0.6F, 10.0F));
    }

    public BlockBase(Material material, float hardness, float resistance) {
        this(Block.Properties.of(material).strength(hardness, resistance));
    }

    public BlockBase(Block.Properties properties) {
        super(properties);
    }
}