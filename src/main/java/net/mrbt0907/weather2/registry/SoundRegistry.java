package net.mrbt0907.weather2.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.mrbt0907.weather2.Weather2;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Weather2.MODID);

    public static final RegistryObject<SoundEvent> siren = register("block.siren");

    public static final RegistryObject<SoundEvent> sirenDarude = register("block.siren.darude");

    public static final RegistryObject<SoundEvent> sirenAdvanced = register("block.siren.advanced");

    public static final RegistryObject<SoundEvent> leaves = register("ambient.leaves");

    public static final RegistryObject<SoundEvent> waterfall = register("ambient.waterfall");

    public static final RegistryObject<SoundEvent> windReallyFast = register("ambient.wind.really_fast");

    public static final RegistryObject<SoundEvent> windFast = register("ambient.wind.fast");

    public static final RegistryObject<SoundEvent> wind = register("ambient.wind");

    public static final RegistryObject<SoundEvent> windSlow = register("ambient.wind.slow");

    public static final RegistryObject<SoundEvent> windBreeze = register("ambient.wind.breeze");

    public static final RegistryObject<SoundEvent> rainLight = register("ambient.rain.light");

    public static final RegistryObject<SoundEvent> rainHeavy = register("ambient.rain.heavy");

    public static final RegistryObject<SoundEvent> debris = register("weather.debris");

    public static final RegistryObject<SoundEvent> storm = register("weather.storm");

    public static final RegistryObject<SoundEvent> sandstormFast = register("weather.sandstorm.fast");

    public static final RegistryObject<SoundEvent> sandstorm = register("weather.sandstorm");

    public static final RegistryObject<SoundEvent> sandstormSlow = register("weather.sandstorm.slow");

    public static final RegistryObject<SoundEvent> thunderDangerouslyClose = register("entity.lightning.thunder.dangerous");

    public static final RegistryObject<SoundEvent> thunderNear = register("entity.lightning.thunder.near");

    public static final RegistryObject<SoundEvent> thunderFar = register("entity.lightning.thunder.far");

    private static RegistryObject<SoundEvent> register(String path) {
        ResourceLocation id = new ResourceLocation(Weather2.OLD_MODID, path);
        return SOUNDS.register(path.replace(".", "_"), () -> new SoundEvent(id));
    }
}