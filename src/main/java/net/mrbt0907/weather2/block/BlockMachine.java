package net.mrbt0907.weather2.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import net.mrbt0907.weather2.block.tile.TileMachine;

public class BlockMachine extends Block {
    public BlockMachine(Material material) {
        this(material, 0.6F, 10.0F);
    }

    public BlockMachine(Material material, float hardness, float resistance) {
        super(AbstractBlock.Properties.of(material)
                .strength(hardness, resistance));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileMachine();
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }
}