package com.diablo.smp.listeners;

import com.diablo.smp.DiabloPlugin;
import com.diablo.smp.abilities.SoulExchangeAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public class AbilityListener implements Listener {

    private final DiabloPlugin plugin;
    private final SoulExchangeAbility soulExchange;

    public AbilityListener(DiabloPlugin plugin) {
        this.plugin = plugin;
        this.soulExchange = new SoulExchangeAbility(plugin);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (e.isSneaking()) {
            if (plugin.getAbilityManager().checkDoubleCrouch(e.getPlayer())) {
                plugin.getAbilityManager().cycleStage(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        if (!(e.getEntity() instanceof org.bukkit.entity.LivingEntity target)) return;
        
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (!BookProtectionListener.isSoulBookStatic(hand)) return;

        int stage = plugin.getAbilityManager().getStage(p);
        
        // Stage 1: Soul Exchange
        if (stage == 0) {
            e.setCancelled(true);
            soulExchange.activate(p, target);
        } 
        // Future stages can be added here
        else if (stage == 1) {
            e.setCancelled(true);
            p.sendMessage("§cStage 2 ability coming soon!");
        }
        else if (stage == 2) {
            e.setCancelled(true);
            p.sendMessage("§cStage 3 ability coming soon!");
        }
    }
}
