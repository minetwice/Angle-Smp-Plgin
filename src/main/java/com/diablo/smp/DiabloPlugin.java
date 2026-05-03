package com.diablo.smp;

import com.diablo.smp.commands.TrustCommand;
import com.diablo.smp.listeners.AbilityListener;
import com.diablo.smp.listeners.BookProtectionListener;
import com.diablo.smp.listeners.TrustListener;
import com.diablo.smp.managers.AbilityManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DiabloPlugin extends JavaPlugin {

    private static DiabloPlugin instance;
    private AbilityManager abilityManager;
    private TrustListener trustListener;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("§5§lDiablo SMP §r§5Plugin Loaded Successfully!");
        getLogger().info("§7Initializing Boss Pack Features...");

        // Initialize Managers
        this.abilityManager = new AbilityManager();

        // Initialize Listeners (store reference for commands)
        this.trustListener = new TrustListener(this);
        
        // Register Listeners
        getServer().getPluginManager().registerEvents(new AbilityListener(this), this);
        getServer().getPluginManager().registerEvents(new BookProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(trustListener, this);

        // Register Commands
        getCommand("trust").setExecutor(new TrustCommand(this, trustListener));
        // Admin command logic can be added similarly
        
        getLogger().info("§aAll systems operational. Ready for Angel SMP.");
    }

    @Override
    public void onDisable() {
        getLogger().info("§cDiablo SMP shutting down...");
        if (abilityManager != null) abilityManager.cleanup();
    }

    public static DiabloPlugin getInstance() {
        return instance;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }
    
    public TrustListener getTrustListener() {
        return trustListener;
    }
}
