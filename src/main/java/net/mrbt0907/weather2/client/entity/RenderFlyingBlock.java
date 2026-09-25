package net.mrbt0907.weather2.client.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.mrbt0907.weather2.entity.EntityHail;
import net.mrbt0907.weather2.entity.EntityMovingBlock;
import net.mrbt0907.weather2.util.Maths;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class RenderFlyingBlock extends EntityRenderer<Entity> {
    Block renderBlock;
    TileEntity tile;

    public RenderFlyingBlock(EntityRendererManager renderManager) {
        this(renderManager, null);
    }

    public RenderFlyingBlock(EntityRendererManager renderManager, Block parBlock) {
        super(renderManager);
        renderBlock = parBlock;
        tile = null;
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return AtlasTexture.LOCATION_BLOCKS;
    }

    @Override
    public void render(Entity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        BlockState state = null;
        if (entity instanceof EntityMovingBlock)
            state = ((EntityMovingBlock) entity).state;
        else if (renderBlock != null)
            state = renderBlock.defaultBlockState();

        if (state == null) return;

        double size = entity.getBbWidth();
        if (entity instanceof EntityHail)
            size = ((EntityHail) entity).size;

        BlockRenderType renderType = state.getRenderShape();
        World world = entity.level;

        float yaw = (float) Math.toDegrees(Maths.fastATan2(entity.getDeltaMovement().z, entity.getDeltaMovement().x)) - 90F;
        float pitch = (float) -Math.toDegrees(Maths.fastATan2(entity.getDeltaMovement().y, Math.sqrt(entity.getDeltaMovement().x * entity.getDeltaMovement().x + entity.getDeltaMovement().z * entity.getDeltaMovement().z)));

        if (renderType == BlockRenderType.MODEL) {
            matrixStack.pushPose();

            BlockPos blockpos = new BlockPos(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());

            matrixStack.mulPose(Vector3f.YP.rotationDegrees(yaw));
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(pitch));
            matrixStack.scale((float) size, (float) size, (float) size);

            BlockRendererDispatcher blockrenderer = Minecraft.getInstance().getBlockRenderer();

            for (net.minecraft.client.renderer.RenderType rendertype : net.minecraft.client.renderer.RenderType.chunkBufferLayers()) {
                if (RenderTypeLookup.canRenderInLayer(state, rendertype)) {
                    net.minecraftforge.client.ForgeHooksClient.setRenderLayer(rendertype);
                    IVertexBuilder vertexBuilder = buffer.getBuffer(rendertype);
                    blockrenderer.getModelRenderer().renderModel(
                            world,
                            blockrenderer.getBlockModel(state),
                            state,
                            blockpos,
                            matrixStack,
                            vertexBuilder,
                            false,
                            new Random(),
                            state.getSeed(blockpos),
                            OverlayTexture.NO_OVERLAY,
                            EmptyModelData.INSTANCE
                    );
                }
            }
            net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);

            matrixStack.popPose();
            super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
        } else if (renderType == BlockRenderType.ENTITYBLOCK_ANIMATED) {
            if (entity instanceof EntityMovingBlock) {
                EntityMovingBlock movingBlock = (EntityMovingBlock) entity;

                if (movingBlock.tileClass != null) {
                    if (tile == null)
                        tile = state.getBlock().createTileEntity(state, world);

                    if (tile != null) {
                        try {
                            matrixStack.pushPose();

                            matrixStack.mulPose(Vector3f.YP.rotationDegrees(yaw));
                            matrixStack.mulPose(Vector3f.XP.rotationDegrees(pitch));
                            matrixStack.scale((float) size, (float) size, (float) size);

                            TileEntityRendererDispatcher.instance.render(tile, partialTicks, matrixStack, buffer);

                            matrixStack.popPose();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
        }
    }
}