package net.mrbt0907.weather2.client.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.function.Function;

public class ModelAnemometer extends Model {
    private final ModelRenderer Shape1;
    private final ModelRenderer Shape22;
    private final ModelRenderer Shape2;
    private final ModelRenderer Shape3;
    private final ModelRenderer Shape44;
    private final ModelRenderer Shape4;
    private final ModelRenderer Shape55;
    private final ModelRenderer Shape5;
    private final ModelRenderer Shape444;
    private final ModelRenderer Shape5555;
    private final ModelRenderer Shape555;
    private final ModelRenderer Shape4444;
    private final ModelRenderer Shape33;
    private final ModelRenderer Shape6;
    private final ModelRenderer Shape7;
    private final ModelRenderer Shape8;
    private final ModelRenderer Shape9;
    private final ModelRenderer Shape10;
    private final ModelRenderer Shape11;
    private final ModelRenderer Shape12;
    private final ModelRenderer Shape13;
    private final ModelRenderer Shape14;
    private final ModelRenderer Shape15;

    public float scaleX = 1f;
    public float scaleY = 1f;
    public float scaleZ = 1f;
    public float scaleItem = 1;
    public float offsetX = 0;
    public float offsetY = 0;
    public float offsetZ = 0;
    public float offsetInvX = 0;
    public float offsetInvY = 0;

    public ModelAnemometer() {
        this(RenderType::entityCutoutNoCull);
    }

    public ModelAnemometer(Function<ResourceLocation, RenderType> renderTypeFunc) {
        super(renderTypeFunc);

        texWidth = 64;
        texHeight = 32;

        Shape1 = new ModelRenderer(this, 0, 0);
        Shape1.addBox(0F, 0F, 0F, 2, 15, 2);
        Shape1.setPos(-1F, 9F, -1F);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);

        Shape22 = new ModelRenderer(this, 0, 0);
        Shape22.addBox(-8F, 0F, -1F, 16, 1, 2);
        Shape22.setPos(0F, 8F, 0F);
        Shape22.mirror = true;
        setRotation(Shape22, 0F, 0F, 0F);

