package com.akito_sekuna.lobby.commands;

import com.akito_sekuna.lobby.Main;
import com.akito_sekuna.lobby.worldcentral.WorldCentral;
import com.akito_sekuna.lobby.worldcentral.WorldCentralMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A standalone entry point into the same WorldCentral destination menu the
 * physical compass item opens, for whenever the compass isn't available
 * (dropped, wiped, or the player just wants a quick menu without needing to
 * hold the item). Deliberately not an alias of /lobby, this opens the full
 * menu rather than teleporting straight to one fixed destination.
 */
public class HubCommand implements CommandExecutor {

    private final Main plugin;

    public HubCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("[Akitos] Only players can execute this command.").color(NamedTextColor.RED));
            return true;
        }

        WorldCentral entry = plugin.getWorldCentralRegistry().getById("lobby");
        if (entry == null) {
            player.sendMessage(Component.text("[Akitos] Error: no 'lobby' entry configured under worldcentral:.").color(NamedTextColor.RED));
            return true;
        }

        WorldCentralMenu.open(player, entry);
        return true;
    }
}
