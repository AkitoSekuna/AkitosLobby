package com.akito_sekuna.lobby.managers;

import com.akito_sekuna.lobby.Main;
import com.akito_sekuna.lobby.api.ITreasureCooldownService;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TreasureManager implements ITreasureCooldownService {

    private final Main plugin;
    private final NamespacedKey headKey;
    private final NamespacedKey rarityKey;
    private final NamespacedKey headIdKey;

    public TreasureManager(Main plugin) {
        this.plugin = plugin;
        this.headKey = new NamespacedKey(plugin, "treasure_head");
        this.rarityKey = new NamespacedKey(plugin, "treasure_rarity");
        this.headIdKey = new NamespacedKey(plugin, "treasure_head_id");
    }

    public boolean placeTreasureHead(Player player, String rarity) {
        Block blockAtFeet = player.getLocation().getBlock();
        blockAtFeet.setType(Material.PLAYER_HEAD);

        if (blockAtFeet.getState() instanceof Skull skull) {
            String textureUrl = plugin.getConfigManager().getRarityTextureUrl(rarity);

            applyCustomSkin(skull, textureUrl, player);

            PersistentDataContainer pdc = skull.getPersistentDataContainer();
            pdc.set(headKey, PersistentDataType.BYTE, (byte) 1);
            pdc.set(rarityKey, PersistentDataType.STRING, rarity);
            pdc.set(headIdKey, PersistentDataType.STRING, UUID.randomUUID().toString());
            skull.update(true);
            return true;
        }
        return false;
    }

    // Applies a custom skin directly via a texture URL (minecraft-heads.com's "Minecraft-URL"
    // field, not its base64 "Value" field, no decoding needed either way, both point at the
    // same underlying texture). Uses the standard org.bukkit.profile.PlayerProfile/PlayerTextures
    // API, no experimental or Paper-specific types involved. Falls back to the placing player's
    // own face if the URL is blank or malformed, so a bad config value degrades gracefully
    // instead of throwing at placement time.
    private void applyCustomSkin(Skull skull, String textureUrl, Player fallbackPlayer) {
        if (textureUrl != null && !textureUrl.isBlank()) {
            try {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                PlayerTextures textures = profile.getTextures();
                textures.setSkin(URI.create(textureUrl).toURL());
                profile.setTextures(textures);
                skull.setOwnerProfile(profile);
                return;
            } catch (IllegalArgumentException | MalformedURLException e) {
                plugin.getLogger().warning("Invalid treasure head texture-url, falling back to player face: " + e.getMessage());
            }
        }
        skull.setOwningPlayer(fallbackPlayer);
    }

    public void processHeadClick(Player player, Skull skull) {
        PersistentDataContainer blockPdc = skull.getPersistentDataContainer();
        if (!blockPdc.has(headKey, PersistentDataType.BYTE)) return;

        PersistentDataContainer playerPdc = player.getPersistentDataContainer();

        // Each physical head has its own independent cooldown per player, identified
        // by the UUID stamped on it at placement time. Heads placed before this fix
        // have no stored ID, they fall back to a shared "legacy" bucket rather than
        // crashing, since NamespacedKey requires a non-empty key.
        String headId = blockPdc.getOrDefault(headIdKey, PersistentDataType.STRING, "legacy");
        NamespacedKey headCooldownKey = new NamespacedKey(plugin, "treasure_cooldown_" + headId);

        long cooldownDays = plugin.getConfigManager().getTreasureHeadCooldownDays();
        long cooldownMs = cooldownDays * 24 * 60 * 60 * 1000;
        long currentTime = System.currentTimeMillis();

        long lastClaim = playerPdc.getOrDefault(headCooldownKey, PersistentDataType.LONG, 0L);

        if (currentTime - lastClaim < cooldownMs) {
            long remainingMs = cooldownMs - (currentTime - lastClaim);
            long daysLeft = remainingMs / (1000 * 60 * 60 * 24);
            long hoursLeft = (remainingMs / (1000 * 60 * 60)) % 24;

            player.sendMessage(Component.text("[Akitos] You have already claimed a treasure head! Try again in "
                    + daysLeft + "d " + hoursLeft + "h.").color(NamedTextColor.RED));
            return;
        }

        String rarity = blockPdc.getOrDefault(rarityKey, PersistentDataType.STRING, "normal");
        int minReward = plugin.getConfigManager().getRarityMinReward(rarity);
        int maxReward = plugin.getConfigManager().getRarityMaxReward(rarity);
        int reward = ThreadLocalRandom.current().nextInt(minReward, maxReward + 1);

        playerPdc.set(headCooldownKey, PersistentDataType.LONG, currentTime);
        plugin.getTreasureClaimTracker().record(rarity);
        player.sendMessage(Component.text("[Akitos] You found a " + rarity.toUpperCase()
                + " treasure head and received $" + reward + "!").color(NamedTextColor.GOLD));
    }

    // Called through AkitosCore's service registry by other plugins (for example,
    // Amnesia Powder). Paper 1.21.11 exposes OfflinePlayer persistent data as a
    // read-only PersistentDataContainerView, so cooldown data can only be mutated
    // through the live Player instance. The service is therefore a no-op when the
    // target is offline.
    @Override
    public void resetAllCooldowns(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }

        PersistentDataContainer pdc = player.getPersistentDataContainer();
        String namespace = plugin.getName().toLowerCase(Locale.ROOT);

        List<NamespacedKey> toRemove = new ArrayList<>();
        for (NamespacedKey key : pdc.getKeys()) {
            if (key.getNamespace().equals(namespace) && key.getKey().startsWith("treasure_cooldown_")) {
                toRemove.add(key);
            }
        }
        for (NamespacedKey key : toRemove) {
            pdc.remove(key);
        }
    }
}
