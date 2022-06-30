package com.steamsworld.pockethorse.listeners;

import com.steamsworld.pockethorse.PocketHorse;
import com.steamsworld.pockethorse.handlers.HorseHandler;
import de.tr7zw.nbtapi.NBTItem;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class HorseListeners implements Listener {

    private final HorseHandler horseHandler;
    private final Map<Player, ItemStack> mountItems = new HashMap<>();
    private final Map<Player, AbstractHorse> liveHorses = new HashMap<>();
    private final Map<Player, Integer> previousBlockCount = new HashMap<>();

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (!event.getAction().name().contains("RIGHT_")) return;
        if (!horseHandler.isSimilar(event.getItem())) return;

        Player player = event.getPlayer();
        PlayerInventory playerInventory = player.getInventory();
        int itemSlot = playerInventory.getHeldItemSlot();

        player.getInventory().remove(Objects.requireNonNull(playerInventory.getItem(itemSlot)));
        mountItems.put(event.getPlayer(), event.getItem());

        NBTItem nbtItem = new NBTItem(event.getItem());
        String horseType = nbtItem.getString("horse-type");

        Horse.Color color = null;
        System.out.println("debug here! " + horseType);
        switch (horseType.toUpperCase()) {
            case "BROWN":
                color = Horse.Color.BROWN;
                break;
            case "BLACK":
                color = Horse.Color.BLACK;
                break;
            case "WHITE":
                color = Horse.Color.WHITE;
                break;
        }

        AbstractHorse spawnedHorse;
        if (color == null)
            spawnedHorse = player.getWorld().spawn(player.getLocation(), SkeletonHorse.class);
        else spawnedHorse = player.getWorld().spawn(player.getLocation(), Horse.class);

        spawnedHorse.setOwner(player);
        spawnedHorse.setAdult();
        spawnedHorse.setMetadata("NoDrops", new FixedMetadataValue(PocketHorse.getPlugin(PocketHorse.class), true));
        spawnedHorse.setAI(false);
        spawnedHorse.getInventory().setSaddle(new ItemStack(Material.SADDLE));

        AttributeInstance attribute = spawnedHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        assert attribute != null;

        Objects.requireNonNull(spawnedHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(horseHandler.getSpeed(attribute.getBaseValue(), nbtItem.getInteger("level")));

        if (color != null) {
            ((Horse) spawnedHorse).setColor(color);
            ((Horse) spawnedHorse).setStyle(Horse.Style.NONE);
        }

        spawnedHorse.addPassenger(player);
        liveHorses.put(player, spawnedHorse);
        previousBlockCount.put(player, player.getStatistic(Statistic.HORSE_ONE_CM));
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (event.getEntity().hasMetadata("NoDrops"))
            event.getDrops().clear();
    }

    @EventHandler
    public void onEntityDismountEvent(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!(event.getDismounted() instanceof AbstractHorse)) return;
        if (!(liveHorses.containsKey(player))) return;
        if (!mountItems.containsKey(player)) return;

        ItemStack inventoryItem = mountItems.remove(player).clone();
        event.getDismounted().remove(); /* kill the horse */

        int previousBlockCount = this.previousBlockCount.remove(player);
        int currentBlockCount = player.getStatistic(Statistic.HORSE_ONE_CM);
        int blocksWalked = (currentBlockCount - previousBlockCount) / 100;

        NBTItem nbtItem = new NBTItem(inventoryItem);

        int nbtBlocksWalked = nbtItem.getInteger("blocks-walked");
        int nowWalked = nbtBlocksWalked + blocksWalked;

        int currLevel = nbtItem.getInteger("horse-level");
        int newLevel = currLevel + 1;

        int walkDistanceRequirement = 5000 * currLevel;

        System.out.println("nowWalked: " + nowWalked);
        System.out.println("walkDistanceRequirement: " + walkDistanceRequirement);
        if (nowWalked - walkDistanceRequirement >= 0 && !isCapped(nbtItem.getString("horse-type"), currLevel)) { /* the horse travelled another 5k blocks, and can level up. */
            nbtItem.setInteger("horse-level", newLevel);
            inventoryItem = horseHandler.buildItem(nbtItem.getString("horse-type"), newLevel);
        }

        nbtItem.setInteger("blocks-walked", nowWalked);
        nbtItem.mergeCustomNBT(inventoryItem);

        player.getInventory().addItem(inventoryItem);
        player.updateInventory();
    }

    /**
     * Check if this horse type is capped at the given level.
     *
     * @param horseType the horse type
     * @param currLevel the current level
     * @return true if the horse type is capped at the given level, false otherwise.
     */
    private boolean isCapped(String horseType, int currLevel) {
        switch (horseType.toUpperCase()) {
            case "BROWN":
                return currLevel >= 10;
            case "BLACK":
                return currLevel >= 15;
            case "WHITE":
                return currLevel >= 20;
            case "SKELETON":
                return currLevel >= 25;
            default:
                throw new IllegalStateException("what da heck! how this possible!");
        }
    }

}
