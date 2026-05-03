package com.diablo.smp.listeners;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class GUIListener implements Listener {

    private final DiabloPlugin plugin;

    public GUIListener(DiabloPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("§5§lSoul Absorption")) return;
        e.setCancelled(true);

        if (e.getSlot() == 4) {
            ItemStack cursor = e.getCursor();
            ItemStack current = e.getCurrentItem();

            if (BookProtectionListener.isSoulBookStatic(cursor) || BookProtectionListener.isSoulBookStatic(current)) {
                ItemStack book = BookProtectionListener.isSoulBookStatic(cursor) ? cursor : current;
                if (book != null) {
                    performAbsorption((Player) e.getWhoClicked());
                    e.getWhoClicked().closeInventory();
                    if (BookProtectionListener.isSoulBookStatic(cursor)) e.setCursor(null);
                    else e.setCurrentItem(null);
                }
            }
        }
    }

    private void performAbsorption(Player p) {
        p.sendMessage("§5§lABSORPTION STARTED...");
        Location loc = p.getLocation();

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!p.isOnline() || ticks > 100) {
                    createCrown(p);
                    cancel();
                    return;
                }

                double angle = ticks * 0.2;
                double radius = 1.5;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                double y = (ticks % 20) * 0.1;

                p.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc.clone().add(x, y, z), 2, 0.1, 0.1, 0.1, 0.05);
                p.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, y, z), 1, 0, 0, 0, 0);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private void createCrown(Player p) {
        Location head = p.getLocation().add(0, 1.8, 0);
        p.getWorld().spawnParticle(Particle.END_ROD, head, 36, 1.2, 0, 1.2, 0, 0.1);
        p.getWorld().playSound(head, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.5f);
        p.sendMessage("§5§lPOWER ABSORBED! §r§dAngel Crown Activated.");
    }
}
