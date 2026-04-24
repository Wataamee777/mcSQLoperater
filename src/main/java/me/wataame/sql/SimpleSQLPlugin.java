package me.wataame.sql;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleSQLPlugin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private Economy economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        setupEconomy();
        registerCommands();

        getLogger().info("SimpleSQL enabled. Version: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("SimpleSQL disabled.");
    }

    private void registerCommands() {
        PluginCommand sqlCommand = getCommand("sql");
        if (sqlCommand == null) {
            getLogger().severe("Command /sql is not defined in plugin.yml");
            return;
        }

        SqlCommand executor = new SqlCommand(this);
        sqlCommand.setExecutor(executor);
        sqlCommand.setTabCompleter(new SqlTabCompleter());
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().info("Vault not found. Economy hook disabled.");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("Vault found but no economy provider is registered.");
            return;
        }

        economy = rsp.getProvider();
        if (economy != null) {
            getLogger().info("Vault economy provider hooked: " + economy.getName());
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public Economy getEconomy() {
        return economy;
    }
}
