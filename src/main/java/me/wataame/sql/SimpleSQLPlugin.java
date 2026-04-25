package me.wataame.sql;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class SimpleSQLPlugin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private Economy economy;
    private FileConfiguration langConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("lang.yml", false);
        loadLang();

        this.databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        setupEconomy();
        registerCommands();

        getLogger().info(colorize(getLang("startup.enabled", "&aSimpleSQL enabled. Version: {version}")
                .replace("{version}", getDescription().getVersion())));
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info(colorize(getLang("startup.disabled", "&eSimpleSQL disabled.")));
    }

    private void registerCommands() {
        PluginCommand sqlCommand = getCommand("sql");
        if (sqlCommand == null) {
            getLogger().severe(colorize(getLang("errors.command-not-defined", "&cCommand /sql is not defined in plugin.yml")));
            return;
        }

        SqlCommand executor = new SqlCommand(this);
        sqlCommand.setExecutor(executor);
        sqlCommand.setTabCompleter(new SqlTabCompleter());
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().info(colorize(getLang("vault.not-found", "&eVault not found. Economy hook disabled.")));
            return;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning(colorize(getLang("vault.no-provider", "&6Vault found but no economy provider is registered.")));
            return;
        }

        economy = rsp.getProvider();
        if (economy != null) {
            getLogger().info(colorize(getLang("vault.hooked", "&aVault economy provider hooked: {provider}")
                    .replace("{provider}", economy.getName())));
        }
    }

    private void loadLang() {
        File langFile = new File(getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            saveResource("lang.yml", false);
        }
        this.langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getLang(String path, String fallback) {
        return langConfig.getString(path, fallback);
    }

    public String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public Economy getEconomy() {
        return economy;
    }
}
