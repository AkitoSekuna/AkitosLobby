package com.akito_sekuna.akitoslobby.commands;

import com.akito_sekuna.akitoslobby.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            sender.sendMessage(ChatColor.RED + "[Akitos] Only players can execute this command.");
            return true;
        }

        String lobbyWorldName = plugin.getConfig().getString("lobby-world", "world");
        World lobbyWorld = Bukkit.getWorld(lobbyWorldName);

        if (lobbyWorld == null) {
            player.sendMessage(ChatColor.RED + "[Akitos] Error: Lobby world '" + lobbyWorldName + "' is not loaded!");
            return true;
        }

        Location lobbySpawn = lobbyWorld.getSpawnLocation();
        player.teleportAsync(lobbySpawn).thenAccept(success -> {
            if (success) {
                player.sendMessage(ChatColor.GREEN + "[Akitos] Teleported to the lobby!");
            } else {
                player.sendMessage(ChatColor.RED + "[Akitos] Teleportation failed.");
            }
        });

        return true;
    }
}
