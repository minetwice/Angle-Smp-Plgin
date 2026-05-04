package com.diablo.smp.managers;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {
    private final Map<UUID, Integer> playerStages = new HashMap<>();
    private final Map<UUID, Long> lastCrouch = new HashMap<>();

    public int getStage(Player p) {
        return playerStages.getOrDefault(p.getUniqueId(), 0);
    }

    public void cycleStage(Player p) {
        int next = (getStage(p) + 1) % 3;
        playerStages.put(p.getUniqueId(), next);
        p.sendMessage("§5§l♦ ABILITY SWITCHED §r§7to Stage " + (next + 1));
        p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
        p.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, p.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
    }

    public boolean checkDoubleCrouch(Player p) {
        long now = System.currentTimeMillis();
        long last = lastCrouch.getOrDefault(p.getUniqueId(), 0L);
        
        if (now - last < 500) { // 500ms window
            lastCrouch.put(p.getUniqueId(), 0L);
            return true;
        }
        lastCrouch.put(p.getUniqueId(), now);
        return false;
    }

    public void cleanup() {
        playerStages.clear();
        lastCrouch.clear();
    }
}
