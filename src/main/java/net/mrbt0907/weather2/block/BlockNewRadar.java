package net.mrbt0907.weather2.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import net.mrbt0907.weather2.block.tile.TileRadar;

public class BlockNewRadar extends BlockMachine {
    private int tier = 0;

    public BlockNewRadar() {
        super(Material.CLAY);
    }

    public BlockNewRadar(int tier) {
        this();
        this.tier = tier;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileRadar(tier);
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public int getTier() {
        return tier;
    }
}