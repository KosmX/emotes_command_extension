package dev.kosmx.emotesCommand;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ServerCommands {
    public static void registerCommands() {
        new CommandTree("emotes").withPermission(BukkitConnector.getEmoteUser())
                .then(
                new LiteralArgument("play")
                        .then(new TextArgument("emote").replaceSuggestions(new EmoteArgumentProvider())
                                .executes((sender, args) -> {
                                    Player player = getPlayerFromSource(sender);

                                    if (!sender.hasPermission(BukkitConnector.getEmoteMaster().toString()) && ServerEmoteAPI.isForcedEmote(player.getUniqueId()))
                                        throw CommandAPI.fail("Can't stop forced emote without admin rights");

                                    ServerEmoteAPI.playEmote(
                                            player.getUniqueId(),
                                            EmoteArgumentProvider.getEmote(sender, (String) args[0]),
                                            false);
                                })
                                .then(new PlayerArgument("player").withPermission(BukkitConnector.getEmoteMaster())
                                        .executes((sender, args) -> {
                                            ServerEmoteAPI.playEmote(
                                                    ((Player)args[1]).getUniqueId(),
                                                    EmoteArgumentProvider.getEmote(sender, (String) args[0]),
                                                    false);
                                        })
                                        .then(new BooleanArgument("forced")
                                                .executes(((sender, args) -> {
                                                    ServerEmoteAPI.playEmote(
                                                            ((Player)args[1]).getUniqueId(),
                                                            EmoteArgumentProvider.getEmote(sender, (String) args[0]),
                                                            (boolean) args[2]);
                                                }))
                                        )
                                )
                        )
                )
                .then(new LiteralArgument("stop")
                        .executes(((sender, args) -> {
                            Player player = getPlayerFromSource(sender);

                            if (!sender.hasPermission(BukkitConnector.getEmoteMaster().toString()) && ServerEmoteAPI.isForcedEmote(player.getUniqueId()))
                                throw CommandAPI.fail("Can't stop forced emote without admin rights");

                            ServerEmoteAPI.playEmote(player.getUniqueId(), null, false);
                        }))
                        .then(new PlayerArgument("player").withPermission(BukkitConnector.getEmoteMaster())
                                .executes(((sender, args) -> {
                                    ServerEmoteAPI.playEmote(((Player)args[1]).getUniqueId(), null, false);
                                }))
                        )
                )
                .then(new LiteralArgument("reload").withPermission(CommandPermission.OP)
                        .executes((sender, args) -> {
                            UniversalEmoteSerializer.loadEmotes();
                        }))
                .register();
    }


    private static Player getPlayerFromSource(CommandSender sender) throws WrapperCommandSyntaxException {
        if (sender instanceof Player) {
            return (Player) sender;
        } else if (sender instanceof ProxiedCommandSender proxiedCommandSender && proxiedCommandSender.getCallee() instanceof Player player) {
            return player;
        }
        throw CommandAPI.fail("No player target defined");
    }

    private static class EmoteArgumentProvider implements ArgumentSuggestions {

        @Override
        public CompletableFuture<Suggestions> suggest(SuggestionInfo info, SuggestionsBuilder builder) throws CommandSyntaxException {
            HashMap<UUID, KeyframeAnimation> emotes = getEmotes(info.sender().hasPermission(BukkitConnector.getEmoteMaster().toString()));

            for (KeyframeAnimation emote : emotes.values()) {
                if (emote.extraData.containsKey("name")) {
                    String name = Util.textToString(emote.extraData.get("name"));
                    if (name.contains(" ")) {
                        name = "\"" + name + "\"";
                    }
                    if (info.currentArg().isEmpty() || name.toLowerCase().contains(info.currentArg().toLowerCase())) {
                        builder.suggest(name);
                    }
                } else {
                    if (info.currentArg().isEmpty() || emote.getUuid().toString().toLowerCase().contains(info.currentArg().toLowerCase())) {
                        builder.suggest(emote.getUuid().toString());
                    }
                }
            }

            return builder.buildFuture();
        }

        private static HashMap<UUID, KeyframeAnimation> getEmotes(boolean allowHidden) {
            return allowHidden ? ServerEmoteAPI.getLoadedEmotes() : ServerEmoteAPI.getPublicEmotes();
        }
        public static KeyframeAnimation getEmote(CommandSender sender, String id) throws WrapperCommandSyntaxException {
            HashMap<UUID, KeyframeAnimation> emotes = getEmotes(sender.hasPermission(BukkitConnector.getEmoteMaster().toString()));

            try {
                UUID emoteID = UUID.fromString(id);
                KeyframeAnimation emote = emotes.get(emoteID);
                if (emote == null) throw CommandAPI.fail("No emote with ID: " + emoteID);
                return emote;
            } catch(IllegalArgumentException ignore) {} //Not a UUID

            for (KeyframeAnimation emote : emotes.values()) {
                if (emote.extraData.containsKey("name")) {
                    String name = Util.textToString(emote.extraData.get("name"));
                    if (name.equals(id)) return emote;
                }
            }
            throw CommandAPI.fail("Not emote with name: " + id);
        }
    }

}
