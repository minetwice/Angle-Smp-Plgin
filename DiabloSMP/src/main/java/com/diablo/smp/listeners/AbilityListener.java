package com.diablo.smp.listeners;

import com.diablo.smp.DiabloPlugin;
import com.diablo.smp.abilities.SoulExchangeAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
        
        if (stage == 0) {
            e.setCancelled(true);
            soulExchange.activate(p, target);
        }
    }
}
