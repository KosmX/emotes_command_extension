package dev.kosmx.emotesCommand;

import dev.jorel.commandapi.CommandPermission;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitConnector extends JavaPlugin {

    @Getter(lazy = true)
    private static final CommandPermission emoteMaster = CommandPermission.fromString("emotes.master");
    @Getter(lazy = true)
    private static final CommandPermission emoteUser = CommandPermission.fromString("emotes.user");

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        ServerCommands.registerCommands();
    }
    @Override
    public void onDisable() {
    }

}
