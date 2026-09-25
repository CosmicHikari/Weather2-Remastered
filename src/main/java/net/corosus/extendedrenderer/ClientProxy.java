package net.corosus.extendedrenderer;

import net.corosus.extendedrenderer.render.FoliageRenderer;
import net.corosus.extendedrenderer.render.RotatingParticleManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    public static Minecraft mc;

    public ClientProxy() {
        mc = Minecraft.getInstance();
    }

    @Override
    public void preInit() {
        super.preInit();
    }

    @Override
    public void postInit() {
        super.postInit();
        ExtendedRenderer.rotEffRenderer = new RotatingParticleManager(mc.level, mc.textureManager);
        ExtendedRenderer.foliageRenderer = new FoliageRenderer();
    }

    @Override
    public void init() {
        super.init();
    }
}