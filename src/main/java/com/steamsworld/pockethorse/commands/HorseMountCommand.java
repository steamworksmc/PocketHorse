package com.steamsworld.pockethorse.commands;

import com.steamsworld.pockethorse.Locale;
import com.steamsworld.pockethorse.handlers.HorseHandler;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@SuppressWarnings("NullableProblems")
@RequiredArgsConstructor
public class HorseMountCommand implements CommandExecutor {

    private final HorseHandler horseHandler;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(Locale.USAGE.format());
            return true;
        }

        String horseType = args[1].toLowerCase();
        if (!Arrays.asList("brown", "black", "white", "skeleton").contains(horseType)) {
            sender.sendMessage(Locale.INVALID_HORSE_TYPE.format());
            return true;
        }

        String targetName = args[2];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(Locale.PLAYER_NOT_ONLINE.format().replace("{player}", targetName));
            return true;
        }

        if (target.getInventory().firstEmpty() == -1) {
            sender.sendMessage(Locale.FULL_INVENTORY.format());
            return true;
        }

        ItemStack itemStack = horseHandler.buildItem(horseType, 1);
        target.getInventory().addItem(itemStack);

        sender.sendMessage(Locale.GIVEN_ITEM.format().replace("{player}", targetName).replace("{horse-type}", horseType));
        target.sendMessage(Locale.RECEIVED_ITEM.format().replace("{horse-type}", horseType));
        return false;
    }

}
