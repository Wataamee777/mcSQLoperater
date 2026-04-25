package me.wataame.sql;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class SqlCommand implements CommandExecutor {

    private final SimpleSQLPlugin plugin;

    public SqlCommand(SimpleSQLPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(plugin.colorize(plugin.getLang("command.no-permission", "&cYou must be OP to use this command.")));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(plugin.colorize(plugin.getLang("help.title", "&eSimpleSQL Help")));
            sender.sendMessage(plugin.colorize(plugin.getLang("help.version", "&7Version: &f{version}")
                    .replace("{version}", plugin.getDescription().getVersion())));
            sender.sendMessage(plugin.colorize(plugin.getLang("help.usage", "&7Usage: &f/sql <query>")));
            sender.sendMessage(plugin.colorize(plugin.getLang("help.example", "&7Example: &f/sql SHOW TABLES;")));
            return true;
        }

        String query = String.join(" ", args).trim();
        if (query.isEmpty()) {
            sender.sendMessage(plugin.colorize(plugin.getLang("command.empty-query", "&cQuery cannot be empty.")));
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                DatabaseManager.QueryResult result = plugin.getDatabaseManager().executeRaw(query);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (result.hasResultSet()) {
                        sender.sendMessage(plugin.colorize(plugin.getLang("result.success-rows", "&aSQL executed successfully. Rows: {count}")
                                .replace("{count}", String.valueOf(result.count()))));
                        if (result.rows().isEmpty()) {
                            sender.sendMessage(plugin.colorize(plugin.getLang("result.empty", "&7(empty result set)")));
                        } else {
                            for (String row : result.rows()) {
                                sender.sendMessage(plugin.colorize(plugin.getLang("result.row-format", "&f{row}").replace("{row}", row)));
                            }
                        }
                    } else {
                        sender.sendMessage(plugin.colorize(plugin.getLang("result.success-affected", "&aSQL executed successfully. Affected rows: {count}")
                                .replace("{count}", String.valueOf(result.count()))));
                    }
                });
            } catch (SQLException e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (plugin.getDatabaseManager().isTimeout(e)) {
                        sender.sendMessage(plugin.colorize(plugin.getLang("errors.timeout", "&cDatabase connection timed out (3 seconds).")));
                    }
                    sender.sendMessage(plugin.colorize(plugin.getLang("errors.sql", "&cSQL error: {message}")
                            .replace("{message}", String.valueOf(e.getMessage()))));
                    sender.sendMessage(plugin.colorize(plugin.getLang("errors.sql-detail", "&cSQLState={state} ErrorCode={code}")
                            .replace("{state}", String.valueOf(e.getSQLState()))
                            .replace("{code}", String.valueOf(e.getErrorCode()))));
                });
            }
        });

        sender.sendMessage(plugin.colorize(plugin.getLang("command.running", "&eExecuting query asynchronously...")));
        return true;
    }
}
