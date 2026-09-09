package com.akito_sekuna.lobby.worldcentral;

import com.akito_sekuna.lobby.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

/**
 * Gives (and enforces) the config-driven lobby compass item, and opens the
 * WorldCentral destination menu on right-click.
 *
 * The compass is pinned to hotbar slot 0 rather than wiping the player's
 * inventory: whatever previously occupied slot 0 is moved elsewhere in the
 * inventory (or dropped only if there is truly no room), and any stray
 * duplicate compass elsewhere in the inventory is removed so the player
 * never ends up holding more than one. Once pinned, it cannot be dropped,
 * clicked out of slot 0, number-key-swapped out of slot 0, or moved to the
 * offhand.
 */
public class WorldCentralListener implements Listener {

    private final Main plugin;

    public WorldCentralListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        checkAndEquip(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        checkAndEquip(event.getPlayer());
    }

    private void checkAndEquip(Player player) {
        WorldCentral entry = plugin.getWorldCentralRegistry().getForWorld(player.getWorld().getName());
        if (entry == null) return;

        var inv = player.getInventory();

        // Remove any stray copy of this (or any other) WorldCentral compass sitting
        // elsewhere in the inventory first, so the player never ends up holding two.
        for (int i = 1; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack != null && plugin.getWorldCentralRegistry().getIdFromItem(stack) != null) {
                inv.setItem(i, null);
            }
        }

        ItemStack slotZero = inv.getItem(0);
        String slotZeroId = plugin.getWorldCentralRegistry().getIdFromItem(slotZero);
        if (entry.id().equals(slotZeroId)) {
            return; // already correctly pinned, nothing to do
        }

        ItemStack bumped = (slotZero != null && !slotZero.getType().isAir()) ? slotZero : null;
        inv.setItem(0, plugin.getWorldCentralRegistry().buildItem(entry));

        if (bumped != null) {
            // Slot 0 is occupied now, so addItem will naturally skip it and use
            // the next free slot, only dropping if the whole inventory is full.
            HashMap<Integer, ItemStack> overflow = inv.addItem(bumped);
            overflow.values().forEach(leftover ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (plugin.getWorldCentralRegistry().getIdFromItem(item) != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("[Akitos] You can't drop this!", NamedTextColor.RED));
        }
    }

    // Blocks the two common ways to move the pinned compass out of hotbar slot 0
    // via an inventory screen: clicking directly on it, or number-key-swapping
    // slot 0 with whatever the player is hovering in another open inventory.
    // Does NOT cover shift-drag (InventoryDragEvent) across multiple slots
    // including slot 0, a rare edge case, left as a known gap rather than guessed at.
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getClickedInventory() == player.getInventory() && event.getSlot() == 0) {
            ItemStack current = event.getCurrentItem();
            if (current != null && plugin.getWorldCentralRegistry().getIdFromItem(current) != null) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getClick() == ClickType.NUMBER_KEY && event.getHotbarButton() == 0) {
            ItemStack current = player.getInventory().getItem(0);
            if (current != null && plugin.getWorldCentralRegistry().getIdFromItem(current) != null) {
                event.setCancelled(true);
            }
        }
    }

    // Swapping main hand and offhand (default F key) would otherwise move a
    // pinned compass in the currently-selected hotbar slot into the offhand.
    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getHeldItemSlot() != 0) return;
        ItemStack mainHand = player.getInventory().getItem(0);
        if (mainHand != null && plugin.getWorldCentralRegistry().getIdFromItem(mainHand) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        String id = plugin.getWorldCentralRegistry().getIdFromItem(item);
        if (id == null) return;

        event.setCancelled(true);
        WorldCentral entry = plugin.getWorldCentralRegistry().getById(id);
        if (entry == null) return;

        WorldCentralMenu.open(event.getPlayer(), entry);
    }
}
