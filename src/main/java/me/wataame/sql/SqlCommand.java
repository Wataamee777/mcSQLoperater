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
            sender.sendMessage("§7Example: §f/sql SELECT * FROM users;");
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
                        sender.sendMessage("§aSQL executed successfully. Returned rows: " + result.count());
                    } else {
                        sender.sendMessage("§aSQL executed successfully. Affected rows: " + result.count());
                    }
                });
            } catch (SQLException e) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> sender.sendMessage("§cSQL error: " + e.getMessage()));
            }
        });

        sender.sendMessage("§eExecuting query asynchronously...");
        return true;
    }
}
