package net.mrbt0907.weather2.mixins.injection;

import net.minecraft.client.audio.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SoundSystem.class)
public class MixinSoundSystem {
    @ModifyArg(
            method = "init()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/audio/SoundSystem$HandlerImpl;<init>(I)V",
                    ordinal = 1
            ),
            index = 0
    )
    private int increaseStreamingChannels(int original) {
        return 32;
    }
}