package net.mrbt0907.weather2.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.config.ConfigGrab;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.EZConfigParser;
import net.mrbt0907.weather2.event.ServerTickHandler;
import net.mrbt0907.weather2.network.packets.PacketRefresh;
import net.mrbt0907.weather2.network.packets.PacketShader;
import net.mrbt0907.weather2.network.packets.PacketVolcanoObject;
import net.mrbt0907.weather2.network.packets.PacketWeatherObject;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.ReflectionHelper;
import net.mrbt0907.weather2.weather.WeatherManagerServer;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.StormObject.StormType;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;

import javax.annotation.Nullable;
import java.util.List;

public class CommandWeather2 {
    private static final SuggestionProvider<CommandSource> SUGGEST_CONFIG_SUBCOMMANDS = (context, builder) ->
            ISuggestionProvider.suggest(new String[]{"refresh", "grablist"}, builder);

    private static final SuggestionProvider<CommandSource> SUGGEST_CREATE_TYPES = (context, builder) ->
            ISuggestionProvider.suggest(new String[]{
                    "random", "clouds", "rainstorm", "thunderstorm", "supercell",
                    "tropicaldisturbance", "tropicaldepression", "tropicalstorm",
                    "sandstorm", "ef0", "ef1", "ef2", "ef3", "ef4", "ef5",
                    "f0", "f1", "f2", "f3", "f4", "f5",
                    "c1", "c2", "c3", "c4", "c5"
            }, builder);

    private static final SuggestionProvider<CommandSource> SUGGEST_KILL_TARGETS = (context, builder) ->
            ISuggestionProvider.suggest(new String[]{"all", "particles"}, builder);

    private static final SuggestionProvider<CommandSource> SUGGEST_TEST_TYPES = (context, builder) ->
            ISuggestionProvider.suggest(new String[]{"class", "volcano"}, builder);

    private static final SuggestionProvider<CommandSource> SUGGEST_REFRESH_TARGETS = (context, builder) ->
            ISuggestionProvider.suggest(new String[]{
                    "all", "dimensionlist", "grablist", "replacelist",
                    "stagelist", "windlist", "sounds", "scene"
            }, builder);

    private static final SuggestionProvider<CommandSource> SUGGEST_GRABLIST_ACTIONS = (context, builder) ->
            ISuggestionProvider.suggest(new String[]{"addGrabEntry", "addReplaceEntry", "addWindEntry"}, builder);

    private static final SuggestionProvider<CommandSource> SUGGEST_STORM_FLAGS = (context, builder) ->
            ISuggestionProvider.suggest(new String[]{
                    "alwaysprogress", "ishailing", "norain", "isviolent",
                    "isnatural", "isfirenado", "neverdissipate", "dontconvert"
            }, builder);

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        String commandName = ConfigMisc.command_weather2;

        LiteralArgumentBuilder<CommandSource> command = Commands.literal(commandName)
                .requires(source -> true)
                .executes(context -> {
                    sendMessage(context.getSource(), "usage");
                    return 1;
                });

