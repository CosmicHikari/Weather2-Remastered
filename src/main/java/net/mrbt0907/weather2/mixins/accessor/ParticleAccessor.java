package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {


    @Accessor("x")
    double getX();

    @Accessor("x")
    void setX(double value);

    @Accessor("y")
    double getY();

    @Accessor("y")
    void setY(double value);

    @Accessor("z")
    double getZ();

    @Accessor("z")
    void setZ(double value);


    @Accessor("xo")
    double getXo();

    @Accessor("xo")
    void setXo(double value);

    @Accessor("yo")
    double getYo();

    @Accessor("yo")
    void setYo(double value);

    @Accessor("zo")
    double getZo();

    @Accessor("zo")
    void setZo(double value);


    @Accessor("xd")
    double getXd();

    @Accessor("xd")
    void setXd(double value);

    @Accessor("yd")
    double getYd();

    @Accessor("yd")
    void setYd(double value);

    @Accessor("zd")
    double getZd();

    @Accessor("zd")
    void setZd(double value);

    @Accessor("level")
    net.minecraft.client.world.ClientWorld getLevel();


    @Accessor("bbWidth")
    float getBbWidth();

    @Accessor("bbWidth")
    void setBbWidth(float value);

    @Accessor("bbHeight")
    float getBbHeight();

    @Accessor("bbHeight")
    void setBbHeight(float value);

    @Accessor("removed")
    boolean isRemoved();

    @Accessor("removed")
    boolean getRemoved();

    @Accessor("removed")
    void setRemoved(boolean value);

    @Accessor("age")
    int getAge();

    @Accessor("age")
    void setAge(int age);

    @Accessor("lifetime")
    int getLifetime();

}
