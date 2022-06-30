package com.steamsworld.pockethorse;

import org.bukkit.ChatColor;

import java.util.Objects;

public enum Locale {

    USAGE,
    INVALID_HORSE_TYPE,
    PLAYER_NOT_ONLINE,
    FULL_INVENTORY,
    GIVEN_ITEM,
    RECEIVED_ITEM;

    public String format() {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(PocketHorse.getPlugin(PocketHorse.class).getConfig().getString("messages." + this.name().toLowerCase().replace("_", "-"))));
    }

}
