package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.client.audio.AudioStreamManager;
import net.minecraft.resources.IResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AudioStreamManager.class)
public interface AudioStreamManagerAccessor {
    @Accessor("resourceManager")
    IResourceManager getResourceManager();
}