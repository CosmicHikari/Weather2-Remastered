package net.mrbt0907.configex.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiBetterSlot {
    public static final int scrollUpButtonID = 7;
    public static final int scrollDownButtonID = 8;
    public final Minecraft mc;
    public final int slotHeight;
    public int width;
    public int height;
    public int top;
    public int bottom;
    public int right;
    public int left;
    public int mouseX;
    public int mouseY;
    public float initialClickY = -2.0F;
    public float scrollMultiplier;
    public float amountScrolled;
    public int selectedElement = -1;
    public long lastClicked = 0L;

    public ResourceLocation resBG = new ResourceLocation("/gui/background.png");

    public GuiBetterSlot(Minecraft mc, int width, int height, int left, int top, int bottom, int slotHeight) {
        this.mc = mc;
        this.width = width + left;
        this.height = height;
        this.left = 0;
        this.top = top;
        this.bottom = bottom;
        this.slotHeight = slotHeight;
        right = width;
    }

    protected abstract int getSize();

    protected abstract void elementClicked(int i, boolean flag);

    protected abstract boolean isSelected(int i);

    protected int getContentHeight() {
        return getSize() * slotHeight;
    }

    protected abstract void drawBackground(Tessellator tessellator);

    protected abstract void drawForeground(Tessellator tessellator);

    protected abstract void drawSlotPre(int i, int j, int k, int l, Tessellator tessellator);

    protected abstract void drawSlotPost(int i, int j, int k, int l, Tessellator tessellator);

    private void bindAmountScrolled() {
        int i = func_77209_d();

        if (i < 0)
            i /= 2;

        if (amountScrolled < 0.0F)
            amountScrolled = 0.0F;

        if (amountScrolled > (float) i)
            amountScrolled = (float) i;
    }

    public int func_77209_d() {
        return getContentHeight() - (bottom - top - 4);
    }

    public void drawScreen(MatrixStack matrixStack, int par1, int par2, float par3) {
        mouseX = par1;
        mouseY = par2;
        int k = getSize();
        int l = getScrollBarX();
        int i1 = l + 6;
        int j1;
        int k1;
        int l1;
        int i2;
        int j2;


        bindAmountScrolled();

        RenderSystem.disableLighting();
        RenderSystem.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuilder();
        drawBackground(tessellator);
        j1 = width / 2 - 16;
        k1 = top + 4 - (int) amountScrolled;

        for (l1 = 0; l1 < k; ++l1) {
            j2 = k1 + l1 * slotHeight;
            i2 = slotHeight - 4;

            if (j2 <= bottom && j2 + i2 >= top)
                drawSlotPre(l1, j1, j2, i2, tessellator);
        }

        drawForeground(tessellator);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();
        j2 = func_77209_d();

        if (j2 > 0) {
            i2 = (bottom - top) * (bottom - top) / getContentHeight();

            if (i2 < 32)
                i2 = 32;

            if (i2 > bottom - top - 8)
                i2 = bottom - top - 8;

            int i3 = (int) amountScrolled * (bottom - top - i2) / j2 + top;
            if (i3 < top)
                i3 = top;

            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.vertex(l, bottom, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 100).endVertex();
            vertexbuffer.vertex(i1, bottom, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 100).endVertex();
            vertexbuffer.vertex(i1, top, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 100).endVertex();
            vertexbuffer.vertex(l, top, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 100).endVertex();
            tessellator.end();

            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.vertex(l, i3 + i2, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            vertexbuffer.vertex(i1, i3 + i2, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            vertexbuffer.vertex(i1, i3, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            vertexbuffer.vertex(l, i3, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            tessellator.end();

            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.vertex(l, i3 + i2 - 1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            vertexbuffer.vertex(i1 - 1, i3 + i2 - 1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            vertexbuffer.vertex(i1 - 1, i3, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            vertexbuffer.vertex(l, i3, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            tessellator.end();
        }

        for (l1 = 0; l1 < k; ++l1) {
            j2 = k1 + l1 * slotHeight;
            i2 = slotHeight - 4;

            if (j2 <= bottom && j2 + i2 >= top)
                drawSlotPost(l1, j1, j2, i2, tessellator);
        }

        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
    }

    protected int getScrollBarX() {
        return width / 2 + 124;
    }
}