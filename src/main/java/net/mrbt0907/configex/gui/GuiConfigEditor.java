package net.mrbt0907.configex.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.network.NetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@OnlyIn(Dist.CLIENT)
public class GuiConfigEditor extends Screen {
    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(ConfigModEX.MODID, "textures/gui/advanced_gui.png");
    public static final ResourceLocation GUI_BORDER_TEXTURE = new ResourceLocation(ConfigModEX.MODID, "textures/gui/advanced_gui_border.png");
    public static int curIndex = 0;
    public static boolean serverMode = false;

    public GuiConfigScrollPanel scrollPane;
    public int xCenter;
    public int yCenter;
    public int xStart;
    public int yStart;
    public int xSize = 320;
    public int ySize = 246;
    public boolean changed;

    public GuiConfigEditor() {
        super(new TranslationTextComponent("config.gui.title"));
        this.minecraft = Minecraft.getInstance();
        scrollPane = new GuiConfigScrollPanel(this, minecraft, xStart + 169, 130, 175, 8, 20);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        drawBackgroundLayer(matrixStack);
        drawForegroundLayer(matrixStack);
        scrollPane.render(matrixStack, mouseX, mouseY, partialTicks);
        drawButtons(matrixStack, mouseX, mouseY, partialTicks);
    }

    public void drawButtons(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    protected void drawForegroundLayer(MatrixStack matrixStack) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(GUI_BORDER_TEXTURE);
        blit(matrixStack, xStart, yStart, 0, 0, 512, 512);

        String title = TextFormatting.BOLD + scrollPane.configs.get(curIndex).name;
        String subtitle = format("title." + (serverMode ? "server" : "client"));
        drawString(matrixStack, font, subtitle, xStart + 160 - font.width(subtitle) / 2, yStart + 23, 16777215);
        drawString(matrixStack, font, title, xStart + 160 - font.width(title) / 2, yStart + 10, 16777215);
        drawString(matrixStack, font, (curIndex + 1) + "/" + scrollPane.configs.size(), xStart + 40, yStart + 226, 16777215);

        if (scrollPane.getSize() == 0) {
            String noEntries = TextFormatting.GRAY + format("entries." + (scrollPane.configs.get(curIndex).size() == 0 ? "empty" : "permission"));
            drawString(matrixStack, font, noEntries, xStart + 160 - font.width(noEntries) / 2, yStart + 120, 16777215);
        }
    }

