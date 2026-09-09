package com.akito_sekuna.lobby.worldcentral;

import com.akito_sekuna.lobby.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses the config-driven {@code worldcentral:} section into typed {@link WorldCentral} definitions,
 * and builds/identifies the physical compass items given to players. Config-driven text uses
 * '&'-ampersand codes, matching the convention already used by AkitosCore's lang.yml and
 * AkitosDrugs' settings.yml.
 */
public class WorldCentralRegistry {

    private final Main plugin;
    private final NamespacedKey idKey;

    private Map<String, WorldCentral> entriesById = new LinkedHashMap<>();
    private Map<String, WorldCentral> entriesByTriggerWorld = new LinkedHashMap<>();

    public WorldCentralRegistry(Main plugin) {
        this.plugin = plugin;
        this.idKey = new NamespacedKey(plugin, "lobby_worldcentral_id");
        load();
    }

    public void reload() {
        load();
    }

    private void load() {
        Map<String, WorldCentral> byId = new LinkedHashMap<>();
        Map<String, WorldCentral> byWorld = new LinkedHashMap<>();

        FileConfiguration config = plugin.getConfigManager().getRaw();
        ConfigurationSection section = config.getConfigurationSection("worldcentral");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection c = section.getConfigurationSection(id);
                if (c == null) continue;

                String triggerWorld = c.getString("trigger-world", "");
                Component displayName = legacy(c.getString("display-name", "&bCompass"));
                List<Component> lore = legacyList(c.getStringList("lore"));
                Component menuTitle = legacy(c.getString("menu-title", "&1Where to?"));
                List<WorldCentralDestination> destinations = loadDestinations(c.getConfigurationSection("destinations"));

                WorldCentral entry = new WorldCentral(id, triggerWorld, displayName, lore, menuTitle, destinations);
                byId.put(id, entry);
                if (!triggerWorld.isEmpty()) {
                    byWorld.put(triggerWorld, entry);
                }
            }
        }

        this.entriesById = byId;
        this.entriesByTriggerWorld = byWorld;
    }

    private List<WorldCentralDestination> loadDestinations(ConfigurationSection destSection) {
        List<WorldCentralDestination> destinations = new ArrayList<>();
        if (destSection == null) return destinations;

        for (String destId : destSection.getKeys(false)) {
            ConfigurationSection d = destSection.getConfigurationSection(destId);
            if (d == null) continue;

            Component destName = legacy(d.getString("display-name", destId));
            Material material = Material.matchMaterial(d.getString("material", "PAPER"));
            if (material == null) material = Material.PAPER;
            List<Component> destLore = legacyList(d.getStringList("lore"));
            String world = d.getString("world", "world");

            destinations.add(new WorldCentralDestination(destId, destName, material, destLore, world));
        }
        return destinations;
    }

    public WorldCentral getForWorld(String worldName) {
        return entriesByTriggerWorld.get(worldName);
    }

    public WorldCentral getById(String id) {
        return entriesById.get(id);
    }

    public ItemStack buildItem(WorldCentral entry) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(entry.displayName());
        meta.lore(entry.lore());
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, entry.id());
        item.setItemMeta(meta);
        return item;
    }

    public String getIdFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
    }

    private Component legacy(String raw) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
    }

    private List<Component> legacyList(List<String> raw) {
        List<Component> result = new ArrayList<>();
        for (String s : raw) result.add(legacy(s));
        return result;
    }
}
