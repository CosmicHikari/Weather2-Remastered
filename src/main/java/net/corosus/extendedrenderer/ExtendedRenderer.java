package net.corosus.extendedrenderer;

import net.corosus.coroutillegacy.config.ConfigCoroUtilLegacy;
import net.corosus.extendedrenderer.render.FoliageRenderer;
import net.corosus.extendedrenderer.render.RotatingParticleManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("extendedrenderer")
public class ExtendedRenderer {

    public static final String modid = "extendedrenderer";

    public static ExtendedRenderer instance;

    public static CommonProxy proxy;

    @OnlyIn(Dist.CLIENT)
    public static RotatingParticleManager rotEffRenderer;

    @OnlyIn(Dist.CLIENT)
    public static FoliageRenderer foliageRenderer;

    public ExtendedRenderer() {
        instance = this;

        proxy = DistExecutor.safeRunForDist(
                () -> ClientProxy::new,
                () -> CommonProxy::new);

        MinecraftForge.EVENT_BUS.register(new EventHandler());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    public static void dbg(Object obj) {
        System.out.println(obj);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        proxy.preInit();
        proxy.init();
    }

    private void clientSetup(FMLClientSetupEvent event) {
        proxy.postInit();
        EventHandler.foliageUseLast = ConfigCoroUtilLegacy.foliageShaders;
    }
}