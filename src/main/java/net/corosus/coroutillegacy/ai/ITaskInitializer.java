package net.corosus.coroutillegacy.ai;

import net.minecraft.entity.CreatureEntity;

public interface ITaskInitializer {

    default void setEntity(CreatureEntity creature) {
    }
}