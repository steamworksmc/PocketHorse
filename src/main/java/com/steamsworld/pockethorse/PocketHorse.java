package com.steamsworld.pockethorse;

import com.steamsworld.pockethorse.commands.HorseMountCommand;
import com.steamsworld.pockethorse.handlers.HorseHandler;
import com.steamsworld.pockethorse.listeners.HorseListeners;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class PocketHorse extends JavaPlugin {

    private HorseHandler horseHandler;

    @Override
    public void onEnable() {
        if (!new File(this.getDataFolder(), "config.yml").exists())
            saveResource("config.yml", false);

        horseHandler = new HorseHandler(this);
        Objects.requireNonNull(getCommand("horsemount")).setExecutor(new HorseMountCommand(horseHandler));
        getServer().getPluginManager().registerEvents(new HorseListeners(horseHandler), this);
    }

    @Override
    public void onDisable() {
        if (horseHandler != null)
            horseHandler = null;
    }
}
