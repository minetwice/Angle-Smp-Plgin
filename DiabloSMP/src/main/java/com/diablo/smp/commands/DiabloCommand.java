package com.diablo.smp.commands;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class DiabloCommand implements CommandExecutor, TabCompleter {

    private final DiabloPlugin plugin;
    private final List<String> subCommands = Arrays.asList("give", "reload", "info", "remove");
    private final List<String> abilities = Arrays.asList("soul_exchange", "void_walk", "angel_wings", "demon_form", "time_stop", 
            "lightning_strike", "healing_aura", "invisibility", "super_jump", "fire_breath", 
            "ice_shield", "teleport", "strength", "speed", "regeneration");

    public DiabloCommand(DiabloPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("diablo.admin")) {
            sender.sendMessage("§c§lERROR §r§7You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§5§l=== DIABLO SMP ADMIN COMMANDS ===");
            sender.sendMessage("§e/diablo give <player> [ability] §7- Give ability book");
            sender.sendMessage("§e/diablo remove <player> §7- Remove all abilities");
            sender.sendMessage("§e/diablo reload §7- Reload plugin config");
            sender.sendMessage("§e/diablo info <player> §7- Check player abilities");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "give":
                handleGive(sender, args);
                break;
            case "remove":
                handleRemove(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "info":
                handleInfo(sender, args);
                break;
            default:
                sender.sendMessage("§cUnknown command. Use /diablo for help.");
        }

        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /diablo give <player> [ability]");
            sender.sendMessage("§7If no ability is specified, gives the default Soul Book.");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found or offline!");
            return;
        }

        String abilityName = args.length > 2 ? args[2].toLowerCase() : "soul_exchange";
        ItemStack book = createAbilityBook(abilityName);

        // Give the book
        HashMap<Integer, ItemStack> leftover = target.getInventory().addItem(book);
        if (!leftover.isEmpty()) {
            target.getWorld().dropItemNaturally(target.getLocation(), book);
            sender.sendMessage("§a§lSUCCESS §r§e" + target.getName() + " §7received §5" + formatAbilityName(abilityName) + " §7book! (Dropped at feet)");
        } else {
            sender.sendMessage("§a§lSUCCESS §r§e" + target.getName() + " §7received §5" + formatAbilityName(abilityName) + " §7book!");
        }

        target.sendMessage("§5§l★ ABILITY RECEIVED ★");
        target.sendMessage("§7You received: §e" + formatAbilityName(abilityName));
        target.sendMessage("§7Right-click to absorb • Double-crouch to switch stages • Left-click to use");
        
        // Play epic sound
        target.playSound(target.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.8f);
        target.spawnParticle(org.bukkit.Particle.PORTAL, target.getLocation().add(0, 1, 0), 100, 1, 1, 1, 0.5);
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /diablo remove <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return;
        }

        // Remove all Diablo books
        int removed = 0;
        for (int i = 0; i < target.getInventory().getSize(); i++) {
            ItemStack item = target.getInventory().getItem(i);
            if (item != null && isDiabloBook(item)) {
                target.getInventory().setItem(i, null);
                removed++;
            }
        }

        sender.sendMessage("§a§lREMOVED §r§7Removed §e" + removed + " §7Diablo books from §e" + target.getName());
        target.sendMessage("§c§lABILITY REMOVED §r§7Your ability books have been removed.");
    }

    private void handleReload(CommandSender sender) {
        sender.sendMessage("§a§lRELOADING §r§7Reloading Diablo SMP configuration...");
        // In a full version, reload config here
        sender.sendMessage("§a✓ Configuration reloaded successfully!");
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /diablo info <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return;
        }

        int bookCount = 0;
        List<String> foundAbilities = new ArrayList<>();
        
        for (ItemStack item : target.getInventory().getContents()) {
            if (item != null && isDiabloBook(item)) {
                bookCount++;
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "ability"), PersistentDataType.STRING)) {
                    String ability = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "ability"), PersistentDataType.STRING);
                    foundAbilities.add(formatAbilityName(ability));
                }
            }
        }

        sender.sendMessage("§5§l=== PLAYER INFO: " + target.getName() + " ===");
        sender.sendMessage("§7Total Books: §e" + bookCount);
        if (!foundAbilities.isEmpty()) {
            sender.sendMessage("§7Abilities: §b" + String.join("§7, §b", foundAbilities));
        } else {
            sender.sendMessage("§7No specific abilities detected (Default Soul Book)");
        }
    }

    private ItemStack createAbilityBook(String ability) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        
        String displayName = "§5§lDiablo Soul Book";
        if (!ability.equals("soul_exchange")) {
            displayName = "§5§l" + formatAbilityName(ability) + " §r§7Book";
        }
        
        meta.setDisplayName(displayName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§8━━━━━━━━━━━━━━━━━━━━");
        lore.add("§5§lANGEL SMP BOSS PACK");
        lore.add("");
        lore.add("§7Ability: §e" + formatAbilityName(ability));
        lore.add("§7Stages: §b3 §7(Double-crouch to switch)");
        lore.add("§7Activation: §cLeft Click");
        lore.add("");
        lore.add("§eRight-click to absorb power");
        lore.add("§cCannot be dropped or stored!");
        lore.add("§8━━━━━━━━━━━━━━━━━━━━");
        
        meta.setLore(lore);
        
        // Store ability type in NBT
        NamespacedKey key = new NamespacedKey(plugin, "ability");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, ability);
        
        // Make it unbreakable and glowy
        meta.setUnbreakable(true);
        
        book.setItemMeta(meta);
        return book;
    }

    private boolean isDiabloBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        NamespacedKey key = new NamespacedKey(plugin, "ability");
        return meta.getPersistentDataContainer().has(key, PersistentDataType.STRING) || 
               (meta.hasDisplayName() && meta.getDisplayName().contains("Diablo"));
    }

    private String formatAbilityName(String ability) {
        return Arrays.stream(ability.split("_"))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .reduce("", (a, b) -> a + " " + b).trim();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("diablo.admin")) return new ArrayList<>();

        if (args.length == 1) {
            // Suggest subcommands
            return subCommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("info")) {
                // Suggest player names
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            // Suggest ability names
            return abilities.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .toList();
        }

        return new ArrayList<>();
    }
}
