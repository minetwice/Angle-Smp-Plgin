package com.diablo.smp.models;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SoulLink {
    private final Player controller;
    private final LivingEntity target;
    private final Location originalBodyLocation;

    public SoulLink(Player controller, LivingEntity target, Location originalBodyLocation) {
        this.controller = controller;
        this.target = target;
        this.originalBodyLocation = originalBodyLocation;
    }

    public Player getController() { return controller; }
    public LivingEntity getTarget() { return target; }
    public Location getOriginalBodyLocation() { return originalBodyLocation; }
}
