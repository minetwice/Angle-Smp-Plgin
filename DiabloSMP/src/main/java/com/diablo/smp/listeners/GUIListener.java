package com.diablo.smp.listeners;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class GUIListener implements Listener {

    private final DiabloPlugin plugin;

    public GUIListener(DiabloPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryOpen(org.bukkit.event.inventory.InventoryOpenEvent e) {
        if (!e.getView().getTitle().equals("§5§l♦ Soul Absorption")) return;
        
        Inventory inv = e.getInventory();
        // Fill with black glass panes except center slot
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }
        
        for (int i = 0; i < 9; i++) {
            if (i != 4) { // Skip center slot
                inv.setItem(i, glass);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("§5§l♦ Soul Absorption")) return;
        e.setCancelled(true);

        if (e.getSlot() == 4) { // Center Slot
            ItemStack cursor = e.getCursor();
            ItemStack current = e.getCurrentItem();

            // Check if player puts book in center
            if (BookProtectionListener.isSoulBookStatic(cursor) || BookProtectionListener.isSoulBookStatic(current)) {
                ItemStack book = BookProtectionListener.isSoulBookStatic(cursor) ? cursor : current;
                if (book != null) {
                    Player p = (Player) e.getWhoClicked();
                    performAbsorption(p, book);
                    p.closeInventory();
                    // Remove book from inventory/cursor
                    if (BookProtectionListener.isSoulBookStatic(cursor)) {
                        e.setCursor(null);
                    } else {
                        e.setCurrentItem(null);
                    }
                }
            }
        }
    }

    private void performAbsorption(Player p, ItemStack book) {
        p.sendMessage("");
        p.sendMessage("§5§l♦ ♦ ♦ ABSORPTION STARTED ♦ ♦ ♦");
        p.sendMessage("§7Feel the power flowing through your veins...");
        p.sendMessage("");
        
        Location loc = p.getLocation();
        World world = p.getWorld();

        // DRAGON SPIRAL PARTICLES - HEAVY EDITION
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!p.isOnline() || ticks > 120) { // 6 seconds
                    createCrown(p);
                    cancel();
                    return;
                }

                // Spiral parameters
                double angle = ticks * 0.3;
                double radius = 1.8;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                
                // Spiral up and down
                double y = Math.sin(ticks * 0.2) * 1.5;

                // Heavy particle density
                for (int i = 0; i < 5; i++) {
                    world.spawnParticle(Particle.DRAGON_BREATH, 
                        loc.clone().add(x, y + 1, z), 3, 0.2, 0.2, 0.2, 0.05);
                    world.spawnParticle(Particle.END_ROD, 
                        loc.clone().add(x, y + 1, z), 2, 0.1, 0.1, 0.1, 0);
                    world.spawnParticle(Particle.PORTAL, 
                        loc.clone().add(x, y + 1, z), 2, 0.15, 0.15, 0.15, 0.3);
                }
                
                // Body particles
                world.spawnParticle(Particle.SPELL_MOB, 
                    loc.clone().add(0, 0.5, 0), 10, 0.3, 0.5, 0.3, 0, 
                    Color.fromRGB(139, 0, 139));
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private void createCrown(Player p) {
        Location head = p.getLocation().add(0, 1.8, 0);
        World world = p.getWorld();
        
        // Angel Crown - 36 End Rods in circle
        for (int i = 0; i < 36; i++) {
            double angle = (i / 36.0) * 2 * Math.PI;
            double x = Math.cos(angle) * 0.6;
            double z = Math.sin(angle) * 0.6;
            world.spawnParticle(Particle.END_ROD, 
                head.clone().add(x, 0, z), 5, 0.1, 0.1, 0.1, 0.1);
        }
        
        // Flash effect
        world.spawnParticle(Particle.FLASH, head, 1, 0, 0, 0, 0);
        
        // Epic sound
        p.playSound(head, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.5f);
        p.playSound(head, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        
        p.sendMessage("");
        p.sendMessage("§5§l♦ POWER ABSORBED!");
        p.sendMessage("§dAngel Crown Activated §7(Stage 1 Ready)");
        p.sendMessage("§7Double crouch to switch stages");
        p.sendMessage("§7Left click to use ability");
        p.sendMessage("");
        
        // Give temporary glow effect (using Night Vision as substitute for older versions)
        p.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.NIGHT_VISION, 200, 0, false, false));
    }
}
