package com.akito_sekuna.akitoslobby.listeners;

import com.akito_sekuna.akitoslobby.Main;
import com.akito_sekuna.akitoslobby.managers.TreasureManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class HeadClickListener implements Listener {

    private final Main plugin;
    private final TreasureManager treasureManager;

    public HeadClickListener(Main plugin, TreasureManager treasureManager) {
        this.plugin = plugin;
        this.treasureManager = treasureManager;
    }

    @EventHandler
    public void onHeadClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || (clickedBlock.getType() != Material.PLAYER_HEAD && clickedBlock.getType() != Material.PLAYER_WALL_HEAD)) {
            return;
        }

        if (clickedBlock.getState() instanceof Skull skull) {
            treasureManager.processHeadClick(event.getPlayer(), skull);
        }
    }
}
