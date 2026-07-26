package com.akito_sekuna.akitoslobby;

import com.akito_sekuna.akitoslobby.commands.LobbyCommand;
import com.akito_sekuna.akitoslobby.commands.TreasureCommand;
import com.akito_sekuna.akitoslobby.listeners.HeadClickListener;
import com.akito_sekuna.akitoslobby.managers.TreasureManager;
import com.akito_sekuna.akitoslobby.utils.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private TreasureManager treasureManager;

    @Override
    public void onEnable() {
        // 1. Hook into AkitosCore dependency & version check
        Plugin corePlugin = Bukkit.getPluginManager().getPlugin("AkitosCore");
        if (corePlugin == null || !corePlugin.isEnabled()) {
            getLogger().severe("[AkitosLobby] AkitosCore is missing or disabled! Disabling addon...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!VersionUtil.isCompatible(getDescription().getVersion(), corePlugin.getDescription().getVersion())) {
            getLogger().severe("[AkitosLobby] Version mismatch detected with AkitosCore! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // 2. Load Config & Managers
        saveDefaultConfig();
        this.treasureManager = new TreasureManager(this);

        // 3. Register Commands
        if (getCommand("lobby") != null) getCommand("lobby").setExecutor(new LobbyCommand(this));
        if (getCommand("treasurehead") != null) getCommand("treasurehead").setExecutor(new TreasureCommand(this, treasureManager));

        // 4. Register Listeners
        Bukkit.getPluginManager().registerEvents(new HeadClickListener(this, treasureManager), this);

        getLogger().info("[AkitosLobby] Successfully loaded as an AkitosCore addon!");
    }

    public TreasureManager getTreasureManager() {
        return treasureManager;
    }
}
