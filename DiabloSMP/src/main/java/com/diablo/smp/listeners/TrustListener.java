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
    private final Map<UUID, UUID> activeTrusts = new HashMap<>();
    private final Map<UUID, Long> trustExpiry = new HashMap<>();

    public TrustListener(DiabloPlugin plugin) {
        this.plugin = plugin;
    }

    public void initiateTrust(Player holder, Player trusted) {
        UUID hUuid = holder.getUniqueId();
        UUID tUuid = trusted.getUniqueId();

        activeTrusts.put(hUuid, tUuid);
        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);
        trustExpiry.put(hUuid, expiryTime);

        holder.sendMessage("§a§lTRUST ESTABLISHED §r§7with " + trusted.getName() + " for 5 mins.");
        trusted.sendMessage("§a§lTRUST RECEIVED §r§7from " + holder.getName());

        new BukkitRunnable() {
            int seconds = 300;
            @Override
            public void run() {
                if (!holder.isOnline() || System.currentTimeMillis() >= expiryTime) {
                    activeTrusts.remove(hUuid);
                    trustExpiry.remove(hUuid);
                    if(holder.isOnline()) holder.sendMessage("§c§lTRUST EXPIRED");
                    cancel();
                    return;
                }
                if (seconds % 60 == 0 && seconds > 0) {
                    holder.sendMessage("§eTrust expires in " + (seconds/60) + " minutes.");
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

            if (activeTrusts.containsKey(vUuid) && activeTrusts.get(vUuid).equals(kUuid)) {
                long expiry = trustExpiry.getOrDefault(vUuid, 0L);
                
                if (System.currentTimeMillis() < expiry) {
                    ItemStack book = findSoulBook(victim);
                    if (book != null) {
                        e.getDrops().removeIf(i -> BookProtectionListener.isSoulBookStatic(i));
                        victim.getWorld().dropItemNaturally(victim.getLocation(), book);
                        
                        killer.sendMessage("§4§lBETRAYAL SUCCESSFUL! §r§cYou stole the Soul Book!");
                        victim.sendMessage("§4§lBETRAYED! §r§cYour power has been stolen.");
                        
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
