package com.akito_sekuna.lobby.api;

import java.util.UUID;

/**
 * Registered into AkitosCore's service registry in Main.onCoreReady(), so other
 * plugins can reset a player's treasure-head cooldowns without depending on
 * AkitosLobby's internals directly. UUID rather than Player/OfflinePlayer since
 * this is a service contract, not tied to whether the target is currently online.
 */
public interface ITreasureCooldownService {

    /**
     * Clears every treasure-head cooldown currently stored on this player,
     * across every physical head they have ever claimed from. Safe to call
     * even if the player has no cooldowns at all, it's simply a no-op then.
     */
    void resetAllCooldowns(UUID uuid);
}
