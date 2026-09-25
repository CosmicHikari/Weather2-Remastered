package net.mrbt0907.weather2.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

public class SoundInstance extends TickableSound {
    private static final Minecraft MC = Minecraft.getInstance();
    private final Object source;
    private final double range;
    private final int priority;
    private float maxVolume;
    private volatile boolean killed;

    public SoundInstance(SoundEvent sound, SoundCategory category,
                         float volume, float pitch,
                         Object source, double range, int priority) {
        super(sound, category);
        this.source = source;
        this.maxVolume = Maths.clamp(volume, 0.0f, 1.0f);
        this.range = range;
        this.priority = priority;
        this.looping = true;
        this.delay = 0;
        this.relative = false;
        this.attenuation = AttenuationType.NONE;
        this.pitch = Maths.clamp(pitch, 0.5f, 2.0f);
        this.volume = 0.0f;

        if (MC.player != null) {
            this.x = MC.player.getX();
            this.y = MC.player.getY();
            this.z = MC.player.getZ();
        }
    }

    public void setMaxVolume(float v) {
        this.maxVolume = Maths.clamp(v, 0.0f, 1.0f);
    }

    public int getPriority() {
        return priority;
    }

    public void kill() {
        killed = true;
        stop();
    }

    @Override
    public void tick() {
        if (killed || MC.player == null || MC.level == null) {
            stop();
            return;
        }
        updateVolumeAndPosition();
    }

    private void updateVolumeAndPosition() {
        double px = MC.player.getX();
        double py = MC.player.getY();
        double pz = MC.player.getZ();

        if (source == null) {
            this.x = px;
            this.y = py;
            this.z = pz;
            this.volume = maxVolume;
            return;
        }

        Vec3 pos = getSourcePos();
        if (pos == null) {
            stop();
            return;
        }

        if (range > 0) {
            double dist = Math.sqrt(pos.distanceSq(px, py, pz));
            this.volume = maxVolume * (float) Maths.clamp((range - dist) / range, 0.0, 1.0);
        } else {
            this.volume = maxVolume;
        }

        this.x = Maths.clamp(pos.posX, px - 6.0, px + 6.0);
        this.y = Maths.clamp(pos.posY, py - 6.0, py + 6.0);
        this.z = Maths.clamp(pos.posZ, pz - 6.0, pz + 6.0);
    }

    @Override
    public boolean canPlaySound() {
        return !isStopped() && !killed;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    private Vec3 getSourcePos() {
        if (source instanceof Vec3) return (Vec3) source;
        if (source instanceof BlockPos) return new Vec3((BlockPos) source);
        if (source instanceof WeatherObject) return ((WeatherObject) source).pos;
        if (source instanceof Entity) {
            Entity e = (Entity) source;
            return e.isAlive() ? new Vec3(e.getX(), e.getY(), e.getZ()) : null;
        }
        return null;
    }
}