package net.mrbt0907.weather2.registry;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.entity.EntityHail;
import net.mrbt0907.weather2.entity.EntityLightningEX;
import net.mrbt0907.weather2.entity.EntityMovingBlock;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Weather2.MODID);


    public static final RegistryObject<EntityType<EntityHail>> WEATHER_HAIL = ENTITIES.register("weather_hail",
            () -> EntityType.Builder.<EntityHail>of(EntityHail::new, EntityClassification.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(5)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("weather_hail"));

    public static final RegistryObject<EntityType<EntityMovingBlock>> MOVING_BLOCK = ENTITIES.register("moving_block",
            () -> EntityType.Builder.<EntityMovingBlock>of(EntityMovingBlock::new, EntityClassification.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(8)
                    .updateInterval(5)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("moving_block"));

    public static final RegistryObject<EntityType<EntityLightningEX>> LIGHTNING_BOLT = ENTITIES.register("lightning_bolt",
            () -> EntityType.Builder.<EntityLightningEX>of(EntityLightningEX::new, EntityClassification.MISC)
                    .sized(0.0F, 0.0F)
                    .clientTrackingRange(16)
                    .updateInterval(5)
                    .setShouldReceiveVelocityUpdates(true)
                    .fireImmune()
                    .build("lightning_bolt"));
}