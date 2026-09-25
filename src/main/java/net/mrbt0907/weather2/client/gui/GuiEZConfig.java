package net.mrbt0907.weather2.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.gui.GuiConfigEditor;
import net.mrbt0907.weather2.ClientProxy;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.EZGuiAPI;
import net.mrbt0907.weather2.client.gui.elements.GuiButtonBoolean;
import net.mrbt0907.weather2.client.gui.elements.GuiButtonCycle;
import net.mrbt0907.weather2.config.EZConfigParser;
import net.mrbt0907.weather2.network.packets.PacketEZGUI;
import net.mrbt0907.weather2.util.TriMapEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GuiEZConfig extends Screen {
    public static final String PREFIX = "btn_";
    public static final int ID_EXIT = 0;
    public static final int ID_ADVANCED = 1;
    public static final int ID_GRAPHICS = 2;
    public static final int ID_SYSTEM = 3;
    public static final int ID_STORM = 4;
    public static final int ID_DIMENSION = 5;
    public static final int ID_NEXT = 6;
    public static final int ID_PREVIOUS = 7;
    private static final String[] PAGEL = {"graphics", "system", "storms", "dimensions"};
    @SuppressWarnings("unused")
    private static final String[] FLAGL = {"flag.op", "flag.reload"};
    private final Map<Button, Integer> buttonIdMap = new HashMap<>();
    public int xCenter;
    public int yCenter;
    public int xStart;
    public int yStart;
    public ResourceLocation backgroundA = new ResourceLocation(Weather2.OLD_MODID + ":textures/gui/ez_gui_2.png");
    public ResourceLocation[] backgroundB = new ResourceLocation[0];
    public int page = 0;
    public int subPage = 0;
    public int maxSubPages = 0;
    public int maxEntries = 6;
    public HashMap<Integer, String> buttonsMap = new HashMap<Integer, String>();
    public CompoundNBT nbtSendCache;
    protected int xSize = 256;
    protected int ySize = 256;
    List<String> settings = new ArrayList<String>();
    private int currentContentSize = 0;
    private boolean send = false;

    public GuiEZConfig() {
        super(new StringTextComponent("Weather2 EZ Config"));
        EZConfigParser.nbtRealServerData = new CompoundNBT();
        PacketEZGUI.sync();
        nbtSendCache = new CompoundNBT();
        ClientProxy.clientTickHandler.op = ConfigManager.getPermissionLevel() > 3;
        EZGuiAPI.refreshOptions();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void removed() {
        super.removed();
    }

    public void resetGuiElements() {
        buttonsMap.clear();
        buttonIdMap.clear();
        this.buttons.clear();
        this.children.clear();
        currentContentSize = 0;
    }

    public void drawBackground(MatrixStack matrixStack) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(backgroundA);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        String title = format("title") + " - " + format(PAGEL[page]);

        renderBackground(matrixStack);
        blit(matrixStack, x, y, 0, 0, xSize, ySize);

        drawString(matrixStack, this.font, title,
                (int) ((xStart + 128) - this.font.width(title) * 0.5F),
                yStart + 15, 0xFFFFFF);
    }

    public void drawElements(MatrixStack matrixStack) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int buttonRowBX = 14;
        int buttonRowBY = 85;
        int buttonHeight = 25;

        if (page != 3) {
            for (int i = 0; i < currentContentSize; i++) {
                int id = i + EZGuiAPI.BUTTON_MIN + (maxEntries * subPage);
                String entry = buttonsMap.get(id);
                if (entry == null) continue;
                drawString(matrixStack, this.font,
                        format("button." + entry + ".tooltip"),
                        xStart + buttonRowBX,
                        yStart + buttonRowBY + (buttonHeight * i),
                        0xFFFFFF);
            }
        } else {
            for (int i = 0; i < currentContentSize; i++) {
                int id = EZGuiAPI.BUTTON_MIN + (i + maxEntries * subPage) * 2;
                String entry = buttonsMap.get(id);
                if (entry == null) continue;
                drawString(matrixStack, this.font,
                        capitalize(entry.replaceAll("\\_", " ")),
                        xStart + buttonRowBX,
                        yStart + buttonRowBY + (buttonHeight * i),
                        0xFFFFFF);
            }
        }

        if (maxSubPages > 0)
            drawString(matrixStack, this.font,
                    format("misc.page", subPage + 1, maxSubPages + 1),
                    xStart + 46, yStart + 238, 0xFFFFFF);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        StringBuilder result = new StringBuilder(str.length());
        boolean capitalizeNext = true;
        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    protected <T extends Button> T addButton(T button, int id, String localization) {
        buttonIdMap.put(button, id);
        buttonsMap.put(id, localization);
        return addButton(button, id);
    }

    protected <T extends Button> T addButton(T button, int id) {
        buttonIdMap.put(button, id);
        return super.addButton(button);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        drawBackground(matrixStack);
        drawElements(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        try {
            return super.keyPressed(keyCode, scanCode, modifiers);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        try {
            return super.mouseClicked(mouseX, mouseY, button);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void init() {
        super.init();
        if (send) send();
        resetGuiElements();

        MainWindow window = minecraft.getWindow();
        int scaledWidth = window.getGuiScaledWidth();
        int scaledHeight = window.getGuiScaledHeight();
        int buttonWidth = 56;
        int buttonHeight = 20;
        int buttonRowAY = 48;
        int buttonRowBX = 187;
        int buttonRowBY = 80;
        int buttonRowCY = 232;

        xSize = 256;
        ySize = 256;
        xCenter = (int) (scaledWidth * 0.5F);
        yCenter = (int) (scaledHeight * 0.5F);
        xStart = (int) (xCenter - xSize * 0.5F);
        yStart = (int) (yCenter - ySize * 0.5F);
        maxSubPages = 1;

        addButton(new Button(xStart + 193, yStart + buttonRowCY, buttonWidth, buttonHeight,
                new StringTextComponent(format(EZGuiAPI.BUTTON_LIST[4])), btn -> onPress(btn)), ID_EXIT);
        addButton(new Button(xStart + 110, yStart + buttonRowCY, buttonWidth + 20, buttonHeight,
                new StringTextComponent(format(EZGuiAPI.BUTTON_LIST[5])), btn -> onPress(btn)), ID_ADVANCED);
        addButton(new Button(xStart + 8, yStart + buttonRowAY, buttonWidth, buttonHeight,
                new StringTextComponent(format(EZGuiAPI.BUTTON_LIST[25])), btn -> onPress(btn)), ID_GRAPHICS);
        addButton(new Button(xStart + 69, yStart + buttonRowAY, buttonWidth, buttonHeight,
                new StringTextComponent(format(EZGuiAPI.BUTTON_LIST[26])), btn -> onPress(btn)), ID_SYSTEM);
        addButton(new Button(xStart + 130, yStart + buttonRowAY, buttonWidth, buttonHeight,
                new StringTextComponent(format(EZGuiAPI.BUTTON_LIST[27])), btn -> onPress(btn)), ID_STORM);
        addButton(new Button(xStart + 191, yStart + buttonRowAY, buttonWidth, buttonHeight,
                new StringTextComponent(format(EZGuiAPI.BUTTON_LIST[28])), btn -> onPress(btn)), ID_DIMENSION);

        int size = 0;
        int startingIndex = maxEntries * subPage;

        if (page == 0 || EZConfigParser.isOp() || ConfigManager.isSinglePlayer()) {
            try {
                switch (page) {
                    case 3: {
                        size = EZConfigParser.dimNames.size();
                        currentContentSize = Math.min(size - startingIndex, maxEntries);
                        maxSubPages = size / this.maxEntries;
                        Object[] keys = EZConfigParser.dimNames.keySet().toArray();
                        Object[] values = EZConfigParser.dimNames.values().toArray();
                        int iii = 0;
                        if (ClientProxy.clientTickHandler.op) {
                            for (int i = startingIndex; i < size && i - startingIndex < this.maxEntries; i++) {
                                int heightOffset = i - startingIndex;
                                int ii = i + iii + EZGuiAPI.BUTTON_MIN + startingIndex;
                                String dimPath = (String) keys[i];
                                addButton(new GuiButtonCycle(
                                                xStart + buttonRowBX - (buttonWidth + 5),
                                                yStart + buttonRowBY + (buttonHeight + 5) * heightOffset,
                                                buttonWidth, buttonHeight,
                                                EZGuiAPI.BL_WTOGGLE,
                                                EZConfigParser.isWeatherEnabled(dimPath) ? 1 : 0,
                                                btn -> onPress(btn)),
                                        ii, dimPath);
                                addButton(new GuiButtonCycle(
                                                xStart + buttonRowBX,
                                                yStart + buttonRowBY + (buttonHeight + 5) * heightOffset,
                                                buttonWidth, buttonHeight,
                                                EZGuiAPI.BL_ETOGGLE,
                                                EZConfigParser.isEffectsEnabled(dimPath) ? 1 : 0,
                                                btn -> onPress(btn)),
                                        ii + 1, dimPath);
                                iii++;
                            }
                        }
                        break;
                    }
                    default: {
                        TriMapEx<String, List<String>, Integer> options = EZGuiAPI.getOptions();
                        Map<String, Integer> categories = EZGuiAPI.getOptionCategories();
                        settings.clear();

                        for (Entry<String, Integer> entry : categories.entrySet())
                            if (entry.getValue() == page)
                                settings.add(entry.getKey());

                        size = settings.size();
                        currentContentSize = Math.min(size - startingIndex, maxEntries);
                        maxSubPages = ((size - 1) / this.maxEntries);

                        for (int i = 0; i < size && i - startingIndex < this.maxEntries; i++) {
                            if (i >= startingIndex) {
                                String id = settings.get(i);
                                int btnId = i + EZGuiAPI.BUTTON_MIN;
                                addButton(new GuiButtonCycle(
                                                xStart + buttonRowBX,
                                                yStart + buttonRowBY + (buttonHeight + 5) * (i - startingIndex),
                                                buttonWidth, buttonHeight,
                                                options.getA(id),
                                                EZConfigParser.getConfigValue(id),
                                                btn -> onPress(btn)),
                                        btnId, id);
                            }
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (maxSubPages > 0) {
            if (subPage < maxSubPages)
                addButton(new Button(xStart + 67, yStart + buttonRowCY, buttonWidth - 20, buttonHeight,
                        new StringTextComponent(format(EZGuiAPI.BUTTON_LIST[7])), btn -> onPress(btn)), ID_NEXT);
            if (subPage > 0)
                addButton(new Button(xStart + 7, yStart + buttonRowCY, buttonWidth - 20, buttonHeight,
                        new StringTextComponent(format(EZGuiAPI.BUTTON_LIST[8])), btn -> onPress(btn)), ID_PREVIOUS);
        }
    }

    private void onPress(Button button) {
        Integer buttonId = buttonIdMap.get(button);
        if (buttonId == null) {
            Weather2.warn("Pressed button has no registered ID!");
            return;
        }

        int index = buttonId - EZGuiAPI.BUTTON_MIN;

        if (button instanceof GuiButtonBoolean) {
            GuiButtonBoolean boolBtn = (GuiButtonBoolean) button;
            boolBtn.setBooleanToggle();
            int toggleValue = boolBtn.getBoolean() ? 1 : 0;
            switch (page) {
                case 0:
                    if (!nbtSendCache.contains("client"))
                        nbtSendCache.put("client", new CompoundNBT());
                    nbtSendCache.getCompound("client").putInt(PREFIX + settings.get(index), toggleValue);
                    break;
                case 3: {
                    String dimPath = buttonsMap.get(buttonId);
                    String dimKey = (buttonId - EZGuiAPI.BUTTON_MIN) % 2 == 0
                            ? "dimb_" + dimPath
                            : "dimc_" + dimPath;
                    if (!nbtSendCache.contains("server"))
                        nbtSendCache.put("server", new CompoundNBT());
                    if (!nbtSendCache.getCompound("server").contains("dimData"))
                        nbtSendCache.getCompound("server").put("dimData", new CompoundNBT());
                    nbtSendCache.getCompound("server").getCompound("dimData").putInt(dimKey, toggleValue);
                    break;
                }
                default:
                    if (!nbtSendCache.contains("server"))
                        nbtSendCache.put("server", new CompoundNBT());
                    nbtSendCache.getCompound("server").putInt(PREFIX + settings.get(index), toggleValue);
            }
        } else if (button instanceof GuiButtonCycle) {
            GuiButtonCycle cycleBtn = (GuiButtonCycle) button;
            cycleBtn.cycleIndex();
            switch (page) {
                case 0:
                    if (!nbtSendCache.contains("client"))
                        nbtSendCache.put("client", new CompoundNBT());
                    nbtSendCache.getCompound("client").putInt(PREFIX + settings.get(index), cycleBtn.index);
                    break;
                case 3: {
                    String dimPath = buttonsMap.get(buttonId);
                    String dimKey = (buttonId - EZGuiAPI.BUTTON_MIN) % 2 == 0
                            ? "dimb_" + dimPath
                            : "dimc_" + dimPath;
                    if (!nbtSendCache.contains("server"))
                        nbtSendCache.put("server", new CompoundNBT());
                    if (!nbtSendCache.getCompound("server").contains("dimData"))
                        nbtSendCache.getCompound("server").put("dimData", new CompoundNBT());
                    nbtSendCache.getCompound("server").getCompound("dimData").putInt(dimKey, cycleBtn.index);
                    break;
                }
                default:
                    if (!nbtSendCache.contains("server"))
                        nbtSendCache.put("server", new CompoundNBT());
                    nbtSendCache.getCompound("server").putInt(PREFIX + settings.get(index), cycleBtn.index);
            }
        }

        switch (buttonId) {
            case ID_EXIT:
                minecraft.setScreen(null);
                if (send) send();
                break;
            case ID_ADVANCED:
                if (send) send();
                minecraft.setScreen(new GuiConfigEditor());
                break;
            case ID_GRAPHICS:
            case ID_SYSTEM:
            case ID_STORM:
            case ID_DIMENSION:
                page = buttonId == ID_GRAPHICS ? 0
                        : buttonId == ID_SYSTEM ? 1
                        : buttonId == ID_STORM ? 2 : 3;
                subPage = 0;
                init();
                break;
            case ID_NEXT:
                if (subPage < maxSubPages) subPage++;
                init();
                break;
            case ID_PREVIOUS:
                if (subPage > 0) subPage--;
                init();
                break;
            default:
                if (buttonId > -1)
                    send = true;
                else
                    Weather2.warn("Unknown GUI button was pressed (ID:" + buttonId + ")");
        }
    }

    public int sanitize(int val) {
        return sanitize(val, 0, 9999);
    }

    public int sanitize(int val, int min, int max) {
        if (val > max) val = max;
        if (val < min) val = min;
        return val;
    }

    private void send() {
        Weather2.debug("Preparing to send packets... " + nbtSendCache);
        ClientProxy.clientTickHandler.op = ConfigManager.getPermissionLevel() > 3;
        if (nbtSendCache.contains("client")) {
            Weather2.debug("Sending config packet to client");
            nbtSendCache.getCompound("client").putInt("server", 0);
            EZConfigParser.nbtReceiveClient(nbtSendCache.getCompound("client"));
            nbtSendCache.remove("client");
        }
        if (nbtSendCache.contains("server") && ClientProxy.clientTickHandler.op) {
            Weather2.debug("Sending config packet to server");
            PacketEZGUI.apply(nbtSendCache.getCompound("server"));
            nbtSendCache.remove("server");
        }
        send = false;
        PacketEZGUI.sync();
    }

    private String format(String local, Object... args) {
        return I18n.get("config.ezgui." + local, args);
    }
}