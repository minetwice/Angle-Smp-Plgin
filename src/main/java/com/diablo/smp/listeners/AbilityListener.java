package com.diablo.smp.listeners;

import com.diablo.smp.DiabloPlugin;
import com.diablo.smp.models.SoulLink;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.*;

public class AbilityListener implements Listener {

    private final DiabloPlugin plugin;
    private final Map<UUID, SoulLink> activeLinks = new HashMap<>();
    private final Set<UUID> statueMode = new HashSet<>();

    public AbilityListener(DiabloPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            if (plugin.getAbilityManager().checkDoubleCrouch(event.getPlayer())) {
                plugin.getAbilityManager().cycleStage(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        // Check if attacker has the book in hand
        ItemStack item = attacker.getInventory().getItemInMainHand();
        if (!isDiabloBook(item)) return;
        
        // Check Stage (Stage 0 = First Ability)
        int stage = plugin.getAbilityManager().getStage(attacker);
        if (stage != 0) return; 

        event.setCancelled(true); // No damage, just ability trigger
        
        // Start Soul Exchange Logic
        initiateSoulExchange(attacker, victim);
    }

    private boolean isDiabloBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return item.getItemMeta().getDisplayName().contains("Diablo");
    }

    private void initiateSoulExchange(Player controller, LivingEntity target) {
        if (activeLinks.containsKey(controller.getUniqueId())) {
            controller.sendMessage("§cAbility already active!");
            return;
        }

        // 1. Make Controller's Body a Statue
        statueMode.add(controller.getUniqueId());
        controller.setInvulnerable(true);
        controller.setGravity(false);
        // Freeze effect using potion instead of setFrozenTicks (not available in 1.20.4)
        controller.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.getByName("SLOW"), 600, 255, true, false));
        controller.sendMessage("§5§lSOUL EXCHANGE STARTED §r§7Your body is now a statue.");

        Location originalLoc = controller.getLocation().clone();
        
        // 2. Create Link
        SoulLink link = new SoulLink(controller, target, originalLoc);
        activeLinks.put(controller.getUniqueId(), link);

        // 3. Particle Line & Timer
        new BukkitRunnable() {
            int timeLeft = 30; // 30 seconds
            @Override
            public void run() {
                if (!controller.isOnline() || timeLeft <= 0 || target.isDead()) {
                    endExchange(controller.getUniqueId());
                    cancel();
                    return;
                }

                // Draw Line
                drawParticleLine(controller.getLocation().add(0, 1, 0), target.getLocation().add(0, 1, 0));
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void drawParticleLine(Location start, Location end) {
        World world = start.getWorld();
        if (world == null) return;
        
        int particles = 20;
        for (int i = 0; i <= particles; i++) {
            double t = (double) i / particles;
            double x = start.getX() + (end.getX() - start.getX()) * t;
            double y = start.getY() + (end.getY() - start.getY()) * t;
            double z = start.getZ() + (end.getZ() - start.getZ()) * t;
            world.spawnParticle(Particle.END_ROD, x, y, z, 1, 0, 0, 0, 0);
        }
    }

    private void endExchange(UUID controllerUuid) {
        SoulLink link = activeLinks.remove(controllerUuid);
        if (link == null) return;

        Player controller = link.getController();
        if (controller != null && controller.isOnline()) {
            statueMode.remove(controllerUuid);
            controller.setInvulnerable(false);
            controller.setGravity(true);
            controller.removePotionEffect(org.bukkit.potion.PotionEffectType.getByName("SLOW"));
            controller.sendMessage("§5§lSOUL RETURNED §r§7Back in your body.");
        }
        
        if (link.getTarget() != null && !link.getTarget().isDead()) {
             link.getTarget().sendMessage("§7Soul link broken.");
        }
    }
    
    @EventHandler
    public void onStatueDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player p) {
            if (statueMode.contains(p.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
