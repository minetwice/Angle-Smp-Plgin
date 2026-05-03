package com.diablo.smp.abilities;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

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
            controller.sendMessage("§cAbility already active!");
            return;
        }

        controller.sendMessage("§5§lSOUL EXCHANGE INITIATED");
        target.sendMessage("§4§lYOUR SOUL IS BEING TAKEN!");

        controller.setInvulnerable(true);
        controller.setGravity(false);
        controller.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 30*20, -1));
        controller.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30*20, 255));
        
        Location originalLoc = controller.getLocation().clone();

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
                
                timeLeft--;
            }
        };
        
        task.runTaskTimer(plugin, 0, 20);
        activeExchanges.put(controller.getUniqueId(), task);
    }

    private void drawLine(Location start, Location end) {
        World w = start.getWorld();
        int steps = 20;
        for(int i=0; i<=steps; i++) {
            double t = (double)i/steps;
            double x = start.getX() + (end.getX()-start.getX())*t;
            double y = start.getY() + (end.getY()-start.getY())*t;
            double z = start.getZ() + (end.getZ()-start.getZ())*t;
            w.spawnParticle(Particle.END_ROD, x, y, z, 1, 0,0,0,0);
            w.spawnParticle(Particle.PORTAL, x, y, z, 1, 0,0,0,0);
        }
    }

    private void endExchange(Player controller) {
        activeExchanges.remove(controller.getUniqueId());
        controller.setInvulnerable(false);
        controller.setGravity(true);
        controller.removePotionEffect(PotionEffectType.JUMP);
        controller.removePotionEffect(PotionEffectType.SLOW);
        controller.getWorld().playSound(controller.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 2.0f);
        controller.sendMessage("§5§lSOUL RETURNED");
    }
}
