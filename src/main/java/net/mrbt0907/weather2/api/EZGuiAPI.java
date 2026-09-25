package net.mrbt0907.weather2.api;

import net.minecraftforge.common.MinecraftForge;
import net.mrbt0907.weather2.api.event.EventRegisterEZGuiOption;
import net.mrbt0907.weather2.util.TriMapEx;

import java.util.*;

public class EZGuiAPI {
    public static final String[] BUTTON_LIST = {"button.on", "button.off", "button.both", "button.none", "button.exit", "button.advanced", "button.reset", "button.next", "button.previous", "button.highest", "button.veryhigh", "button.high", "button.medium", "button.low", "button.verylow", "button.lowest", "button.highest.alt", "button.lowest.alt", "button.mostcommon", "button.verycommon", "button.common", "button.normal", "button.rare", "button.veryrare", "button.realistic", "graphics", "system", "storms", "dimensions"};
    public static final List<String> BL_WTOGGLE = new ArrayList<String>(Arrays.asList("button.off.w", "button.on.w"));
    public static final List<String> BL_ETOGGLE = new ArrayList<String>(Arrays.asList("button.off.e", "button.on.e"));
    public static final List<String> BL_SHADERS = new ArrayList<String>(Arrays.asList(BUTTON_LIST[1], "button.shader.1", "button.shader.2", "button.shader.3", "button.shader.4", "button.shader.5", "button.shader.6", "button.shader.7", "button.shader.8", "button.shader.9", "button.shader.10", "button.shader.11"));
    public static final List<String> BL_TOGGLE = new ArrayList<String>(Arrays.asList(BUTTON_LIST[1], BUTTON_LIST[0]));
    public static final List<String> BL_STR = new ArrayList<String>(Arrays.asList(BUTTON_LIST[17], BUTTON_LIST[14], BUTTON_LIST[13], BUTTON_LIST[12], BUTTON_LIST[11], BUTTON_LIST[10], BUTTON_LIST[16]));
    public static final List<String> BL_STR_ALT = new ArrayList<String>(Arrays.asList(BUTTON_LIST[3], BUTTON_LIST[15], BUTTON_LIST[14], BUTTON_LIST[13], BUTTON_LIST[12], BUTTON_LIST[11], BUTTON_LIST[10], BUTTON_LIST[9]));
    public static final List<String> BL_RARE = new ArrayList<String>(Arrays.asList(BUTTON_LIST[24], BUTTON_LIST[23], BUTTON_LIST[22], BUTTON_LIST[21], BUTTON_LIST[20], BUTTON_LIST[19], BUTTON_LIST[18]));
    public static final List<String> BL_RARE_ALT = new ArrayList<String>(Arrays.asList(BUTTON_LIST[3], BUTTON_LIST[24], BUTTON_LIST[23], BUTTON_LIST[22], BUTTON_LIST[21], BUTTON_LIST[20], BUTTON_LIST[19], BUTTON_LIST[18]));


    public static final int BUTTON_MIN = 8;
    public static final String BA_CLOUD = "a_cloud";
    public static final String BA_FUNNEL = "a_funnel";
    public static final String BA_PRECIPITATION = "a_precipitation";
    public static final String BA_EFFECT = "a_effect";
    public static final String BA_EF = "a_ef";
    public static final String BA_SHADER = "a_shader";
    public static final String BA_CUSTOMFOG = "a_customfog";
    public static final String BA_FOLIAGE = "a_foliage";
    public static final String BA_RENDER_DISTANCE = "a_render_distance";
    public static final String BA_FANCY_RENDERING = "a_fancy_rendering";
    public static final String BB_GLOBAL = "b_global";
    public static final String BA_RADAR = "a_radar";
    public static final String BC_ENABLE_TORNADO = "c_tornado";
    public static final String BC_ENABLE_CYCLONE = "c_cyclone";
    public static final String BC_ENABLE_SANDSTORM = "c_sandstorm";
    public static final String BC_FREQUENCY = "c_frequency";
    public static final String BC_GRAB_BLOCK = "c_grab_block";
    public static final String BC_GRAB_ITEM = "c_grab_item";
    public static final String BC_GRAB_MOB = "c_grab_mob";
    public static final String BC_GRAB_PLAYER = "c_grab_player";
    public static final String BC_STORM_PER_PLAYER = "c_storm_per_player";

    private static TriMapEx<String, List<String>, Integer> options = new TriMapEx<String, List<String>, Integer>();
    private static Map<String, Integer> optionCategories = new LinkedHashMap<String, Integer>();

    public static TriMapEx<String, List<String>, Integer> getOptions() {
        return options;
    }

    public static Map<String, Integer> getOptionCategories() {
        return optionCategories;
    }

    public static void refreshOptions() {
        options.clear();
        optionCategories.clear();
        options.put(BA_CLOUD, BL_STR, 3);
        options.put(BA_FUNNEL, BL_STR, 3);
        options.put(BA_PRECIPITATION, BL_STR_ALT, 3);
        options.put(BA_EFFECT, BL_STR_ALT, 3);
        options.put(BA_EF, BL_TOGGLE, 0);
        options.put(BA_SHADER, BL_SHADERS, 0);
        options.put(BA_CUSTOMFOG, BL_TOGGLE, 1);
        options.put(BA_FOLIAGE, BL_TOGGLE, 0);
        options.put(BA_RENDER_DISTANCE, BL_STR_ALT, 4);
        options.put(BA_FANCY_RENDERING, BL_TOGGLE, 0);
        options.put(BB_GLOBAL, BL_TOGGLE, 0);
        options.put(BA_RADAR, BL_TOGGLE, 0);
        options.put(BC_ENABLE_TORNADO, BL_TOGGLE, 1);
        options.put(BC_ENABLE_CYCLONE, BL_TOGGLE, 1);
        options.put(BC_ENABLE_SANDSTORM, BL_TOGGLE, 1);
        options.put(BC_FREQUENCY, BL_RARE, 3);
        options.put(BC_GRAB_BLOCK, BL_TOGGLE, 1);
        options.put(BC_GRAB_ITEM, BL_TOGGLE, 0);
        options.put(BC_GRAB_MOB, BL_TOGGLE, 1);
        options.put(BC_GRAB_PLAYER, BL_TOGGLE, 1);
        options.put(BC_STORM_PER_PLAYER, BL_TOGGLE, 0);

        for (String key : options.keys()) {
            if (key.matches("^a_.*"))
                optionCategories.put(key, 0);
            else if (key.matches("^b_.*"))
                optionCategories.put(key, 1);
            else if (key.matches("^c_.*"))
                optionCategories.put(key, 2);
        }
        EventRegisterEZGuiOption event = new EventRegisterEZGuiOption(options, optionCategories);
        MinecraftForge.EVENT_BUS.post(event);
        options = event.getOptions();
        optionCategories = event.getOptionCategories();
    }
}
