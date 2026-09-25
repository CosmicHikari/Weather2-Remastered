package net.mrbt0907.weather2.client.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.block.tile.TileAnemometer;

public class RenderAnemometer extends TileEntityRenderer<TileAnemometer> {
    public ModelAnemometer model;
    public ResourceLocation texture = new ResourceLocation(Weather2.OLD_MODID + ":textures/block/anemometer_custom.png");

    public RenderAnemometer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        model = new ModelAnemometer();
    }

    @Override
    public void render(TileAnemometer tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        float renderAngle = tileEntity.smoothAnglePrev + (tileEntity.smoothAngle - tileEntity.smoothAnglePrev) * partialTicks;

        float scale = 1F;

        model.scaleX = scale;
        model.scaleY = scale;
        model.scaleZ = scale;

        matrixStack.pushPose();

        matrixStack.translate(0.5D, 0D, 0.5D);

        boolean isInv = false;

        if (isInv) {
            matrixStack.translate(0, 1.0F * model.scaleY * model.scaleItem, 0);
            matrixStack.scale(model.scaleItem, model.scaleItem, model.scaleItem);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));
        } else {
            matrixStack.translate(0, 1.5F * model.scaleY, 0);
        }

        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));

        if (!isInv) {
            matrixStack.translate(model.offsetX, model.offsetY, model.offsetZ);
        } else {
            matrixStack.translate(model.offsetInvX, model.offsetInvY, 0);
        }

        matrixStack.scale(model.scaleX, model.scaleY, model.scaleZ);

        IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));

        model.renderWithRotation(matrixStack, vertexBuilder, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F, renderAngle);

        matrixStack.popPose();
    }
}