package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.client.audio.LocatableSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocatableSound.class)
public interface LocatableSoundAccessor {
    @Accessor("looping")
    boolean getLooping();

    @Mutable
    @Accessor("looping")
    void setLooping(boolean looping);
}