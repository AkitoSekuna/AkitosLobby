package com.akito_sekuna.lobby.worldcentral;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class WorldCentralMenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        WorldCentral entry = WorldCentralMenu.getOpenEntry(player.getUniqueId());
        if (entry == null) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= entry.destinations().size()) return;

        WorldCentralDestination destination = entry.destinations().get(slot);
        World world = Bukkit.getWorld(destination.world());
        if (world == null) {
            player.sendMessage(Component.text("[Akitos] Error: destination world '" + destination.world() + "' is not loaded!", NamedTextColor.RED));
            return;
        }

        Location spawn = world.getSpawnLocation();
        player.closeInventory();
        player.teleportAsync(spawn).thenAccept(success -> {
            if (success) {
                player.sendMessage(Component.text("[Akitos] Teleported!", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("[Akitos] Teleportation failed.", NamedTextColor.RED));
            }
        });
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            WorldCentralMenu.clear(player.getUniqueId());
        }
    }
}
