package com.diablo.smp.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    
    private final Map<String, Long> cooldowns = new HashMap<>();
    
    public void setCooldown(UUID playerUuid, String abilityId, long durationMillis) {
        String key = playerUuid.toString() + ":" + abilityId;
        cooldowns.put(key, System.currentTimeMillis() + durationMillis);
    }
    
    public boolean isOnCooldown(UUID playerUuid, String abilityId) {
        String key = playerUuid.toString() + ":" + abilityId;
        Long expiry = cooldowns.get(key);
        return expiry != null && System.currentTimeMillis() < expiry;
    }
    
    public long getRemainingCooldown(UUID playerUuid, String abilityId) {
        String key = playerUuid.toString() + ":" + abilityId;
        Long expiry = cooldowns.get(key);
        if (expiry == null) return 0;
        
        long remaining = expiry - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    public void removeCooldown(UUID playerUuid, String abilityId) {
        String key = playerUuid.toString() + ":" + abilityId;
        cooldowns.remove(key);
    }
    
    public void cleanup() {
        cooldowns.clear();
    }
}
