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
    private BookProtectionListener bookProtectionListener;
    private TrustListener trustListener;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("§5§l========================================");
        getLogger().info("§5§l      DIABLO SMP PLUGIN LOADED        ");
        getLogger().info("§5§l         Angel SMP Boss Pack          ");
        getLogger().info("§5§l========================================");
        getLogger().info("§7Initializing Epic Features...");

        // Initialize Managers
        this.abilityManager = new AbilityManager();
        this.cooldownManager = new CooldownManager();

        // Initialize Listeners
        this.bookProtectionListener = new BookProtectionListener(this);
        this.trustListener = new TrustListener(this);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new AbilityListener(this), this);
        getServer().getPluginManager().registerEvents(bookProtectionListener, this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(trustListener, this);

        // Register Commands with Tab Completion
        DiabloCommand diabloCommand = new DiabloCommand(this);
        getCommand("diablo").setExecutor(diabloCommand);
        getCommand("diablo").setTabCompleter(diabloCommand);
        
        TrustCommand trustCommand = new TrustCommand(this, trustListener);
        getCommand("trust").setExecutor(trustCommand);
        getCommand("trust").setTabCompleter(trustCommand);

        getLogger().info("§a✓ All systems operational.");
        getLogger().info("§a✓ 15 Ability Slots Ready (Stage 1 Active)");
        getLogger().info("§a✓ Mass Particle Effects Enabled");
        getLogger().info("§a✓ Advanced Tab Completion Active");
        getLogger().info("§eReady for Angel SMP!");
    }

    @Override
    public void onDisable() {
        getLogger().info("§cDiablo SMP shutting down...");
        if (abilityManager != null) abilityManager.cleanup();
        if (cooldownManager != null) cooldownManager.cleanup();
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

    public BookProtectionListener getBookProtectionListener() {
        return bookProtectionListener;
    }

    public TrustListener getTrustListener() {
        return trustListener;
    }
}
