package net.mrbt0907.weather2;

import net.mrbt0907.weather2.registry.RecipeRegistry;

public class CommonProxy {
    public void commonSetup() {

    }

    public void clientSetup() {

    }

    public void postInit() {
        RecipeRegistry.postInit();
    }
}