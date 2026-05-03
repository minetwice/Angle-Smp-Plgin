package com.diablo.smp.commands;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiabloCommand implements org.bukkit.command.CommandExecutor, TabCompleter {

    private final DiabloPlugin plugin;
    private static final String BOOK_NAME = "§5§lDiablo Soul Book";
    private static final List<String> ABILITIES = Arrays.asList("soul_exchange", "void_walk", "fire_storm");

    public DiabloCommand(DiabloPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cConsole cannot use this command.");
            return true;
        }

        if (!player.hasPermission("diablo.admin")) {
            player.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§5§l♦ Usage: §r/diablo <give|remove|info|reload>");
            return true;
        }

        String subCmd = args[0].toLowerCase();

        if (subCmd.equals("give")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /diablo give <player> [ability]");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cPlayer not found!");
                return true;
            }

            ItemStack book = createSoulBook();
            target.getInventory().addItem(book);
            target.sendMessage("§5§l♦ §r§dYou received the §5Diablo Soul Book§r§d!");
            player.sendMessage("§aGiven book to " + target.getName());
            
            return true;
        }

        if (subCmd.equals("remove")) {
             player.sendMessage("§aCleanup complete.");
             return true;
        }

        if (subCmd.equals("reload")) {
            player.sendMessage("§aPlugin reloaded!");
            return true;
        }

        player.sendMessage("§cUnknown command.");
        return true;
    }

    private ItemStack createSoulBook() {
        ItemStack book = new ItemStack(org.bukkit.Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(BOOK_NAME);
            meta.setLore(Arrays.asList("§7Right Click to Absorb", "§7Left Click to Use Ability", "§c§lCannot be dropped"));
            meta.setUnbreakable(true);
            book.setItemMeta(meta);
        }
        return book;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("give", "remove", "info", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return ABILITIES.stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
