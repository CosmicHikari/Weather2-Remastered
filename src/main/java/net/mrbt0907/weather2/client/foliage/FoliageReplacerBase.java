package net.mrbt0907.weather2.client.foliage;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.state.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class FoliageReplacerBase {


    public BlockState state;

    public List<TextureAtlasSprite> sprites = new ArrayList<>();
    public int expectedHeight = 1;
    public Material baseMaterial = Material.GRASS;
    public boolean biomeColorize = true;
    public boolean randomizeCoord = true;
    public boolean stateSensitive = false;
    public HashMap<Property<?>, Comparable<?>> lookupPropertiesToComparable = new HashMap<>();
    public int animationID;
    public float looseness = 1F;

    public FoliageReplacerBase(BlockState state) {
        this.state = state;
    }

    public FoliageReplacerBase setSprites(List<TextureAtlasSprite> sprites) {
        this.sprites = sprites;
        return this;
    }

    public FoliageReplacerBase setSprite(TextureAtlasSprite sprite) {
        this.sprites.add(sprite);
        return this;
    }

    public FoliageReplacerBase setBaseMaterial(Material material) {
        this.baseMaterial = material;
        return this;
    }

    public FoliageReplacerBase setBiomeColorize(boolean val) {
        this.biomeColorize = val;
        return this;
    }

    public FoliageReplacerBase setRandomizeCoord(boolean val) {
        this.randomizeCoord = val;
        return this;
    }

    public FoliageReplacerBase setStateSensitive(boolean val) {
        this.stateSensitive = val;
        return this;
    }

    public <T extends Comparable<T>> FoliageReplacerBase addComparable(Property<T> property, T comparable) {
        lookupPropertiesToComparable.put(property, comparable);
        return this;
    }

    public abstract boolean validFoliageSpot(World world, BlockPos pos);

    public abstract void addForPos(World world, BlockPos pos);

    public void markMeshesDirty() {
        for (TextureAtlasSprite sprite : sprites) {
            FoliageEnhancerShader.markMeshDirty(sprite, true);
        }
    }

    public FoliageReplacerBase setLooseness(float val) {
        this.looseness = val;
        return this;
    }

    public boolean isActive() {
        return true;
    }
}