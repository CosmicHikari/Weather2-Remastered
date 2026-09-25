package net.mrbt0907.weather2.client.rendering;

import net.corosus.coroutillegacy.config.ConfigCoroUtilLegacy;
import net.corosus.extendedrenderer.particle.ParticleRegistry;
import net.corosus.extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.api.weather.AbstractWeatherRenderer;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.client.entity.particle.ExtendedEntityRotFX;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.util.ChunkUtils;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.StormObject.StormType;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NormalStormRenderer extends AbstractWeatherRenderer {
    @OnlyIn(Dist.CLIENT)
    public List<ExtendedEntityRotFX> listParticlesCloud,
            listParticlesFunnel,
            listParticlesMeso,
            listParticlesMesoAlt,
            listParticlesGround,
            listParticlesRain;

    public int particleLimitCloud, particleLimitFunnel, particleLimitGround, particleLimitRain, particleLimitMeso;

    public NormalStormRenderer(WeatherObject system) {
        super(system);
        listParticlesCloud = new ArrayList<>();
        listParticlesGround = new ArrayList<>();
        listParticlesRain = new ArrayList<>();
        listParticlesFunnel = new ArrayList<>();
        listParticlesMeso = new ArrayList<>();
        listParticlesMesoAlt = new ArrayList<>();
    }

    public static int getAdjustedLayerHeight() {
        return (ConfigClient.extended_render_distance > 512.0D ? 128 : 64);
    }

    @Override
    public void onTick(WeatherManagerClient manager) {
        if (!(system instanceof StormObject)) return;
        StormObject storm = (StormObject) this.system;

        double gapRadius = storm.stormType == StormType.WATER.ordinal()
                ? (storm.size / 1.75D)
                : (storm.size / Maths.random(2.25D, 1.5D));

        PlayerEntity entP = Minecraft.getInstance().player;

        BlockState state = ConfigCoroUtilLegacy.optimizedCloudRendering
                ? Blocks.AIR.defaultBlockState()
                : ChunkUtils.getBlockState(manager.getWorld(),
                (int) storm.pos_funnel_base.posX,
                (int) storm.pos_funnel_base.posY - 1,
                (int) storm.pos_funnel_base.posZ);

        Material material = state.getMaterial();
        int layerHeight = storm.getLayerHeight() + NormalStormRenderer.getAdjustedLayerHeight();
        double maxRenderDistance = NewSceneEnhancer.instance().renderDistance + 64.0D;
        float sizeCloudMult = Math.min(Math.max(storm.size * 0.0011F, 0.45F) * (float) ConfigClient.particle_scale_mult, layerHeight * 0.01F);
        float sizeFunnelMult = Math.min(Math.max(storm.funnelSize * 0.015F, 0.2F) * (float) ConfigClient.particle_scale_mult, layerHeight * 0.0025F);
        float sizeOtherMult = Math.min(Math.max(storm.size * 0.002F, 0.45F) * (float) ConfigClient.particle_scale_mult, layerHeight * 0.03F);
        float heightMult = layerHeight * 0.0065F;
        if (Maths.random(0, 10) >= 5) heightMult = layerHeight * Maths.random(0.0054F, 0.0100F);
        float rotationMult = Math.max(heightMult * 0.55F, 1.0F);
        float r = -1.0F, g = -1.0F, b = -1.0F;

        if (ConfigClient.enable_tornado_block_colors) {
            if (!ConfigCoroUtilLegacy.optimizedCloudRendering && state.isAir()) {
                state = storm.isSpout ? Blocks.WATER.defaultBlockState() : Blocks.DIRT.defaultBlockState();
                material = state.getMaterial();
            }

            if (material == Material.DIRT || material == Material.GRASS
                    || material == Material.LEAVES || material == Material.PLANT) {
                r = 0.3F;
                g = 0.25F;
                b = 0.15F;
            } else if (material == Material.SAND) {
                r = 0.5F;
                g = 0.45F;
                b = 0.35F;
            } else if (material == Material.SNOW || material == Material.TOP_SNOW) {
                r = 0.5F;
                g = 0.5F;
                b = 0.7F;
            } else if (material == Material.WATER || storm.isSpout) {
                r = 0.3F;
                g = 0.35F;
                b = 0.7F;
            } else if (material == Material.LAVA) {
                r = 1.0F;
                g = 0.45F;
                b = 0.35F;
            }
        }

        int delay = Math.max(1, (int) (100F / storm.size));
        int loopSize = 1;
        int extraSpawning = 0;

        if (storm.isSevere()) {
            loopSize += 4;
            extraSpawning = (int) Math.min(storm.funnelSize * 2.375F, 1000.0F);
        }

        Random rand = new Random();
        Vec3 playerAdjPos = new Vec3(entP.getX(), storm.pos.posY, entP.getZ());

        if (ConfigClient.enable_cloud_rendering || storm.isStorm()) {
            if (ConfigCoroUtilLegacy.optimizedCloudRendering) {
                boolean isStorm = storm.stage >= Stage.RAIN.getStage();
                int height = layerHeight + (isStorm ? -5 : 0);
                int size = (int) (storm.size * 1.2D);
                float finalBright = isStorm ? 0.5F : 0.75F;

                for (int i = 0; i < loopSize && shouldSpawn(0); i++) {
                    Vec3 tryPos = new Vec3(storm.pos.posX + Maths.random(size), height,
                            storm.pos.posZ + Maths.random(size));

                    ExtendedEntityRotFX particle = WeatherUtil.isAprilFoolsDay()
                            ? spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0, ParticleRegistry.chicken)
                            : spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0);

                    if (particle == null) break;

                    if (i != 0) {
                        double rotPos = (i - 1);
                        float radStart = (float) ((360D / 8D) * rotPos);
                        particle.rotationAroundCenter = radStart;
                    }

                    particle.setColor(finalBright, finalBright, finalBright);
                    particle.setScale(400.0F * sizeCloudMult);
                    particle.setMaxAge(120);
                    particle.setVolumetric();
                    listParticlesCloud.add(particle);
                }
            } else {
                if (manager.getWorld().getGameTime() % (delay + ConfigClient.cloud_particle_delay) == 0) {
                    for (int i = 0; i < loopSize && shouldSpawn(0); i++) {
                        if (listParticlesCloud.size() < (storm.size + extraSpawning)) {
                            double spawnRad = storm.size * 1.2D;
                            Vec3 tryPos = new Vec3(
                                    storm.pos.posX + (rand.nextDouble() * spawnRad) - (rand.nextDouble() * spawnRad),
                                    layerHeight + (rand.nextDouble() * 40.0F) + (storm.stage >= Stage.RAIN.getStage() ? 30.0F : 60.0D),
                                    storm.pos.posZ + (rand.nextDouble() * spawnRad) - (rand.nextDouble() * spawnRad));

                            if (storm.stormType == StormType.WATER.ordinal()) {
                                double dx1 = tryPos.posX - storm.pos.posX;
                                double dz1 = tryPos.posZ - storm.pos.posZ;
                                if ((dx1 * dx1 + dz1 * dz1) < (gapRadius * gapRadius)) continue;
                            }

                            if (tryPos.distanceSq(playerAdjPos) < maxRenderDistance) {
                                if (storm.pos.distanceSq(tryPos) > 200.0D || storm.stormType == 0)
                                    if (storm.getAvoidAngleIfTerrainAtOrAheadOfPosition(storm.getAngle(), tryPos) == 0) {
                                        ExtendedEntityRotFX particle;
                                        if (WeatherUtil.isAprilFoolsDay()) {
                                            particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0, ParticleRegistry.chicken);
                                            if (particle == null) break;
                                            particle.setColor(1F, 1F, 1F);
                                        } else {
                                            float finalRed = Math.min(0.8F, 0.6F + (rand.nextFloat() * 0.2F)) + (storm.stage >= Stage.RAIN.getStage() ? -0.3F : 0.0F);
                                            float finalGreen = finalRed;
                                            float finalBlue = finalRed;
                                            if (!storm.isFirenado && !WeatherUtil.isAprilFoolsDay())
                                                finalRed = Maths.clamp(finalRed -= NewSceneEnhancer.instance().overcast, (finalGreen + finalBlue) * ConfigClient.cloud_greenblue_mult, 1.0F);

                                            particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0,
                                                    storm.stage <= Stage.RAIN.getStage()
                                                            ? net.mrbt0907.weather2.registry.ParticleRegistry.cloud256_light
                                                            : net.mrbt0907.weather2.registry.ParticleRegistry.cloud256);
                                            if (particle == null) break;
                                            particle.setColor(finalRed, finalGreen, finalBlue);

                                            if (storm.isSevere() && storm.isFirenado) {
                                                particle.setParticleTexture(net.mrbt0907.weather2.registry.ParticleRegistry.cloud256_fire);
                                                particle.setColor(1F, 1F, 1F);
                                            }
                                        }
                                        particle.rotationPitch = Maths.random(80.0F, 100.0F);
                                        particle.setScale(2250.0F * sizeCloudMult);
                                        particle.setVolumetric();
                                        listParticlesCloud.add(particle);
                                    }
                            }
                        }
                    }
                }
            }
        }

        if (ConfigClient.enable_tornado_debris && !ConfigCoroUtilLegacy.optimizedCloudRendering
                && storm.stormType == StormType.LAND.ordinal()
                && storm.stage > Stage.SEVERE.getStage()
                && r >= 0.0F && !material.isLiquid()) {
            if (manager.getWorld().getGameTime() % (delay + ConfigClient.ground_debris_particle_delay) == 0) {
                for (int i = 0; i < Math.round(((float) storm.stage / storm.stageMax) * 32) && shouldSpawn(2); i++) {
                    double spawnRad = storm.funnelSize * 2.0D;
                    Vec3 tryPos = new Vec3(
                            storm.pos_funnel_base.posX + (rand.nextDouble() * spawnRad) - (rand.nextDouble() * spawnRad),
                            storm.pos_funnel_base.posY,
                            storm.pos_funnel_base.posZ + (rand.nextDouble() * spawnRad) - (rand.nextDouble() * spawnRad));
                    double distance = tryPos.distanceSq(playerAdjPos);

                    if (distance < maxRenderDistance && distance < 320.0D) {
                        int groundY = WeatherUtilBlock.getPrecipitationHeightSafe(manager.getWorld(),
                                new BlockPos((int) tryPos.posX, 0, (int) tryPos.posZ)).getY();
                        ExtendedEntityRotFX particle;
                        if (WeatherUtil.isAprilFoolsDay())
                            particle = spawnParticle(tryPos.posX, groundY, tryPos.posZ, 1, ParticleRegistry.potato);
                        else {
                            int reee = Maths.random(3);
                            TextureAtlasSprite sprite = reee == 1 ? ParticleRegistry.debris_1
                                    : reee == 2 ? ParticleRegistry.debris_2
                                    : reee == 3 ? ParticleRegistry.debris_3
                                    : ParticleRegistry.leaf;
                            particle = spawnParticle(tryPos.posX, groundY, tryPos.posZ, 1, sprite);
                        }
                        if (particle == null) break;
                        particle.setColor(r, g, b);
                        particle.setTicksFadeInMax(8);
                        particle.setTicksFadeOutMax(80);
                        particle.setGravity(0.25F);
                        particle.setMaxAge(80);
                        particle.setScale(Maths.random(3.0F, 25.0F));
                        particle.rotationYaw = rand.nextInt(360);
                        particle.rotationPitch = 30.0F + rand.nextInt(60);
                        particle.setMotionY(0.9D);
                        listParticlesGround.add(particle);
                    }

                    if (distance < maxRenderDistance && distance < 2048.0D && Maths.random(0, 9) > 7) {
                        int groundY = WeatherUtilBlock.getPrecipitationHeightSafe(manager.getWorld(),
                                new BlockPos((int) tryPos.posX, 0, (int) tryPos.posZ)).getY();
                        ExtendedEntityRotFX particle;
                        if (WeatherUtil.isAprilFoolsDay())
                            particle = spawnParticle(tryPos.posX, groundY, tryPos.posZ, 1, ParticleRegistry.potato);
                        else
                            particle = spawnParticle(tryPos.posX, groundY, tryPos.posZ, 1, ParticleRegistry.cloud256);
                        if (particle == null) break;
                        particle.setColor(r, g, b);
                        particle.setTicksFadeInMax(8);
                        particle.setTicksFadeOutMax(50);
                        particle.setGravity(0.25F);
                        particle.setMaxAge(60);
                        particle.setScale(Maths.random(230.0F, (storm.stage / storm.stageMax) * 425.0F));
                        particle.rotationYaw = rand.nextInt(360);
                        particle.rotationPitch = 30.0F + rand.nextInt(60);
                        particle.setMotionY(0.9D);
                        listParticlesGround.add(particle);
                    }
                }
            }
        }

        delay = 1;
        loopSize = 3 + (storm.funnelSize > 300.0F ? 4 : (int) (storm.funnelSize / 80.0F));
        double spawnRad = storm.funnelSize * 0.0005F;

        if (storm.stage >= Stage.TORNADO.getStage() + 1)
            spawnRad *= 48.25D;

        if (storm.isDeadly() && storm.stormType == 0 || storm.isSpout) {
            if (manager.getWorld().getGameTime() % (delay + ConfigClient.funnel_particle_delay) == 0) {
                for (int i = 0; i < loopSize && shouldSpawn(1); i++) {
                    Vec3 tryPos = new Vec3(
                            storm.pos_funnel_base.posX + (rand.nextDouble() * spawnRad) - (rand.nextDouble() * spawnRad),
                            storm.pos.posY,
                            storm.pos_funnel_base.posZ + (rand.nextDouble() * spawnRad) - (rand.nextDouble() * spawnRad));

                    if (tryPos.distanceSq(playerAdjPos) < maxRenderDistance) {
                        ExtendedEntityRotFX particle;
                        if (!storm.isFirenado) {
                            if (WeatherUtil.isAprilFoolsDay())
                                particle = spawnParticle(tryPos.posX, storm.pos_funnel_base.posY, tryPos.posZ, 2);
                            else
                                particle = spawnParticle(tryPos.posX, storm.pos_funnel_base.posY, tryPos.posZ, 2,
                                        net.mrbt0907.weather2.registry.ParticleRegistry.tornado256);
                        } else
                            particle = spawnParticle(tryPos.posX, storm.pos_funnel_base.posY, tryPos.posZ, 2,
                                    net.mrbt0907.weather2.registry.ParticleRegistry.cloud256_fire);

                        if (particle == null) break;

                        particle.setMaxAge(120 + ((storm.stage - 1) * 10) + rand.nextInt(100));
                        particle.rotationYaw = rand.nextInt(360);

                        float finalRed = Math.min(0.6F, 0.4F + (rand.nextFloat() * 0.2F));
                        float finalGreen = finalRed;
                        float finalBlue = finalRed;
                        if (!storm.isFirenado && !WeatherUtil.isAprilFoolsDay())
                            finalRed = Maths.clamp(finalRed -= NewSceneEnhancer.instance().overcast, (finalGreen + finalBlue) * ConfigClient.funnel_greenblue_mult, 1.0F);

                        if (storm.stage == Stage.SEVERE.getStage())
                            particle.setScale(100.0F * sizeFunnelMult * heightMult);
                        else
                            particle.setScale(450.0F * sizeFunnelMult * heightMult);

                        if (r >= 0.0F) {
                            particle.setColor(r, g, b);
                            particle.setFinalColor(0.0F, finalRed, finalGreen, finalBlue);
                            particle.setColorFade(0.75F);
                        } else
                            particle.setColor(finalRed, finalGreen, finalBlue);

                        if (storm.isFirenado) {
                            particle.setColor(1F, 1F, 1F);
                            particle.setScale(particle.getScale() * 0.7F);
                        }

                        particle.setVolumetric();
                        listParticlesFunnel.add(particle);
                    }
                }
            }
        }

        if (ConfigClient.enable_distant_downfall && storm.ticks % 20 == 0
                && ConfigClient.distant_downfall_particle_rate > 0.0F
                && listParticlesRain.size() < 1000
                && storm.stage > Stage.THUNDER.getStage()
                && storm.isRaining()
                && storm.temperature > 0.0F) {
            int particleCount = (int) Math.min(
                    Math.ceil(storm.rain * storm.stage * ConfigClient.distant_downfall_particle_rate * 0.005F), 50.0F);

            for (int i = 0; i < particleCount && shouldSpawn(3); i++) {
                double spawnRad2 = storm.size;
                Vec3 tryPos = new Vec3(
                        storm.pos.posX + (rand.nextDouble() * spawnRad2) - (rand.nextDouble() * spawnRad2),
                        storm.pos.posY + rand.nextInt(50),
                        storm.pos.posZ + (rand.nextDouble() * spawnRad2) - (rand.nextDouble() * spawnRad2));

                if (tryPos.distanceSq(playerAdjPos) < maxRenderDistance) {
                    float finalBright = Math.min(0.7F, 0.5F + (rand.nextFloat() * 0.2F))
                            + (storm.stage >= Stage.RAIN.getStage() ? -0.2F : 0.0F);
                    ExtendedEntityRotFX particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 2,
                            net.mrbt0907.weather2.registry.ParticleRegistry.distant_downfall);
                    if (particle == null) break;
                    particle.setMaxAge(200 + rand.nextInt(100));
                    particle.setScale(550.0F * sizeOtherMult);
                    particle.setAlphaF(0.0F);
                    particle.setColor(finalBright, finalBright, finalBright);
                    particle.facePlayer = false;
                    listParticlesRain.add(particle);
                }
            }
        }

        for (int i = listParticlesFunnel.size() - 1; i >= 0; i--) {
            EntityRotFX ent = listParticlesFunnel.get(i);
            if (!ent.isAlive() || ent.getPosY() > layerHeight + 200) {
                ent.remove();
                listParticlesFunnel.remove(i);
                continue;
            }
            double var16 = storm.pos.posX - ent.getPosX();
            double var18 = storm.pos.posZ - ent.getPosZ();
            ent.rotationYaw = (float) (Maths.fastATan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
            ent.rotationYaw += ent.getEntityId() % 90;
            ent.rotationPitch = -30F;
            storm.spinEntity(ent);
        }

        if (storm.getStage() > Stage.THUNDER.getStage()
                && manager.getWorld().getGameTime() % (delay + ConfigClient.cloud_particle_delay) == 0) {
            for (int i = 0; i < loopSize && shouldSpawn(4); i++) {
                if ((listParticlesMeso.size() < (storm.size + extraSpawning))
                        && (listParticlesMesoAlt.size() < (storm.size + extraSpawning))) {
                    int cloud = Maths.random(0, 2);
                    double stormRad = storm.size * (storm.stormType == StormType.WATER.ordinal() ? 1.2 : cloud != 0 ? 1.1D : 0.95D);
                    double stormRad2 = storm.size * 0.80D;
                    double defMesoHeight = layerHeight + (rand.nextDouble() * 40.0F);

                    Vec3 tryPos = new Vec3(
                            storm.pos.posX + (rand.nextDouble() * stormRad) - (rand.nextDouble() * stormRad),
                            storm.stormType == StormType.WATER.ordinal() ? defMesoHeight : defMesoHeight * ConfigClient.meso_height,
                            storm.pos.posZ + (rand.nextDouble() * stormRad) - (rand.nextDouble() * stormRad));

                    Vec3 tryPos2 = new Vec3(
                            storm.pos.posX + (rand.nextDouble() * stormRad2) - (rand.nextDouble() * stormRad2),
                            storm.stormType == StormType.WATER.ordinal() ? defMesoHeight + 250.0F : defMesoHeight * ConfigClient.meso_height,
                            storm.pos.posZ + (rand.nextDouble() * stormRad2) - (rand.nextDouble() * stormRad2));

                    double dx1 = tryPos.posX - storm.pos.posX;
                    double dz1 = tryPos.posZ - storm.pos.posZ;
                    double dx2 = tryPos2.posX - storm.pos.posX;
                    double dz2 = tryPos2.posZ - storm.pos.posZ;

                    if ((dx1 * dx1 + dz1 * dz1) < (gapRadius * gapRadius)) continue;
                    if ((dx2 * dx2 + dz2 * dz2) < (gapRadius * gapRadius)) continue;

                    if (tryPos.distanceSq(playerAdjPos) < maxRenderDistance) {
                        if (storm.stormType == 1 && storm.pos.distanceSq(tryPos) > 350.0D || storm.stormType == 0)
                            if (storm.getAvoidAngleIfTerrainAtOrAheadOfPosition(storm.getAngle(), tryPos) == 0) {
                                ExtendedEntityRotFX particle = null;
                                ExtendedEntityRotFX particle2 = null;

                                if (WeatherUtil.isAprilFoolsDay()) {
                                    particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0, ParticleRegistry.chicken);
                                    particle2 = spawnParticle(tryPos2.posX, tryPos.posY - 60D, tryPos2.posZ, 0, ParticleRegistry.chicken);
                                    if (particle != null) particle.setColor(1F, 1F, 1F);
                                    if (particle2 != null) particle2.setColor(1F, 1F, 1F);
                                } else {
                                    float finalRed = Math.min(0.8F, 0.65F + (rand.nextFloat() * 0.2F) - 0.3F);
                                    float finalGreen = finalRed;
                                    float finalBlue = finalRed;

                                    if (!storm.isFirenado && !WeatherUtil.isAprilFoolsDay())
                                        finalRed = Maths.clamp(finalRed -= NewSceneEnhancer.instance().overcast, (finalGreen + finalBlue) * ConfigClient.meso_greenblue_mult, 1.0F);

                                    particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 1,
                                            cloud != 0
                                                    ? net.mrbt0907.weather2.registry.ParticleRegistry.cloud256_meso
                                                    : net.mrbt0907.weather2.registry.ParticleRegistry.cloud256_meso_wall);
                                    particle2 = spawnParticle(tryPos2.posX, tryPos.posY - 60D, tryPos2.posZ, 1,
                                            net.mrbt0907.weather2.registry.ParticleRegistry.cloud256_meso);

                                    if (particle != null) particle.setColor(finalRed, finalGreen, finalBlue);
                                    if (particle2 != null) particle2.setColor(finalRed, finalGreen, finalBlue);

                                    if (storm.isFirenado) {
                                        if (particle != null) {
                                            particle.setParticleTexture(ParticleRegistry.cloud256_fire);
                                            particle.setColor(1F, 1F, 1F);
                                        }
                                        if (particle2 != null) {
                                            particle2.setParticleTexture(ParticleRegistry.cloud256_fire);
                                            particle2.setColor(1F, 1F, 1F);
                                        }
                                    }
                                }

                                if (particle == null && particle2 == null) break;

                                if (particle != null) {
                                    particle.rotationPitch = Maths.random(70.0F, 110.0F);
                                    particle.setScale(1250.0F * sizeCloudMult);
                                    particle.setVolumetric();
                                    listParticlesMeso.add(particle);
                                }

                                if (particle2 != null) {
                                    particle2.setScale(2200.0F * sizeCloudMult);
                                    particle2.setVolumetric();
                                    listParticlesMesoAlt.add(particle2);
                                }
                            }
                    }
                }
            }
        }

        for (int i = listParticlesMeso.size() - 1; i >= 0; i--) {
            EntityRotFX ent = listParticlesMeso.get(i);
            if (!ent.isAlive()) {
                ent.remove();
                listParticlesMeso.remove(i);
                continue;
            }

            double speed = storm.spin + (rand.nextDouble() * 0.04D) * rotationMult;
            double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
            if (storm.stormType == 1) speed *= 2.0D;

            double vecX = ent.getPosX() - storm.pos.posX;
            double vecZ = ent.getPosZ() - storm.pos.posZ;
            float angle = (float) (Maths.fastATan2(vecZ, vecX) * 180.0D / Math.PI);
            double curDist = 1000.0D;

            angle += speed * 2D;
            angle -= (ent.getEntityId() % 10) * 3D;
            angle += rand.nextInt(10) - rand.nextInt(10);

            if (ent.getPosY() > layerHeight + 10.0D)
                ent.setPos(ent.getPosX(), layerHeight + 10.0D, ent.getPosZ());

            if (storm.stage >= Stage.TORNADO.getStage()) {
                if (storm.stormType == StormType.WATER.ordinal()) {
                    ent.setPos(ent.getPosX(), layerHeight + 40.0D, ent.getPosZ());
                    angle += 30 + ((ent.getEntityId() % 5) * 4);
                    if (curDist > 150 + ((storm.stage - Stage.TORNADO.getStage() + 1) * 30)) angle += 10;
                } else
                    angle += 30 + ((ent.getEntityId() % 5) * 4);
            } else if (curDist > 150)
                angle += 10 + ((ent.getEntityId() % 5) * 4);

            double var16 = storm.pos.posX - ent.getPosX();
            double var18 = storm.pos.posZ - ent.getPosZ();
            ent.rotationYaw = (float) (Maths.fastATan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
            ent.rotationPitch = -30F - (ent.getEntityId() % 10);
            ent.setScale((storm.stormType == StormType.WATER.ordinal() ? 1500.0F : 900.0F) * sizeCloudMult);

            if (curSpeed < speed * 20D) {
                ent.setMotionX(ent.getMotionX() + -Maths.fastSin(Math.toRadians(angle)) * speed);
                ent.setMotionZ(ent.getMotionZ() + Maths.fastCos(Math.toRadians(angle)) * speed);
            }
        }

        for (int i = listParticlesMesoAlt.size() - 1; i >= 0; i--) {
            EntityRotFX ent = listParticlesMesoAlt.get(i);
            if (!ent.isAlive()) {
                ent.remove();
                listParticlesMesoAlt.remove(i);
                continue;
            }

            double speed = storm.spin + (rand.nextDouble() * 0.04D) * rotationMult;
            double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
            if (storm.stormType == 1) speed *= 2.0D;

            double vecX = ent.getPosX() - storm.pos.posX;
            double vecZ = ent.getPosZ() - storm.pos.posZ;
            float angle = (float) (Maths.fastATan2(vecZ, vecX) * 180.0D / Math.PI);
            double var16 = storm.pos.posX - ent.getPosX();
            double var18 = storm.pos.posZ - ent.getPosZ();
            ent.rotationYaw = (float) (Maths.fastATan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
            ent.rotationPitch = 90F;
            ent.setParticleTexture(net.mrbt0907.weather2.registry.ParticleRegistry.cloud256_meso);

            if (curSpeed < speed * 20D) {
                ent.setMotionX(ent.getMotionX() + -Maths.fastSin(Math.toRadians(angle)) * speed);
                ent.setMotionZ(ent.getMotionZ() + Maths.fastCos(Math.toRadians(angle)) * speed);
            }
        }

        for (int i = listParticlesCloud.size() - 1; i >= 0; i--) {
            EntityRotFX ent = listParticlesCloud.get(i);
            if (!ent.isAlive() || (storm.stormType == StormType.WATER.ordinal()
                    && ent.getDistance(storm.pos.posX, ent.getPosY(), storm.pos.posZ) < storm.funnelSize)) {
                ent.remove();
                listParticlesCloud.remove(i);
                continue;
            }

            double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
            double curDist = 10D;

            if (storm.isSevere()) {
                double speed = storm.spin + (rand.nextDouble() * 0.04D) * rotationMult;
                double distt = Math.max(ConfigStorm.max_storm_size, storm.funnelSize + 20.0D);
                if (storm.stormType == 1) {
                    speed *= 2.0D;
                    distt *= 2.0D;
                }

                double vecX = ent.getPosX() - storm.pos.posX;
                double vecZ = ent.getPosZ() - storm.pos.posZ;
                float angle = (float) (Maths.fastATan2(vecZ, vecX) * 180.0D / Math.PI);

                angle += speed * 10D;
                angle -= (ent.getEntityId() % 10) * 3D;
                angle += rand.nextInt(10) - rand.nextInt(10);

                if (curDist > distt) angle += 40;

                ent.rotationPitch = (float) (90.0F - (90.0f * Math.min(ent.getPosY() / (layerHeight + ent.getScale() * 0.75F), 1.0F)));
                if (ConfigClient.enable_extended_render_distance) ent.setScale(1000.0F * sizeCloudMult);

                if (curSpeed < speed * 20D) {
                    ent.setMotionX(ent.getMotionX() + -Maths.fastSin(Math.toRadians(angle)) * speed);
                    ent.setMotionZ(ent.getMotionZ() + Maths.fastCos(Math.toRadians(angle)) * speed);
                }
            } else {
                float cloudMoveAmp = 0.1F * (1 + storm.layer);
                float speed = storm.getSpeed() * cloudMoveAmp;
                float angle = storm.getAngle();

                if ((manager.getWorld().getGameTime()) % 40 == 0)
                    ent.avoidTerrainAngle = storm.getAvoidAngleIfTerrainAtOrAheadOfPosition(
                            angle, new Vec3(ent.getPos()));

                angle += ent.avoidTerrainAngle;
                if (ent.avoidTerrainAngle != 0) speed *= 0.5D;

                if (curSpeed < speed * 1D) {
                    ent.setMotionX(ent.getMotionX() + -Maths.fastSin(Math.toRadians(angle)) * speed);
                    ent.setMotionZ(ent.getMotionZ() + Maths.fastCos(Math.toRadians(angle)) * speed);
                }
            }

            float dropDownSpeedMax = 0.5F;
            if (storm.stormType == 1 || storm.stage > 8) dropDownSpeedMax = 1.9F;
            if (ent.getMotionY() < -dropDownSpeedMax) ent.setMotionY(-dropDownSpeedMax);
            if (ent.getMotionY() > dropDownSpeedMax) ent.setMotionY(dropDownSpeedMax);
        }

        for (int i = listParticlesRain.size() - 1; i >= 0; i--) {
            EntityRotFX ent = listParticlesRain.get(i);
            if (!ent.isAlive()) {
                listParticlesRain.remove(i);
                continue;
            }

            if (ent.getMotionY() > -heightMult) ent.setMotionY(ent.getMotionY() - 0.1D);

            double speed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
            double spin = storm.spin + 0.04F;
            double vecX = ent.getPosX() - storm.pos.posX;
            double vecZ = ent.getPosZ() - storm.pos.posZ;
            float angle = (float) (Maths.fastATan2(vecZ, vecX) * 180.0D / Math.PI);
            ent.rotationPitch = 0.0F;
            ent.rotationYaw = angle + 90;

            if (ent.getAlphaF() < 0.01F) ent.setAlphaF(ent.getAlphaF() + 0.01F);

            if (speed < spin * 15.0D * rotationMult) {
                ent.setMotionX(ent.getMotionX() + -Maths.fastSin(Math.toRadians(angle)) * spin);
                ent.setMotionZ(ent.getMotionZ() + Maths.fastCos(Math.toRadians(angle)) * spin);
            }
        }

        for (int i = listParticlesGround.size() - 1; i >= 0; i--) {
            EntityRotFX ent = listParticlesGround.get(i);
            if (!ent.isAlive()) {
                listParticlesGround.remove(i);
                continue;
            }
            storm.spinEntity(ent);
        }

        if (storm.strength != 100.0F) storm.strength = 100.0F;
    }

    @Override
    public void onParticleLimitRefresh(WeatherManagerClient manager, int newParticleLimit) {
        particleLimitCloud = (int) (newParticleLimit * 0.05F);
        particleLimitGround = (int) (newParticleLimit * 0.25F);
        particleLimitRain = (int) (newParticleLimit * 0.1F);
        particleLimitFunnel = (int) (newParticleLimit * 0.5F);
        particleLimitMeso = (int) (newParticleLimit * 0.1F);
    }

    @Override
    public void cleanupRenderer() {
        listParticlesCloud.clear();
        listParticlesFunnel.clear();
        listParticlesGround.clear();
        listParticlesRain.clear();
        listParticlesMeso.clear();
        listParticlesMesoAlt.clear();
    }

    private boolean shouldSpawn(int type) {
        switch (type) {
            case 0:
                return listParticlesCloud.size() < particleLimitCloud;
            case 1:
                return listParticlesFunnel.size() < particleLimitFunnel;
            case 2:
                return listParticlesGround.size() < particleLimitGround;
            case 3:
                return listParticlesRain.size() < particleLimitRain;
            case 4:
                return listParticlesMeso.size() < particleLimitMeso;
            default:
                return false;
        }
    }

    @Override
    public List<String> onDebugInfo() {
        List<String> debugInfo = new ArrayList<>();
        debugInfo.add("Default Renderer - Version 2.0");
        debugInfo.add("");
        debugInfo.add("Cloud Particles: " + listParticlesCloud.size() + "/" + particleLimitCloud);
        debugInfo.add("Funnel Particles: " + listParticlesFunnel.size() + "/" + particleLimitFunnel);
        debugInfo.add("Ground Particles: " + listParticlesGround.size() + "/" + particleLimitGround);
        debugInfo.add("Rain Particles: " + listParticlesRain.size() + "/" + particleLimitRain);
        return debugInfo;
    }
}