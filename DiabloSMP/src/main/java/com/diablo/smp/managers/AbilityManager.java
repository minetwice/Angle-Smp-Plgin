package com.diablo.smp.managers;

import org.bukkit.entity.Player;
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

    public void cycleStage(Player player) {
        int current = getStage(player);
        int next = (current + 1) % 3; // Cycles 0 -> 1 -> 2 -> 0
        playerStages.put(player.getUniqueId(), next);
        player.sendMessage("§5§lABILITY SWITCHED §r§7to Stage: §e" + (next + 1));
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
    }

    public boolean checkDoubleCrouch(Player player) {
        long now = System.currentTimeMillis();
        long last = lastCrouchTime.getOrDefault(player.getUniqueId(), 0L);
        
        if (now - last < 600) { // Within 600ms = Double Crouch
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
