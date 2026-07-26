package com.akito_sekuna.akitoslobby.commands;

import com.akito_sekuna.akitoslobby.Main;
import com.akito_sekuna.akitoslobby.managers.TreasureManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

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
            sender.sendMessage(ChatColor.RED + "[Akitos] Only players can execute this command.");
            return true;
        }

        if (!player.hasPermission("akitoslobby.admin")) {
            player.sendMessage(ChatColor.RED + "[Akitos] You do not have permission to place treasure heads.");
            return true;
        }

        String rarity = "normal";
        if (args.length > 0) {
            rarity = args[0].toLowerCase(Locale.ROOT);
        }

        if (!rarity.equals("normal") && !rarity.equals("big") && !rarity.equals("mega")) {
            player.sendMessage(ChatColor.RED + "[Akitos] Invalid rarity! Use: /treasurehead [normal|big|mega]");
            return true;
        }

        boolean success = treasureManager.placeTreasureHead(player, rarity);
        if (success) {
            String headName = plugin.getConfig().getString("treasure-head.rarities." + rarity + ".headsmith-name", "mini copper block");
            player.sendMessage(ChatColor.GREEN + "[Akitos] Placed " + rarity.toUpperCase() + " treasure head (" + headName + ") at your feet!");
        } else {
            player.sendMessage(ChatColor.RED + "[Akitos] Failed to place treasure head.");
        }

        return true;
    }
}
