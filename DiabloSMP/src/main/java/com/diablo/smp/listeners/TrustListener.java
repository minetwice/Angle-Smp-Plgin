package com.diablo.smp.listeners;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TrustListener implements Listener {

    private final DiabloPlugin plugin;
    // Holder UUID -> Trusted Player UUID
    private final Map<UUID, UUID> activeTrusts = new HashMap<>();
    // Holder UUID -> Expiry Time (Millis)
    private final Map<UUID, Long> trustExpiry = new HashMap<>();

    public TrustListener(DiabloPlugin plugin) {
        this.plugin = plugin;
    }

    public void initiateTrust(Player holder, Player trusted) {
        UUID hUuid = holder.getUniqueId();
        UUID tUuid = trusted.getUniqueId();

        activeTrusts.put(hUuid, tUuid);
        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 Minutes
        trustExpiry.put(hUuid, expiryTime);

        holder.sendMessage("");
        holder.sendMessage("§5§l♦ TRUST ESTABLISHED");
        holder.sendMessage("§7Duration: §e5 minutes");
        holder.sendMessage("§7Trusted Player: §a" + trusted.getName());
        holder.sendMessage("§cWarning: §7If they kill you, your book drops!");
        holder.sendMessage("");
        
        trusted.sendMessage("");
        trusted.sendMessage("§5§l♦ TRUST RECEIVED");
        trusted.sendMessage("§7From: §a" + holder.getName());
        trusted.sendMessage("§7You can now safely trade the Soul Book");
        trusted.sendMessage("§cWarning: §7Killing them will drop their book!");
        trusted.sendMessage("");

        // Countdown Reminder
        new BukkitRunnable() {
            int seconds = 300;
            @Override
            public void run() {
                if (!holder.isOnline() || System.currentTimeMillis() >= expiryTime) {
                    activeTrusts.remove(hUuid);
                    trustExpiry.remove(hUuid);
                    if(holder.isOnline()) {
                        holder.sendMessage("");
                        holder.sendMessage("§c§l♦ TRUST EXPIRED");
                        holder.sendMessage("§7Safe trade window closed.");
                        holder.sendMessage("");
                    }
                    cancel();
                    return;
                }
                if (seconds % 60 == 0 && seconds > 0) {
                    holder.sendMessage("§e§l♦ Trust expires in " + (seconds/60) + " minute(s).");
                }
                seconds--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            UUID vUuid = victim.getUniqueId();
            UUID kUuid = killer.getUniqueId();

            // Check if Killer is the Trusted Player
            if (activeTrusts.containsKey(vUuid) && activeTrusts.get(vUuid).equals(kUuid)) {
                long expiry = trustExpiry.getOrDefault(vUuid, 0L);
                
                if (System.currentTimeMillis() < expiry) {
                    // Betrayal Success - Drop Book
                    ItemStack book = findSoulBook(victim);
                    if (book != null) {
                        // Remove from drops list if auto-added
                        e.getDrops().removeIf(i -> BookProtectionListener.isSoulBookStatic(i));
                        victim.getWorld().dropItemNaturally(victim.getLocation(), book);
                        
                        killer.sendMessage("");
                        killer.sendMessage("§4§l♦ BETRAYAL SUCCESSFUL!");
                        killer.sendMessage("§cYou stole the Soul Book!");
                        killer.sendMessage("");
                        killer.playSound(killer.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
                        
                        victim.sendMessage("");
                        victim.sendMessage("§4§l♦ BETRAYED!");
                        victim.sendMessage("§cYour power has been stolen.");
                        victim.sendMessage("");
                        
                        // Clean up trust
                        activeTrusts.remove(vUuid);
                        trustExpiry.remove(vUuid);
                    }
                }
            }
        }
    }

    private ItemStack findSoulBook(Player p) {
        for (ItemStack i : p.getInventory().getContents()) {
            if (BookProtectionListener.isSoulBookStatic(i)) return i;
        }
        return null;
    }
}
