package com.akito_sekuna.akitoslobby.managers;

import com.akito_sekuna.akitoslobby.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ThreadLocalRandom;

public class TreasureManager {

    private final Main plugin;
    private final NamespacedKey headKey;
    private final NamespacedKey rarityKey;
    private final NamespacedKey cooldownKey;

    public TreasureManager(Main plugin) {
        this.plugin = plugin;
        this.headKey = new NamespacedKey(plugin, "treasure_head");
        this.rarityKey = new NamespacedKey(plugin, "treasure_rarity");
        this.cooldownKey = new NamespacedKey(plugin, "treasure_cooldown");
    }

    public boolean placeTreasureHead(Player player, String rarity) {
        Block blockAtFeet = player.getLocation().getBlock();
        blockAtFeet.setType(Material.PLAYER_HEAD);

        if (blockAtFeet.getState() instanceof Skull skull) {
            String headName = plugin.getConfig().getString("treasure-head.rarities." + rarity + ".headsmith-name", "mini copper block");
            
            applyHeadsmithSkin(skull, headName, player);

            PersistentDataContainer pdc = skull.getPersistentDataContainer();
            pdc.set(headKey, PersistentDataType.BYTE, (byte) 1);
            pdc.set(rarityKey, PersistentDataType.STRING, rarity);
            skull.update(true);
            return true;
        }
        return false;
    }

    private void applyHeadsmithSkin(Skull skull, String headsmithName, Player fallbackPlayer) {
        Plugin headsmith = Bukkit.getPluginManager().getPlugin("Headsmith");
        if (headsmith != null && headsmith.isEnabled()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "headsmith givehead " + fallbackPlayer.getName() + " " + headsmithName);
        } else {
            skull.setOwningPlayer(fallbackPlayer);
        }
    }

    public void processHeadClick(Player player, Skull skull) {
        PersistentDataContainer blockPdc = skull.getPersistentDataContainer();
        if (!blockPdc.has(headKey, PersistentDataType.BYTE)) return;

        PersistentDataContainer playerPdc = player.getPersistentDataContainer();

        long cooldownDays = plugin.getConfig().getLong("treasure-head.cooldown-days", 30);
        long cooldownMs = cooldownDays * 24 * 60 * 60 * 1000;
        long currentTime = System.currentTimeMillis();

        long lastClaim = playerPdc.getOrDefault(cooldownKey, PersistentDataType.LONG, 0L);

        if (currentTime - lastClaim < cooldownMs) {
            long remainingMs = cooldownMs - (currentTime - lastClaim);
            long daysLeft = remainingMs / (1000 * 60 * 60 * 24);
            long hoursLeft = (remainingMs / (1000 * 60 * 60)) % 24;

            player.sendMessage(ChatColor.RED + "[Akitos] You have already claimed a treasure head! Try again in " 
                    + daysLeft + "d " + hoursLeft + "h.");
            return;
        }

        String rarity = blockPdc.getOrDefault(rarityKey, PersistentDataType.STRING, "normal");
        int minReward = plugin.getConfig().getInt("treasure-head.rarities." + rarity + ".min-reward", 100);
        int maxReward = plugin.getConfig().getInt("treasure-head.rarities." + rarity + ".max-reward", 500);
        int reward = ThreadLocalRandom.current().nextInt(minReward, maxReward + 1);

        playerPdc.set(cooldownKey, PersistentDataType.LONG, currentTime);
        player.sendMessage(ChatColor.GOLD + "[Akitos] You found a " + rarity.toUpperCase() + " treasure head and received $" + reward + "!");
    }
}
