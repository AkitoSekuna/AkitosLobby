package com.akito_sekuna.lobby.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class DebugListener implements Listener {

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Bukkit.getLogger().info(
                "[DEBUG] " + event.getPlayer().getName()
                        + " teleport: "
                        + event.getFrom()
                        + " -> "
                        + event.getTo()
                        + " | cause: " + event.getCause()
        );
    }
}
