package me.wataame.sql;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SqlSendCommand implements CommandExecutor {

    private final SimpleSQLPlugin plugin;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public SqlSendCommand(SimpleSQLPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(plugin.colorize(plugin.getLang("command.no-permission", "&cYou must be OP to use this command.")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.colorize(plugin.getLang("sql-send.usage", "&cUsage: /sql-send <query>")));
            return true;
        }

        String query = String.join(" ", args).trim();
        if (query.isEmpty()) {
            sender.sendMessage(plugin.colorize(plugin.getLang("command.empty-query", "&cQuery cannot be empty.")));
            return true;
        }

        String url = plugin.getConfig().getString("sql-send.url", "").trim();
        if (url.isEmpty()) {
            sender.sendMessage(plugin.colorize(plugin.getLang("sql-send.url-empty", "&csql-send.url is empty in config.yml")));
            return true;
        }

        sender.sendMessage(plugin.colorize(plugin.getLang("sql-send.running", "&eExecuting query and sending result...")));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("query", query);

            try {
                DatabaseManager.QueryResult result = plugin.getDatabaseManager().executeRaw(query);
                payload.put("success", true);
                payload.put("resultSet", result.hasResultSet());
                payload.put("count", result.count());
                payload.put("rows", result.rows());

                postPayload(url, payload);
                Bukkit.getScheduler().runTask(plugin,
                        () -> sender.sendMessage(plugin.colorize(plugin.getLang("sql-send.success", "&aSent SQL result to configured URL."))));
            } catch (SQLException e) {
                payload.put("success", false);
                payload.put("error", safeMessage(e));
                payload.put("sqlState", e.getSQLState());
                payload.put("errorCode", e.getErrorCode());

                try {
                    postPayload(url, payload);
                } catch (Exception ignored) {
                    // ignore secondary webhook errors while reporting original SQL issue to sender
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (plugin.getDatabaseManager().isTimeout(e)) {
                        sender.sendMessage(plugin.colorize(plugin.getLang("errors.timeout", "&cタイムアウトしました (3秒)。")));
                    }
                    sender.sendMessage(plugin.colorize(plugin.getLang("errors.sql", "&cSQL error: {message}")
                            .replace("{message}", safeMessage(e))));
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Bukkit.getScheduler().runTask(plugin,
                        () -> sender.sendMessage(plugin.colorize(plugin.getLang("sql-send.post-failed", "&cFailed to POST SQL result: {message}")
                                .replace("{message}", safeMessage(e)))));
            } catch (IOException e) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> sender.sendMessage(plugin.colorize(plugin.getLang("sql-send.post-failed", "&cFailed to POST SQL result: {message}")
                                .replace("{message}", safeMessage(e)))));
            }
        });

        return true;
    }

    private String safeMessage(Throwable throwable) {
        String msg = throwable.getMessage();
        return msg == null || msg.isBlank() ? throwable.getClass().getSimpleName() : msg;
    }

    private void postPayload(String url, Map<String, Object> payload) throws IOException, InterruptedException {
        String json = toJson(payload);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));

        ConfigurationSection headers = plugin.getConfig().getConfigurationSection("sql-send.headers");
        if (headers != null) {
            for (String key : headers.getKeys(false)) {
                builder.header(key, String.valueOf(headers.get(key)));
            }
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP " + response.statusCode() + " body=" + response.body());
        }
    }

    private String toJson(Map<String, Object> payload) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('"').append(escape(entry.getKey())).append('"').append(':');
            json.append(toJsonValue(entry.getValue()));
        }
        json.append('}');
        return json.toString();
    }

    @SuppressWarnings("unchecked")
    private String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Iterable<?> iterable) {
            StringBuilder out = new StringBuilder("[");
            boolean first = true;
            for (Object item : iterable) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                out.append(toJsonValue(item));
            }
            out.append(']');
            return out.toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder out = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                out.append('"').append(escape(String.valueOf(entry.getKey()))).append('"')
                        .append(':')
                        .append(toJsonValue(entry.getValue()));
            }
            out.append('}');
            return out.toString();
        }
        return '"' + escape(String.valueOf(value)) + '"';
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
