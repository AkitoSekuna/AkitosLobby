package com.akito_sekuna.lobby;

import com.akito_sekuna.lobby.api.ITreasureCooldownService;
import com.akito_sekuna.lobby.commands.LobbyCommand;
import com.akito_sekuna.lobby.commands.HubCommand;
import com.akito_sekuna.lobby.commands.TreasureCommand;
import com.akito_sekuna.lobby.listeners.DebugListener;
import com.akito_sekuna.lobby.listeners.HeadClickListener;
import com.akito_sekuna.lobby.managers.TreasureManager;
import com.akito_sekuna.lobby.managers.TreasureClaimTracker;
import com.akito_sekuna.lobby.managers.ConfigManager;
import com.akito_sekuna.lobby.worldcentral.WorldCentralRegistry;
import com.akito_sekuna.lobby.worldcentral.WorldCentralListener;
import com.akito_sekuna.lobby.worldcentral.WorldCentralMenuListener;
import com.akito_sekuna.lobby.utils.VersionUtil;
import com.akito_sekuna.core.AkitosAddon;
import com.akito_sekuna.core.api.ICoreAPI;
import com.akito_sekuna.core.ReloadReason;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public final class Main extends JavaPlugin implements AkitosAddon {

    private TreasureManager treasureManager;
    private ConfigManager configManager;
    private WorldCentralRegistry worldCentralRegistry;
    private ICoreAPI coreAPI;
    private File customFolder;
    private final TreasureClaimTracker treasureClaimTracker = new TreasureClaimTracker();

    // Custom getter for your AkitosPlugins directory
    public File getCustomDataFolder() {
        if (customFolder == null) {
            customFolder = new File(getServer().getPluginsFolder(), "AkitosPlugins/AkitosLobby");
        }
        return customFolder;
    }

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
        api.getMetrics().registerBarChart("treasure_rarities_claimed", treasureClaimTracker::getAndReset);
        // Example using Bukkit's standard registry if ICoreAPI delegates to it:
        Bukkit.getServicesManager().register(ITreasureCooldownService.class, treasureManager, this, ServicePriority.Normal);
        getLogger().info("[AkitosLobby] Successfully hooked into AkitosCore API!");
    }

    @Override
    public void onCoreReload(ICoreAPI newApi, ReloadReason reason) {
        this.coreAPI = newApi;
        this.configManager.reload();
        this.worldCentralRegistry.reload();
    }

    @Override
    public void onCoreShutdown() {
        // Cleanup resources on core shutdown if necessary
    }

    // --- Custom Config Loader ---

    public void loadCustomConfig() {
        File folder = getCustomDataFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File configFile = new File(folder, "config.yml");
        if (!configFile.exists()) {
            // Copy the embedded default config.yml directly into the custom folder.
            // Deliberately avoids saveResource(), which always writes to Bukkit's
            // standard getDataFolder() first and would leave an empty plugins/AkitosLobby/
            // folder behind after the file is relocated.
            try (InputStream in = getResource("config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                }
            } catch (Exception e) {
                getLogger().severe("Failed to copy default config.yml: " + e.getMessage());
            }
        }
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

        if (!VersionUtil.isCompatible(getPluginMeta().getVersion(), corePlugin.getPluginMeta().getVersion())) {
            getLogger().severe("[AkitosLobby] Version mismatch detected with AkitosCore! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // 2. Register addon with AkitosCore
        com.akito_sekuna.core.Main.registerAddon(this);

        // 3. Ensure custom folder exists & save configuration
        loadCustomConfig();
        this.configManager = new ConfigManager(this);
        this.worldCentralRegistry = new WorldCentralRegistry(this);

        // 4. Initialize Managers
        this.treasureManager = new TreasureManager(this);

        // 5. Register Commands
        if (getCommand("lobby") != null) getCommand("lobby").setExecutor(new LobbyCommand(this));
        if (getCommand("hub") != null) getCommand("hub").setExecutor(new HubCommand(this));
        if (getCommand("treasurehead") != null) getCommand("treasurehead").setExecutor(new TreasureCommand(this, treasureManager));

        // 6. Register Listeners
        Bukkit.getPluginManager().registerEvents(new HeadClickListener(this, treasureManager), this);
        Bukkit.getPluginManager().registerEvents(new WorldCentralListener(this), this);
        Bukkit.getPluginManager().registerEvents(new WorldCentralMenuListener(), this);

        // 7. Register Debug
        getServer().getPluginManager().registerEvents(new DebugListener(), this);
    }

    public TreasureManager getTreasureManager() {
        return treasureManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public WorldCentralRegistry getWorldCentralRegistry() {
        return worldCentralRegistry;
    }

    public TreasureClaimTracker getTreasureClaimTracker() {
        return treasureClaimTracker;
    }

    public ICoreAPI getCoreAPI() {
        return coreAPI;
    }
}