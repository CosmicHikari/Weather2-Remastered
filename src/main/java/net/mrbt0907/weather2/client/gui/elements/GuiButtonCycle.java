package net.mrbt0907.weather2.client.gui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.Weather2;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuiButtonCycle extends Button {
    public List<String> listDescEntries = new ArrayList<>();
    public int index = 0;

    public GuiButtonCycle(int x, int y, int width, int height,
                          List<String> entries, int defaultIndex,
                          Button.IPressable onPress) throws Exception {
        super(x, y, width, height, new StringTextComponent("unused"), onPress);
        if (entries == null)
            Weather2.fatal("Entries list passed to GuiButtonCycle was null!");
        this.listDescEntries = entries;
        this.index = defaultIndex;
    }

    public void cycleIndex() {
        if (!active) return;
        index++;
        if (index >= listDescEntries.size()) index = 0;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int idx) {
        index = idx;
    }

    public String getDisplayString() {
        return listDescEntries.get(index);
    }

    private int getButtonState(boolean isHovered) {
        if (!this.active) return 0;
        if (isHovered) return 2;
        return 1;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;

        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bind(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        this.isHovered = mouseX >= this.x && mouseY >= this.y
                && mouseX < this.x + this.width && mouseY < this.y + this.height;
        int state = getButtonState(this.isHovered);

        this.blit(matrixStack, this.x, this.y, 0, 46 + state * 20, this.width / 2, this.height);
        this.blit(matrixStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + state * 20, this.width / 2, this.height);

        int textColor = 14737632;
        if (!this.active) textColor = -6250336;
        else if (this.isHovered) textColor = 16777120;

        String label = I18n.get("config.ezgui." + getDisplayString());
        drawCenteredString(matrixStack, mc.font, label,
                this.x + this.width / 2,
                this.y + (this.height - 8) / 2,
                textColor);
    }
}