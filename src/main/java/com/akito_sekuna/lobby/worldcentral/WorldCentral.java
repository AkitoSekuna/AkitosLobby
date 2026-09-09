package com.akito_sekuna.lobby.worldcentral;

import net.kyori.adventure.text.Component;

import java.util.List;

public record WorldCentral(String id, String triggerWorld, Component displayName, List<Component> lore,
                            Component menuTitle, List<WorldCentralDestination> destinations) {}
