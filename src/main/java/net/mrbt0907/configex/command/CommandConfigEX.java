package net.mrbt0907.configex.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.manager.FieldInstance;
import net.mrbt0907.configex.network.NetworkHandler;

public class CommandConfigEX {
    private static final SuggestionProvider<CommandSource> CLIENT_SERVER_SUGGEST = (ctx, builder) ->
            ISuggestionProvider.suggest(new String[]{"client", "server"}, builder);
    private static final SuggestionProvider<CommandSource> FIELD_SUGGEST = (ctx, builder) ->
            ISuggestionProvider.suggest(ConfigManager.getFieldIDs(), builder);

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("configex")
                .executes(ctx -> {
                    say(ctx.getSource(), TextFormatting.RED + "Usage: /configex <config/default/get/set>");
                    return 1;
                })
                .then(Commands.literal("get")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .suggests(CLIENT_SERVER_SUGGEST)
                                .then(Commands.argument("field", StringArgumentType.greedyString())
                                        .suggests(FIELD_SUGGEST)
                                        .executes(ctx -> {
                                            String mode = StringArgumentType.getString(ctx, "mode");
                                            String field = StringArgumentType.getString(ctx, "field");
                                            get(ctx.getSource(), mode.equalsIgnoreCase("server"), field);
                                            return 1;
                                        }))))
                .then(Commands.literal("set")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .suggests(CLIENT_SERVER_SUGGEST)
                                .then(Commands.argument("field", StringArgumentType.word())
                                        .suggests(FIELD_SUGGEST)
                                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    String mode = StringArgumentType.getString(ctx, "mode");
                                                    String field = StringArgumentType.getString(ctx, "field");
                                                    String value = StringArgumentType.getString(ctx, "value");
                                                    set(ctx.getSource(), mode.equalsIgnoreCase("server"), field, value);
                                                    return 1;
                                                })))))
                .then(Commands.literal("default")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .suggests(CLIENT_SERVER_SUGGEST)
                                .then(Commands.argument("field", StringArgumentType.greedyString())
                                        .suggests(FIELD_SUGGEST)
                                        .executes(ctx -> {
                                            String mode = StringArgumentType.getString(ctx, "mode");
                                            String field = StringArgumentType.getString(ctx, "field");
                                            setDefault(ctx.getSource(), mode.equalsIgnoreCase("server"), field);
                                            return 1;
                                        }))))
                .then(Commands.literal("config")
                        .executes(ctx -> {
                            if (ctx.getSource().getEntity() instanceof ServerPlayerEntity) {
                                say(ctx.getSource(), "Opening config gui...");
                                sendPacket(5, new CompoundNBT(), ctx.getSource());
                            } else
                                say(ctx.getSource(), TextFormatting.RED + "You cannot run this sub command in the console");
                            return 1;
                        }));

        dispatcher.register(builder);
    }

    private static void setDefault(CommandSource source, boolean toServer, String registryName) {
        int permissionLevel = getPermission(source);
        String[] registryNames = registryName.split(":");

        if (registryNames.length > 1) {
            if (toServer && !ConfigManager.isRemote) {
                FieldInstance field = ConfigManager.getFieldInstance(registryNames[0], registryName);

                if (field == null)
                    say(source, TextFormatting.RED + registryName + " does not exist");
                else if (field.hasPermission(permissionLevel) || !field.enforce && !field.hide) {
                    field.setToDefault();
                    ConfigManager.save(field.config.getName(), field.registryName);
                    say(source, field.name + " was set to default successfully! Value: " + field.getRealCachedValue());
                } else
                    say(source, TextFormatting.RED + field.name + " requires a higher permission level to set to default");
            } else {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putString("config", registryNames[0]);
                nbt.putString("field", registryName);
                sendPacket(3, nbt, source);
            }
        } else
            say(source, TextFormatting.RED + registryName + " is not a valid registry name");
    }

    private static void set(CommandSource source, boolean toServer, String registryName, String value) {
        int permissionLevel = getPermission(source);
        String[] registryNames = registryName.split(":");

        if (registryNames.length > 1) {
            if (toServer) {
                FieldInstance field = ConfigManager.getFieldInstance(registryNames[0], registryName);

                if (field == null)
                    say(source, TextFormatting.RED + registryName + " does not exist");
                else if (field.hasPermission(permissionLevel) || !field.enforce && !field.hide)
                    if (field.setServerValue(value)) {
                        ConfigManager.save(field.config.getName(), field.registryName);
                        say(source, field.name + " was set successfully! Value: " + field.getRealCachedValue());
                    } else
                        say(source, TextFormatting.RED + field.name + " was not set successfully");
                else
                    say(source, TextFormatting.RED + field.name + " requires a higher permission level to set");
            } else {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putString("config", registryNames[0]);
                nbt.putString("field", registryName);
                nbt.putString("value", value);
                sendPacket(2, nbt, source);
            }
        } else
            say(source, TextFormatting.RED + registryName + " is not a valid registry name");
    }

    private static void get(CommandSource source, boolean fromServer, String registryName) {
        int permissionLevel = getPermission(source);
        String[] registryNames = registryName.split(":");

        if (registryNames.length > 1) {
            if (fromServer) {
                FieldInstance field = ConfigManager.getFieldInstance(registryNames[0], registryName);
                if (field == null)
                    say(source, TextFormatting.RED + registryName + " does not exist");
                else if (field.hasPermission(permissionLevel) || !field.enforce && !field.hide)
                    say(source, "Field " + field.name + "\n-----  -----\nFrom: " + field.config.getName() + "\nDefault Value: " + field.defaultValue + "\nCurrent Value: " + field.getRealCachedValue());
                else
                    say(source, TextFormatting.RED + field.name + " requires a higher permission level to access");
            } else {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putString("config", registryNames[0]);
                nbt.putString("field", registryName);
                sendPacket(1, nbt, source);
            }
        } else
            say(source, TextFormatting.RED + registryName + " is not a valid registry name");
    }

    private static int getPermission(CommandSource source) {
        return source.getEntity() instanceof ServerPlayerEntity ?
                source.getServer().isSingleplayer() ? 4 :
                        source.getServer().getPlayerList().getOps().get(((ServerPlayerEntity) source.getEntity()).getGameProfile()) != null ?
                                source.getServer().getPlayerList().getOps().get(((ServerPlayerEntity) source.getEntity()).getGameProfile()).getLevel() : 0
                : 4;
    }

    private static void sendPacket(int index, CompoundNBT nbt, CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            say(source, TextFormatting.RED + "You cannot run this sub command in the console");
            return;
        }

        nbt.putString("command", "configex");
        NetworkHandler.sendClientPacket(index, nbt, source);
    }

    private static void say(CommandSource source, String message) {
        source.sendSuccess(new StringTextComponent(message), false);
    }
}