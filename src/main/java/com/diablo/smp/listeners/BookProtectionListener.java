package com.diablo.smp.listeners;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
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
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        if (isDiabloBook(item)) {
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();

            if (throwCooldowns.containsKey(uuid) && now - throwCooldowns.get(uuid) < 60000) {
                event.setCancelled(true);
                player.sendMessage("§c§lSECURITY LOCKED §r§7You cannot drop this yet.");
                return;
            }

            // First throw warning
            event.setCancelled(true); // Cancel actual drop
            throwCooldowns.put(uuid, now);
            
            // Effects
            player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, player.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
            player.sendMessage("§4§lWARNING! §r§cDo not attempt to drop the Soul Book again for 1 minute!");
            
            player.setVelocity(new Vector(0, 0.2, 0));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item != null && isDiabloBook(item) && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            openAbsorptionGUI(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§5§lSoul Absorption")) return;
        
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        // Slot 4 is the center slot
        if (event.getSlot() == 4) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && isDiabloBook(clicked)) {
                // Remove book and absorb
                player.getInventory().remove(clicked);
                player.closeInventory();
                performAbsorption(player);
            } else {
                player.sendMessage("§cOnly the Diablo Soul Book can be absorbed here.");
            }
        }
    }

    private void openAbsorptionGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "§5§lSoul Absorption");
        player.openInventory(gui);
        player.sendMessage("§7Place the book in the center slot to absorb power.");
    }

    public void performAbsorption(Player player) {
        new BukkitRunnable() {
            int ticks = 0;
            Location loc = player.getLocation().clone();
            @Override
            public void run() {
                if (!player.isOnline() || ticks > 60) {
                    cancel();
                    player.spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1.8, 0), 20, 0.5, 0, 0.5, 0.05);
                    player.sendMessage("§5§lPOWER ABSORBED! §r§dAngel Crown Activated.");
                    return;
                }
                
                double angle = ticks * 0.5;
                double x = Math.cos(angle) * 0.8;
                double z = Math.sin(angle) * 0.8;
                
                player.getWorld().spawnParticle(Particle.DRAGON_BREATH, 
                    loc.clone().add(x, 0.5, z), 1, 0, 0, 0, 0);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private boolean isDiabloBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(BOOK_NAME);
    }
}
