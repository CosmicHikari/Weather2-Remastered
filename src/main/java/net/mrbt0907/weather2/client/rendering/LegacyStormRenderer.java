package net.mrbt0907.weather2.client.rendering;

import net.corosus.extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.api.weather.AbstractWeatherRenderer;
import net.mrbt0907.weather2.client.entity.particle.ExtendedEntityRotFX;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.registry.ParticleRegistry;
import net.mrbt0907.weather2.util.ChunkUtils;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.weather.storm.NewTornadoHelper;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LegacyStormRenderer extends AbstractWeatherRenderer {
    @OnlyIn(Dist.CLIENT)
    public List<EntityRotFX> listParticlesCloud;
    @OnlyIn(Dist.CLIENT)
    public List<EntityRotFX> listParticlesFunnel;

    public LegacyStormRenderer(WeatherObject system) {
        super(system);
        listParticlesFunnel = new ArrayList<>();
        listParticlesCloud = new ArrayList<>();
    }

    @Override
    public void onTick(WeatherManagerClient manager) {
        if (!(system instanceof StormObject)) return;
        StormObject storm = (StormObject) this.system;

        if (manager.getWorld().getGameTime() % (ConfigClient.cloud_particle_delay + 1L) == 0L) {
            for (int i = 0; i < 1; i++) {
                double x = storm.pos.posX + (Maths.random(storm.size) - Maths.random(storm.size)) * 0.3D;
                double z = storm.pos.posZ + (Maths.random(storm.size) - Maths.random(storm.size)) * 0.3D;
                ExtendedEntityRotFX particle = spawnParticle(x, storm.getLayerHeight(), z, 0, ParticleRegistry.cloud_legacy);

                if (particle == null) continue;

                if (storm.isFirenado)
                    particle.setColor(0.9F, 0.5F, 0.2F);
                else
                    particle.setColor(0.7F, 0.7F, 0.7F);

                particle.setScale(600);
                particle.setMaxAge(140);
                listParticlesCloud.add(particle);
            }
        }

        if (storm.isDeadly()) {
            int itCount = storm.isSpout ? 4 : 6;

            if (manager.getWorld().getGameTime() % (ConfigClient.funnel_particle_delay + 1L) == 0L) {
                for (int i = 0; i < itCount; i++) {
                    double tryX2 = storm.pos_funnel_base.posX + Maths.random(NewTornadoHelper.getTornadoBaseSize(storm) * 2) - NewTornadoHelper.getTornadoBaseSize(storm);
                    double tryZ2 = storm.pos_funnel_base.posZ + Maths.random(NewTornadoHelper.getTornadoBaseSize(storm) * 2) - NewTornadoHelper.getTornadoBaseSize(storm);
                    Block blockID = ChunkUtils.getBlockState(manager.getWorld(), (int) tryX2, (int) storm.pos_funnel_base.posY - 1, (int) tryZ2).getBlock();
                    Block blockIDUp = ChunkUtils.getBlockState(manager.getWorld(), (int) tryX2, (int) storm.pos_funnel_base.posY, (int) tryZ2).getBlock();
                    Block blockIDDown = ChunkUtils.getBlockState(manager.getWorld(), (int) tryX2, (int) storm.pos_funnel_base.posY - 2, (int) tryZ2).getBlock();
                    int colorID = 0;

                    if (ConfigClient.enable_tornado_block_colors) {
                        if (storm.isFirenado)
                            colorID = 7;
                        else if (storm.isSpout)
                            colorID = 3;
                        else {
                            if (blockID == Blocks.WATER || blockIDUp == Blocks.WATER || blockIDDown == Blocks.WATER)
                                colorID = 3;
                            else if (blockID == Blocks.SAND || blockID == Blocks.SANDSTONE)
                                colorID = 2;
                            else if (blockID == Blocks.DIRT)
                                colorID = 1;
                            else if (blockID == Blocks.SNOW)
                                colorID = 4;
                            else if (blockID.is(BlockTags.LEAVES) || blockID == Blocks.GRASS_BLOCK)
                                colorID = 6;
                        }
                    }

                    float r = 0.7F, g = 0.7F, b = 0.7F;
                    Color color = null;
                    switch (colorID) {
                        case 1:
                            color = new Color(7951674);
                            break;
                        case 2:
                            color = new Color(14077848);
                            break;
                        case 3:
                            color = new Color(10973);
                            break;
                        case 4:
                            color = new Color(15663103);
                            break;
                        case 5:
                            color = new Color(0xFFFFFF);
                            break;
                        case 6:
                            color = Color.GREEN;
                            break;
                        case 7:
                            color = new Color(0xFF7722);
                            break;
                    }

                    if (color != null) {
                        r = color.getRed() / 255.0F;
                        g = color.getGreen() / 255.0F;
                        b = color.getBlue() / 255.0F;
                    }

                    ExtendedEntityRotFX particle = spawnParticle(tryX2, storm.pos_funnel_base.posY, tryZ2, 2, ParticleRegistry.cloud_legacy);

                    if (particle == null) continue;

                    particle.setColor(r, g, b);
                    particle.setScale(200);
                    particle.setMaxAge(140);
                    listParticlesFunnel.add(particle);
                }
            }

            if (storm.strength != 120.0F)
                storm.strength = 120.0F;

            if (!listParticlesFunnel.isEmpty()) {
                for (int var9 = listParticlesFunnel.size() - 1; var9 >= 0; var9--) {
                    EntityRotFX var30 = listParticlesFunnel.get(var9);

                    double var16 = storm.pos_funnel_base.posX - var30.getPosX();
                    double var18 = storm.pos_funnel_base.posZ - var30.getPosZ();
                    var30.rotationYaw = (float) (Maths.fastATan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
                    var30.rotationPitch = -30F;

                    if (!var30.isAlive()) {
                        var30.remove();
                        listParticlesFunnel.remove(var9);
                    } else {
                        storm.spinEntity(var30);
                    }
                }
            }

            if (!listParticlesCloud.isEmpty()) {
                for (int var9 = listParticlesCloud.size() - 1; var9 >= 0; var9--) {
                    EntityRotFX var30 = listParticlesCloud.get(var9);

                    double var16 = storm.pos_funnel_base.posX - var30.getPosX();
                    double var18 = storm.pos_funnel_base.posZ - var30.getPosZ();
                    var30.rotationYaw = (float) (Maths.fastATan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
                    var30.rotationPitch = 90F;

                    if (!var30.isAlive()) {
                        var30.remove();
                        listParticlesCloud.remove(var9);
                    } else {
                        storm.spinEntity(var30);
                    }
                }
            }
        }
    }

    @Override
    public void cleanupRenderer() {
        listParticlesFunnel.clear();
        listParticlesCloud.clear();
    }

    @Override
    public void onParticleLimitRefresh(WeatherManagerClient manager, int newParticleLimit) {
    }

    @Override
    public List<String> onDebugInfo() {
        List<String> debugInfo = new ArrayList<>();
        debugInfo.add("Legacy Renderer - Version 1.0");
        return debugInfo;
    }
}