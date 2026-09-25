package net.corosus.coroutillegacy.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class Vec3 {

    private static final String __OBFID = "CL_00000612";
    public double xCoord;
    public double yCoord;
    public double zCoord;


    public Vec3(Vec3 pos) {
        this(pos.xCoord, pos.yCoord, pos.zCoord);
    }

    public Vec3(Vector3d pos) {
        this(pos.x, pos.y, pos.z);
    }

    public Vec3(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vec3(double p_i1108_1_, double p_i1108_3_, double p_i1108_5_) {
        if (p_i1108_1_ == -0.0D) {
            p_i1108_1_ = 0.0D;
        }

        if (p_i1108_3_ == -0.0D) {
            p_i1108_3_ = 0.0D;
        }

        if (p_i1108_5_ == -0.0D) {
            p_i1108_5_ = 0.0D;
        }

        this.xCoord = p_i1108_1_;
        this.yCoord = p_i1108_3_;
        this.zCoord = p_i1108_5_;
    }


    protected Vec3 setComponents(double p_72439_1_, double p_72439_3_, double p_72439_5_) {
        this.xCoord = p_72439_1_;
        this.yCoord = p_72439_3_;
        this.zCoord = p_72439_5_;
        return this;
    }


    public Vec3 subtract(Vec3 p_72444_1_) {

        return new Vec3(p_72444_1_.xCoord - this.xCoord, p_72444_1_.yCoord - this.yCoord, p_72444_1_.zCoord - this.zCoord);
    }


    public Vec3 normalize() {
        double d0 = MathHelper.sqrt(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
        return d0 < 1.0E-4D ? new Vec3(0.0D, 0.0D, 0.0D) : new Vec3(this.xCoord / d0, this.yCoord / d0, this.zCoord / d0);
    }

    public double dotProduct(Vec3 p_72430_1_) {
        return this.xCoord * p_72430_1_.xCoord + this.yCoord * p_72430_1_.yCoord + this.zCoord * p_72430_1_.zCoord;
    }


    public Vec3 crossProduct(Vec3 p_72431_1_) {

        return new Vec3(this.yCoord * p_72431_1_.zCoord - this.zCoord * p_72431_1_.yCoord, this.zCoord * p_72431_1_.xCoord - this.xCoord * p_72431_1_.zCoord, this.xCoord * p_72431_1_.yCoord - this.yCoord * p_72431_1_.xCoord);
    }


    public Vec3 addVector(double p_72441_1_, double p_72441_3_, double p_72441_5_) {

        return new Vec3(this.xCoord + p_72441_1_, this.yCoord + p_72441_3_, this.zCoord + p_72441_5_);
    }


    public double distanceTo(Vec3 p_72438_1_) {
        double d0 = p_72438_1_.xCoord - this.xCoord;
        double d1 = p_72438_1_.yCoord - this.yCoord;
        double d2 = p_72438_1_.zCoord - this.zCoord;
        return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }


    public double squareDistanceTo(Vec3 p_72436_1_) {
        double d0 = p_72436_1_.xCoord - this.xCoord;
        double d1 = p_72436_1_.yCoord - this.yCoord;
        double d2 = p_72436_1_.zCoord - this.zCoord;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }


    public double squareDistanceTo(double p_72445_1_, double p_72445_3_, double p_72445_5_) {
        double d3 = p_72445_1_ - this.xCoord;
        double d4 = p_72445_3_ - this.yCoord;
        double d5 = p_72445_5_ - this.zCoord;
        return d3 * d3 + d4 * d4 + d5 * d5;
    }


    public double lengthVector() {
        return MathHelper.sqrt(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
    }


    public Vec3 getIntermediateWithXValue(Vec3 p_72429_1_, double p_72429_2_) {
        double d1 = p_72429_1_.xCoord - this.xCoord;
        double d2 = p_72429_1_.yCoord - this.yCoord;
        double d3 = p_72429_1_.zCoord - this.zCoord;

        if (d1 * d1 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d4 = (p_72429_2_ - this.xCoord) / d1;
            return d4 >= 0.0D && d4 <= 1.0D ? new Vec3(this.xCoord + d1 * d4, this.yCoord + d2 * d4, this.zCoord + d3 * d4) : null;
        }
    }


    public Vec3 getIntermediateWithYValue(Vec3 p_72435_1_, double p_72435_2_) {
        double d1 = p_72435_1_.xCoord - this.xCoord;
        double d2 = p_72435_1_.yCoord - this.yCoord;
        double d3 = p_72435_1_.zCoord - this.zCoord;

        if (d2 * d2 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d4 = (p_72435_2_ - this.yCoord) / d2;
            return d4 >= 0.0D && d4 <= 1.0D ? new Vec3(this.xCoord + d1 * d4, this.yCoord + d2 * d4, this.zCoord + d3 * d4) : null;
        }
    }


    public Vec3 getIntermediateWithZValue(Vec3 p_72434_1_, double p_72434_2_) {
        double d1 = p_72434_1_.xCoord - this.xCoord;
        double d2 = p_72434_1_.yCoord - this.yCoord;
        double d3 = p_72434_1_.zCoord - this.zCoord;

        if (d3 * d3 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d4 = (p_72434_2_ - this.zCoord) / d3;
            return d4 >= 0.0D && d4 <= 1.0D ? new Vec3(this.xCoord + d1 * d4, this.yCoord + d2 * d4, this.zCoord + d3 * d4) : null;
        }
    }

    public String toString() {
        return "(" + this.xCoord + ", " + this.yCoord + ", " + this.zCoord + ")";
    }


    public void rotateAroundX(float p_72440_1_) {
        float f1 = MathHelper.cos(p_72440_1_);
        float f2 = MathHelper.sin(p_72440_1_);
        double d0 = this.xCoord;
        double d1 = this.yCoord * (double) f1 + this.zCoord * (double) f2;
        double d2 = this.zCoord * (double) f1 - this.yCoord * (double) f2;
        this.setComponents(d0, d1, d2);
    }


    public void rotateAroundY(float p_72442_1_) {
        float f1 = MathHelper.cos(p_72442_1_);
        float f2 = MathHelper.sin(p_72442_1_);
        double d0 = this.xCoord * (double) f1 + this.zCoord * (double) f2;
        double d1 = this.yCoord;
        double d2 = this.zCoord * (double) f1 - this.xCoord * (double) f2;
        this.setComponents(d0, d1, d2);
    }


    public void rotateAroundZ(float p_72446_1_) {
        float f1 = MathHelper.cos(p_72446_1_);
        float f2 = MathHelper.sin(p_72446_1_);
        double d0 = this.xCoord * (double) f1 + this.yCoord * (double) f2;
        double d1 = this.yCoord * (double) f1 - this.xCoord * (double) f2;
        double d2 = this.zCoord;
        this.setComponents(d0, d1, d2);
    }

    public net.minecraft.util.math.vector.Vector3d toMCVec() {
        return new net.minecraft.util.math.vector.Vector3d(this.xCoord, this.yCoord, this.zCoord);
    }

    public BlockPos toBlockPos() {
        return new BlockPos(this.xCoord, this.yCoord, this.zCoord);
    }
}