package net.mrbt0907.weather2.util;

import net.minecraft.nbt.CompoundNBT;

import java.util.UUID;


public class CachedNBTTagCompound {
    private CompoundNBT newData;
    private CompoundNBT cachedData;
    private boolean forced = false;

    public CachedNBTTagCompound() {
        this.newData = new CompoundNBT();
        this.cachedData = new CompoundNBT();
    }

    public CompoundNBT getCachedNBT() {
        return cachedData;
    }

    public void setCachedNBT(CompoundNBT cachedData) {
        if (cachedData == null)
            cachedData = new CompoundNBT();
        this.cachedData = cachedData;
    }

    public CompoundNBT getNewNBT() {
        return newData;
    }

    public void setNewNBT(CompoundNBT newData) {
        this.newData = newData;
    }

    public void setUpdateForced(boolean forced) {
        this.forced = forced;
    }

    public long getLong(String key) {
        if (!newData.contains(key))
            newData.putLong(key, cachedData.getLong(key));
        return newData.getLong(key);
    }

    public void setLong(String key, long newVal) {
        if (!cachedData.contains(key) || cachedData.getLong(key) != newVal || forced) {
            newData.putLong(key, newVal);
        }
        cachedData.putLong(key, newVal);
    }

    public int getInteger(String key) {
        if (!newData.contains(key))
            newData.putInt(key, cachedData.getInt(key));
        return newData.getInt(key);
    }

    public void setInteger(String key, int newVal) {
        if (!cachedData.contains(key) || cachedData.getInt(key) != newVal || forced) {
            newData.putInt(key, newVal);
        }
        cachedData.putInt(key, newVal);
    }

    public short getShort(String key) {
        if (!newData.contains(key))
            newData.putShort(key, cachedData.getShort(key));
        return newData.getShort(key);
    }

    public void setShort(String key, short newVal) {
        if (!cachedData.contains(key) || cachedData.getShort(key) != newVal || forced) {
            newData.putShort(key, newVal);
        }
        cachedData.putShort(key, newVal);
    }

    public String getString(String key) {
        if (!newData.contains(key))
            newData.putString(key, cachedData.getString(key));
        return newData.getString(key);
    }

    public void setString(String key, String newVal) {
        if (!cachedData.contains(key) || cachedData.getString(key) != newVal || forced) {
            newData.putString(key, newVal);
        }
        cachedData.putString(key, newVal);
    }

    public boolean getBoolean(String key) {
        if (!newData.contains(key))
            newData.putBoolean(key, cachedData.getBoolean(key));
        return newData.getBoolean(key);
    }

    public void setBoolean(String key, boolean newVal) {
        if (!cachedData.contains(key) || cachedData.getBoolean(key) != newVal || forced) {
            newData.putBoolean(key, newVal);
        }
        cachedData.putBoolean(key, newVal);
    }

    public float getFloat(String key) {
        if (!newData.contains(key))
            newData.putFloat(key, cachedData.getFloat(key));
        return newData.getFloat(key);
    }

    public void setFloat(String key, float newVal) {
        if (!cachedData.contains(key) || cachedData.getFloat(key) != newVal || forced) {
            newData.putFloat(key, newVal);
        }
        cachedData.putFloat(key, newVal);
    }

    public double getDouble(String key) {
        if (!newData.contains(key))
            newData.putDouble(key, cachedData.getDouble(key));
        return newData.getDouble(key);
    }

    public void setDouble(String key, double newVal) {
        if (!cachedData.contains(key) || cachedData.getDouble(key) != newVal || forced) {
            newData.putDouble(key, newVal);
        }
        cachedData.putDouble(key, newVal);
    }

    public UUID getUUID(String key) {
        if (!newData.contains(key)) {

            if (cachedData.hasUUID(key)) {
                newData.putUUID(key, cachedData.getUUID(key));
            } else {

                return null;
            }
        }
        return newData.getUUID(key);
    }

    public void setUUID(String key, UUID newVal) {
        if (!cachedData.contains(key) || cachedData.getUUID(key) != newVal || forced)
            newData.putUUID(key, newVal);
        cachedData.putUUID(key, newVal);
    }

    public void updateCacheFromNew() {
        this.cachedData = this.newData;
    }

}