package me.katto.deathspectator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.io.*;
import java.util.Set;

public class Command {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("deathspectator").requires(s -> s.hasPermission(4))
                .then(Commands.literal("setconfig")
                        .then(Commands.argument("property", StringArgumentType.string()).suggests(PROPERTY_SUGGESTIONS)
                                .then(Commands.argument("value", BoolArgumentType.bool()).executes(arguments -> {
                                    Entity entity = arguments.getSource().getEntity();
                                    execute(arguments, entity);
                                    return 0;
                                })))));
    }

    public static void execute(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null)
            return;
        if (DeathspectatorVariables.main.get((StringArgumentType.getString(arguments, "property"))).isJsonPrimitive()
                ? DeathspectatorVariables.main.get((StringArgumentType.getString(arguments, "property"))).getAsJsonPrimitive().isBoolean()
                : false) {
            {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(DeathspectatorVariables.file));
                    StringBuilder jsonstringbuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        jsonstringbuilder.append(line);
                    }
                    bufferedReader.close();
                    DeathspectatorVariables.main = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
                    if (DeathspectatorVariables.main.get((StringArgumentType.getString(arguments, "property"))).getAsBoolean() == BoolArgumentType.getBool(arguments, "value")) {
                        if (entity instanceof Player _player && !_player.level.isClientSide())
                            _player.displayClientMessage(Component.literal(String.format("§cThe property '%s' is already set: %s", StringArgumentType.getString(arguments, "property"), BoolArgumentType.getBool(arguments, "value"))), false);
                    } else {
                        DeathspectatorVariables.main.addProperty((StringArgumentType.getString(arguments, "property")), (BoolArgumentType.getBool(arguments, "value")));
                        {
                            com.google.gson.Gson mainGSONBuilderVariable = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
                            try {
                                FileWriter fileWriter = new FileWriter(DeathspectatorVariables.file);
                                fileWriter.write(mainGSONBuilderVariable.toJson(DeathspectatorVariables.main));
                                fileWriter.close();
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }
                        }

                        Config.lastModified = DeathspectatorVariables.file.lastModified();
                        Config.execute();

                        if (entity instanceof Player _player && !_player.level.isClientSide())
                            _player.displayClientMessage(Component.literal(("The property '" + StringArgumentType.getString(arguments, "property") + "' has been set to: " + BoolArgumentType.getBool(arguments, "value"))), false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final SuggestionProvider<CommandSourceStack> PROPERTY_SUGGESTIONS = (context, builder) -> {
        JsonObject main = DeathspectatorVariables.main;

        if (main != null) {
            Set<String> keys = main.keySet();
            for (String key : keys) {
                JsonElement element = main.get(key);
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
                    builder.suggest(key);
                }
            }
        }

        return builder.buildFuture();
    };
}