package com.diablo.smp.listeners;

import com.diablo.smp.DiabloPlugin;
import com.diablo.smp.models.SoulLink;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

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

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        // Check if attacker has Diablo book in hand
        ItemStack item = attacker.getInventory().getItemInMainHand();
        if (!isDiabloBook(item)) {
            // Check offhand too
            item = attacker.getInventory().getItemInOffHand();
            if (!isDiabloBook(item)) return;
        }
        
        // Check Stage (Stage 0 = Soul Exchange ability)
        int stage = plugin.getAbilityManager().getStage(attacker);
        if (stage != 0) return; // Only activate on Stage 1 (index 0)

        event.setCancelled(true); // No damage, just ability trigger
        
        // Start Soul Exchange Logic
        initiateSoulExchange(attacker, victim);
    }

    private void initiateSoulExchange(Player controller, LivingEntity target) {
        if (activeLinks.containsKey(controller.getUniqueId())) {
            controller.sendMessage("§c§lERROR §r§7Ability already active! Wait for current link to end.");
            return;
        }

        // 1. Make Controller's Body a Statue
        statueMode.add(controller.getUniqueId());
        controller.setInvulnerable(true);
        controller.setGravity(false);
        controller.setFreezeTicks(Integer.MAX_VALUE);
        controller.setGlowing(true);
        controller.setSilent(true);
        
        // Send message
        controller.sendMessage("§5§l⚡ SOUL EXCHANGE INITIATED ⚡");
        controller.sendMessage("§7Your body is now an §einvulnerable statue§7.");
        controller.sendMessage("§7Duration: §e30 seconds");
        controller.playSound(controller.getLocation(), org.bukkit.Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 0.8f);

        // 2. Create Link
        Location originalLoc = controller.getLocation().clone();
        SoulLink link = new SoulLink(controller, target, originalLoc);
        activeLinks.put(controller.getUniqueId(), link);

        // 3. Target effect
        if (target instanceof Player targetPlayer) {
            targetPlayer.sendMessage("§4§l⚠ SOUL LINKED ⚠");
            targetPlayer.sendMessage("§7Your soul is temporarily linked!");
            targetPlayer.playSound(target.getLocation(), Sound.ENTITY_GHAST_WARN, 0.8f, 1.2f);
        }

        // 4. Particle Line + Timer
        new BukkitRunnable() {
            int timeLeft = 30; // 30 seconds
            
            @Override
            public void run() {
                if (!controller.isOnline() || timeLeft <= 0) {
                    endExchange(controller.getUniqueId());
                    cancel();
                    return;
                }

                // Draw MASSIVE particle line between bodies
                if (controller.isOnline() && !target.isDead()) {
                    drawParticleLine(controller.getLocation().add(0, 1, 0), 
                                   target.getLocation().add(0, 1, 0));
                    
                    // Show timer every 5 seconds
                    if (timeLeft % 5 == 0 && timeLeft > 0) {
                        controller.sendActionBar("§5§lSOUL LINK §r§7| §eTime: " + timeLeft + "s");
                    }
                } else {
                    // If either died or disconnected, end early
                    endExchange(controller.getUniqueId());
                    cancel();
                    return;
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void drawParticleLine(Location start, Location end) {
        World world = start.getWorld();
        if (world == null) return;
        
        int particles = 40; // High density
        for (int i = 0; i <= particles; i++) {
            double t = (double) i / particles;
            double x = start.getX() + (end.getX() - start.getX()) * t;
            double y = start.getY() + (end.getY() - start.getY()) * t;
            double z = start.getZ() + (end.getZ() - start.getZ()) * t;
            
            // Multiple particle types for epic effect
            world.spawnParticle(Particle.END_ROD, x, y, z, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.PORTAL, x, y, z, 1, 0, 0, 0, 0);
            
            if (i % 5 == 0) {
                world.spawnParticle(Particle.SPELL_MOB, x, y, z, 1, 0, 0, 0, 0.8);
            }
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
            controller.setFreezeTicks(0);
            controller.setGlowing(false);
            controller.setSilent(false);
            
            controller.sendMessage("§5§l✓ SOUL RETURNED ✓");
            controller.sendMessage("§7Back in your body.");
            controller.playSound(controller.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            controller.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, controller.getLocation().add(0, 1, 0), 50, 1, 1, 1, 0.5);
        }
        
        // Release target
        if (link.getTarget() != null && !link.getTarget().isDead()) {
            if (link.getTarget() instanceof Player targetPlayer) {
                targetPlayer.sendMessage("§a§l✓ SOUL LINK BROKEN ✓");
                targetPlayer.playSound(targetPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.5f);
            }
        }
    }
    
    // Protect Statue from all damage
    @EventHandler
    public void onStatueDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (statueMode.contains(player.getUniqueId())) {
                event.setCancelled(true);
                // Show shield particles
                player.getWorld().spawnParticle(Particle.SPELL_WITCH, 
                    player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }
    
    // Prevent statue from moving
    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        if (statueMode.contains(event.getPlayer().getUniqueId())) {
            // Cancel movement but allow looking around
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ() || from.getY() != to.getY())) {
                event.setTo(from);
            }
        }
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
