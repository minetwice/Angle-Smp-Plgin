package com.diablo.smp.listeners;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
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

            // Check if on cooldown (hidden 1 minute cooldown)
            if (throwCooldowns.containsKey(uuid) && now - throwCooldowns.get(uuid) < 60000) {
                event.setCancelled(true);
                player.sendMessage("§c§l🔒 SECURITY LOCKED §r§7You cannot drop the Soul Book yet.");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);
                return;
            }

            // First throw attempt - WARNING ONLY
            event.setCancelled(true); // Cancel actual drop
            throwCooldowns.put(uuid, now);
            
            // MASSIVE PARTICLE EFFECTS
            Location loc = player.getLocation();
            player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc.add(0, 1, 0), 100, 2, 2, 2, 0.1);
            player.getWorld().spawnParticle(Particle.FLAME, loc, 50, 1, 1, 1, 0.05);
            player.getWorld().spawnParticle(Particle.PORTAL, loc.add(0, 0.5, 0), 80, 1.5, 1.5, 1.5, 0.2);
            
            // Epic sound
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.8f, 1.2f);
            
            // Title warning
            player.sendTitle("§4§l⚠ WARNING ⚠", "§cDo not attempt to drop again for 1 minute!", 10, 60, 20);
            player.sendMessage("§4§l🚨 SECURITY ALERT 🚨");
            player.sendMessage("§7First violation: Warning issued");
            player.sendMessage("§7Cooldown: §e60 seconds §7(hidden)");
            player.sendMessage("§cNext attempt will be blocked silently!");
            
            // Small knockback
            player.setVelocity(new Vector(0, 0.3, 0));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item != null && isDiabloBook(item) && 
            (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            
            event.setCancelled(true);
            
            // Open absorption GUI
            openAbsorptionGUI(player);
        }
    }

    private void openAbsorptionGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "§5§l✦ Soul Absorption ✦");
        
        // Fill with decorative items
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        
        for (int i = 0; i < 9; i++) {
            if (i != 4) { // Center slot empty
                gui.setItem(i, filler);
            }
        }
        
        player.openInventory(gui);
        player.sendMessage("§5§l✦ ABSORPTION GUI OPENED ✦");
        player.sendMessage("§7Place the §5Diablo Soul Book §7in the center slot");
        player.sendMessage("§7to absorb its power and unlock abilities!");
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_END_PORTAL_FRAME_FILL, 0.8f, 1.2f);
    }

    public void performAbsorption(Player player, ItemStack book) {
        // Remove book from inventory (handled by GUI listener)
        player.closeInventory();
        
        // EPIC ABSORPTION SEQUENCE
        player.sendMessage("§5§l⚡ POWER ABSORPTION INITIATED ⚡");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 0.8f);
        
        Location loc = player.getLocation();
        
        // Dragon particles circling body
        new BukkitRunnable() {
            int ticks = 0;
            int totalTicks = 100; // 5 seconds
            
            @Override
            public void run() {
                if (!player.isOnline() || ticks >= totalTicks) {
                    // Final crown effect
                    createAngelCrown(player);
                    player.sendMessage("§5§l✓ POWER ABSORBED SUCCESSFULLY ✓");
                    player.sendMessage("§7Abilities unlocked! Double-crouch to switch stages.");
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                    cancel();
                    return;
                }
                
                // Spiral dragon breath particles
                double angle = ticks * 0.3;
                double radius = 1.2;
                
                for (int i = 0; i < 3; i++) {
                    double spiralAngle = angle + (i * (Math.PI * 2 / 3));
                    double x = Math.cos(spiralAngle) * radius;
                    double z = Math.sin(spiralAngle) * radius;
                    double y = 0.5 + (ticks * 0.03);
                    
                    player.getWorld().spawnParticle(
                        Particle.DRAGON_BREATH,
                        loc.clone().add(x, y, z),
                        5, 0.1, 0.1, 0.1, 0.01
                    );
                    
                    // End rod sparks
                    player.getWorld().spawnParticle(
                        Particle.END_ROD,
                        loc.clone().add(x, y, z),
                        2, 0.05, 0.05, 0.05, 0
                    );
                }
                
                // Mass portal particles around player
                player.getWorld().spawnParticle(
                    Particle.PORTAL,
                    loc.clone().add(0, 1, 0),
                    20, 0.8, 1, 0.8, 0.05
                );
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private void createAngelCrown(Player player) {
        Location headLoc = player.getLocation().add(0, 1.8, 0);
        
        // Create circular crown of end rods
        for (int i = 0; i < 36; i++) {
            double angle = (i / 36.0) * Math.PI * 2;
            double x = Math.cos(angle) * 0.6;
            double z = Math.sin(angle) * 0.6;
            
            player.getWorld().spawnParticle(
                Particle.END_ROD,
                headLoc.clone().add(x, 0, z),
                3, 0.1, 0.1, 0.1, 0
            );
        }
        
        // Flash of light
        player.getWorld().spawnParticle(
            Particle.FLASH,
            headLoc,
            1, 0, 0, 0, 0
        );
        
        // Colored particles based on ability (purple for default)
        for (int i = 0; i < 50; i++) {
            player.getWorld().spawnParticle(
                Particle.SPELL_MOB,
                headLoc.clone().add(0, 0.2, 0),
                1, 0.5, 0.3, 0.5, 0.8
            );
        }
    }

    private boolean isDiabloBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        // Check by NBT or display name
        if (meta.getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(plugin, "ability"), 
                PersistentDataType.STRING)) {
            return true;
        }
        
        return meta.hasDisplayName() && meta.getDisplayName().contains("Diablo");
    }
}
