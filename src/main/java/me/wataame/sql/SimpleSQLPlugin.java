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
        // config.yml の生成と読み込み
        saveDefaultConfig();
        // config.yml の設定に基づいて言語ファイルをフォルダに保存・読み込み
        saveDefaultLangFiles();
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

        PluginCommand sqlSendCommand = getCommand("sql-send");
        if (sqlSendCommand == null) {
            getLogger().severe(colorize(getLang("errors.command-not-defined-send", "&cCommand /sql-send is not defined in plugin.yml")));
            return;
        }
        sqlSendCommand.setExecutor(new SqlSendCommand(this));
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

    /**
     * Jar内の初期言語ファイルをプラグインフォルダに保存する処理
     */
    private void saveDefaultLangFiles() {
        File langDir = new File(getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }
        
        // config.yml から言語設定を取得（未指定なら "ja"）
        String lang = getConfig().getString("lang", "ja");
        // パスを lang/{lang}.lang.yml に指定
        String langResourcePath = "lang/" + lang + ".lang.yml";
        
        File langFile = new File(getDataFolder(), langResourcePath);
        if (!langFile.exists()) {
            // Jar内に該当する言語ファイルがある場合のみコピー
            if (getResource(langResourcePath) != null) {
                saveResource(langResourcePath, false);
            }
        }
    }

    /**
     * config.yml の lang 設定に基づき、lang/{lang}.lang.yml を読み込む
     */
    private void loadLang() {
        String lang = getConfig().getString("lang", "ja");
        
        // パスを lang/{lang}.lang.yml に指定
        File langFile = new File(getDataFolder(), "lang/" + lang + ".lang.yml");
        
        if (!langFile.exists()) {
            getLogger().warning("Language file " + langFile.getName() + " not found. Falling back to default values.");
        }
        
        this.langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getLang(String path, String fallback) {
        if (langConfig == null) {
            return fallback;
        }
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
