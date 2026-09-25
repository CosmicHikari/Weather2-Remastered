package net.corosus.extendedrenderer.particle.entity;

import net.corosus.coroutillegacy.util.CoroUtilColor;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.ConcurrentHashMap;

public class ParticleTexLeafColor extends ParticleTexFX {


    private static final ConcurrentHashMap<BlockState, int[]> colorCache = new ConcurrentHashMap<>();

    static {

        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(
                (stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
                    colorCache.clear();
                    return stage.wait(null);
                }
        );
    }

    public float rotationYawMomentum = 0;
    public float rotationPitchMomentum = 0;

    public ParticleTexLeafColor(ClientWorld worldIn, double posXIn, double posYIn,
                                double posZIn, double mX, double mY, double mZ,
                                TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);

        BlockPos pos = new BlockPos(posXIn, posYIn, posZIn);
        BlockState state = worldIn.getBlockState(pos);


        if (state.getBlock() instanceof DoublePlantBlock
                && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            state = worldIn.getBlockState(pos.below());
        }

        int multiplier = Minecraft.getInstance().getBlockColors().getColor(state, worldIn, pos, 0);
        if (multiplier == -1) {
            multiplier = 0xFFFFFF;
        }

        int[] blockColors = colorCache.get(state);
        if (blockColors == null) {
            blockColors = CoroUtilColor.getColors(state);

            if (blockColors.length == 0) {


                if ((multiplier & 0xFFFFFF) == 0xFFFFFF) {
                    multiplier = 5811761;
                }

                blockColors = new int[]{0xFFFFFF};
            }

            if (blockColors.length > 1) {
                while (blockColors.length > 1 &&
                        blockColors[blockColors.length - 1] == blockColors[blockColors.length - 2]) {
                    blockColors = ArrayUtils.remove(blockColors, blockColors.length - 1);
                }
            }

            colorCache.put(state, blockColors);
        }

        int randMax = 1 << (blockColors.length - 1);
        int choice = 32 - Integer.numberOfLeadingZeros(worldIn.random.nextInt(Math.max(randMax, 1)));
        choice = Math.min(choice, blockColors.length - 1);
        int color = blockColors[choice];

        float mr = ((multiplier >>> 16) & 0xFF) / 255f;
        float mg = ((multiplier >>> 8) & 0xFF) / 255f;
        float mb = (multiplier & 0xFF) / 255f;

        this.rCol *= (float) (color >> 16 & 255) / 255.0F * mr;
        this.gCol *= (float) (color >> 8 & 255) / 255.0F * mg;
        this.bCol *= (float) (color & 255) / 255.0F * mb;
    }

    @Override
    public void tick() {

        super.tick();


        if (isCollidedVerticallyDownwards && this.random.nextInt(10) == 0) {

            double speed = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
            if (speed > 0.07) {
                this.yd = 0.02D + this.random.nextDouble() * 0.03D;
                this.xd *= 0.6D;
                this.zd *= 0.6D;
                rotationYawMomentum = 30;
                rotationPitchMomentum = 30;
            }
        }

        if (rotationYawMomentum > 0) {
            this.rotationYaw += rotationYawMomentum;
            rotationYawMomentum -= 1.5F;
            if (rotationYawMomentum < 0) rotationYawMomentum = 0;
        }

        if (rotationPitchMomentum > 0) {
            this.rotationPitch += rotationPitchMomentum;
            rotationPitchMomentum -= 1.5F;
            if (rotationPitchMomentum < 0) rotationPitchMomentum = 0;
        }
    }
}