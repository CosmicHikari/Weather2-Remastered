package net.corosus.coroutillegacy.util;

import net.minecraft.command.ICommandSource;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

public class CoroUtilMisc {

    public static void sendCommandSenderMsg(ICommandSource entP, String msg) {
        entP.sendMessage(new StringTextComponent(msg), Util.NIL_UUID);

    }

    public static float adjVal(float source, float target, float adj) {
        if (source < target) {
            source += adj;

            if (source > target) {
                source = target;
            }
        } else if (source > target) {
            source -= adj;

            if (source < target) {
                source = target;
            }
        }
        return source;
    }
}