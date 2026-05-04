package com.diablo.smp.listeners;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BookProtectionListener implements Listener {

    private final DiabloPlugin plugin;
    private final Map<UUID, Long> throwCooldowns = new HashMap<>();
    private static final String BOOK_NAME = "§5§lDiablo Soul Book";

    public BookProtectionListener(DiabloPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        if (isSoulBook(item)) {
            UUID uuid = p.getUniqueId();
            long now = System.currentTimeMillis();

            // Check Cooldown (1 minute = 60000ms)
            if (throwCooldowns.containsKey(uuid) && now - throwCooldowns.get(uuid) < 60000) {
                event.setCancelled(true);
                long remaining = 60 - ((now - throwCooldowns.get(uuid)) / 1000);
                p.sendMessage("§c§l🔒 SECURITY LOCKED §r§7Wait " + remaining + "s before attempting again.");
                return;
            }

            event.setCancelled(true); // Block Drop Completely
            throwCooldowns.put(uuid, now);

            // MASS PARTICLES - HEAVY EDITION
            Location loc = p.getLocation().add(0, 1, 0);
            World world = p.getWorld();
            
            // Smoke Explosion (100 particles)
            world.spawnParticle(Particle.SMOKE_NORMAL, loc, 100, 1.0, 1.0, 1.0, 0.1);
            world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 50, 0.8, 0.8, 0.8, 0.05);
            
            // Flame Ring (50 particles)
            world.spawnParticle(Particle.FLAME, loc, 50, 0.6, 0.6, 0.6, 0.1);
            
            // Portal Swirl (80 particles)
            world.spawnParticle(Particle.PORTAL, loc, 80, 1.2, 1.2, 1.2, 0.5);
            
            // Spell Effect (40 particles)
            world.spawnParticle(Particle.SPELL_MOB, loc, 40, 0.8, 0.8, 0.8, 0.3, Color.fromRGB(139, 0, 139));
            
            // Epic Sounds
            p.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.5f);
            p.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 2.0f);
            p.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
            
            // Title Warning
            p.sendTitle("§4§l⚠ WARNING ⚠", "§cDo NOT drop the Soul Book!", 10, 50, 20);
            
            // Action Bar Countdown
            p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText("§4§lSECURITY ALERT §r§7- Cooldown: 60s"));
            
            p.sendMessage("");
            p.sendMessage("§4§l♦ SECURITY ALERT ♦");
            p.sendMessage("§7Attempt recorded. Cooldown started (1 minute).");
            p.sendMessage("§7Next attempt will be logged permanently.");
            p.sendMessage("");
            
            // Knockback warning
            p.setVelocity(new Vector(0, 0.4, 0));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (isSoulBook(item) && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            
            // Send to GUI
            p.openInventory(Bukkit.createInventory(null, 9, "§5§l♦ Soul Absorption"));
            
            // Instructions
            p.sendMessage("");
            p.sendMessage("§5§l♦ ABSORPTION MODE ACTIVATED");
            p.sendMessage("§7Place the book in the §eCENTER SLOT§7.");
            p.sendMessage("§7Watch the dragon particles...");
            p.sendMessage("");
        }
    }

    private boolean isSoulBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(BOOK_NAME);
    }
    
    public static boolean isSoulBookStatic(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§5§lDiablo Soul Book");
    }
}
