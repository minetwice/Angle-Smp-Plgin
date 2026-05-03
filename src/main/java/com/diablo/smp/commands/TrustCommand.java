package com.diablo.smp.commands;

import com.diablo.smp.DiabloPlugin;
import com.diablo.smp.listeners.TrustListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cUsage: /trust <playerName>");
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer not found or is offline!");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage("§cYou cannot trust yourself!");
            return true;
        }

        // Execute trust logic
        trustListener.addTrust(player, target);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(prefix)) {
                    completions.add(p.getName());
                }
            }
        }
        return completions;
    }
}
