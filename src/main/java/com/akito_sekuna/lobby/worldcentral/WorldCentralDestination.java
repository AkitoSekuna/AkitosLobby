package com.akito_sekuna.lobby.worldcentral;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.List;

public record WorldCentralDestination(String id, Component displayName, Material material, List<Component> lore, String world) {}