        Shape2 = new ModelRenderer(this, 0, 0);
        Shape2.addBox(-1F, 0F, -8F, 2, 1, 16);
        Shape2.setPos(0F, 8F, 0F);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);

        Shape3 = new ModelRenderer(this, 0, 0);
        Shape3.addBox(-9F, 0F, -2F, 3, 3, 1);
        Shape3.setPos(0F, 7F, 0F);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);

        Shape44 = new ModelRenderer(this, 0, 0);
        Shape44.addBox(-9F, 0F, -3F, 3, 1, 1);
        Shape44.setPos(0F, 9F, 0F);
        Shape44.mirror = true;
        setRotation(Shape44, 0F, 0F, 0F);

        Shape4 = new ModelRenderer(this, 0, 0);
        Shape4.addBox(-9F, 0F, -3F, 3, 1, 1);
        Shape4.setPos(0F, 7F, 0F);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);

        Shape55 = new ModelRenderer(this, 0, 0);
        Shape55.addBox(-9F, 0F, -3F, 1, 1, 1);
        Shape55.setPos(0F, 8F, 0F);
        Shape55.mirror = true;
        setRotation(Shape55, 0F, 0F, 0F);

        Shape5 = new ModelRenderer(this, 0, 0);
        Shape5.addBox(-7F, 0F, -3F, 1, 1, 1);
        Shape5.setPos(0F, 8F, 0F);
        Shape5.mirror = true;
        setRotation(Shape5, 0F, 0F, 0F);

        Shape444 = new ModelRenderer(this, 0, 0);
        Shape444.addBox(6F, 0F, 2F, 3, 1, 1);
        Shape444.setPos(0F, 7F, 0F);
        Shape444.mirror = true;
        setRotation(Shape444, 0F, 0F, 0F);

        Shape5555 = new ModelRenderer(this, 0, 0);
        Shape5555.addBox(6F, 0F, 1F, 1, 1, 1);
        Shape5555.setPos(0F, 8F, 1F);
        Shape5555.mirror = true;
        setRotation(Shape5555, 0F, 0F, 0F);

        Shape555 = new ModelRenderer(this, 0, 0);
        Shape555.addBox(8F, 0F, 2F, 1, 1, 1);
        Shape555.setPos(0F, 8F, 0F);
        Shape555.mirror = true;
        setRotation(Shape555, 0F, 0F, 0F);

        Shape4444 = new ModelRenderer(this, 0, 0);
        Shape4444.addBox(6F, 0F, 2F, 3, 1, 1);
        Shape4444.setPos(0F, 9F, 0F);
        Shape4444.mirror = true;
        setRotation(Shape4444, 0F, 0F, 0F);

        Shape33 = new ModelRenderer(this, 0, 0);
        Shape33.addBox(6F, 0F, 1F, 3, 3, 1);
        Shape33.setPos(0F, 7F, 0F);
        Shape33.mirror = true;
        setRotation(Shape33, 0F, 0F, 0F);

        Shape6 = new ModelRenderer(this, 0, 0);
        Shape6.addBox(1F, 0F, -9F, 1, 3, 3);
        Shape6.setPos(0F, 7F, 0F);
        Shape6.mirror = true;
        setRotation(Shape6, 0F, 0F, 0F);

        Shape7 = new ModelRenderer(this, 0, 0);
        Shape7.addBox(-2F, 0F, 6F, 1, 3, 3);
        Shape7.setPos(0F, 7F, 0F);
        Shape7.mirror = true;
        setRotation(Shape7, 0F, 0F, 0F);

        Shape8 = new ModelRenderer(this, 0, 0);
        Shape8.addBox(-3F, 0F, 8F, 1, 1, 1);
        Shape8.setPos(0F, 8F, 0F);
        Shape8.mirror = true;
        setRotation(Shape8, 0F, 0F, 0F);

        Shape9 = new ModelRenderer(this, 0, 0);
        Shape9.addBox(-3F, 0F, 6F, 1, 1, 1);
        Shape9.setPos(0F, 8F, 0F);
        Shape9.mirror = true;
        setRotation(Shape9, 0F, 0F, 0F);

        Shape10 = new ModelRenderer(this, 0, 0);
        Shape10.addBox(-3F, 0F, 6F, 1, 1, 3);
        Shape10.setPos(0F, 7F, 0F);
        Shape10.mirror = true;
        setRotation(Shape10, 0F, 0F, 0F);

        Shape11 = new ModelRenderer(this, 0, 0);
        Shape11.addBox(-3F, 0F, 6F, 1, 1, 3);
        Shape11.setPos(0F, 9F, 0F);
        Shape11.mirror = true;
        setRotation(Shape11, 0F, 0F, 0F);

        Shape12 = new ModelRenderer(this, 0, 0);
        Shape12.addBox(2F, 0F, -9F, 1, 1, 3);
        Shape12.setPos(0F, 7F, 0F);
        Shape12.mirror = true;
        setRotation(Shape12, 0F, 0F, 0F);

        Shape13 = new ModelRenderer(this, 0, 0);
        Shape13.addBox(2F, 0F, -9F, 1, 1, 3);
        Shape13.setPos(0F, 9F, 0F);
        Shape13.mirror = true;
        setRotation(Shape13, 0F, 0F, 0F);

        Shape14 = new ModelRenderer(this, 0, 0);
        Shape14.addBox(2F, 0F, -7F, 1, 1, 1);
        Shape14.setPos(0F, 8F, 0F);
        Shape14.mirror = true;
        setRotation(Shape14, 0F, 0F, 0F);

        Shape15 = new ModelRenderer(this, 0, 0);
        Shape15.addBox(2F, 0F, -9F, 1, 1, 1);
        Shape15.setPos(0F, 8F, 0F);
        Shape15.mirror = true;
        setRotation(Shape15, 0F, 0F, 0F);
    }

    public void renderWithRotation(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight,
                                   int packedOverlay, float red, float green, float blue, float alpha,
                                   float topPieceRotation) {
        matrixStack.pushPose();

        Shape1.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(topPieceRotation));

        Shape22.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape2.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape3.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape44.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape4.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape55.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape5.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape444.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape5555.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape555.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape4444.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape33.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape6.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape7.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape8.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape9.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape10.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape11.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape12.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape13.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape14.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        Shape15.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);

        matrixStack.popPose();
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        renderWithRotation(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, 0F);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }
}