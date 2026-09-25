package net.mrbt0907.configex.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.SharedConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiBetterTextField extends AbstractGui {
    public final String defaultText;
    public final String originalText;
    public FontRenderer fontRenderer;
    public int xPos;
    public int yPos;
    public int width;
    public int height;
    public String text = "";
    public int maxStringLength = 10000;
    public int cursorCounter;
    public boolean enableBackgroundDrawing = true;
    public boolean canLoseFocus = true;
    public boolean isFocused = false;
    public boolean isEnabled = true;
    public int lineScrollOffset = 0;
    public int cursorPosition = 0;
    public int selectionEnd = 0;
    public int enabledColor = 14737632;
    public int disabledColor = 0xBB1010;
    public boolean visible = true;
    public boolean hasChanged;

    public GuiBetterTextField(FontRenderer fontRenderer, int x, int y, int width, int height, String text, String defaultText) {
        this.fontRenderer = fontRenderer;
        this.xPos = x;
        this.yPos = y;
        this.width = width;
        this.height = height;
        this.text = originalText = text;
        this.defaultText = defaultText;
    }

    public void updateChange() {
        hasChanged = !text.equals(originalText);
    }

    public void updateCursorCounter() {
        ++cursorCounter;
    }

    public String getText() {
        return text;
    }

    public void setText(String str) {
        if (str.length() > maxStringLength)
            text = str.substring(0, maxStringLength);
        else
            text = str;

        setCursorPositionEnd();
    }

    public String getSelectedtext() {
        int i = cursorPosition < selectionEnd ? cursorPosition : selectionEnd;
        int j = cursorPosition < selectionEnd ? selectionEnd : cursorPosition;
        return text.substring(i, j);
    }

    public void writeText(String str) {
        String s1 = "";
        String s2 = SharedConstants.filterText(str);
        int i = cursorPosition < selectionEnd ? cursorPosition : selectionEnd;
        int j = cursorPosition < selectionEnd ? selectionEnd : cursorPosition;
        int k = maxStringLength - text.length() - (i - selectionEnd);

        if (text.length() > 0)
            s1 = s1 + text.substring(0, i);

        int l;

        if (k < s2.length()) {
            s1 = s1 + s2.substring(0, k);
            l = k;
        } else {
            s1 = s1 + s2;
            l = s2.length();
        }

        if (text.length() > 0 && j < text.length())
            s1 = s1 + text.substring(j);

        text = s1;
        moveCursorBy(i - selectionEnd + l);
    }

    public void deleteWords(int num) {
        if (text.length() != 0) {
            if (selectionEnd != cursorPosition)
                writeText("");
            else
                deleteFromCursor(getNthWordFromCursor(num) - cursorPosition);
        }
    }

    public void deleteFromCursor(int num) {
        if (text.length() != 0) {
            if (selectionEnd != cursorPosition)
                writeText("");
            else {
                boolean flag = num < 0;
                int j = flag ? cursorPosition + num : cursorPosition;
                int k = flag ? cursorPosition : cursorPosition + num;
                String s = "";

                if (j >= 0)
                    s = text.substring(0, j);

                if (k < text.length())
                    s = s + text.substring(k);

                text = s;

                if (flag)
                    moveCursorBy(num);
            }
        }
    }

    public int getNthWordFromCursor(int n) {
        return getNthWordFromPos(n, getCursorPosition());
    }

    public int getNthWordFromPos(int n, int pos) {
        return func_73798_a(n, getCursorPosition(), true);
    }

    public int func_73798_a(int n, int pos, boolean skipSpaces) {
        int k = pos;
        boolean flag1 = n < 0;
        int l = Math.abs(n);

        for (int i1 = 0; i1 < l; ++i1)
            if (flag1) {
                while (skipSpaces && k > 0 && text.charAt(k - 1) == 32)
                    --k;

                while (k > 0 && text.charAt(k - 1) != 32)
                    --k;
            } else {
                int j1 = text.length();
                k = text.indexOf(32, k);

                if (k == -1)
                    k = j1;
                else
                    while (skipSpaces && k < j1 && text.charAt(k) == 32)
                        ++k;
            }

        return k;
    }

    public void moveCursorBy(int num) {
        setCursorPosition(selectionEnd + num);
    }

    public void setCursorPositionZero() {
        setCursorPosition(0);
    }

    public void setCursorPositionEnd() {
        setCursorPosition(text.length());
    }

    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        if (isEnabled && isFocused)
            switch (typedChar) {
                case 1:
                    setCursorPositionEnd();
                    setSelectionPos(0);
                    return true;
                case 3:
                    Minecraft.getInstance().keyboardHandler.setClipboard(getSelectedtext());
                    return true;
                case 22:
                    writeText(Minecraft.getInstance().keyboardHandler.getClipboard());
                    return true;
                case 24:
                    Minecraft.getInstance().keyboardHandler.setClipboard(getSelectedtext());
                    writeText("");
                    return true;
                default:
                    switch (keyCode) {
                        case GLFW.GLFW_KEY_BACKSPACE:
                            if (Screen.hasControlDown())
                                deleteWords(-1);
                            else
                                deleteFromCursor(-1);
                            return true;
                        case GLFW.GLFW_KEY_HOME:
                            if (Screen.hasShiftDown())
                                setSelectionPos(0);
                            else
                                setCursorPositionZero();
                            return true;
                        case GLFW.GLFW_KEY_LEFT:
                            if (Screen.hasShiftDown())
                                if (Screen.hasControlDown())
                                    setSelectionPos(getNthWordFromPos(-1, getSelectionEnd()));
                                else
                                    setSelectionPos(getSelectionEnd() - 1);
                            else if (Screen.hasControlDown())
                                setCursorPosition(getNthWordFromCursor(-1));
                            else
                                moveCursorBy(-1);
                            return true;
                        case GLFW.GLFW_KEY_RIGHT:
                            if (Screen.hasShiftDown())
                                if (Screen.hasControlDown())
                                    setSelectionPos(getNthWordFromPos(1, getSelectionEnd()));
                                else
                                    setSelectionPos(getSelectionEnd() + 1);
                            else if (Screen.hasControlDown())
                                setCursorPosition(getNthWordFromCursor(1));
                            else
                                moveCursorBy(1);
                            return true;
                        case GLFW.GLFW_KEY_END:
                            if (Screen.hasShiftDown())
                                setSelectionPos(text.length());
                            else
                                setCursorPositionEnd();
                            return true;
                        case GLFW.GLFW_KEY_DELETE:
                            if (Screen.hasControlDown())
                                deleteWords(1);
                            else
                                deleteFromCursor(1);
                            return true;
                        default:
                            if (SharedConstants.isAllowedChatCharacter(typedChar)) {
                                writeText(Character.toString(typedChar));
                                return true;
                            } else
                                return false;
                    }
            }
        else
            return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean flag = mouseX >= xPos && mouseX < xPos + width && mouseY >= yPos && mouseY < yPos + height;

        if (canLoseFocus)
            setFocused(isEnabled && flag);

        if (isFocused && (button == 0 || button == 1)) {
            int l = (int) mouseX - xPos;

            if (enableBackgroundDrawing)
                l -= 4;

            String s = fontRenderer.plainSubstrByWidth(text.substring(lineScrollOffset), getWidth());
            setCursorPosition(fontRenderer.plainSubstrByWidth(s, l).length() + lineScrollOffset);

            if (flag && (Screen.hasShiftDown())) {
                if (button == 0)
                    if (text.equals("true"))
                        text = "false";
                    else if (text.equals("false"))
                        text = "true";
                    else
                        text = originalText;
                else if (button == 1)
                    text = defaultText;

                setCursorPosition(0);
                updateChange();
            }
        }
        return flag;
    }

    public void drawTextBox(MatrixStack matrixStack) {
        if (getVisible()) {
            updateCursorCounter();
            if (getEnableBackgroundDrawing()) {
                fill(matrixStack, xPos - 1, yPos - 1, xPos + width + 1, yPos + height + 1, isEnabled ? isFocused ? hasChanged ? 0xFFAACCFF : 0xFF88AADD : hasChanged ? 0xFF75AA75 : 0xFFAAAAAA : 0xFFAA6060);
                fill(matrixStack, xPos, yPos, xPos + width, yPos + height, 0x70000000);
            }

            int i = isEnabled ? isFocused ? hasChanged ? 0xDDDDFF : enabledColor : hasChanged ? 0x90FF90 : enabledColor : disabledColor;
            int j = cursorPosition - lineScrollOffset;
            int k = selectionEnd - lineScrollOffset;
            String s = fontRenderer.plainSubstrByWidth(text.substring(lineScrollOffset), getWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = isFocused && cursorCounter / 40 % 2 == 0 && flag;
            int l = enableBackgroundDrawing ? xPos + 4 : xPos;
            int i1 = enableBackgroundDrawing ? yPos + (height - 8) / 2 : yPos;
            int j1 = l;

            if (k > s.length())
                k = s.length();

            if (s.length() > 0) {
                String s1 = flag ? s.substring(0, j) : s;
                j1 = fontRenderer.drawShadow(matrixStack, s1, l, i1, i);
            }

            boolean flag2 = cursorPosition < text.length() || text.length() >= getMaxStringLength();
            int k1 = j1;

            if (!flag)
                k1 = j > 0 ? l + width : l;
            else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }

            if (s.length() > 0 && flag && j < s.length())
                fontRenderer.drawShadow(matrixStack, s.substring(j), j1, i1, i);

            if (flag1) {
                if (flag2)
                    AbstractGui.fill(matrixStack, k1, i1 - 1, k1 + 1, i1 + 1 + fontRenderer.lineHeight, -3092272);
                else
                    fontRenderer.drawShadow(matrixStack, "_", k1, i1, i);
            }

            if (k != j) {
                int l1 = l + fontRenderer.width(s.substring(0, k));
                drawCursorVertical(matrixStack, k1, i1 - 1, l1 - 1, i1 + 1 + fontRenderer.lineHeight);
            }
        }
    }

    private void drawCursorVertical(MatrixStack matrixStack, int startX, int startY, int endX, int endY) {
        int i1;

        if (startX < endX) {
            i1 = startX;
            startX = endX;
            endX = i1;
        }

        if (startY < endY) {
            i1 = startY;
            startY = endY;
            endY = i1;
        }

        RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        fill(matrixStack, startX, endY, endX, startY, 0xFF0000FF);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    public int getMaxStringLength() {
        return maxStringLength;
    }

    public void setMaxStringLength(int length) {
        maxStringLength = length;
        if (text.length() > length)
            text = text.substring(0, length);
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(int pos) {
        cursorPosition = pos;
        int j = text.length();

        if (cursorPosition < 0)
            cursorPosition = 0;

        if (cursorPosition > j)
            cursorPosition = j;

        setSelectionPos(cursorPosition);
    }

    public boolean getEnableBackgroundDrawing() {
        return enableBackgroundDrawing;
    }

    public void setEnableBackgroundDrawing(boolean enable) {
        enableBackgroundDrawing = enable;
    }

    public void setTextColor(int color) {
        enabledColor = color;
    }

    public void setDisabledTextColour(int color) {
        disabledColor = color;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setFocused(boolean focused) {
        if (focused && !isFocused)
            cursorCounter = 0;
        isFocused = focused;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getSelectionEnd() {
        return selectionEnd;
    }

    public int getWidth() {
        return getEnableBackgroundDrawing() ? width - 8 : width;
    }

    public void setSelectionPos(int pos) {
        int j = text.length();

        if (pos > j)
            pos = j;

        if (pos < 0)
            pos = 0;

        selectionEnd = pos;

        if (fontRenderer != null) {
            if (lineScrollOffset > j)
                lineScrollOffset = j;

            int k = getWidth();
            String s = fontRenderer.plainSubstrByWidth(text.substring(lineScrollOffset), k);
            int l = s.length() + lineScrollOffset;

            if (pos == lineScrollOffset)
                lineScrollOffset -= fontRenderer.plainSubstrByWidth(text, k, true).length();

            if (pos > l)
                lineScrollOffset += pos - l;
            else if (pos <= lineScrollOffset)
                lineScrollOffset -= lineScrollOffset - pos;

            if (lineScrollOffset < 0)
                lineScrollOffset = 0;

            if (lineScrollOffset > j)
                lineScrollOffset = j;
        }
    }

    public void setCanLoseFocus(boolean canLose) {
        canLoseFocus = canLose;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean isVisible) {
        visible = isVisible;
    }
}