        command.then(Commands.literal("config")
                .then(Commands.literal("refresh")
                        .then(Commands.argument("target", StringArgumentType.word())
                                .suggests(SUGGEST_REFRESH_TARGETS)
                                .executes(context -> executeConfigRefresh(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "target")
                                ))
                        )
                        .executes(context -> {
                            sendMessage(context.getSource(), "config.refresh.usage");
                            return 0;
                        })
                )
                .then(Commands.literal("grablist")
                        .then(Commands.argument("action", StringArgumentType.word())
                                .suggests(SUGGEST_GRABLIST_ACTIONS)
                                .then(Commands.argument("entry1", StringArgumentType.greedyString())
                                        .executes(context -> executeGrablistAction(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "action"),
                                                StringArgumentType.getString(context, "entry1")
                                        ))
                                )
                                .executes(context -> executeGrablistAction(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "action"),
                                        null
                                ))
                        )
                )
                .executes(context -> {
                    sendMessage(context.getSource(), "config.usage");
                    return 0;
                })
        );

        command.then(Commands.literal("create")
                .requires(source -> hasPermission(source, 4))
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests(SUGGEST_CREATE_TYPES)
                        .executes(context -> executeCreate(
                                context.getSource(),
                                StringArgumentType.getString(context, "type"),
                                null,
                                new String[0]
                        ))
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .executes(context -> executeCreate(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "type"),
                                        Vec3Argument.getVec3(context, "pos"),
                                        new String[0]
                                ))
                                .then(Commands.argument("flags", StringArgumentType.greedyString())
                                        .suggests(SUGGEST_STORM_FLAGS)
                                        .executes(context -> executeCreate(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "type"),
                                                Vec3Argument.getVec3(context, "pos"),
                                                StringArgumentType.getString(context, "flags").split(" ")
                                        ))
                                )
                        )
                )
                .executes(context -> {
                    sendMessage(context.getSource(), "create.usage");
                    return 0;
                })
        );

        command.then(Commands.literal("kill")
                .then(Commands.argument("target", StringArgumentType.word())
                        .suggests(SUGGEST_KILL_TARGETS)
                        .executes(context -> executeKill(
                                context.getSource(),
                                StringArgumentType.getString(context, "target")
                        ))
                )
                .executes(context -> {
                    sendMessage(context.getSource(), "kill.usage");
                    return 0;
                })
        );

        command.then(Commands.literal("view")
                .executes(context -> {
                    sendMessage(context.getSource(), "view.fail");
                    return 0;
                })
        );

        command.then(Commands.literal("test")
                .requires(source -> hasPermission(source, 4))
                .then(Commands.literal("volcano")
                        .executes(context -> executeTestVolcano(context.getSource()))
                )
                .then(Commands.literal("class")
                        .then(Commands.argument("className", StringArgumentType.greedyString())
                                .executes(context -> executeTestClass(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "className")
                                ))
                        )
                        .executes(context -> {
                            sendMessage(context.getSource(), "test.class.usage");
                            return 0;
                        })
                )
                .executes(context -> {
                    sendMessage(context.getSource(), "test.usage");
                    return 0;
                })
        );

        command.then(Commands.literal("reloadshaders")
                .executes(context -> executeReloadShaders(context.getSource()))
        );

        dispatcher.register(command);
    }

    private static boolean hasPermission(CommandSource source, int level) {
        try {
            ServerPlayerEntity player = source.getPlayerOrException();
            return ConfigManager.getPermissionLevel(player) >= level;
        } catch (CommandSyntaxException e) {
            return source.hasPermission(level);
        }
    }

    private static int executeConfigRefresh(CommandSource source, String target) throws CommandSyntaxException {
        switch (target.toLowerCase()) {
            case "all":
                WeatherAPI.refreshDimensionRules();
                WeatherAPI.refreshGrabRules();
                try {
                    PacketRefresh.resetSounds(source.getPlayerOrException());
                } catch (CommandSyntaxException e) {
                }
                sendMessage(source, "config.refresh.all.success");
                break;
            case "dimensionlist":
                WeatherAPI.refreshDimensionRules();
                sendMessage(source, "config.refresh.dimensionlist.success");
                break;
            case "grablist":
                WeatherAPI.refreshGrabRules();
                sendMessage(source, "config.refresh.grablist.success");
                break;
            case "replacelist":
                WeatherAPI.refreshGrabRules();
                sendMessage(source, "config.refresh.replacelist.success");
                break;
            case "stagelist":
                WeatherAPI.refreshStages();
                sendMessage(source, "config.refresh.stagelist.success");
                break;
            case "windlist":
                WeatherAPI.refreshGrabRules();
                sendMessage(source, "config.refresh.windlist.success");
                break;
            case "sounds":
            case "sound":
                try {
                    PacketRefresh.resetSounds(source.getPlayerOrException());
                    sendMessage(source, "config.refresh.sounds.success");
                } catch (CommandSyntaxException e) {
                    sendMessage(source, "config.refresh.sounds.fail");
                }
                break;
            case "scene":
            case "sceneenhancer":
                try {
                    PacketRefresh.resetSceneEnhancer(source.getPlayerOrException());
                    sendMessage(source, "config.refresh.sceneenhancer.success");
                } catch (CommandSyntaxException e) {
                    sendMessage(source, "config.refresh.sceneenhancer.fail");
                }
                break;
            default:
                sendMessage(source, "config.refresh.usage");
                return 0;
        }
        return 1;
    }

    private static int executeGrablistAction(CommandSource source, String action, @Nullable String entry1) {
        try {
            ServerPlayerEntity player = source.getPlayerOrException();
            Item itemMain = player.getMainHandItem().getItem();
            Block blockMain = itemMain != Items.AIR ? Block.byItem(itemMain) : null;
            String entryMain = entry1 != null ? entry1 : (blockMain != null ? blockMain.getRegistryName().toString() : "");

            switch (action.toLowerCase()) {
                case "addgrabentry":
                    if (entryMain.isEmpty()) {
                        sendMessage(source, "config.grablist.addgrabentry.fail");
                        return 0;
                    }
                    if (ConfigGrab.grab_list_entries.isEmpty())
                        ConfigGrab.grab_list_entries += entryMain;
                    else
                        ConfigGrab.grab_list_entries += ", " + entryMain;
                    sendMessage(source, "config.grablist.addgrabentry.success");
                    ConfigManager.save("Weather2 Remastered - Grab");
                    return 1;

                case "addreplaceentry": {
                    String entrySecondary = "";
                    if (entry1 != null) {
                        String[] parts = entry1.split("\\s+", 2);
                        entryMain = parts[0];
                        entrySecondary = parts.length > 1 ? parts[1] : "";
                    } else {
                        Item itemSecondary = player.getOffhandItem().getItem();
                        Block blockSecondary = itemSecondary != Items.AIR ? Block.byItem(itemSecondary) : null;
                        entrySecondary = blockSecondary != null ? blockSecondary.getRegistryName().toString() : "";
                    }

                    if (entryMain.isEmpty() || entrySecondary.isEmpty()) {
                        sendMessage(source, "config.grablist.addreplaceentry.fail");
                        return 0;
                    }
                    if (ConfigGrab.replace_list_entries.isEmpty())
                        ConfigGrab.replace_list_entries += entryMain + "=" + entrySecondary;
                    else
                        ConfigGrab.replace_list_entries += ", " + entryMain + "=" + entrySecondary;
                    sendMessage(source, "config.grablist.addreplaceentry.success");
                    ConfigManager.save("Weather2 Remastered - Grab");
                    return 1;
                }

                case "addwindentry": {
                    if (entry1 == null) {
                        sendMessage(source, "config.grablist.addwindentry.fail");
                        return 0;
                    }
                    String[] parts = entry1.split("\\s+", 2);
                    entryMain = parts[0];
                    if (entryMain.isEmpty() || parts.length < 2) {
                        sendMessage(source, "config.grablist.addwindentry.fail");
                        return 0;
                    }
                    String resistance = parts[1].replaceAll("[^\\d\\.]", "");
                    if (resistance.isEmpty()) {
                        sendMessage(source, "config.grablist.addwindentry.fail");
                        return 0;
                    }
                    if (ConfigGrab.wind_resistance_entries.isEmpty())
                        ConfigGrab.wind_resistance_entries += entryMain + "=" + resistance;
                    else
                        ConfigGrab.wind_resistance_entries += ", " + entryMain + "=" + resistance;
                    sendMessage(source, "config.grablist.addwindentry.success");
                    ConfigManager.save("Weather2 Remastered - Grab");
                    return 1;
                }

                default:
                    return 0;
            }
        } catch (CommandSyntaxException e) {
            sendMessage(source, "config.grablist.fail");
            return 0;
        }
    }

    private static int executeCreate(CommandSource source, String type, @Nullable Vector3d position, String[] flags) {
        World world = source.getLevel();
        Vector3d pos = position != null ? position : source.getPosition();

        if (ConfigMisc.overcast_mode && !world.isRaining()) {
            sendMessage(source, "create.fail.b");
            return 0;
        }

        int stage = -1;
        boolean isRaining = false, isSandstorm = false, isCyclone = false, isRandom = false;

        switch (type.toLowerCase()) {
            case "random":
                isRandom = true;
                stage = Stage.NORMAL.getStage();
                break;
            case "cloud":
            case "clouds":
                stage = Stage.NORMAL.getStage();
                break;
            case "rain":
            case "rainstorm":
                isRaining = true;
                stage = Stage.RAIN.getStage();
                break;
            case "thunder":
            case "thunderstorm":
            case "lightning":
            case "lightningstorm":
                isRaining = true;
                stage = Stage.THUNDER.getStage();
                break;
            case "supercell":
            case "cell":
            case "severe":
            case "severethunder":
            case "severethunderstorm":
            case "severelightning":
            case "severelightningstorm":
                isRaining = true;
                stage = Stage.SEVERE.getStage();
                break;
            case "tropicaldisturbance":
            case "td1":
                isRaining = true;
                isCyclone = true;
                stage = Stage.TROPICAL_DISTURBANCE.getStage();
                break;
            case "tropicaldepression":
            case "td2":
                isRaining = true;
                isCyclone = true;
                stage = Stage.TROPICAL_DEPRESSION.getStage();
                break;
            case "tropicalstorm":
            case "ts":
                isRaining = true;
                isCyclone = true;
                stage = Stage.TROPICAL_STORM.getStage();
                break;
            case "sandstorm":
                isSandstorm = true;
                stage = Stage.NORMAL.getStage();
                break;
            default:
                isRaining = true;

                if (type.matches("(ef|f)\\d+"))
                    stage = Stage.TORNADO.getStage() + Integer.parseInt(type.replaceAll("\\D*", ""));
                else if (type.matches("(category|c)\\d+")) {
                    isCyclone = true;
                    stage = Stage.TROPICAL_STORM.getStage() + Integer.parseInt(type.replaceAll("\\D*", ""));
                }
        }

        if (stage > -1) {
            if (isSandstorm) {
                return createSandstorm(source, world, pos);
            } else {
                return createStorm(source, world, pos, stage, isRaining, isCyclone, isRandom, flags);
            }
        } else {
            sendMessage(source, "create.usage");
            return 0;
        }
    }

    private static int createSandstorm(CommandSource source, World world, Vector3d pos) {
        RegistryKey<World> dimension = world.dimension();

        WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(dimension);
        if (wm == null || !EZConfigParser.isWeatherEnabled(dimension.location())) {
            sendMessage(source, "fail.nomanager");
            return 0;
        }

        boolean spawned = wm.spawnSandstorm(new Vec3(pos));

        if (!spawned) {
            sendMessage(source, "create.sandstorm.fail.b");
            return 0;
        } else {
            sendMessage(source, "create.sandstorm.success", Math.round(pos.x), Math.round(pos.z));
            return 1;
        }
    }

    private static int createStorm(CommandSource source, World world, Vector3d pos, int stage,
                                   boolean isRaining, boolean isCyclone, boolean isRandom, String[] flagsArray) {
        boolean isViolent = false, isNatural = false, isFirenado = false, neverDissipate = false,
                shouldConvert = true, alwaysProgress = false, isHailing = false;
        float sizeMultiplier = -1.0F, angle = -1.0F, speed = -1.0F;
        int revives = -1;
        RegistryKey<World> dimension = world.dimension();
        String flagsStr = "", name = "";

        for (String flag : flagsArray) {
            flag = flag.toLowerCase();
            switch (flag) {
                case "alwaysprogress":
                    if (!alwaysProgress) {
                        alwaysProgress = true;
                        flagsStr += ", Always Progresses";
                    }
                    break;
                case "isviolent":
                case "violent":
                    if (!isViolent) {
                        isViolent = true;
                        flagsStr += ", Violent Storm";
                    }
                    break;
                case "isnatural":
                case "natural":
                    if (!isNatural) {
                        isNatural = true;
                        flagsStr += ", Starts Naturally";
                    }
                    break;
                case "isfirenado":
                case "firenado":
                    if (!isFirenado) {
                        isFirenado = true;
                        flagsStr += ", Is A Firenado";
                    }
                    break;
                case "neverdissipate":
                case "neverdie":
                    if (!neverDissipate) {
                        neverDissipate = true;
                        flagsStr += ", Never Dissipates";
                    }
                    break;
                case "dontconvert":
                case "noconvert":
                case "convert":
                    if (shouldConvert) {
                        shouldConvert = false;
                        flagsStr += ", Never Converts To Hurricane";
                    }
                    break;
                case "ishailing":
                case "hailing":
                case "hail":
                    if (!isHailing) {
                        isHailing = true;
                        flagsStr += ", Storm Is Hailing";
                    }
                    isRaining = false;
                    flagsStr += ", No Rain";
                    break;
                case "norain":
                    isRaining = false;
                    flagsStr += ", No Rain";
                    break;
                default:
                    if (flag.matches("revives=\\d+")) {
                        if (revives < 0) {
                            revives = Integer.parseInt(flag.replaceAll("\\D*", ""));
                            flagsStr += ", Will Revive " + revives + " time" + (revives > 1 ? "s" : "");
                        }
                    } else if (flag.matches("(angle|direction)=(north|south|east|west|\\d+)")) {
                        if (angle < 0.0F) {
                            angle = flag.contains("north") ? 180.0F :
                                    flag.contains("east") ? 270.0F :
                                            flag.contains("south") ? 0.0F :
                                                    flag.contains("west") ? 90.0F :
                                                            Float.parseFloat(flag.replaceAll("[^\\d\\.]*", ""));
                            flagsStr += ", Aiming at " + angle + " degrees";
                        }
                    } else if (flag.matches("speed=[\\d\\.]+")) {
                        if (speed < 0.0F) {
                            speed = Float.parseFloat(flag.replaceAll("[^\\d\\.]*", "")) * 0.05F;
                            flagsStr += ", Moving At " + (speed * 20.0F) + " M/s";
                        }
                    } else if (flag.matches("size=[\\d\\.%]+")) {
                        if (sizeMultiplier < 0.0F) {
                            sizeMultiplier = Float.parseFloat(flag.replaceAll("[^\\d\\.]*", "")) * 0.01F;
                            flagsStr += ", Will Grow " + (sizeMultiplier * 100.0F) + "%" +
                                    (sizeMultiplier < 1.0F ? " Smaller Than Normal" :
                                            sizeMultiplier == 1.0F ? "" : " Larger Than Normal");
                        }
                    } else if (flag.matches("name=\\w+")) {
                        if (name.isEmpty()) {
                            name = flag.replaceFirst("[nN][aA][mM][eE]=", "");
                            flagsStr += ", Named " + name;
                        }
                    }
                    break;
            }
        }

        WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(dimension);
        if (wm == null || !EZConfigParser.isWeatherEnabled(dimension.location())) {
            sendMessage(source, "fail.nomanager");
            return 0;
        }

        StormObject so = new StormObject(wm.getGlobalFront());

        so.layer = 0;
        so.isNatural = isNatural;
        so.temperature = 0.1F;
        so.pos = new Vec3(pos.x, so.getLayerHeight(), pos.z);
        so.stage = isRandom ? so.rollDiceOnMaxIntensity() : stage;
        so.stageMax = so.stage;
        so.intensity = so.stage - 0.99F;
        so.shouldBuildHumidity = isRaining;
        so.rain = isRaining ? (isNatural ? 50.0F : isHailing ? 200.0F : so.stage * 50.0F) + 1.0F : 0.0F;
        so.hail = isHailing ? (isNatural ? 0.0F : 150.0F) : 0.0F;
        so.hailRate = isHailing ? Math.min((float) ConfigStorm.hail_max_buildup_rate, 1.0F) : 0.0F;
        so.sizeRate = sizeMultiplier;
        if (angle >= 0.0F)
            so.setAngle(angle);
        if (speed >= 0.0F)
            so.setSpeed(speed);
        so.alwaysProgresses = alwaysProgress;
        so.neverDissipate = neverDissipate;
        so.isFirenado = isFirenado;
        so.shouldConvert = shouldConvert;
        so.isViolent = isViolent;
        so.maxRevives = revives;
        so.name = name;
        so.shouldBuildHumidity = true;

        if (isCyclone || (isRandom && so.stage > 3 && Maths.chance(25)))
            so.stormType = StormType.WATER.ordinal();

        so.init();

        if (so.rain > 0.0F && so.isNatural)
            so.initRealStorm();
        else {
            so.canProgress = true;

            if (so.sizeRate < 0.0F)
                so.sizeRate = (float) Maths.random(ConfigStorm.min_size_growth, ConfigStorm.max_size_growth);

            if (so.isViolent) {
                so.sizeRate += Maths.random(ConfigStorm.min_violent_size_growth, ConfigStorm.max_violent_size_growth);
                if (so.stageMax < 9)
                    so.stageMax += 1;
            }
        }

        so.updateType();

        wm.getGlobalFront().addWeatherObject(so);
        PacketWeatherObject.create(wm.getDimension(), so);

        sendMessage(source, "create.success", so.getName(true), Math.round(pos.x), Math.round(pos.z), flagsStr);
        return 1;
    }

    private static int executeKill(CommandSource source, String target) {
        switch (target.toLowerCase()) {
            case "all":
                if (!hasPermission(source, 4)) {
                    sendMessage(source, "nopermission");
                    return 0;
                }

                World world = source.getLevel();
                RegistryKey<World> dimension = world.dimension();
                WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(dimension);

                if (wm == null || !EZConfigParser.isWeatherEnabled(dimension.location())) {
                    sendMessage(source, "fail.nomanager");
                    return 0;
                }

                List<FrontObject> fronts = wm.getFronts();
                int size = wm.getWeatherObjects().size();

                if (size > 0) {
                    for (int i = 0; i < fronts.size(); i++) {
                        FrontObject front = fronts.get(i);
                        Weather2.debug("Killing front " + front.getUUID());
                        front.isDead = true;
                    }
                    sendMessage(source, "kill.all.success", size);
                    return 1;
                } else {
                    sendMessage(source, "kill.all.fail");
                    return 0;
                }

            case "particle":
            case "particles":
                try {
                    ServerPlayerEntity player = source.getPlayerOrException();
                    PacketWeatherObject.clientCleanup(player);
                    sendMessage(source, "kill.particles.success");
                    return 1;
                } catch (CommandSyntaxException e) {
                    sendMessage(source, "kill.particles.fail");
                    return 0;
                }

            default:
                sendMessage(source, "kill.usage");
                return 0;
        }
    }

    private static int executeTestVolcano(CommandSource source) {
        Vector3d pos = source.getPosition();
        RegistryKey<World> dimension = source.getLevel().dimension();

        WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(dimension);
        if (wm == null || !EZConfigParser.isWeatherEnabled(dimension.location())) {
            sendMessage(source, "fail.nomanager");
            return 0;
        }

        VolcanoObject vo = new VolcanoObject(wm);
        vo.pos = new net.corosus.coroutillegacy.util.Vec3(pos);
        vo.init();
        wm.addVolcanoObject(vo);
        PacketVolcanoObject.create(wm.getDimension(), vo);

        sendMessage(source, "test.volcano.success");
        return 1;
    }

    private static int executeTestClass(CommandSource source, String className) {
        List<String> found = ReflectionHelper.viewFields(className);
        for (String line : found) {
            sendMessage(source, "test.class.success", line);
        }
        return 1;
    }

    private static int executeReloadShaders(CommandSource source) {
        try {
            ServerPlayerEntity player = source.getPlayerOrException();
            PacketShader.refreshShaders(player);
            return 1;
        } catch (CommandSyntaxException e) {
            return 0;
        }
    }

    private static void sendMessage(CommandSource source, String key, Object... args) {
        source.sendSuccess(new TranslationTextComponent("command.storm." + key, args), true);
    }
}