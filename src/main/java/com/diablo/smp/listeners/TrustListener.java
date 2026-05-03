package com.diablo.smp.listeners;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
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
        
        holder.sendMessage("§a§lTRUST ESTABLISHED §r§7with " + trusted.getName() + " for 5 minutes.");
        trusted.sendMessage("§a§lTRUST RECEIVED §r§7from " + holder.getName());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() > expiry) {
                    activeTrusts.remove(holder.getUniqueId());
                    trustExpiry.remove(holder.getUniqueId());
                    if(holder.isOnline()) holder.sendMessage("§c§lTRUST EXPIRED §r§7Safe trade window closed.");
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
                    // Find and drop the book specifically
                    ItemStack book = findDiabloBook(victim);
                    if (book != null) {
                        // Remove from drops list if it was there
                        event.getDrops().removeIf(i -> i.hasItemMeta() && i.getItemMeta().hasDisplayName() && i.getItemMeta().getDisplayName().contains("Diablo"));
                        victim.getWorld().dropItemNaturally(victim.getLocation(), book);
                        killer.sendMessage("§4§lBETRAYAL SUCCESSFUL §r§cYou stole the Soul Book!");
                    }
                }
            }
        }
    }
    
    private ItemStack findDiabloBook(Player p) {
        for(ItemStack i : p.getInventory().getContents()) {
            if(i != null && i.hasItemMeta() && i.getItemMeta().hasDisplayName() && i.getItemMeta().getDisplayName().contains("Diablo")) {
                return i.clone();
            }
        }
        return null;
    }
    
    public boolean isTrusted(Player holder, Player other) {
        if (!activeTrusts.containsKey(holder.getUniqueId())) return false;
        UUID trustedUuid = activeTrusts.get(holder.getUniqueId());
        Long expiry = trustExpiry.get(holder.getUniqueId());
        
        if (expiry != null && System.currentTimeMillis() > expiry) {
            activeTrusts.remove(holder.getUniqueId());
            trustExpiry.remove(holder.getUniqueId());
            return false;
        }
        
        return trustedUuid != null && trustedUuid.equals(other.getUniqueId());
    }
}
