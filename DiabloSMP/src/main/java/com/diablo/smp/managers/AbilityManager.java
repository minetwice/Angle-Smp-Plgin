package com.diablo.smp.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {
    
    // Stores current stage (0, 1, 2) for each player
    private final Map<UUID, Integer> playerStages = new HashMap<>();
    // Stores last crouch time for double-crouch detection
    private final Map<UUID, Long> lastCrouchTime = new HashMap<>();
    
    public int getStage(Player player) {
        return playerStages.getOrDefault(player.getUniqueId(), 0);
    }

    public void setStage(Player player, int stage) {
        playerStages.put(player.getUniqueId(), Math.max(0, Math.min(2, stage)));
    }

    public void cycleStage(Player player) {
        int current = getStage(player);
        int next = (current + 1) % 3; // Cycles 0 -> 1 -> 2 -> 0
        playerStages.put(player.getUniqueId(), next);
        
        // Epic notification
        player.sendMessage("§5§l⚡ ABILITY SWITCHED ⚡");
        player.sendMessage("§7Now using: §eStage " + (next + 1) + "/3");
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_END_PORTAL_FRAME_FILL, 0.8f, 1.5f);
        player.spawnParticle(org.bukkit.Particle.END_ROD, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.05);
    }

    public boolean checkDoubleCrouch(Player player) {
        long now = System.currentTimeMillis();
        long last = lastCrouchTime.getOrDefault(player.getUniqueId(), 0L);
        
        if (now - last < 500) { // Within 500ms = Double Crouch
            lastCrouchTime.put(player.getUniqueId(), 0L); // Reset
            return true;
        }
        
        lastCrouchTime.put(player.getUniqueId(), now);
        return false;
    }

    public void cleanup() {
        playerStages.clear();
        lastCrouchTime.clear();
    }
}
