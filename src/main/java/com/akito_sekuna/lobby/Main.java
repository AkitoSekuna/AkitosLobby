package com.akito_sekuna.lobby;

import com.akito_sekuna.lobby.commands.LobbyCommand;
import com.akito_sekuna.lobby.commands.TreasureCommand;
import com.akito_sekuna.lobby.listeners.HeadClickListener;
import com.akito_sekuna.lobby.managers.TreasureManager;
import com.akito_sekuna.lobby.utils.VersionUtil;
import com.akito_sekuna.core.AkitosAddon;
import com.akito_sekuna.core.api.ICoreAPI;
import com.akito_sekuna.core.ReloadReason;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements AkitosAddon {

    private TreasureManager treasureManager;
    private ICoreAPI coreAPI;

    // --- AkitosAddon Implementation ---

    @Override
    public String getAddonName() {
        return "AkitosLobby";
    }

    @Override
    public String getAddonVersion() {
        return getPluginMeta().getVersion();
    }

    @Override
    public void onCoreReady(ICoreAPI api) {
        this.coreAPI = api;
        getLogger().info("[AkitosLobby] Successfully hooked into AkitosCore API!");
    }

    @Override
    public void onCoreReload(ICoreAPI newApi, ReloadReason reason) {
        this.coreAPI = newApi;
        reloadConfig();
    }

    @Override
    public void onCoreShutdown() {
        // Cleanup resources if needed on core shutdown
    }

    // --- Plugin Lifecycle ---

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

        // 2. Register addon with AkitosCore
        com.akito_sekuna.core.Main.registerAddon(this);

        // 3. Load Config & Managers
        saveDefaultConfig();
        this.treasureManager = new TreasureManager(this);

        // 4. Register Commands
        if (getCommand("lobby") != null) getCommand("lobby").setExecutor(new LobbyCommand(this));
        if (getCommand("treasurehead") != null) getCommand("treasurehead").setExecutor(new TreasureCommand(this, treasureManager));

        // 5. Register Listeners
        Bukkit.getPluginManager().registerEvents(new HeadClickListener(this, treasureManager), this);
    }

    public TreasureManager getTreasureManager() {
        return treasureManager;
    }

    public ICoreAPI getCoreAPI() {
        return coreAPI;
    }
}