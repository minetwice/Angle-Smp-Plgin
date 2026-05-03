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

            if (throwCooldowns.containsKey(uuid) && now - throwCooldowns.get(uuid) < 60000) {
                event.setCancelled(true);
                p.sendMessage("§c§lLOCKED §r§7Wait before attempting to drop again.");
                return;
            }

            event.setCancelled(true);
            throwCooldowns.put(uuid, now);

            Location loc = p.getLocation();
            p.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc.clone().add(0, 1, 0), 100, 0.5, 0.5, 0.5, 0.1);
            p.getWorld().spawnParticle(Particle.FLAME, loc.clone(), 50, 0.5, 0.5, 0.5, 0.1);
            p.getWorld().spawnParticle(Particle.PORTAL, loc.clone(), 80, 0.5, 1, 0.5, 0.5);
            
            p.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.5f);
            p.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 2.0f);
            
            p.sendTitle("§4§lWARNING", "§cDo not drop the Soul Book!", 10, 40, 10);
            p.sendMessage("§4§lSECURITY ALERT: §rAttempt recorded. Cooldown started (1 min).");
            
            p.setVelocity(new Vector(0, 0.3, 0));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (isSoulBook(item) && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            p.openInventory(Bukkit.createInventory(null, 9, "§5§lSoul Absorption"));
            p.sendMessage("§7Place the book in the center slot to absorb power.");
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
