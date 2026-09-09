package com.akito_sekuna.lobby.worldcentral;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The destination-selector GUI opened either by right-clicking the physical
 * compass item, or by running /hub. Tracks which WorldCentral entry each
 * open menu belongs to by player UUID, rather than matching on title, since
 * menu titles are admin-configurable and multiple entries can exist under
 * the registry pattern.
 * <p>
 * Whichever destination matches the player's own current world is left out
 * of the menu entirely, since teleporting to where you already are isn't a
 * real destination. This filtering applies identically no matter which entry
 * point opened the menu, so the physical compass and /hub always behave the
 * same way.
 */
public class WorldCentralMenu {

    private static final Map<UUID, WorldCentral> openMenus = new HashMap<>();

    public static void open(Player player, WorldCentral entry) {
        List<WorldCentralDestination> visible = new ArrayList<>();
        for (WorldCentralDestination dest : entry.destinations()) {
            if (!dest.world().equals(player.getWorld().getName())) {
                visible.add(dest);
            }
        }

        int rows = Math.max(1, (Math.max(visible.size(), 1) - 1) / 9 + 1);
        Inventory menu = Bukkit.createInventory(null, rows * 9, entry.menuTitle());

        for (int i = 0; i < visible.size() && i < rows * 9; i++) {
            WorldCentralDestination dest = visible.get(i);
            ItemStack item = new ItemStack(dest.material());
            ItemMeta meta = item.getItemMeta();
            meta.displayName(dest.displayName());
            meta.lore(dest.lore());
            item.setItemMeta(meta);
            menu.setItem(i, item);
        }

        // Store the filtered list, not the entry's full original list, so the
        // click listener's slot lookup below matches exactly what's on screen.
        WorldCentral filteredEntry = new WorldCentral(entry.id(), entry.triggerWorld(), entry.displayName(),
                entry.lore(), entry.menuTitle(), visible);
        openMenus.put(player.getUniqueId(), filteredEntry);
        player.openInventory(menu);
    }

    public static WorldCentral getOpenEntry(UUID uuid) {
        return openMenus.get(uuid);
    }

    public static void clear(UUID uuid) {
        openMenus.remove(uuid);
    }
}
