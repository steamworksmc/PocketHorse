package com.steamsworld.pockethorse.handlers;

import com.steamsworld.pockethorse.PocketHorse;
import com.steamsworld.pockethorse.utils.Color;
import de.tr7zw.nbtapi.NBTItem;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class HorseHandler {

    private final PocketHorse plugin;
    private ItemStack baseItem;

    /**
     * Builds the mount item with the given horse type and level.
     *
     * @param horseType The type of horse to mount.
     * @param level     The level of the horse.
     * @return The item stack of the horse.
     */
    public ItemStack buildItem(String horseType, int level) {
        horseType = WordUtils.capitalize(horseType.toLowerCase());

        if (baseItem == null) {
            ConfigurationSection itemConfig = plugin.getConfig().getConfigurationSection("mount-item");
            assert itemConfig != null;

            ItemStack itemStack = new ItemStack(Material.valueOf(itemConfig.getString("material")));
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;

            if (itemConfig.contains("display-name") && !itemConfig.getString("display-name", "").isEmpty())
                itemMeta.setDisplayName(Color.color(itemConfig.getString("display-name")));

            if (itemConfig.contains("lore") && itemConfig.isList("lore"))
                itemMeta.setLore(itemConfig.getStringList("lore").stream().map(Color::color).collect(java.util.stream.Collectors.toList()));

            itemStack.setItemMeta(itemMeta);
            baseItem = itemStack;
        }

        ItemStack clonedItem = baseItem.clone();
        ItemMeta itemMeta = clonedItem.getItemMeta();
        assert itemMeta != null;

        List<String> lore = new ArrayList<>();
        String formattedHorseType = "";
        if (horseType.equalsIgnoreCase("skeleton")) {
            formattedHorseType = "Skeleton";
        } else if (horseType.equalsIgnoreCase("brown")) {
            formattedHorseType = "&#A52A2a";
        } else if (horseType.equalsIgnoreCase("white")) {
            formattedHorseType = "&f";
        } else if (horseType.equalsIgnoreCase("black")) {
            formattedHorseType = "&0";
        }

        if (itemMeta.hasLore())
            for (String loreLine : Objects.requireNonNull(itemMeta.getLore()))
                lore.add(Color.color(loreLine.replace("{horse-type}", horseType).replace("{horse-type-formatted}", formattedHorseType + horseType).replace("{level}", String.valueOf(level))));

        if (!lore.isEmpty()) {
            itemMeta.setLore(lore);
            clonedItem.setItemMeta(itemMeta);
        }

        /* apply nbt tags */
        NBTItem nbtItem = new NBTItem(clonedItem);
        nbtItem.setBoolean("PocketHorse", true);
        applyHorseNBT(nbtItem, horseType, level);
        nbtItem.mergeNBT(clonedItem);

        return clonedItem;
    }

    /**
     * Applies the horse nbt tags to the given nbt item.
     *
     * @param nbtItem   The nbt item to apply the nbt tags to.
     * @param horseType The type of horse to mount.
     * @param level     The level of the horse.
     */
    private void applyHorseNBT(NBTItem nbtItem, String horseType, int level) {
        nbtItem.setString("horse-type", horseType);
        nbtItem.setInteger("horse-level", level);
        nbtItem.setInteger("blocks-walked", 0);
    }

    /**
     * Get the speed for the given level.
     *
     * @param baseValue The base value of the speed.
     * @param level     The level of the horse.
     * @return The speed of the horse.
     */
    public double getSpeed(double baseValue, int level) {
        return baseValue * plugin.getConfig().getDouble("levels." + level, 1.00);
    }

    /**
     * Check if this item is a horse mount.
     *
     * @param itemStack The item stack to check.
     * @return True if this item is a horse mount.
     */
    public boolean isSimilar(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getBoolean("PocketHorse");
    }

}
