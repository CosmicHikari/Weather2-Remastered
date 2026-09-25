package net.mrbt0907.configex.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiScrollPanel {
    public static final int scrollUpButtonID = 7;
    public static final int scrollDownButtonID = 8;
    protected static float scrollPos;
    public final Minecraft mc;
    public final int slotHeight;
    public final int scrollUpID;
    public final int scrollDownID;
    public int xStart;
    public int yStart;
    public int xScrollBar;
    public int xSize;
    public int ySize;
    public int scrollBarSize;
    public int selected = -1;
    protected float initialMouseY;


    public GuiScrollPanel(Minecraft mc, int xStart, int yStart, int xScrollBar, int xSize, int ySize, int scrollBarSize, int slotHeight, int scrollUpID, int scrollDownID) {
        this.mc = mc;
        this.xStart = xStart;
        this.yStart = yStart;
        this.xScrollBar = xScrollBar;
        this.xSize = xSize;
        this.ySize = ySize;
        this.scrollBarSize = scrollBarSize;
        this.slotHeight = slotHeight;
        this.scrollUpID = scrollUpID;
        this.scrollDownID = scrollDownID;
    }


    protected abstract void onSlotClicked(int slot, boolean doubleClicked);

    protected abstract boolean isSelected(int slot);

    protected abstract void drawBackground(MatrixStack matrixStack, Tessellator tessellator, int mouseX, int mouseY, float partialTicks);

    protected abstract void drawScrollBar(MatrixStack matrixStack, Tessellator tessellator, int mouseX, int mouseY, float partialTicks);

    protected abstract void drawForeground(MatrixStack matrixStack, Tessellator tessellator, int mouseX, int mouseY, float partialTicks);

    protected abstract void drawSlotPre(MatrixStack matrixStack, Tessellator tessellator, int xPos, int yPos, int slot);

    protected abstract void drawSlotPost(MatrixStack matrixStack, Tessellator tessellator, int xPos, int yPos, int slot);

    protected abstract int getSize();

    protected int getScrollHeight() {
        return getSize() * slotHeight;
    }


    protected void setScrollPos(float scroll) {
        scrollPos = MathHelper.clamp(scroll, 0.0F, getScrollHeight() - ySize);
    }


    protected void adjustScrollPos(float adjustment) {
        setScrollPos(scrollPos + adjustment);
    }


    protected void setScrollPosPerc(float perc) {
        setScrollPos((getScrollHeight() - ySize) * perc);
    }


    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int size = getSize();
        int slot = (int) (scrollPos / this.slotHeight);
        int slotHeight = slot * this.slotHeight;

        RenderSystem.pushMatrix();
        RenderSystem.disableLighting();
        RenderSystem.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        drawBackground(matrixStack, tessellator, mouseX, mouseY, partialTicks);
        for (int i = slot; i < size && slotHeight - scrollPos < ySize; i++, slotHeight += this.slotHeight)
            drawSlotPre(matrixStack, tessellator, xStart, (int) (yStart + slotHeight - scrollPos), i);
        drawScrollBar(matrixStack, tessellator, mouseX, mouseY, partialTicks);
        drawForeground(matrixStack, tessellator, mouseX, mouseY, partialTicks);

        slotHeight = slot * this.slotHeight;
        for (int i = slot; i < size && slotHeight - scrollPos < ySize; i++, slotHeight += this.slotHeight)
            drawSlotPost(matrixStack, tessellator, xStart, (int) (yStart + slotHeight - scrollPos), i);
        RenderSystem.popMatrix();
    }


    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (getScrollHeight() - ySize > 0) {
            int height = getScrollHeight();
            if (scrollDelta > 0)
                adjustScrollPos(-height / getSize() * 0.75F);
            else if (scrollDelta < 0)
                adjustScrollPos(height / getSize() * 0.75F);
            return true;
        }
        return false;
    }


    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int size = getSize();
            int realY = yStart + ySize;
            int slot = (int) ((scrollPos + (mouseY - yStart)) / this.slotHeight);
            if (slot < size && mouseX > xStart && mouseX < xStart + xSize && mouseY > yStart && mouseY < realY) {
                onSlotClicked(slot, false);
                return true;
            } else
                selected = -1;
        }
        return false;
    }


    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }


    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }
}