package net.mrbt0907.weather2.client.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.function.Function;

public class ModelWindVane extends Model {
    private final ModelRenderer bottom;
    private final ModelRenderer cross;
    private final ModelRenderer cross2;
    private final ModelRenderer N;
    private final ModelRenderer S;
    private final ModelRenderer E;
    private final ModelRenderer W;
    private final ModelRenderer Block2;
    private final ModelRenderer Block3;
    private final ModelRenderer top1;
    private final ModelRenderer chicken;
    private final ModelRenderer Block4;
    private final ModelRenderer Block5;
    private final ModelRenderer arrow;
    private final ModelRenderer point;
    private final ModelRenderer feather;

    public float scaleX = 1f;
    public float scaleY = 1f;
    public float scaleZ = 1f;
    public float scaleItem = 1;
    public float offsetX = 0;
    public float offsetY = 0;
    public float offsetZ = 0;
    public float offsetInvX = 0;
    public float offsetInvY = 0;

    public ModelWindVane() {
        this(RenderType::entityCutoutNoCull);
    }

    public ModelWindVane(Function<ResourceLocation, RenderType> renderTypeFunc) {
        super(renderTypeFunc);

        texWidth = 64;
        texHeight = 32;

        bottom = new ModelRenderer(this, 0, 0);
        bottom.addBox(-0.5F, -8F, -0.5F, 1, 16, 1);
        bottom.setPos(0F, 16F, 0F);
        bottom.xRot = 0F;
        bottom.yRot = 0F;
        bottom.zRot = 0F;

        cross = new ModelRenderer(this, 7, 3);
        cross.addBox(-0.5F, -0.5F, -5F, 1, 1, 10);
        cross.setPos(0F, 9.500001F, 0F);
        cross.xRot = 0F;
        cross.yRot = 0F;
        cross.zRot = 0F;

        cross2 = new ModelRenderer(this, 7, 3);
        cross2.addBox(-0.5F, -0.5F, -5F, 1, 1, 10);
        cross2.setPos(0F, 9.500001F, 0F);
        cross2.xRot = 0F;
        cross2.yRot = -1.570796F;
        cross2.zRot = 0F;

        N = new ModelRenderer(this, 21, 0);
        N.addBox(0F, -1.5F, -1.5F, 0, 3, 3);
        N.setPos(-2.107342E-07F, 9.500001F, 6.5F);
        N.xRot = 0F;
        N.yRot = 0F;
        N.zRot = 0F;

        S = new ModelRenderer(this, 27, 0);
        S.addBox(0F, -1.5F, -1.5F, 0, 3, 3);
        S.setPos(1.421085E-14F, 9.5F, -6.5F);
        S.xRot = 0F;
        S.yRot = 4.561942E-08F;
        S.zRot = 0F;

        E = new ModelRenderer(this, 33, 0);
        E.addBox(0F, -1.5F, -1.5F, 0, 3, 3);
        E.setPos(6.5F, 9.5F, -4.768372E-07F);
        E.xRot = 0F;
        E.yRot = -1.570796F;
        E.zRot = 0F;

        W = new ModelRenderer(this, 39, 0);
        W.addBox(0F, -1.5F, -1.5F, 0, 3, 3);
        W.setPos(-6.5F, 9.5F, -4.768372E-07F);
        W.xRot = 0F;
        W.yRot = -1.570796F;
        W.zRot = 0F;

        Block2 = new ModelRenderer(this, 6, 0);
        Block2.addBox(-1F, -1F, -1F, 2, 2, 2);
        Block2.setPos(0F, 11F, 0F);
        Block2.xRot = 0F;
        Block2.yRot = -0.7853982F;
        Block2.zRot = 0F;

        Block3 = new ModelRenderer(this, 6, 0);
        Block3.addBox(-1F, -1F, -1F, 2, 2, 2);
        Block3.setPos(0F, 11F, 0F);
        Block3.xRot = -9.134193E-09F;
        Block3.yRot = -3.362374E-08F;
        Block3.zRot = -0.7853982F;

        top1 = new ModelRenderer(this, 0, 0);
        top1.addBox(-0.5F, -3F, -0.5F, 1, 6, 1);
        top1.setPos(0F, 5F, 0F);
        top1.xRot = 0F;
        top1.yRot = 0F;
        top1.zRot = 0F;

        chicken = new ModelRenderer(this, 5, 19);
        chicken.addBox(-5F, -5F, 0F, 10, 10, 0);
        chicken.setPos(0F, -3F, 0F);
        chicken.xRot = 0F;
        chicken.yRot = 0F;
        chicken.zRot = 0F;

        Block4 = new ModelRenderer(this, 6, 0);
        Block4.addBox(-1F, -1F, -1F, 2, 2, 2);
        Block4.setPos(0F, 6F, 0F);
        Block4.xRot = 0F;
        Block4.yRot = -0.7853982F;
        Block4.zRot = 0F;

        Block5 = new ModelRenderer(this, 6, 0);
        Block5.addBox(-1F, -1F, -1F, 2, 2, 2);
        Block5.setPos(0F, 6F, 0F);
        Block5.xRot = -9.134193E-09F;
        Block5.yRot = -3.362374E-08F;
        Block5.zRot = -0.7853982F;

        arrow = new ModelRenderer(this, 25, 16);
        arrow.addBox(-5F, -0.5F, -0.5F, 10, 1, 1);
        arrow.setPos(0F, 3F, 0F);
        arrow.xRot = 0F;
        arrow.yRot = 0F;
        arrow.zRot = 0F;

        point = new ModelRenderer(this, 31, 8);
        point.addBox(0F, -1.5F, -1.5F, 0, 3, 3);
        point.setPos(-6.5F, 3F, 0F);
        point.xRot = 0F;
        point.yRot = -1.570796F;
        point.zRot = 0F;

        feather = new ModelRenderer(this, 37, 8);
        feather.addBox(0F, -1.5F, -1.5F, 0, 3, 3);
        feather.setPos(6.5F, 3F, 0F);
        feather.xRot = 0F;
        feather.yRot = -1.570796F;
        feather.zRot = 0F;
    }

    public void renderWithRotation(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight,
                                   int packedOverlay, float red, float green, float blue, float alpha,
                                   float topPieceRotation) {
        matrixStack.pushPose();

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));

        bottom.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        cross.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        cross2.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        N.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        S.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        E.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        W.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-180));

        Block2.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Block3.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(topPieceRotation));

        top1.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        chicken.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Block4.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Block5.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        arrow.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        point.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        feather.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);

        matrixStack.popPose();
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        renderWithRotation(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, 0F);
    }
}