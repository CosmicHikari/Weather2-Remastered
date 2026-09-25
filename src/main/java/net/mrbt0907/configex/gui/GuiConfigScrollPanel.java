package net.mrbt0907.configex.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.manager.ConfigInstance;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuiConfigScrollPanel extends GuiScrollPanel {
    private static final String NEW_LINE = "\n";
    public final List<ConfigInstance> configs;
    public final List<GuiConfigEntry> options;
    private final GuiConfigEditor config;
    protected int mouseYStart = -1;
    private int mouseX;
    private int mouseY;

    public GuiConfigScrollPanel(GuiConfigEditor controls, Minecraft mc, int scrollBarX, int width, int height, int scrollBarSize, int slotHeight) {
        super(mc, controls.xStart, controls.yStart, scrollBarX, width, height, scrollBarSize, slotHeight, 7, 8);
        this.config = controls;
        configs = ConfigManager.getInstances();
        options = new ArrayList<GuiConfigEntry>();
        populateData();
    }

    @Override
    protected void onSlotClicked(int i, boolean flag) {
        if (!flag) {
            int size = getSize();
            if (i < size)
                selected = i;
            else {
                ConfigModEX.warn("Index was set higher than options list. Bringing index back into range...");
                selected = size - 1;
            }
            KeyBinding.resetMapping();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int eventButton) {
        for (GuiConfigEntry entry : options) {
            if (entry.textField.mouseClicked(mouseX, mouseY, eventButton)) {
                selected = options.indexOf(entry);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, eventButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (selected > -1) {
            selected = -1;
            KeyBinding.resetMapping();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            if (mouseX > xScrollBar && mouseX < xScrollBar + scrollBarSize && mouseY > yStart && mouseY < yStart + ySize) {
                int yScrollMax = ySize - scrollBarSize;
                int yScrollExtra = getScrollHeight() - ySize;
                if (yScrollExtra > 0) {
                    float percExtra = Math.min((float) yScrollExtra / (float) yScrollMax, 1.0F);
                    int yStartMax = (int) (scrollBarSize * 0.5F + (yScrollMax * (1.0F - percExtra) * 0.5F));
                    int yEndMax = (int) (yScrollMax * Math.min(percExtra, 1.0F));
                    setScrollPosPerc(MathHelper.clamp((float) ((mouseY - yStart) - yStartMax) / (float) yEndMax, 0.0F, 1.0F));
                }
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected boolean isSelected(int i) {
        return false;
    }

    @Override
    protected void drawBackground(MatrixStack matrixStack, Tessellator tess, int mouseX, int mouseY, float partialTicks) {
        config.drawBackgroundLayer(matrixStack);
    }

    @Override
    protected void drawForeground(MatrixStack matrixStack, Tessellator tess, int mouseX, int mouseY, float partialTicks) {
        config.drawForegroundLayer(matrixStack);
        config.drawButtons(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void render(MatrixStack matrixStack, int mX, int mY, float f) {
        mouseX = mX;
        mouseY = mY;
        xStart = config.xStart + 169;
        yStart = config.yStart + 40;
        xScrollBar = xStart + 133;

        super.render(matrixStack, mX, mY, f);
    }

    @Override
    protected void drawScrollBar(MatrixStack matrixStack, Tessellator tessellator, int mouseX, int mouseY, float partialTicks) {
        int yScrollMax = ySize - scrollBarSize;
        int yScrollExtra = getScrollHeight() - ySize;
        if (yScrollExtra > 0) {
            float percScrolled = scrollPos / yScrollExtra;
            float percExtra = MathHelper.clamp((float) yScrollExtra / (float) yScrollMax, 0.0F, 1.0F);
            int yStartMax = (int) (yScrollMax * percExtra * percScrolled);
            int yEndMax = (int) (yScrollMax * percExtra * (1.0F - percScrolled));
            fillGradient(matrixStack, xScrollBar, yStart + yStartMax, xScrollBar + scrollBarSize, yStart + scrollBarSize + yScrollMax - yEndMax, 0x55000000, 0x55000000);
            fillGradient(matrixStack, xScrollBar, yStart, xScrollBar + scrollBarSize, yStart + ySize, 0x55000000, 0x55000000);
        }
    }

    @Override
    protected void drawSlotPre(MatrixStack matrixStack, Tessellator tessellator, int xPosition, int yPosition, int slot) {
        if (getSize() == 0) return;
        xPosition -= 20;

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        GuiConfigEntry entry = options.get(slot);
        String name = entry.name;
        if (mc.font.width(name) > xSize + 18)
            name = mc.font.plainSubstrByWidth(name, xSize + 18) + "...";
        int stringWidth = mc.font.width(name);
        AbstractGui.drawString(matrixStack, mc.font, name, xPosition - stringWidth + 15, yPosition + 3, 0xFFFFFFFF);

        entry.textField.xPos = xPosition + 20;
        entry.textField.yPos = yPosition;
        entry.textField.drawTextBox(matrixStack);
    }

    @Override
    protected void drawSlotPost(MatrixStack matrixStack, Tessellator tessellator, int xPosition, int yPosition, int slot) {
        if (getSize() == 0) return;
        xPosition -= 20;
        GuiConfigEntry entry = options.get(slot);

        String name = entry.name;
        if (mc.font.width(name) > xSize + 18)
            name = mc.font.plainSubstrByWidth(name, xSize + 18) + "...";
        int stringWidth = mc.font.width(name);
        int hover_x_min = xPosition - stringWidth + 15;
        int hover_y_min = yPosition;
        int hover_x_max = xPosition + 15;
        int hover_y_max = yPosition + slotHeight;

        boolean hover_string = mouseX >= hover_x_min && mouseY >= hover_y_min && mouseX < hover_x_max && mouseY < hover_y_max;

        if (hover_string) {
            RenderSystem.disableLighting();
            RenderSystem.disableDepthTest();
            int l2 = 0;
            int k2 = hover_y_min - 10;
            String[] lines = (entry.name + NEW_LINE + NEW_LINE + ConfigManager.formatCommentForGui(entry.comment, entry.defaultValue, entry.type, entry.showMin, entry.showMax, entry.min, entry.max) + (entry.requiresRestart ? NEW_LINE + TextFormatting.RED + TextFormatting.BOLD + "Requires full game restart for changes to take effect" : entry.requiresWorldRestart ? NEW_LINE + TextFormatting.RED + TextFormatting.BOLD + "Requires world reload for changes to take effect" : "") + (entry.hasPermission ? "" : NEW_LINE + TextFormatting.RED + TextFormatting.BOLD + "Higher permission level required") + NEW_LINE + NEW_LINE + TextFormatting.GRAY + TextFormatting.ITALIC + "On Text Box" + NEW_LINE + TextFormatting.GRAY + TextFormatting.ITALIC + (entry.type == 7 ? "Shift & Left Click: Switch to true/false" : "Shift & Left Click: Reset to original value") + NEW_LINE + TextFormatting.GRAY + TextFormatting.ITALIC + "Shift & Right Click: Reset to default value" + NEW_LINE + TextFormatting.BLUE + TextFormatting.ITALIC + entry.registryName).split("\\n");
            for (int i = 0; i < lines.length; i++)
                if (mc.font.width(lines[i]) > l2)
                    l2 = mc.font.width(lines[i]);
            fillGradient(matrixStack, mouseX - 3, k2 - 3, mouseX + l2 + 3, k2 + 11 + (10 * (lines.length - 1)), 0xc0000000, 0xc0000000);
            for (int i = 0; i < lines.length; i++)
                mc.font.drawShadow(matrixStack, lines[i], mouseX, k2 + (i * (slotHeight / 2)), -1);
            RenderSystem.enableDepthTest();
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selected > -1 && selected < options.size()) {
            GuiConfigEntry entry = options.get(selected);
            if (entry.textField.isFocused()) {
                entry.textField.textboxKeyTyped((char) 0, keyCode);
                entry.textField.updateChange();
                if (keyCode == 257) {
                    selected = -1;
                    entry.textField.setFocused(false);
                    return true;
                } else if (keyCode == 256) {
                    selected = -1;
                    entry.textField.setFocused(false);
                    return true;
                }
                return true;
            }
        } else {
            if (getScrollHeight() - ySize > 0) {
                int height = getScrollHeight();
                if (keyCode == 265)
                    adjustScrollPos(-height / getSize());
                else if (keyCode == 264)
                    adjustScrollPos(height / getSize());
                else if (keyCode == 267)
                    adjustScrollPos(height / getSize() + slotHeight * 8);
                else if (keyCode == 266)
                    adjustScrollPos(-height / getSize() - slotHeight * 8);
                else if (keyCode == 268)
                    adjustScrollPos(-(getScrollHeight() - ySize));
                else if (keyCode == 269)
                    adjustScrollPos(getScrollHeight() - ySize);
            }
        }

        return true;
    }

    public boolean charTyped(char c, int modifiers) {
        if (selected > -1 && selected < options.size()) {
            GuiConfigEntry entry = options.get(selected);
            if (entry.textField.isFocused()) {
                entry.textField.textboxKeyTyped(c, 0);
                entry.textField.updateChange();
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    protected void fillGradient(MatrixStack matrixStack, int par1, int par2, int par3, int par4, int par5, int par6) {
        float f = (float) (par5 >> 24 & 255) / 255.0F;
        float f1 = (float) (par5 >> 16 & 255) / 255.0F;
        float f2 = (float) (par5 >> 8 & 255) / 255.0F;
        float f3 = (float) (par5 & 255) / 255.0F;
        float f4 = (float) (par6 >> 24 & 255) / 255.0F;
        float f5 = (float) (par6 >> 16 & 255) / 255.0F;
        float f6 = (float) (par6 >> 8 & 255) / 255.0F;
        float f7 = (float) (par6 & 255) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuilder();

        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.vertex(par3, par2, 0).color(f1, f2, f3, f).endVertex();
        vertexbuffer.vertex(par1, par2, 0).color(f1, f2, f3, f).endVertex();
        vertexbuffer.vertex(par1, par4, 0).color(f5, f6, f7, f4).endVertex();
        vertexbuffer.vertex(par3, par4, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.end();

        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public void populateData() {
        scrollPos = 0.0F;
        options.clear();
        ConfigInstance config = configs.get(GuiConfigEditor.curIndex);

        if (config != null)
            config.getFields().forEach(field ->
            {
                boolean hasPermission = field.hasPermission();
                if ((!(!hasPermission && field.hide) || hasPermission) && (!field.enforce || (GuiConfigEditor.serverMode || ConfigManager.isSinglePlayer())))
                    options.add(new GuiConfigEntry(field, GuiConfigEditor.serverMode));
            });
    }

    @Override
    protected int getSize() {
        return options.size();
    }
}