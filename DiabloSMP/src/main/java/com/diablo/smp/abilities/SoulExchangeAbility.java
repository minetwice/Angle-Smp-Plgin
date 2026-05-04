package com.diablo.smp.abilities;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoulExchangeAbility {

    private final DiabloPlugin plugin;
    private final Map<UUID, BukkitRunnable> activeExchanges = new HashMap<>();

    public SoulExchangeAbility(DiabloPlugin plugin) {
        this.plugin = plugin;
    }

    public void activate(Player controller, LivingEntity target) {
        if (activeExchanges.containsKey(controller.getUniqueId())) {
            controller.sendMessage("§c§lERROR: §rAbility already active!");
            return;
        }

        controller.sendMessage("");
        controller.sendMessage("§5§l♦ SOUL EXCHANGE INITIATED");
        controller.sendMessage("§7Duration: §e30 seconds");
        controller.sendMessage("§7Your body is now a statue.");
        controller.sendMessage("");
        
        target.sendMessage("");
        target.sendMessage("§4§l♦ YOUR SOUL IS BEING TAKEN!");
        target.sendMessage("§cYou are under control for 30 seconds.");
        target.sendMessage("");
        target.playSound(target.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);

        // Statue Effect on Controller
        controller.setInvulnerable(true);
        controller.setGravity(false);
        controller.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.getByName("SLOW"), 30*20, 255, false, false));
        
        Location originalLoc = controller.getLocation().clone();

        // Particle Line Task
        BukkitRunnable task = new BukkitRunnable() {
            int timeLeft = 30;
            @Override
            public void run() {
                if (!controller.isOnline() || timeLeft <= 0) {
                    endExchange(controller);
                    cancel();
                    return;
                }

                drawLine(controller.getLocation().add(0, 1, 0), target.getLocation().add(0, 1, 0));
                
                // Optional: Lock target inventory logic here
                
                timeLeft--;
                
                // Update action bar
                if (controller.isOnline()) {
                    controller.spigot().sendMessage(
                        net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                        net.md_5.bungee.api.chat.TextComponent.fromLegacyText(
                            "§5§lSoul Exchange §r§7- §e" + timeLeft + "s remaining")
                    );
                }
            }
        };
        
        task.runTaskTimer(plugin, 0, 20);
        activeExchanges.put(controller.getUniqueId(), task);
    }

    private void drawLine(Location start, Location end) {
        World w = start.getWorld();
        int steps = 30;
        for(int i=0; i<=steps; i++) {
            double t = (double)i/steps;
            double x = start.getX() + (end.getX()-start.getX())*t;
            double y = start.getY() + (end.getY()-start.getY())*t;
            double z = start.getZ() + (end.getZ()-start.getZ())*t;
            
            // Heavy particle density
            w.spawnParticle(Particle.END_ROD, x, y, z, 2, 0, 0, 0, 0);
            w.spawnParticle(Particle.PORTAL, x, y, z, 2, 0, 0, 0, 0.1);
            w.spawnParticle(Particle.SPELL_MOB, x, y, z, 1, 0, 0, 0, 0, 
                Color.fromRGB(139, 0, 139));
        }
    }

    private void endExchange(Player controller) {
        activeExchanges.remove(controller.getUniqueId());
        controller.setInvulnerable(false);
        controller.setGravity(true);
        controller.removePotionEffect(org.bukkit.potion.PotionEffectType.getByName("SLOW"));
        
        Location loc = controller.getLocation();
        World world = controller.getWorld();
        
        // Return explosion particles
        world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.END_ROD, loc.add(0, 1, 0), 50, 1, 1, 1, 0.1);
        
        controller.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 2.0f);
        controller.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.5f);
        
        controller.sendMessage("");
        controller.sendMessage("§5§l♦ SOUL RETURNED");
        controller.sendMessage("§7You are back in your body.");
        controller.sendMessage("");
    }
}
