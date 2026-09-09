package com.akito_sekuna.lobby.commands;

import com.akito_sekuna.lobby.Main;
import com.akito_sekuna.lobby.managers.TreasureManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Set;

public class TreasureCommand implements CommandExecutor {

    private final Main plugin;
    private final TreasureManager treasureManager;

    public TreasureCommand(Main plugin, TreasureManager treasureManager) {
        this.plugin = plugin;
        this.treasureManager = treasureManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("[Akitos] Only players can execute this command.").color(NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("akitoslobby.admin")) {
            player.sendMessage(Component.text("[Akitos] You do not have permission to place treasure heads.").color(NamedTextColor.RED));
            return true;
        }

        String sub = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";

        if (sub.equals("create")) {
            handleCreate(player, args);
            return true;
        }
        if (sub.equals("remove")) {
            handleRemove(player, args);
            return true;
        }
        if (sub.equals("list")) {
            handleList(player);
            return true;
        }

        handlePlace(player, args);
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Component.text("[Akitos] Usage: /treasurehead create <rarity> <minReward> <maxReward> <textureUrl>").color(NamedTextColor.RED));
            return;
        }

        String rarity = args[1].toLowerCase(Locale.ROOT);
        int minReward;
        int maxReward;
        try {
            minReward = Integer.parseInt(args[2]);
            maxReward = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("[Akitos] Min and max reward must be whole numbers.").color(NamedTextColor.RED));
            return;
        }
        String textureUrl = args[4];

        boolean success = plugin.getConfigManager().setRarity(rarity, minReward, maxReward, textureUrl);
        if (success) {
            player.sendMessage(Component.text("[Akitos] Rarity '" + rarity + "' created/updated.").color(NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("[Akitos] Failed to save rarity '" + rarity + "'.").color(NamedTextColor.RED));
        }
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("[Akitos] Usage: /treasurehead remove <rarity>").color(NamedTextColor.RED));
            return;
        }

        String rarity = args[1].toLowerCase(Locale.ROOT);
        if (!plugin.getConfigManager().rarityExists(rarity)) {
            player.sendMessage(Component.text("[Akitos] Rarity '" + rarity + "' does not exist.").color(NamedTextColor.RED));
            return;
        }

        boolean success = plugin.getConfigManager().removeRarity(rarity);
        if (success) {
            player.sendMessage(Component.text("[Akitos] Rarity '" + rarity + "' removed. Already-placed heads of this rarity keep their existing skin, but claiming them will use reward defaults.").color(NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("[Akitos] Failed to remove rarity '" + rarity + "'.").color(NamedTextColor.RED));
        }
    }

    private void handleList(Player player) {
        Set<String> rarities = plugin.getConfigManager().getRarityIds();
        if (rarities.isEmpty()) {
            player.sendMessage(Component.text("[Akitos] No rarities configured.").color(NamedTextColor.RED));
            return;
        }
        player.sendMessage(Component.text("[Akitos] Configured rarities: " + String.join(", ", rarities)).color(NamedTextColor.GOLD));
    }

    private void handlePlace(Player player, String[] args) {
        String rarity = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "normal";

        if (!plugin.getConfigManager().rarityExists(rarity)) {
            player.sendMessage(Component.text("[Akitos] Unknown rarity '" + rarity + "'. Use /treasurehead list to see available rarities.").color(NamedTextColor.RED));
            return;
        }

        boolean success = treasureManager.placeTreasureHead(player, rarity);
        if (success) {
            player.sendMessage(Component.text("[Akitos] Placed " + rarity.toUpperCase() + " treasure head at your feet!").color(NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("[Akitos] Failed to place treasure head.").color(NamedTextColor.RED));
        }
    }
}
