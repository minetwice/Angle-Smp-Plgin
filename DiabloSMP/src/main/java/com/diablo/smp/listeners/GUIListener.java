package com.diablo.smp.listeners;

import com.diablo.smp.DiabloPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;

public class GUIListener implements Listener {

    private final DiabloPlugin plugin;

    public GUIListener(DiabloPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        if (!title.contains("Soul Absorption")) return;
        
        event.setCancelled(true); // Prevent moving items
        
        // Check if clicked slot is the center slot (4)
        if (event.getSlot() == 4) {
            ItemStack clickedItem = event.getCurrentItem();
            
            if (clickedItem != null && isDiabloBook(clickedItem)) {
                // Remove book from cursor and inventory
                player.getInventory().remove(clickedItem);
                if (event.getCursor() != null && isDiabloBook(event.getCursor())) {
                    player.setItemOnCursor(null);
                }
                
                // Perform absorption
                plugin.getBookProtectionListener().performAbsorption(player, clickedItem);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        if (title.contains("Soul Absorption")) {
            // Check if book is still in GUI (player didn't absorb)
            if (event.getInventory().getItem(4) != null) {
                ItemStack book = event.getInventory().getItem(4);
                if (isDiabloBook(book)) {
                    // Return book to player
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(book);
                    if (!leftover.isEmpty()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), book);
                    }
                    player.sendMessage("§7Book returned to inventory.");
                }
            }
        }
    }

    private boolean isDiabloBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        if (meta.getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(plugin, "ability"), 
                PersistentDataType.STRING)) {
            return true;
        }
        
        return meta.hasDisplayName() && meta.getDisplayName().contains("Diablo");
    }
}
