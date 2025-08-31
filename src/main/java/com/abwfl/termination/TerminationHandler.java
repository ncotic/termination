package com.abwfl.termination;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.CompletableFuture;

public class TerminationHandler {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("termination")
                .requires((requirement) -> requirement.hasPermission(2))
                .then(Commands.argument("operation", StringArgumentType.string()).suggests(new OperationSuggestionProvider())
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("entity", ResourceArgument.resource(event.getBuildContext(), Registries.ENTITY_TYPE))
                                        .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                        .executes((commandContext) -> editList(StringArgumentType.getString(commandContext, "operation"), EntityArgument.getPlayer(commandContext, "player"), ResourceArgument.getSummonableEntityType(commandContext, "entity").value()))))));
    }

    private static int editList(String operation, Player player, EntityType<?> type) {
        return switch (operation) {
            case "add" -> {
                Termination.terminationList.addEntry(player.getStringUUID(), type);
                yield 0;
            }
            case "remove" -> {
                Termination.terminationList.removeEntry(player.getStringUUID(), type);
                yield 0;
            }
            default -> -1;
        };
    }

    public static class OperationSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
            builder.suggest("add");
            builder.suggest("remove");
            return builder.buildFuture();
        }
    }
}
