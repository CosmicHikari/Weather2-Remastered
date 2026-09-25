package net.mrbt0907.weather2.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.mrbt0907.weather2.config.ConfigGrab;

import java.util.Random;


public class Maths {
    public static final double QTR_PI = Math.PI / 4.0D;
    public static final double THREE_QTR_PI = 3.0D * Math.PI / 4.0D;
    private static final double[] SIN_TABLE = new double[65536];
    private static final double[] ASIN_TABLE = new double[65536];
    private static final Random random = new Random();

    static {
        for (int i = 0; i < 65536; i++)
            ASIN_TABLE[i] = (float) Math.asin(i / 32767.5D - 1.0D);
        for (int i = -1; i < 2; i++)
            ASIN_TABLE[(int) ((i + 1.0D) * 32767.5D) & 0xFFFF] = (float) Math.asin(i);

        for (int i = 0; i < 65536; ++i)
            SIN_TABLE[i] = Math.sin(i * Math.PI * 2.0D / 65536.0D);
    }


    public static int clamp(int input, int min, int max) {
        return min > max ? min : input > min ? input < max ? input : max : min;
    }


    public static float clamp(float input, float min, float max) {
        return min > max ? min : input > min ? input < max ? input : max : min;
    }


    public static double clamp(double input, double min, double max) {
        return min > max ? min : input > min ? input < max ? input : max : min;
    }


    public static boolean chance() {
        return random(1) == 0;
    }


    public static boolean chance(int chance) {
        return random(0, 100) <= chance;
    }


    public static boolean chance(float chance) {
        return random(0.0F, 1.0F) <= chance;
    }


    public static boolean chance(double chance) {
        return random(0.0D, 1.0D) <= chance;
    }


    public static int random(int integerA) {
        return random(0, integerA);
    }


    public static int random(int integerA, int integerB) {
        return integerA >= integerB ? integerA : random.nextInt(integerB + 1 - integerA) + integerA;
    }


    public static float random(float floatA) {
        return random(0.0F, floatA);
    }


    public static float random(float floatA, float floatB) {
        return floatA >= floatB ? floatA : (random.nextFloat() * (floatB - floatA)) + floatA;
    }


    public static double random(double doubleA) {
        return random(0, doubleA);
    }


    public static double random(double doubleA, double doubleB) {
        return doubleA >= doubleB ? doubleA : (random.nextDouble() * (doubleB - doubleA)) + doubleA;
    }


    public static double distance(double posX, double posY, double posZ, Vec3 vector) {
        return distance(posX, posY, posZ, vector.posX, vector.posY, vector.posZ);
    }


    public static double distance(double posXA, double posYA, double posZA, double posXB, double posYB, double posZB) {
        return (posXA - posXB) * (posXA - posXB) + (posYA - posYB) * (posYA - posYB) + (posZA - posZB) * (posZA - posZB);
    }


    public static double distance(double posXA, double posZA, double posXB, double posZB) {
        return (posXA - posXB) * (posXA - posXB) + (posZA - posZB) * (posZA - posZB);
    }


    public static double distance(double posX, double posZ, Vec vector) {
        return distance(posX, posZ, vector.posX, vector.posZ);
    }


    public static double distanceSq(double posX, double posY, double posZ, Vec3 vector) {
        return distanceSq(posX, posY, posZ, vector.posX, vector.posY, vector.posZ);
    }


    public static double distanceSq(double posXA, double posYA, double posZA, double posXB, double posYB, double posZB) {
        return Math.sqrt(distance(posXA, posYA, posZA, posXB, posYB, posZB));
    }


    public static double distanceSq(double posXA, double posZA, double posXB, double posZB) {
        return Math.sqrt(distance(posXA, posZA, posXB, posZB));
    }


    public static double distanceSq(double posX, double posZ, Vec vector) {
        return distanceSq(posX, posZ, vector.posX, vector.posZ);
    }

    public static double speed(double motionX, double motionZ) {
        return motionX * motionX + motionZ * motionZ;
    }

    public static double speed(double motionX, double motionY, double motionZ) {
        return motionX * motionX + motionY * motionY + motionZ * motionZ;
    }

    public static double speedSq(double motionX, double motionZ) {
        return Math.sqrt(speed(motionX, motionZ));
    }

    public static double speedSq(double motionX, double motionY, double motionZ) {
        return Math.sqrt(speed(motionX, motionY, motionZ));
    }


    public static int adjust(int current, int target, int adjustment) {
        if (current > target) {
            current -= adjustment;
            if (current < target)
                return target;
        } else if (current < target) {
            current += adjustment;
            if (current > target)
                return target;
        }

        return current;
    }


    public static float adjust(float current, float target, float adjustment) {
        if (current > target) {
            current -= adjustment;
            if (current < target)
                return target;
        } else if (current < target) {
            current += adjustment;
            if (current > target)
                return target;
        }

        return current;
    }


    public static double adjust(double current, double target, double adjustment) {
        if (current > target) {
            current -= adjustment;
            if (current < target)
                return target;
        } else if (current < target) {
            current += adjustment;
            if (current > target)
                return target;
        }

        return current;
    }

    public static int wrapDegrees(int degrees) {
        return degrees < 0 || degrees > 359 ? degrees % 360 : degrees;
    }

    public static float wrapDegrees(float degrees) {
        return degrees < 0.0F || degrees > 359.0F ? degrees % 360.0F : degrees;
    }

    public static double wrapDegrees(double degrees) {
        return degrees < 0.0D || degrees > 359.0D ? degrees % 360.0D : degrees;
    }