    protected void drawBackgroundLayer(MatrixStack matrixStack) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(GUI_TEXTURE);
        blit(matrixStack, xStart, yStart, 0, 0, 512, 512);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    protected void init() {
        int scaledWidth = this.width;
        int scaledHeight = this.height;
        int buttonWidth = 30;
        int buttonHeight = 20;
        int buttonBottomY = 220;
        xCenter = (int) (scaledWidth * 0.5F);
        yCenter = (int) (scaledHeight * 0.5F);
        xStart = (int) (xCenter - xSize * 0.5F);
        yStart = (int) (yCenter - ySize * 0.5F);

        this.buttons.clear();
        this.children.clear();

        this.addButton(new Button(xStart + 74, yStart + buttonBottomY, buttonWidth, buttonHeight,
                new TranslationTextComponent("config.gui.next"), (button) -> {
            checkForUpdates();
            curIndex++;
            if (curIndex >= scrollPane.configs.size())
                curIndex = 0;
            scrollPane.populateData();
            init();
        }));

        this.addButton(new Button(xStart + 7, yStart + buttonBottomY, buttonWidth, buttonHeight,
                new TranslationTextComponent("config.gui.previous"), (button) -> {
            checkForUpdates();
            curIndex--;
            if (curIndex < 0)
                curIndex = scrollPane.configs.size() - 1;
            scrollPane.populateData();
            init();
        }));

        this.addButton(new Button(xStart + 252, yStart + buttonBottomY, buttonWidth + 32, buttonHeight,
                new TranslationTextComponent("config.gui.exit"), (button) -> {
            checkForUpdates();
            if (minecraft.player != null && changed)
                minecraft.player.sendMessage(new TranslationTextComponent("config.gui.save"), minecraft.player.getUUID());
            minecraft.setScreen(null);
        }));

        if (ConfigManager.isSinglePlayer()) {
            if (serverMode) {
                serverMode = false;
                scrollPane.populateData();
            }
        } else {
            this.addButton(new Button(xStart + 162, yStart + buttonBottomY, buttonWidth + 56, buttonHeight,
                    new TranslationTextComponent(serverMode ? "config.gui.clientmode" : "config.gui.servermode"), (button) -> {
                checkForUpdates();
                serverMode = !serverMode;
                scrollPane.populateData();
                init();
            }));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (scrollPane.keyPressed(keyCode, scanCode, modifiers)) {
            if (keyCode == 256) {
                checkForUpdates();
                if (minecraft.player != null)
                    minecraft.player.sendMessage(new TranslationTextComponent("config.gui." + (changed ? "save" : "nosave")), minecraft.player.getUUID());
                this.minecraft.setScreen(null);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (scrollPane.charTyped(c, modifiers))
            return true;
        return super.charTyped(c, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        try {
            scrollPane.mouseClicked(mouseX, mouseY, button);
            return super.mouseClicked(mouseX, mouseY, button);
        } catch (Exception e) {
            ConfigModEX.error(e);
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (scrollPane.mouseScrolled(mouseX, mouseY, delta))
            return true;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrollPane.mouseDragged(mouseX, mouseY, button, dragX, dragY))
            return true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public void checkForUpdates() {
        Map<String, CompoundNBT> tags = new HashMap<String, CompoundNBT>();
        CompoundNBT nbt = new CompoundNBT();
        CompoundNBT nbtManager = new CompoundNBT();
        CompoundNBT nbtField;
        for (GuiConfigEntry option : scrollPane.options) {
            if (!tags.containsKey(option.categoryName))
                tags.put(option.categoryName, new CompoundNBT());

            if (option.textField.hasChanged) {
                changed = true;
                nbtField = new CompoundNBT();
                nbtField.putString("value", option.textField.text);
                tags.get(option.categoryName).put(option.registryName, nbtField);
            }
        }

        for (Entry<String, CompoundNBT> tag : tags.entrySet())
            if (!tag.getValue().isEmpty())
                nbtManager.put(tag.getKey(), tag.getValue());

        nbt.put("manager", nbtManager);

        if (!nbtManager.isEmpty()) {
            if (serverMode) {
                ConfigModEX.debug("Sending advanced config data to server...");
                NetworkHandler.sendServerPacket(0, nbt);
            } else {
                ConfigModEX.debug("Applying client changes...");
                nbt.putBoolean("setClient", true);
                ConfigManager.readNBT(nbt);
                ConfigManager.save();
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void blit(MatrixStack matrixStack, int x, int y, int textureX, int textureY, int width, int height) {
        RenderSystem.pushMatrix();
        float f = 0.00390625F / 2F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.vertex(x, y + height, this.getBlitOffset()).uv((float) (textureX) * f, (float) (textureY + height) * f).endVertex();
        vertexbuffer.vertex(x + width, y + height, this.getBlitOffset()).uv((float) (textureX + width) * f, (float) (textureY + height) * f).endVertex();
        vertexbuffer.vertex(x + width, y, this.getBlitOffset()).uv((float) (textureX + width) * f, (float) (textureY) * f).endVertex();
        vertexbuffer.vertex(x, y, this.getBlitOffset()).uv((float) (textureX) * f, (float) (textureY) * f).endVertex();
        tessellator.end();
        RenderSystem.popMatrix();
    }

    private String format(String local, Object... args) {
        return I18n.get("config.gui." + local, args);
    }
}