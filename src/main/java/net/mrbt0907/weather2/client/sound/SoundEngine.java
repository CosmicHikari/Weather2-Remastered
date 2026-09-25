package net.mrbt0907.weather2.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SoundEngine {

    private static final int MAX_SOUNDS = 16;
    private static final Map<String, SoundInstance> activeSounds = new ConcurrentHashMap<>();

    private SoundEngine() {
    }

    public static void tick() {
        activeSounds.entrySet().removeIf(e -> {
            SoundInstance inst = e.getValue();
            return inst.isStopped();
        });
    }

    public static void playStatic(String key, SoundEvent sound, SoundCategory category,
                                  int priority, float volume, float pitch) {
        play(key, null, sound, category, priority, volume, pitch, 0);
    }

    public static void playMovingSound(String key, Object source, SoundEvent sound,
                                       SoundCategory category, int priority,
                                       float volume, float pitch, double range) {
        play(key, source, sound, category, priority, volume, pitch, range);
    }

    public static void setVolume(String key, float volume) {
        SoundInstance inst = activeSounds.get(key);
        if (inst != null && !inst.isStopped()) inst.setMaxVolume(volume);
    }

    public static void stop(String key) {
        SoundInstance inst = activeSounds.remove(key);
        if (inst != null) inst.kill();
    }

    public static void stopAll() {
        activeSounds.values().forEach(SoundInstance::kill);
        activeSounds.clear();
    }

    public static boolean isActive(String key) {
        SoundInstance inst = activeSounds.get(key);
        return inst != null && !inst.isStopped();
    }

    private static void play(String key, Object source, SoundEvent sound,
                             SoundCategory category, int priority,
                             float volume, float pitch, double range) {
        SoundInstance existing = activeSounds.get(key);

        if (existing != null && !existing.isStopped()) {
            if (existing.getLocation().equals(sound.getLocation())) {
                existing.setMaxVolume(volume);
                return;
            }
            existing.kill();
        }

        if (activeSounds.size() >= MAX_SOUNDS) {
            String lowestKey = null;
            int lowestPriority = Integer.MAX_VALUE;
            for (Map.Entry<String, SoundInstance> e : activeSounds.entrySet()) {
                int p = e.getValue().getPriority();
                if (p < lowestPriority) {
                    lowestPriority = p;
                    lowestKey = e.getKey();
                }
            }

            if (lowestKey != null && lowestPriority < priority) {
                stop(lowestKey);
            } else {
                return;
            }
        }

        SoundInstance inst = new SoundInstance(
                sound, category, volume, pitch, source, range, priority
        );
        Minecraft.getInstance().getSoundManager().play(inst);
        activeSounds.put(key, inst);
    }
}