package com.diablo.smp.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private final Map<String, Long> cooldowns = new HashMap<>();

    public boolean isOnCooldown(UUID uuid, String ability) {
        String key = uuid.toString() + ":" + ability;
        if (!cooldowns.containsKey(key)) return false;
        
        long expiry = cooldowns.get(key);
        if (System.currentTimeMillis() > expiry) {
            cooldowns.remove(key);
            return false;
        }
        return true;
    }

    public void setCooldown(UUID uuid, String ability, long durationMillis) {
        String key = uuid.toString() + ":" + ability;
        cooldowns.put(key, System.currentTimeMillis() + durationMillis);
    }
}
