package net.mrbt0907.weather2.client.weather;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.IWeatherRain;
import net.mrbt0907.weather2.api.weather.IWeatherStaged;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.ConfigVolume;
import net.mrbt0907.weather2.entity.EntityLightningEX;
import net.mrbt0907.weather2.mixins.accessor.ClientWorldAccessor;
import net.mrbt0907.weather2.registry.SoundRegistry;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WeatherManager;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class WeatherManagerClient extends WeatherManager {
    private static final Minecraft MC = Minecraft.getInstance();
    public static StormObject closestStormCached;
    public List<Particle> effectedParticles = new ArrayList<Particle>();
    public List<Particle> weatherParticles = new ArrayList<Particle>();
    public int weatherID = 0;
    public int weatherRainTime = 0;
    private int particleLimit = 0;

    public WeatherManagerClient(World world) {
        super(world);
    }

    public ClientWorld getWorld() {
        return Minecraft.getInstance().level;
    }

    @Override
    public void tick() {
        super.tick();

        Particle particle;
        for (int i = 0; i < weatherParticles.size(); i++) {
            particle = weatherParticles.get(i);

            if (!particle.isAlive()) {
                weatherParticles.remove(i);
                i--;
            }
        }
    }

    public void tickRender(float partialTick) {
        if (world != null)
            getWeatherObjects().forEach(wo -> wo.tickRender(partialTick));
    }


    public void nbtSyncFromServer(CompoundNBT mainNBT) {
        int command = mainNBT.getInt("command");
        switch (command) {
            case 0:
                weatherID = mainNBT.getInt("weatherID");
                weatherRainTime = mainNBT.getInt("weatherRainTime");
                break;
            case 1: {
                CompoundNBT nbt = mainNBT.getCompound("weatherObject");
                FrontObject front = getFront(nbt.getUUID("frontUUID"));
                UUID uuid = nbt.getUUID("ID");

                Type weatherObjectType = Type.get(nbt.getInt("weatherType"));

                WeatherObject wo = null;
                if (weatherObjectType.ordinal() < Type.SANDSTORM.ordinal()) {
                    Weather2.debug("Creating a new storm: " + uuid);
                    wo = new StormObject(front);
                } else {
                    Weather2.debug("Creating a new sandstorm: " + uuid);
                    wo = new SandstormObject(this);
                }


                wo.nbt.setNewNBT(nbt);
                wo.nbt.updateCacheFromNew();
                wo.readFromNBT();

                front.addWeatherObject(wo);
                refreshParticleLimit();
                break;
            }
            case 2: {
                CompoundNBT nbt = mainNBT.getCompound("weatherObject");
                FrontObject front = getFront(nbt.getUUID("frontUUID"));
                if (front == null) break;

                WeatherObject wo = front.getWeatherObject(nbt.getUUID("ID"));
                if (wo != null) {
                    wo.nbt.setNewNBT(nbt);
                    wo.readFromNBT();
                    wo.nbt.updateCacheFromNew();
                }
                break;
            }
            case 3: {
                UUID uuidA = mainNBT.getUUID("weatherObject"), uuidB = mainNBT.getUUID("frontObject");
                FrontObject front = getFront(uuidB);
                WeatherObject system = systems.get(uuidA);

                if (front != null)
                    front.removeWeatherObject(uuidA);
                else if (system != null)
                    removeWeatherObject(uuidA);

                refreshParticleLimit();
                break;
            }
            case 4: {
                CompoundNBT nbt = mainNBT.getCompound("volcanoObject");
                VolcanoObject vo = new VolcanoObject(this);
                Weather2.debug("Creating a new volcano: " + nbt.getUUID("ID"));
                vo.nbtSyncFromServer(nbt);
                addVolcanoObject(vo);
                break;
            }
            case 5: {
                CompoundNBT stormNBT = mainNBT.getCompound("volcanoObject");
                UUID uuid = stormNBT.getUUID("ID");

                if (volcanoUUIDS.contains(uuid))
                    getVolcanoObjectByID(uuid).nbtSyncFromServer(stormNBT);
                break;
            }
            case 6: {
                CompoundNBT nbt = mainNBT.getCompound("manager");
                windManager.nbtSyncFromServer(nbt);
                break;
            }
            case 7: {
                int posXS = mainNBT.getInt("posX");
                int posYS = mainNBT.getInt("posY");
                int posZS = mainNBT.getInt("posZ");
                if (mainNBT.contains("entityID")) {
                    double posX = posXS;
                    double posY = posYS;
                    double posZ = posZS;
                    Entity ent = new EntityLightningEX(world, posX, posY, posZ);
                    ent.setId(mainNBT.getInt("entityID"));
                    world.addFreshEntity(ent);
                } else {
                    int x = mainNBT.getInt("posX"), y = mainNBT.getInt("posY"), z = mainNBT.getInt("posZ");
                    double distance = Maths.distanceSq(MC.player.getX(), MC.player.getY(), MC.player.getZ(), x, y, z);
                    if (MC.player != null) {
                        if (distance < ConfigStorm.max_lightning_bolt_distance) {
                            if (ConfigClient.enable_sky_lightning && world instanceof ClientWorld) {
                                ((ClientWorldAccessor) world).setSkyFlashTime(2);
                            }

                            world.playSound(MC.player, x, y, z, SoundRegistry.thunderNear.get(), SoundCategory.WEATHER,
                                    10000.0F * ConfigVolume.lightning, Maths.random(0.65F, 0.75F));
                        } else if (distance < ConfigStorm.max_lightning_bolt_distance * 1.5D) {
                            world.playSound(MC.player, x, y, z, SoundRegistry.thunderFar.get(), SoundCategory.WEATHER,
                                    10000.0F * ConfigVolume.lightning, Maths.random(0.65F, 0.75F));
                        }
                    }
                }
                break;
            }
            case 11: {
                CompoundNBT nbt = mainNBT.getCompound("frontObject");
                UUID uuid = mainNBT.getUUID("uuid");
                if (nbt.contains("posX")) {
                    Weather2.debug("Creating a new front: " + uuid);
                    FrontObject front = createFront(nbt.getInt("layer"), nbt.getDouble("posX"), nbt.getDouble("posZ"));
                    front.readNBT(nbt);
                    fronts.put(uuid, front);
                } else {
                    Weather2.debug("Creating a new global front: " + uuid);
                    globalFront = new FrontObject(this, null, 0);
                    globalFront.readNBT(nbt);
                    fronts.put(uuid, globalFront);
                }
                break;
            }
            case 12: {
                CompoundNBT nbt = mainNBT.getCompound("frontObject");
                FrontObject front = getFront(nbt.getUUID("uuid"));
                if (front != null)
                    front.readNBT(nbt);
                break;
            }
            case 13: {
                UUID uuid = mainNBT.getUUID("frontUUID");
                FrontObject front = getFront(uuid);
                if (front != null)
                    if (globalFront.equals(front))
                        globalFront.reset();
                    else {
                        front.reset();
                        removeFront(front);
                    }
                else
                    Weather2.error("error removing front, cant find by ID: " + uuid);
                break;
            }
            case 14: {
                fronts.forEach((uuid, front) -> front.cleanupClient(false));
                weatherParticles.forEach(Particle::remove);
                weatherParticles.clear();
                Weather2.debug("Cleaned up client particles");
                break;
            }
            default:
                Weather2.error("Server sent an invalid network packet");
        }
    }

    public float getRainTargetValue(Vec3 position) {
        float rainTarget = -Float.MAX_VALUE, rain;
        List<WeatherObject> systems = new ArrayList<WeatherObject>(this.systems.values());

        for (WeatherObject system : systems) {
            if (system instanceof IWeatherRain) {
                rain = ((IWeatherRain) system).getDownfall(position) - IWeatherRain.MINIMUM_DRIZZLE;
                if (rain > rainTarget)
                    rainTarget = rain;
            }
        }
        return Maths.clamp(rainTarget / IWeatherRain.MINIMUM_HEAVY_RAIN, 0.0F, 1.0F);
    }

    public float getOvercastTargetValue(Vec3 position) {
        float overcastTarget = -Float.MAX_VALUE, overcast;
        List<WeatherObject> systems = new ArrayList<WeatherObject>(this.systems.values());

        for (WeatherObject system : systems) {
            if (system instanceof IWeatherStaged) {
                float distance = (float) Maths.distanceSq(position.posX, position.posZ, system.pos.posX, system.pos.posZ);
                float distanceMult = 1.0F - Math.min(Math.max(distance - system.size, 0.0F) / (system.size * 0.25F), 1.0F);
                float stageMult = 0.25F + Math.min(system.getStage() * 0.25F, 0.5F) + (system instanceof StormObject && ((StormObject) system).isViolent ? 0.25F : 0.0F);
                overcast = stageMult * distanceMult;
                if (overcast > overcastTarget)
                    overcastTarget = overcast;
            }
        }

        return Maths.clamp(overcastTarget, 0.0F, 1.0F);
    }

    public void addWeatherParticle(Particle particle) {
        weatherParticles.add(particle);
    }

    public void addEffectedParticle(Particle particle) {
        effectedParticles.add(particle);
    }

    public int getParticleCount() {
        return weatherParticles.size();
    }

    public void refreshParticleLimit() {
        int systems = 0;
        final ClientPlayerEntity player = MC.player;
        if (player == null) return;
        for (WeatherObject system : this.systems.values())
            if (system.pos.distanceSq(player.getX(), system.pos.posY, player.getZ()) < NewSceneEnhancer.instance().renderDistance)
                systems++;

        if (systems == 0) systems = 1;

        particleLimit = ConfigClient.max_particles > 0 ? ConfigClient.max_particles / systems : Integer.MAX_VALUE;
        this.systems.forEach((uuid, system) ->
        {
            if (system instanceof StormObject && ((StormObject) system).particleRenderer != null)
                ((StormObject) system).particleRenderer.refreshParticleLimit();
        });
    }

    public int getParticleLimit() {
        return particleLimit;
    }

    @Override
    public void reset(boolean fullReset) {
        weatherParticles.forEach(Particle::remove);
        weatherParticles.clear();
        super.reset(fullReset);
        effectedParticles.clear();
        closestStormCached = null;

        Weather2.debug("Weather2: WeatherManagerClient reset complete");
    }


    public float getRainTarget(Vec3 pos, float maxDistSq) {
        float rainTarget = 0.0F;
        StormObject storm = getStrongestClosestStormWithRain(pos, maxDistSq);
        if (storm != null) {
            float rain = storm.getDownfall(pos) - IWeatherRain.MINIMUM_DRIZZLE;
            if (rain > rainTarget) rainTarget = rain;
        }
        return Maths.clamp(rainTarget / IWeatherRain.MINIMUM_HEAVY_RAIN, 0.0F, 1.0F);
    }


    public float getOvercastTarget(Vec3 pos, float maxDistSq) {
        float overcastTarget = 0.0F;
        StormObject storm = getStrongestClosestStorm(pos, maxDistSq);
        if (storm != null) {
            float distance = (float) Maths.distanceSq(pos.posX, pos.posZ, storm.pos.posX, storm.pos.posZ);
            float distanceMult = 1.0F - Math.min(Math.max(distance - storm.size, 0.0F) / (storm.size * 0.25F), 1.0F);
            float stageMult = 0.25F + Math.min(storm.getStage() * 0.25F, 0.5F) + (storm.isViolent ? 0.25F : 0.0F);
            float overcast = stageMult * distanceMult;
            if (overcast > overcastTarget) overcastTarget = overcast;
        }
        return overcastTarget;
    }
}