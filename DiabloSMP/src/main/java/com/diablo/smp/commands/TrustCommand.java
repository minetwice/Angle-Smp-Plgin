package com.diablo.smp.commands;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrustCommand implements org.bukkit.command.CommandExecutor, TabCompleter {

    private final DiabloPlugin plugin;

    public TrustCommand(DiabloPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c§lERROR: §rOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("diablo.trust")) {
            player.sendMessage("§c§lERROR: §rYou don't have permission!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("");
            player.sendMessage("§5§l♦ §r§5Trust System");
            player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§e/trust <player> §7- Initiate 5-minute trade window");
            player.sendMessage("");
            player.sendMessage("§7Rules:");
            player.sendMessage("• Both players must agree");
            player.sendMessage("• Window expires in 5 minutes");
            player.sendMessage("• If trusted player kills holder, book drops");
            player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§c§lERROR: §rPlayer not found or offline!");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage("§c§lERROR: §rYou cannot trust yourself!");
            return true;
        }

        plugin.getTrustListener().initiateTrust(player, target);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.startsWith(args[0]) && !name.equals(sender.getName()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
