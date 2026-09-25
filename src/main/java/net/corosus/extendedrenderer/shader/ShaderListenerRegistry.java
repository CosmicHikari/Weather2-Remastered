package net.corosus.extendedrenderer.shader;

import java.util.ArrayList;
import java.util.List;

public class ShaderListenerRegistry {

    public static List<IShaderListener> listeners = new ArrayList<>();

    public static void addListener(IShaderListener listener) {
        listeners.add(listener);
    }

    public static void postInit() {
        listeners.forEach(entry -> entry.init());
    }

    public static void postReset() {
        listeners.forEach(entry -> entry.reset());
    }

}
