package com.diablo.smp.listeners;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TrustListener implements Listener {
    
    private final DiabloPlugin plugin;
    // Map<HolderUUID, TrustedPlayerUUID>
    private final Map<UUID, UUID> activeTrusts = new HashMap<>();
    private final Map<UUID, Long> trustExpiry = new HashMap<>();

    public TrustListener(DiabloPlugin plugin) {
        this.plugin = plugin;
    }

    public void addTrust(Player holder, Player trusted) {
        activeTrusts.put(holder.getUniqueId(), trusted.getUniqueId());
        long expiry = System.currentTimeMillis() + (5 * 60 * 1000); // 5 mins
        trustExpiry.put(holder.getUniqueId(), expiry);
        
        holder.sendMessage("§5§l=== TRUST ESTABLISHED ===");
        holder.sendMessage("§7Trusted player: §e" + trusted.getName());
        holder.sendMessage("§7Duration: §e5 minutes");
        holder.sendMessage("");
        holder.sendMessage("§7§oDuring this window:");
        holder.sendMessage("§7§o• You can safely exchange the Soul Book");
        holder.sendMessage("§7§o• If §e" + trusted.getName() + " §7§okills you, the book drops");
        holder.sendMessage("§7§o• After 5 minutes, trust expires automatically");
        holder.playSound(holder.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
        
        trusted.sendMessage("§5§l=== TRUST RECEIVED ===");
        trusted.sendMessage("§7You are now trusted by: §e" + holder.getName());
        trusted.sendMessage("§7Duration: §e5 minutes");
        trusted.sendMessage("§c§oIf you kill them during this time, their Soul Book will drop!");
        trusted.playSound(trusted.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);

        // Countdown reminder
        new BukkitRunnable() {
            int remainingMinutes = 4;
            
            @Override
            public void run() {
                if (!holder.isOnline() || !activeTrusts.containsKey(holder.getUniqueId())) {
                    cancel();
                    return;
                }
                
                if (remainingMinutes > 0) {
                    holder.sendMessage("§7§oTrust expires in §e" + remainingMinutes + " minute(s)§7§o...");
                    remainingMinutes--;
                }
            }
        }.runTaskTimer(plugin, 20 * 60, 20 * 60); // Every minute
        
        // Auto-expire after 5 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                if (trustExpiry.getOrDefault(holder.getUniqueId(), 0L) <= System.currentTimeMillis()) {
                    activeTrusts.remove(holder.getUniqueId());
                    trustExpiry.remove(holder.getUniqueId());
                    
                    if (holder.isOnline()) {
                        holder.sendMessage("§c§l⏰ TRUST EXPIRED ⏰");
                        holder.sendMessage("§7Safe trade window has closed.");
                        holder.playSound(holder.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
                    }
                }
            }
        }.runTaskLater(plugin, 20 * 300); // 5 mins in ticks
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            UUID killerUuid = killer.getUniqueId();
            UUID victimUuid = victim.getUniqueId();

            // Check if Killer is trusted by Victim (Victim holds book)
            if (activeTrusts.containsKey(victimUuid) && activeTrusts.get(victimUuid).equals(killerUuid)) {
                if (trustExpiry.getOrDefault(victimUuid, 0L) > System.currentTimeMillis()) {
                    // Find and drop the Diablo book
                    ItemStack diabloBook = findDiabloBook(victim);
                    
                    if (diabloBook != null) {
                        // Remove from drops if it was there
                        event.getDrops().removeIf(item -> isDiabloBook(item));
                        
                        // Drop the book specifically at victim's location
                        victim.getWorld().dropItemNaturally(victim.getLocation().add(0, 0.5, 0), diabloBook);
                        
                        killer.sendMessage("§4§l⚔️ BETRAYAL SUCCESSFUL ⚔️");
                        killer.sendMessage("§cYou stole the Soul Book from " + victim.getName() + "!");
                        killer.playSound(killer.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
                        killer.spawnParticle(org.bukkit.Particle.SPELL_MOB, killer.getLocation().add(0, 1, 0), 50, 1, 1, 1, 0.5);
                        
                        victim.sendMessage("§4§l☠️ BETRAYED! ☠️");
                        victim.sendMessage("§c" + killer.getName() + " stole your Soul Book!");
                        victim.playSound(victim.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_DEATH, 0.8f, 1.2f);
                    }
                }
            }
        }
    }
    
    private ItemStack findDiabloBook(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isDiabloBook(item)) {
                return item.clone();
            }
        }
        return null;
    }
    
    private boolean isDiabloBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        if (meta.getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(plugin, "ability"), 
                PersistentDataType.STRING)) {
            return true;
        }
        
        return meta.hasDisplayName() && meta.getDisplayName().contains("Diablo");
    }
}