    public static double fastATan2(double y, double x) {
        if (ConfigGrab.disableCheapOptimizations)
            return Math.atan2(y, x);

        double angle, radius, abs = Math.abs(y) + Double.MIN_VALUE;

        if (x < 0.0D) {
            radius = (x + abs) / (abs - x);
            angle = THREE_QTR_PI;
        } else {
            radius = (x - abs) / (x + abs);
            angle = QTR_PI;
        }

        angle += (0.1963D * radius * radius - 0.9817D) * radius;
        return y < 0.0D ? -angle : angle;
    }

    public static final double fastCos(double x) {
        return ConfigGrab.disableCheapOptimizations ? Math.cos(x) : SIN_TABLE[(int) (x * 10430.378D + 16384.0D) & 0xFFFF];
    }

    public static double fastSin(double x) {
        return ConfigGrab.disableCheapOptimizations ? Math.sin(x) : SIN_TABLE[(int) (x * 10430.378D) & 0xFFFF];
    }

    public static double fastASin(double x) {
        return ConfigGrab.disableCheapOptimizations ? Math.asin(x) : ASIN_TABLE[(int) ((x + 1.0D) * 32767.5D) & 0xFFFF];
    }

    public static double fastACos(double x) {
        return ConfigGrab.disableCheapOptimizations ? Math.acos(x) : 1.5707964D - ASIN_TABLE[(int) ((x + 1.0D) * 32767.5D) & 0xFFFF];
    }


    public static class Vec {

        public double posX = 0.0D;

        public double posZ = 0.0D;


        public Vec() {
        }

        public Vec(net.corosus.coroutillegacy.util.Vec3 pos) {
            this.posX = pos.xCoord;
            this.posZ = pos.zCoord;
        }

        public Vec(BlockPos pos) {
            this.posX = pos.getX();
            this.posZ = pos.getZ();
        }

        public Vec(int posX, int posZ) {
            this.posX = posX;
            this.posZ = posZ;
        }

        public Vec(float posX, float posZ) {
            this.posX = posX;
            this.posZ = posZ;
        }

        public Vec(double posX, double posZ) {
            this.posX = posX;
            this.posZ = posZ;
        }

        public Vec copy() {
            return new Vec(posX, posZ);
        }

        public BlockPos toBlockPos(double posY) {
            return new BlockPos(posX, posY, posZ);
        }

        public net.corosus.coroutillegacy.util.Vec3 toVec3Coro() {
            return new net.corosus.coroutillegacy.util.Vec3(posX, 0.0D, posZ);
        }

        public Vector3d toVec3MC() {
            return new Vector3d(posX, 0.0D, posZ);
        }

        public Vec addVector(double x, double z) {
            posX += x;
            posZ += z;
            return this;
        }


        public double distance(double posX, double posZ) {
            return Maths.distance(this.posX, this.posZ, posX, posZ);
        }


        public double distance(Vec vector) {
            return distance(vector.posX, vector.posZ);
        }


        public double distance(Vec3 vector) {
            return distance(vector.posX, vector.posZ);
        }


        public double distanceSq(double posX, double posZ) {
            return Maths.distanceSq(this.posX, this.posZ, posX, posZ);
        }


        public double distanceSq(Vec vector) {
            return distanceSq(vector.posX, vector.posZ);
        }


        public double distanceSq(Vec3 vector) {
            return distanceSq(vector.posX, vector.posZ);
        }


        public double speed() {
            return Maths.speed(posX, posZ);
        }


        public double speedSq() {
            return Maths.speedSq(posX, posZ);
        }
    }


    public static class Vec3 {

        public double posX = 0.0D;

        public double posY = 0.0D;

        public double posZ = 0.0D;


        public Vec3() {
        }


        public Vec3(Vector3d pos) {
            posX = pos.x;
            posY = pos.y;
            posZ = pos.z;
        }

        public Vec3(net.corosus.coroutillegacy.util.Vec3 pos) {
            posX = pos.xCoord;
            posY = pos.yCoord;
            posZ = pos.zCoord;
        }

        public Vec3(BlockPos pos) {
            posX = pos.getX();
            posY = pos.getY();
            posZ = pos.getZ();
        }

        public Vec3(int posX, int posY, int posZ) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }

        public Vec3(float posX, float posY, float posZ) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }

        public Vec3(double posX, double posY, double posZ) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }

        public BlockPos toBlockPos() {
            return new BlockPos(posX, posY, posZ);
        }

        public net.corosus.coroutillegacy.util.Vec3 toVec3Coro() {
            return new net.corosus.coroutillegacy.util.Vec3(posX, posY, posZ);
        }

        public Vector3d toVec3MC() {
            return new Vector3d(posX, posY, posZ);
        }

        public Vec3 copy() {
            return new Vec3(posX, posY, posZ);
        }

        public Vec3 addVector(double x, double y, double z) {
            posX += x;
            posY += y;
            posZ += z;
            return this;
        }


        public double distance(double posX, double posY, double posZ) {
            return Maths.distance(this.posX, this.posY, this.posZ, posX, posY, posZ);
        }


        public double distance(Vec vector) {
            return distance(vector.posX, posY, vector.posZ);
        }


        public double distance(Vec3 vector) {
            return distance(vector.posX, vector.posY, vector.posZ);
        }


        public double distanceSq(double posX, double posY, double posZ) {
            return Maths.distanceSq(this.posX, this.posY, this.posZ, posX, posY, posZ);
        }


        public double distanceSq(Vec vector) {
            return distanceSq(vector.posX, posY, vector.posZ);
        }


        public double distanceSq(Vec3 vector) {
            return distanceSq(vector.posX, vector.posY, vector.posZ);
        }


        public double distanceSq(Vector3d vector) {
            return distanceSq(vector.x, vector.y, vector.z);
        }


        public double speed() {
            return Maths.speed(posX, posY, posZ);
        }


        public double speedSq() {
            return Maths.speedSq(posX, posY, posZ);
        }
    }
}