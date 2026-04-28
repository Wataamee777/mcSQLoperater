package me.wataame.sql;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SqlTabCompleter implements TabCompleter {

    private static final List<String> SQL_KEYWORDS = List.of(
            "SELECT", "INSERT", "UPDATE", "DELETE", "CREATE", "ALTER", "DROP",
            "FROM", "WHERE", "ORDER", "BY", "GROUP", "LIMIT", "VALUES", "SET"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.isOp()) {
            return List.of();
        }

        String current = args.length == 0 ? "" : args[args.length - 1].toUpperCase(Locale.ROOT);
        List<String> completions = new ArrayList<>();

        for (String keyword : SQL_KEYWORDS) {
            if (keyword.startsWith(current)) {
                completions.add(keyword);
            }
        }

        return completions;
    }
}
