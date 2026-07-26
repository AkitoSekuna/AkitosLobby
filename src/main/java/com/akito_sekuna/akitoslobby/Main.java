package com.akito_sekuna.lobby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements CommandExecutor {

    private String lobbyWorldName;

    @Override
    public void onEnable() {
        // 1. Hook into AkitosCore dependency
        Plugin corePlugin = Bukkit.getPluginManager().getPlugin("AkitosCore");
        if (corePlugin == null || !corePlugin.isEnabled()) {
            getLogger().severe("[AkitosLobby] AkitosCore is missing or disabled! Disabling addon...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // 2. Validate version compatibility (Matches XX.YY between Addon and Core)
        String coreVersionStr = corePlugin.getDescription().getVersion();
        String addonVersionStr = getDescription().getVersion();

        if (!isCompatibleVersion(addonVersionStr, coreVersionStr)) {
            getLogger().severe("[AkitosLobby] Version mismatch detected!");
            getLogger().severe("[AkitosLobby] Addon Version: " + addonVersionStr + " | Core Version: " + coreVersionStr);
            getLogger().severe("[AkitosLobby] Major and Minor versions (XX.YY) must match. Disabling addon...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // 3. Load configuration
        saveDefaultConfig();
        this.lobbyWorldName = getConfig().getString("lobby-world", "world");

        // 4. Register command
        if (getCommand("lobby") != null) {
            getCommand("lobby").setExecutor(this);
        }

        getLogger().info("[AkitosLobby] Successfully hooked into AkitosCore v" + coreVersionStr + "!");
    }

    /**
     * Checks if two version strings match in their MAJOR and MINOR (XX.YY) parts.
     * Format expected: XX.YY.ZZ
     */
    private boolean isCompatibleVersion(String addonVer, String coreVer) {
        try {
            String[] addonParts = addonVer.split("\\.");
            String[] coreParts = coreVer.split("\\.");

            if (addonParts.length < 2 || coreParts.length < 2) {
                return false;
            }

            int addonMajor = Integer.parseInt(addonParts[0]);
            int addonMinor = Integer.parseInt(addonParts[1]);

            int coreMajor = Integer.parseInt(coreParts[0]);
            int coreMinor = Integer.parseInt(coreParts[1]);

            // True if XX and YY match regardless of ZZ
            return (addonMajor == coreMajor) && (addonMinor == coreMinor);
        } catch (NumberFormatException e) {
            getLogger().warning("[AkitosLobby] Could not parse version numbers during compatibility check.");
            return false;
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("[AkitosLobby] Addon disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "[Akitos] Only players can execute this command.");
            return true;
        }

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