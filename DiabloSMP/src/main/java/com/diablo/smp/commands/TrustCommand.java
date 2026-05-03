package com.diablo.smp.commands;

import com.diablo.smp.DiabloPlugin;
import com.diablo.smp.listeners.TrustListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrustCommand implements CommandExecutor, TabCompleter {

    private final DiabloPlugin plugin;
    private final TrustListener trustListener;

    public TrustCommand(DiabloPlugin plugin, TrustListener trustListener) {
        this.plugin = plugin;
        this.trustListener = trustListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§5§l=== TRUST SYSTEM ===");
            player.sendMessage("§e/trust <player> §7- Establish a 5-minute trade window");
            player.sendMessage("");
            player.sendMessage("§7During this window:");
            player.sendMessage("• You can safely exchange the Soul Book");
            player.sendMessage("• If the trusted player kills you, the book drops");
            player.sendMessage("• After 5 minutes, the trust expires");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer not found or offline!");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage("§cYou cannot trust yourself!");
            return true;
        }

        // Create trust relationship
        trustListener.addTrust(player, target);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) return new ArrayList<>();

        if (args.length == 1) {
            // Suggest online players (excluding self)
            Player player = (Player) sender;
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> !name.equals(player.getName()))
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
