package net.corosus.extendedrenderer.particle.entity;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.corosus.coroutillegacy.api.weather.IWindHandler;
import net.corosus.coroutillegacy.util.CoroUtilBlockLightCache;
import net.corosus.coroutillegacy.util.CoroUtilMath;
import net.corosus.coroutillegacy.util.Vec3;
import net.corosus.extendedrenderer.ExtendedRenderer;
import net.corosus.extendedrenderer.particle.behavior.ParticleBehaviors;
import net.corosus.extendedrenderer.render.RotatingParticleManager;
import net.corosus.extendedrenderer.shader.IShaderRenderedEntity;
import net.corosus.extendedrenderer.shader.InstancedMeshParticle;
import net.corosus.extendedrenderer.shader.Matrix4fe;
import net.corosus.extendedrenderer.shader.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class EntityRotFX extends SpriteTexturedParticle implements IWindHandler, IShaderRenderedEntity {

    public static double interpPosX;
    public static double interpPosY;
    public static double interpPosZ;
    public boolean weatherEffect = false;
    public float spawnY = -1;
    public int particleTextureIndexInt = 0;
    public float brightness = 0.7F;
    public ParticleBehaviors pb = null;
    public boolean callUpdateSuper = true;
    public boolean callUpdatePB = true;
    public float renderRange = 128F;
    public int renderOrder = 0;
    public int debugID = 0;
    public float rotationYaw;
    public float rotationPitch;
    public float windWeight = 5;
    public int particleDecayExtra = 0;
    public boolean isTransparent = true;
    public boolean killOnCollide = false;
    public boolean facePlayer = false;
    public boolean facePlayerYaw = false;
    public boolean vanillaMotionDampen = true;
    public double aboveGroundHeight = 4.5D;
    public boolean checkAheadToBounce = true;
    public boolean collisionSpeedDampen = true;
    public double bounceSpeed = 0.05D;
    public double bounceSpeedMax = 0.15D;
    public double bounceSpeedAhead = 0.35D;
    public double bounceSpeedMaxAhead = 0.25D;
    public boolean spinFast = false;
    public int killWhenUnderCameraAtLeast = 0;
    public int killWhenFarFromCameraAtLeast = 0;
    public float avoidTerrainAngle = 0;
    public boolean useRotationAroundCenter = false;
    public float rotationAroundCenter = 0;
    public float rotationAroundCenterPrev = 0;
    public float rotationSpeedAroundCenter = 0;
    public float rotationDistAroundCenter = 0;
    public Quaternion rotation;
    public Quaternion rotationPrev;
    public boolean quatControl = false;
    public boolean fastLight = false;
    public float brightnessCache = 0.5F;
    public boolean rotateOrderXY = false;
    public float extraYRotation = 0;
    public boolean isCollidedHorizontally = false;
    public boolean isCollidedVerticallyDownwards = false;
    public boolean isCollidedVerticallyUpwards = false;
    public float particleScale = 1.0F;
    public Vector3f rotationAround = new Vector3f();
    protected boolean fadingOut = false;
    private int entityID = 0;
    private float ticksFadeInMax = 0;
    private float ticksFadeOutMax = 0;
    private boolean dontRenderUnderTopmostBlock = false;
    private boolean killWhenUnderTopmostBlock = false;
    private int killWhenUnderTopmostBlock_ScanAheadRange = 0;
    private float ticksFadeOutMaxOnDeath = -1;
    private float ticksFadeOutCurOnDeath = 0;
    private boolean slantParticleToWind = false;

    public EntityRotFX(ClientWorld world,
                       double posX, double posY, double posZ,
                       double speedX, double speedY, double speedZ) {
        super(world, posX, posY, posZ, speedX, speedY, speedZ);
        setSize(0.3F, 0.3F);

        this.entityID = level.random.nextInt(100000);

        rotation = new Quaternion(0f, 0f, 0f, 1f);

        brightnessCache = CoroUtilBlockLightCache.getBrightnessCached(
                level, (float) x, (float) y, (float) z);
    }

    public TextureAtlasSprite getParticleTexture() {
        return this.sprite;
    }

    public void setParticleTexture(TextureAtlasSprite sprite) {
        this.setSprite(sprite);
    }

    public int getParticleTextureIndex() {
        return particleTextureIndexInt;
    }

    public void setParticleTextureIndex(int idx) {
        particleTextureIndexInt = idx;
    }

    public int getMaxAge() {
        return this.lifetime;
    }

    public void setMaxAge(int n) {
        this.lifetime = n;
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int a) {
        this.age = a;
    }

    public void setGravity(float g) {
        this.gravity = g;
    }

    public boolean getCanCollide() {
        return this.hasPhysics;
    }

    public void setCanCollide(boolean v) {
        this.hasPhysics = v;
    }

    public boolean isCollided() {
        return this.onGround;
    }

    public float getAlphaF() {
        return this.alpha;
    }

    public void setAlphaF(float a) {
        this.setAlpha(a);
    }

    public void setRBGColorF(float r, float g, float b) {
        this.setColor(r, g, b);
    }

    @Override
    public void setColor(float r, float g, float b) {
        super.setColor(r, g, b);
        RotatingParticleManager.markDirtyVBO2();
    }

    @Override
    protected void setAlpha(float a) {
        super.setAlpha(a);
        RotatingParticleManager.markDirtyVBO2();
    }

    public float getRedColorF() {
        return this.rCol;
    }

    public float getGreenColorF() {
        return this.gCol;
    }

    public float getBlueColorF() {
        return this.bCol;
    }

    @Override
    public void remove() {
        if (pb != null) pb.particles.remove(this);
        super.remove();
    }

    public void setExpired() {
        remove();
    }

    @Override
    public void tick() {
        super.tick();

        Entity ent = Minecraft.getInstance().getCameraEntity();

        if (!isVanillaMotionDampen()) {
            this.xd /= 0.9800000190734863D;
            this.yd /= 0.9800000190734863D;
            this.zd /= 0.9800000190734863D;
        }

        if (!this.removed && !fadingOut) {
            if (killOnCollide && isCollided()) startDeath();

            if (killWhenUnderTopmostBlock) {
                int height = this.level.getHeightmapPos(
                        Heightmap.Type.MOTION_BLOCKING,
                        new BlockPos(this.x, this.y, this.z)).getY();
                if (this.y - killWhenUnderTopmostBlock_ScanAheadRange <= height)
                    startDeath();
            }

            if (killWhenUnderCameraAtLeast != 0 && ent != null
                    && this.y < ent.getY() - killWhenUnderCameraAtLeast)
                startDeath();

            if (killWhenFarFromCameraAtLeast != 0 && ent != null
                    && getAge() > 20 && getAge() % 5 == 0
                    && getDistance(ent.getX(), ent.getY(), ent.getZ()) > killWhenFarFromCameraAtLeast)
                startDeath();
        }

        if (!collisionSpeedDampen && this.onGround) {
            this.xd /= 0.699999988079071D;
            this.zd /= 0.699999988079071D;
        }

        if (spinFast) {
            this.rotationPitch += entityID % 2 == 0 ? 10 : -10;
            this.rotationYaw += entityID % 2 == 0 ? -10 : 10;
        }

        if (!fadingOut) {
            if (ticksFadeInMax > 0 && getAge() < ticksFadeInMax) {
                setAlphaF((float) getAge() / ticksFadeInMax);
            } else if (ticksFadeOutMax > 0 && getAge() > getMaxAge() - ticksFadeOutMax) {
                float count = getAge() - (getMaxAge() - ticksFadeOutMax);
                setAlphaF((ticksFadeOutMax - count) / ticksFadeOutMax);
            } else if (ticksFadeInMax > 0 || ticksFadeOutMax > 0) {
                setAlphaF(1F);
            }
        } else {
            if (ticksFadeOutCurOnDeath < ticksFadeOutMaxOnDeath)
                ticksFadeOutCurOnDeath++;
            else
                remove();
            setAlphaF(1F - ticksFadeOutCurOnDeath / ticksFadeOutMaxOnDeath);
        }

        if (level.getGameTime() % 5 == 0)
            brightnessCache = CoroUtilBlockLightCache.getBrightnessCached(
                    level, (float) x, (float) y, (float) z);

        rotationAroundCenter += rotationSpeedAroundCenter;
        rotationAroundCenter %= 360;

        tickExtraRotations();
    }

    public void tickExtraRotations() {
        if (slantParticleToWind) {
            double motionXZ = Math.sqrt(xd * xd + zd * zd);
            rotationPitch = (float) Math.atan2(yd, motionXZ);
        }
        if (!quatControl) {
            rotationPrev = (rotation != null)
                    ? rotation.copy()
                    : new Quaternion(0f, 0f, 0f, 1f);
            updateQuaternion(Minecraft.getInstance().getCameraEntity());
        }
    }

    public void startDeath() {
        if (ticksFadeOutMaxOnDeath > 0) {
            ticksFadeOutCurOnDeath = 0;
            fadingOut = true;
        } else {
            remove();
        }
    }

    @Override
    public IParticleRenderType getRenderType() {
        return isTransparent
                ? IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                : IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public boolean shouldDisableDepth() {
        return isTransparent;
    }

    @Override
    public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        Vector3f fwd = renderInfo.getLookVector();
        Vector3f up = renderInfo.getUpVector();

        float leftX = up.y() * fwd.z() - up.z() * fwd.y();
        float leftZ = up.x() * fwd.y() - up.y() * fwd.x();

        float rotationX = leftX;
        float rotationZ = up.y();
        float rotationYZ = leftZ;
        float rotationXY = up.x();
        float rotationXZ = up.z();

        if (!facePlayer) {
            rotationX = MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F);
            rotationYZ = MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F);
            rotationXY = -rotationYZ * MathHelper.sin(this.rotationPitch * (float) Math.PI / 180.0F);
            rotationXZ = rotationX * MathHelper.sin(this.rotationPitch * (float) Math.PI / 180.0F);
            rotationZ = MathHelper.cos(this.rotationPitch * (float) Math.PI / 180.0F);
        }

        renderRotatedQuad(buffer, partialTicks,
                rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    protected void renderRotatedQuad(IVertexBuilder buf, float partialTicks,
                                     float rotX, float rotZ,
                                     float rotYZ, float rotXY, float rotXZ) {
        float px = (float) (xo + (x - xo) * partialTicks - interpPosX);
        float py = (float) (yo + (y - yo) * partialTicks - interpPosY);
        float pz = (float) (zo + (z - zo) * partialTicks - interpPosZ);

        float s = particleScale * 0.1F;

        float u0 = sprite != null ? sprite.getU0() : 0f;
        float u1 = sprite != null ? sprite.getU1() : 1f;
        float v0 = sprite != null ? sprite.getV0() : 0f;
        float v1 = sprite != null ? sprite.getV1() : 1f;

        int light = getLightColor(partialTicks);
        int lightU = light & 0xFFFF;
        int lightV = (light >> 16) & 0xFFFF;

        buf.vertex(px + (-rotX * s - rotXY * s), py + (-rotZ * s), pz + (-rotYZ * s - rotXZ * s))
                .uv(u1, v1).color(rCol, gCol, bCol, alpha).uv2(lightU, lightV).endVertex();
        buf.vertex(px + (-rotX * s + rotXY * s), py + (rotZ * s), pz + (-rotYZ * s + rotXZ * s))
                .uv(u1, v0).color(rCol, gCol, bCol, alpha).uv2(lightU, lightV).endVertex();
        buf.vertex(px + (rotX * s + rotXY * s), py + (rotZ * s), pz + (rotYZ * s + rotXZ * s))
                .uv(u0, v0).color(rCol, gCol, bCol, alpha).uv2(lightU, lightV).endVertex();
        buf.vertex(px + (rotX * s - rotXY * s), py + (-rotZ * s), pz + (rotYZ * s - rotXZ * s))
                .uv(u0, v1).color(rCol, gCol, bCol, alpha).uv2(lightU, lightV).endVertex();
    }

    public void renderParticleForShader(InstancedMeshParticle mesh,
                                        Transformation transformation,
                                        Matrix4fe viewMatrix,
                                        Entity entityIn,
                                        float partialTicks,
                                        float rotationX, float rotationZ,
                                        float rotationYZ, float rotationXY, float rotationXZ) {
        if (mesh.curBufferPos >= mesh.numInstances) return;

        float posX = (float) (xo + (x - xo) * partialTicks - interpPosX);
        float posY = (float) (yo + (y - yo) * partialTicks - interpPosY);
        float posZ = (float) (zo + (z - zo) * partialTicks - interpPosZ);

        Vector3f pos = new Vector3f(posX, posY, posZ);

        Quaternion q = this.rotation;
        if (this.rotationPrev != null) {
            q = CoroUtilMath.interpolate(this.rotationPrev, this.rotation, partialTicks);
        }

        float scaleAdj = particleScale * 0.2F;

        Matrix4fe modelMatrix = new Matrix4fe();
        modelMatrix.translationRotateScale(
                pos.x(), pos.y(), pos.z(),
                q.i(), q.j(), q.k(), q.r(),
                scaleAdj, scaleAdj, scaleAdj);

        modelMatrix.get(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos, mesh.instanceDataBuffer);

        mesh.instanceDataBuffer.put(
                InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS,
                brightnessCache);

        int i = 0;
        mesh.instanceDataBuffer.put(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS + 1 + (i++), rCol);
        mesh.instanceDataBuffer.put(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS + 1 + (i++), gCol);
        mesh.instanceDataBuffer.put(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS + 1 + (i++), bCol);
        mesh.instanceDataBuffer.put(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS + 1 + (i), alpha);

        mesh.curBufferPos++;
    }

    @Override
    public Vector3f getPosition() {
        return new Vector3f((float) x, (float) y, (float) z);
    }

    @Override
    public Quaternion getQuaternion() {
        return rotation;
    }

    @Override
    public Quaternion getQuaternionPrev() {
        return rotationPrev;
    }

    @Override

    public float getScale() {
        return particleScale;
    }

    public void setScale(float s) {
        particleScale = s;
    }

    @Override
    public float getWindWeight() {
        return windWeight;
    }

    @Override
    public int getParticleDecayExtra() {
        return particleDecayExtra;
    }

    public void spawnAsWeatherEffect() {
        weatherEffect = true;
        ExtendedRenderer.rotEffRenderer.addEffect(this);
    }

    public Vec3 getPos() {
        return new Vec3(x, y, z);
    }

    public double getPosX() {
        return x;
    }

    public void setPosX(double v) {
        x = v;
    }

    public double getPosY() {
        return y;
    }

    public void setPosY(double v) {
        y = v;
    }

    public double getPosZ() {
        return z;
    }

    public void setPosZ(double v) {
        z = v;
    }

    public double getMotionX() {
        return xd;
    }

    public void setMotionX(double v) {
        xd = v;
    }

    public double getMotionY() {
        return yd;
    }

    public void setMotionY(double v) {
        yd = v;
    }

    public double getMotionZ() {
        return zd;
    }

    public void setMotionZ(double v) {
        zd = v;
    }

    public double getPrevPosX() {
        return xo;
    }

    public void setPrevPosX(double v) {
        xo = v;
    }

    public double getPrevPosY() {
        return yo;
    }

    public void setPrevPosY(double v) {
        yo = v;
    }

    public double getPrevPosZ() {
        return zo;
    }

    public void setPrevPosZ(double v) {
        zo = v;
    }

    public void setSize(float width, float height) {
        super.setSize(width, height);
    }

    public int getEntityId() {
        return entityID;
    }

    public ClientWorld getWorld() {
        return level;
    }

    public boolean isVanillaMotionDampen() {
        return vanillaMotionDampen;
    }

    public void setVanillaMotionDampen(boolean v) {
        vanillaMotionDampen = v;
    }

    public void setFacePlayer(boolean v) {
        facePlayer = v;
    }

    public void setKillOnCollide(boolean v) {
        killOnCollide = v;
    }

    public float maxRenderRange() {
        return renderRange;
    }


    public double getDistance(double px, double py, double pz) {
        double dx = x - px, dy = y - py, dz = z - pz;
        return MathHelper.sqrt((float) (dx * dx + dy * dy + dz * dz));
    }

    @Override
    public void move(double moveX, double moveY, double moveZ) {
        double origX = moveX, origY = moveY, origZ = moveZ;

        if (this.hasPhysics && (moveX != 0.0D || moveY != 0.0D || moveZ != 0.0D)) {
            Vector3d resolved = Entity.collideBoundingBoxHeuristically(
                    null,
                    new Vector3d(moveX, moveY, moveZ),
                    getBoundingBox(),
                    level,
                    ISelectionContext.empty(),
                    new ReuseableStream<>(Stream.empty()));
            moveX = resolved.x;
            moveY = resolved.y;
            moveZ = resolved.z;
        }

        if (moveX != 0.0D || moveY != 0.0D || moveZ != 0.0D) {
            setBoundingBox(getBoundingBox().move(moveX, moveY, moveZ));
            setLocationFromBoundingbox();
        }

        onGround = origY != moveY || origX != moveX || origZ != moveZ;
        isCollidedHorizontally = origX != moveX || origZ != moveZ;
        isCollidedVerticallyDownwards = origY < moveY;
        isCollidedVerticallyUpwards = origY > moveY;

        if (origX != moveX) xd = 0.0D;
        if (origZ != moveZ) zd = 0.0D;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return super.getLightColor(partialTick);
    }

    public void updateQuaternion(Entity camera) {
        if (camera != null) {
            if (facePlayer) {
                rotationYaw = camera.yRot;
                rotationPitch = camera.xRot;
            } else if (facePlayerYaw) {
                rotationYaw = camera.yRot;
            }
        }

        Quaternion qY = new Quaternion(Vector3f.YP, -rotationYaw - 180F, true);
        Quaternion qX = new Quaternion(Vector3f.XP, -rotationPitch, true);

        if (rotateOrderXY) {
            this.rotation = qX.copy();
            this.rotation.mul(qY);
        } else {
            this.rotation = qY.copy();
            this.rotation.mul(qX);
        }
    }

    public boolean isSlantParticleToWind() {
        return slantParticleToWind;
    }

    public void setSlantParticleToWind(boolean v) {
        slantParticleToWind = v;
    }

    public float getTicksFadeOutMaxOnDeath() {
        return ticksFadeOutMaxOnDeath;
    }

    public void setTicksFadeOutMaxOnDeath(float v) {
        ticksFadeOutMaxOnDeath = v;
    }

    public boolean isKillWhenUnderTopmostBlock() {
        return killWhenUnderTopmostBlock;
    }

    public void setKillWhenUnderTopmostBlock(boolean v) {
        killWhenUnderTopmostBlock = v;
    }

    public boolean isDontRenderUnderTopmostBlock() {
        return dontRenderUnderTopmostBlock;
    }

    public void setDontRenderUnderTopmostBlock(boolean v) {
        dontRenderUnderTopmostBlock = v;
    }

    public float getTicksFadeInMax() {
        return ticksFadeInMax;
    }

    public void setTicksFadeInMax(float v) {
        ticksFadeInMax = v;
    }

    public float getTicksFadeOutMax() {
        return ticksFadeOutMax;
    }

    public void setTicksFadeOutMax(float v) {
        ticksFadeOutMax = v;
    }

    public int getKillWhenUnderTopmostBlock_ScanAheadRange() {
        return killWhenUnderTopmostBlock_ScanAheadRange;
    }

    public void setKillWhenUnderTopmostBlock_ScanAheadRange(int v) {
        killWhenUnderTopmostBlock_ScanAheadRange = v;
    }

    public boolean isCollidedVertically() {
        return isCollidedVerticallyDownwards || isCollidedVerticallyUpwards;
    }
}