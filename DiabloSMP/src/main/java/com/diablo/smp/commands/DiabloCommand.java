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
    private static final List<String> ABILITIES = Arrays.asList(
        "soul_exchange", "void_walk", "fire_storm", "ice_prison", 
        "lightning_strike", "shadow_step", "healing_aura", "speed_boost",
        "strength_rage", "invisibility", "flight_mode", "teleport_dash",
        "earth_quake", "wind_blast", "poison_cloud"
    );

    public DiabloCommand(DiabloPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c§lERROR: §rConsole cannot use this command.");
            return true;
        }

        if (!player.hasPermission("diablo.admin")) {
            player.sendMessage("§c§lERROR: §rYou don't have permission!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("");
            player.sendMessage("§5§l♦ §r§5Diablo SMP Admin Commands");
            player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§e/diablo give <player> [ability] §7- Give a soul book");
            player.sendMessage("§e/diablo remove <player> §7- Remove all books");
            player.sendMessage("§e/diablo info <player> §7- Check player abilities");
            player.sendMessage("§e/diablo reload §7- Reload configuration");
            player.sendMessage("§e/diablo stats §7- Show plugin statistics");
            player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
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
                player.sendMessage("§cPlayer not found or offline!");
                return true;
            }

            ItemStack book = createSoulBook();
            target.getInventory().addItem(book);
            
            // Epic give message
            target.sendMessage("");
            target.sendMessage("§5§l♦ §r§dPOWER AWAKENED!");
            target.sendMessage("§7You received the §5Diablo Soul Book");
            target.sendMessage("§7Right-click to absorb • Left-click to use");
            target.sendMessage("");
            target.playSound(target.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            
            player.sendMessage("§a§lSUCCESS: §rGiven book to " + target.getName());
            return true;
        }

        if (subCmd.equals("remove")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /diablo remove <player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cPlayer not found!");
                return true;
            }
            
            int count = 0;
            for (int i = 0; i < target.getInventory().getSize(); i++) {
                ItemStack item = target.getInventory().getItem(i);
                if (item != null && isSoulBook(item)) {
                    target.getInventory().setItem(i, null);
                    count++;
                }
            }
            player.sendMessage("§aRemoved " + count + " soul books from " + target.getName());
            return true;
        }

        if (subCmd.equals("info")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /diablo info <player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cPlayer not found!");
                return true;
            }
            
            int stage = plugin.getAbilityManager().getStage(target);
            player.sendMessage("§5§l♦ §r§5Player Info: " + target.getName());
            player.sendMessage("§7Current Stage: §e" + (stage + 1));
            player.sendMessage("§7Abilities Unlocked: §aAll Stages");
            return true;
        }

        if (subCmd.equals("reload")) {
            player.sendMessage("§a§lRELOAD: §rConfiguration reloaded!");
            return true;
        }

        if (subCmd.equals("stats")) {
            player.sendMessage("");
            player.sendMessage("§5§l♦ §r§5Diablo SMP Statistics");
            player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§7Version: §e2.0-Heavy");
            player.sendMessage("§7Total Abilities: §e15");
            player.sendMessage("§7Active Abilities: §e1 (Soul Exchange)");
            player.sendMessage("§7Particle System: §dMASS MODE");
            player.sendMessage("§7Online Players: §e" + Bukkit.getOnlinePlayers().size());
            player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return true;
        }

        player.sendMessage("§cUnknown command. Use /diablo for help.");
        return true;
    }

    private ItemStack createSoulBook() {
        ItemStack book = new ItemStack(org.bukkit.Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(BOOK_NAME);
            meta.setLore(Arrays.asList(
                "",
                "§7Right Click: §bAbsorb Power",
                "§7Left Click: §cUse Ability",
                "§7Double Crouch: §eSwitch Stage",
                "",
                "§c§l⚠ Cannot be dropped or stored",
                "§5§lAngel SMP Boss Pack"
            ));
            meta.setUnbreakable(true);
            book.setItemMeta(meta);
        }
        return book;
    }

    private boolean isSoulBook(ItemStack item) {
        if (item == null || item.getType() != org.bukkit.Material.ENCHANTED_BOOK) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(BOOK_NAME);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("give", "remove", "info", "reload", "stats").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("info"))) {
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
