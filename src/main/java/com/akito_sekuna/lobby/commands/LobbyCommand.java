package com.akito_sekuna.lobby.commands;

import com.akito_sekuna.lobby.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {

    private final Main plugin;

    public LobbyCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("[Akitos] Only players can execute this command.").color(NamedTextColor.RED));
            return true;
        }

        String lobbyWorldName = plugin.getConfigManager().getLobbyWorldName();
        World lobbyWorld = Bukkit.getWorld(lobbyWorldName);

        if (lobbyWorld == null) {
            player.sendMessage(Component.text("[Akitos] Error: Lobby world '" + lobbyWorldName + "' is not loaded!").color(NamedTextColor.RED));
            return true;
        }

        Location lobbySpawn = lobbyWorld.getSpawnLocation();
        player.teleportAsync(lobbySpawn).thenAccept(success -> {
            if (success) {
                player.sendMessage(Component.text("[Akitos] Teleported to the lobby!").color(NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("[Akitos] Teleportation failed.").color(NamedTextColor.RED));
            }
        });

        return true;
    }
}
