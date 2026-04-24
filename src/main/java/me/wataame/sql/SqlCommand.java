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
            sender.sendMessage("§cYou must be OP to use this command.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§eSimpleSQL Help");
            sender.sendMessage("§7Version: §f" + plugin.getDescription().getVersion());
            sender.sendMessage("§7Usage: §f/sql <query>");
            sender.sendMessage("§7Example: §f/sql SHOW TABLES;");
            return true;
        }

        String query = String.join(" ", args).trim();
        if (query.isEmpty()) {
            sender.sendMessage("§cQuery cannot be empty.");
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                DatabaseManager.QueryResult result = plugin.getDatabaseManager().executeRaw(query);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (result.hasResultSet()) {
                        sender.sendMessage("§aSQL executed successfully. Rows: " + result.count());
                        if (result.rows().isEmpty()) {
                            sender.sendMessage("§7(empty result set)");
                        } else {
                            for (String row : result.rows()) {
                                sender.sendMessage("§f" + row);
                            }
                        }
                    } else {
                        sender.sendMessage("§aSQL executed successfully. Affected rows: " + result.count());
                    }
                });
            } catch (SQLException e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage("§cSQL error: " + e.getMessage());
                    sender.sendMessage("§cSQLState=" + e.getSQLState() + " ErrorCode=" + e.getErrorCode());
                });
            }
        });

        sender.sendMessage("§eExecuting query asynchronously...");
        return true;
    }
}
