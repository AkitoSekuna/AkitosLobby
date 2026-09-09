package com.akito_sekuna.lobby.managers;

import com.akito_sekuna.lobby.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class ConfigManager {

    private final Main plugin;
    private final File file;
    private FileConfiguration config;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getCustomDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public String getLobbyWorldName() {
        return config.getString("lobby-world", "world");
    }

    public long getTreasureHeadCooldownDays() {
        return config.getLong("treasure-head.cooldown-days", 30);
    }

    public int getRarityMinReward(String rarity) {
        return config.getInt("treasure-head.rarities." + rarity + ".min-reward", 100);
    }

    public int getRarityMaxReward(String rarity) {
        return config.getInt("treasure-head.rarities." + rarity + ".max-reward", 500);
    }

    public String getRarityTextureUrl(String rarity) {
        return config.getString("treasure-head.rarities." + rarity + ".texture-url", "");
    }

    /**
     * The set of currently configured rarity ids (e.g. "normal", "big", "mega",
     * plus any admin-created custom ones), read directly from the live config
     * rather than a hardcoded list, so custom rarities become usable immediately.
     */
    public Set<String> getRarityIds() {
        ConfigurationSection section = config.getConfigurationSection("treasure-head.rarities");
        if (section == null) return Collections.emptySet();
        return section.getKeys(false);
    }

    public boolean rarityExists(String rarity) {
        return getRarityIds().contains(rarity);
    }

    /**
     * Creates or overwrites a rarity type and immediately saves config.yml to disk,
     * so it survives a restart without needing a separate reload/save step.
     */
    public boolean setRarity(String rarity, int minReward, int maxReward, String textureUrl) {
        config.set("treasure-head.rarities." + rarity + ".min-reward", minReward);
        config.set("treasure-head.rarities." + rarity + ".max-reward", maxReward);
        config.set("treasure-head.rarities." + rarity + ".texture-url", textureUrl);
        return save();
    }

    /**
     * Removes a rarity type entirely and saves. Does not touch any already-placed
     * heads of that rarity, they keep whatever skin they were placed with, but
     * claiming them will fall back to reward defaults since the rarity no longer resolves.
     */
    public boolean removeRarity(String rarity) {
        config.set("treasure-head.rarities." + rarity, null);
        return save();
    }

    private boolean save() {
        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save config.yml: " + e.getMessage());
            return false;
        }
    }

    public FileConfiguration getRaw() {
        return config;
    }
}
