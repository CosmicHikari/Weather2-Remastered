package net.corosus.coroutillegacy.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.mrbt0907.weather2.mixins.accessor.ParticleAccessor;

class CoroUtilParticleProxy {
    static double getPosXParticle(Object obj) {
        return ((ParticleAccessor) obj).getX();
    }

    static double getPosYParticle(Object obj) {
        return ((ParticleAccessor) obj).getY();
    }

    static double getPosZParticle(Object obj) {
        return ((ParticleAccessor) obj).getZ();
    }

    static double getMotionXParticle(Object obj) {
        return ((ParticleAccessor) obj).getXd();
    }

    static double getMotionYParticle(Object obj) {
        return ((ParticleAccessor) obj).getYd();
    }

    static double getMotionZParticle(Object obj) {
        return ((ParticleAccessor) obj).getZd();
    }

    static void setMotionXParticle(Object obj, double val) {
        ((ParticleAccessor) obj).setXd(val);
    }

    static void setMotionYParticle(Object obj, double val) {
        ((ParticleAccessor) obj).setYd(val);
    }

    static void setMotionZParticle(Object obj, double val) {
        ((ParticleAccessor) obj).setZd(val);
    }

    static World getWorldParticle(Object obj) {
        return ((ParticleAccessor) obj).getLevel();
    }

    static void setPosXParticle(Object obj, double val) {
        ((ParticleAccessor) obj).setX(val);
    }

    static void setPosYParticle(Object obj, double val) {
        ((ParticleAccessor) obj).setY(val);
    }

    static void setPosZParticle(Object obj, double val) {
        ((ParticleAccessor) obj).setZ(val);
    }
}

public class CoroUtilEntOrParticle {

    public static double getPosX(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity) obj).getX();
        } else {
            return CoroUtilParticleProxy.getPosXParticle(obj);
        }
    }

    public static double getPosY(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity) obj).getY();
        } else {
            return CoroUtilParticleProxy.getPosYParticle(obj);
        }
    }

    public static double getPosZ(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity) obj).getZ();
        } else {
            return CoroUtilParticleProxy.getPosZParticle(obj);
        }
    }

    public static double getMotionX(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity) obj).getDeltaMovement().x;
        } else {
            return CoroUtilParticleProxy.getMotionXParticle(obj);
        }
    }

    public static double getMotionY(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity) obj).getDeltaMovement().y;
        } else {
            return CoroUtilParticleProxy.getMotionYParticle(obj);
        }
    }

    public static double getMotionZ(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity) obj).getDeltaMovement().z;
        } else {
            return CoroUtilParticleProxy.getMotionZParticle(obj);
        }
    }

    public static void setMotionX(Object obj, double val) {
        if (obj instanceof Entity) {
            Entity ent = (Entity) obj;
            Vector3d motion = ent.getDeltaMovement();
            ent.setDeltaMovement(val, motion.y, motion.z);
        } else {
            CoroUtilParticleProxy.setMotionXParticle(obj, val);
        }
    }

    public static void setMotionY(Object obj, double val) {
        if (obj instanceof Entity) {
            Entity ent = (Entity) obj;
            Vector3d motion = ent.getDeltaMovement();
            ent.setDeltaMovement(motion.x, val, motion.z);
        } else {
            CoroUtilParticleProxy.setMotionYParticle(obj, val);
        }
    }

    public static void setMotionZ(Object obj, double val) {
        if (obj instanceof Entity) {
            Entity ent = (Entity) obj;
            Vector3d motion = ent.getDeltaMovement();
            ent.setDeltaMovement(motion.x, motion.y, val);
        } else {
            CoroUtilParticleProxy.setMotionZParticle(obj, val);
        }
    }

    public static World getWorld(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity) obj).level;
        } else {
            return CoroUtilParticleProxy.getWorldParticle(obj);
        }
    }

    public static double getDistance(Object obj, double x, double y, double z) {
        double d0 = getPosX(obj) - x;
        double d1 = getPosY(obj) - y;
        double d2 = getPosZ(obj) - z;
        return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public static void setPosX(Object obj, double val) {
        if (obj instanceof Entity) {
            Entity ent = (Entity) obj;
            ent.setPos(val, ent.getY(), ent.getZ());
        } else {
            CoroUtilParticleProxy.setPosXParticle(obj, val);
        }
    }

    public static void setPosY(Object obj, double val) {
        if (obj instanceof Entity) {
            Entity ent = (Entity) obj;
            ent.setPos(ent.getX(), val, ent.getZ());
        } else {
            CoroUtilParticleProxy.setPosYParticle(obj, val);
        }
    }

    public static void setPosZ(Object obj, double val) {
        if (obj instanceof Entity) {
            Entity ent = (Entity) obj;
            ent.setPos(ent.getX(), ent.getY(), val);
        } else {
            CoroUtilParticleProxy.setPosZParticle(obj, val);
        }
    }

}