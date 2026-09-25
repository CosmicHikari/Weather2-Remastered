package net.mrbt0907.weather2.client.gui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiButtonBoolean extends Button {
    private final String strEnabled;
    private final String strDisabled;
    private boolean boolState = false;

    public GuiButtonBoolean(int x, int y, String strEnabled, String strDisabled, Button.IPressable onPress) {
        this(x, y, 20, 20, strEnabled, strDisabled, onPress);
    }

    public GuiButtonBoolean(int x, int y, int width, int height, String strEnabled, String strDisabled, Button.IPressable onPress) {
        super(x, y, width, height, new StringTextComponent(strEnabled), onPress);
        this.strEnabled = strEnabled;
        this.strDisabled = strDisabled;
    }

    public void setBooleanToggle() {
        setBoolean(!getBoolean());
    }

    public boolean getBoolean() {
        return boolState;
    }

    public void setBoolean(boolean val) {
        boolState = val;
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

        String label = boolState ? "\u00A7" + '2' + strEnabled : "\u00A7" + 'c' + strDisabled;
        drawCenteredString(matrixStack, mc.font, label,
                this.x + this.width / 2,
                this.y + (this.height - 8) / 2,
                textColor);
    }
}