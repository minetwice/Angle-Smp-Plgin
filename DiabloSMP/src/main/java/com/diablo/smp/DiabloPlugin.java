package com.diablo.smp;

import com.diablo.smp.commands.DiabloCommand;
import com.diablo.smp.commands.TrustCommand;
import com.diablo.smp.listeners.AbilityListener;
import com.diablo.smp.listeners.BookProtectionListener;
import com.diablo.smp.listeners.GUIListener;
import com.diablo.smp.listeners.TrustListener;
import com.diablo.smp.managers.AbilityManager;
import com.diablo.smp.managers.CooldownManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DiabloPlugin extends JavaPlugin {

    private static DiabloPlugin instance;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private TrustListener trustListener;
    private BookProtectionListener bookListener;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("§5§l♦ §r§5Diablo SMP Plugin - HEAVY EDITION Loaded");
        getLogger().info("§7Initializing Angel SMP Boss Pack v2.0...");
        getLogger().info("§7Loading Mass Particle Systems...");
        getLogger().info("§7Setting up Advanced Command Framework...");

        // Managers
        this.abilityManager = new AbilityManager();
        this.cooldownManager = new CooldownManager();

        // Listeners
        this.bookListener = new BookProtectionListener(this);
        this.trustListener = new TrustListener(this);
        
        getServer().getPluginManager().registerEvents(new AbilityListener(this), this);
        getServer().getPluginManager().registerEvents(this.bookListener, this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(trustListener, this);

        // Commands
        DiabloCommand diabloCmd = new DiabloCommand(this);
        if (getCommand("diablo") != null) {
            getCommand("diablo").setExecutor(diabloCmd);
            getCommand("diablo").setTabCompleter(diabloCmd);
        }

        TrustCommand trustCmd = new TrustCommand(this);
        if (getCommand("trust") != null) {
            getCommand("trust").setExecutor(trustCmd);
            getCommand("trust").setTabCompleter(trustCmd);
        }

        getLogger().info("§aAll systems operational. Heavy Edition Ready for Angel SMP.");
        getLogger().info("§dTotal Features: 15 Abilities (1 Active), Mass Particles, Advanced Security");
    }

    @Override
    public void onDisable() {
        if (abilityManager != null) abilityManager.cleanup();
        getLogger().info("§cDiablo SMP Heavy Edition Shutdown Complete.");
    }

    public static DiabloPlugin getInstance() {
        return instance;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public TrustListener getTrustListener() {
        return trustListener;
    }
    
    public BookProtectionListener getBookListener() {
        return bookListener;
    }
}
