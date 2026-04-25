package me.wataame.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final int DB_TIMEOUT_SECONDS = 3;
    private static final long DB_TIMEOUT_MS = 3000L;

    private final SimpleSQLPlugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(SimpleSQLPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        FileConfiguration config = plugin.getConfig();
        String storageType = config.getString("storage-type", "sqlite").toLowerCase();

        HikariConfig hikariConfig = new HikariConfig();

        if ("mysql".equals(storageType)) {
            String host = config.getString("mysql.host", "localhost");
            int port = config.getInt("mysql.port", 3306);
            String database = config.getString("mysql.database", "minecraft");
            String username = config.getString("mysql.username", "root");
            String password = config.getString("mysql.password", "password");
            boolean useSSL = config.getBoolean("mysql.use-ssl", false);

            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL + "&allowPublicKeyRetrieval=true");
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            File dbFile = new File(plugin.getDataFolder(), config.getString("sqlite.file", "simplesql.db"));
            File parent = dbFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                plugin.getLogger().warning(plugin.colorize(plugin.getLang("errors.sqlite-dir", "&cFailed to create plugin data folder for SQLite.")));
            }

            hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
            hikariConfig.setMaximumPoolSize(5);
        }

        hikariConfig.setPoolName("SimpleSQL-Hikari");
        hikariConfig.setMaximumPoolSize(config.getInt("pool.maximum-pool-size", 10));
        hikariConfig.setMinimumIdle(config.getInt("pool.minimum-idle", 2));
        hikariConfig.setConnectionTimeout(DB_TIMEOUT_MS);
        hikariConfig.setValidationTimeout(DB_TIMEOUT_MS);

        dataSource = new HikariDataSource(hikariConfig);
    }

    public QueryResult executeRaw(String query) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setQueryTimeout(DB_TIMEOUT_SECONDS);

            boolean hasResultSet = statement.execute();
            if (hasResultSet) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    List<String> rows = new ArrayList<>();
                    while (resultSet.next()) {
                        StringBuilder line = new StringBuilder();
                        for (int i = 1; i <= columnCount; i++) {
                            if (i > 1) {
                                line.append(" | ");
                            }
                            String label = metaData.getColumnLabel(i);
                            Object value = resultSet.getObject(i);
                            line.append(label).append('=').append(value);
                        }
                        rows.add(line.toString());
                    }

                    return QueryResult.select(rows);
                }
            }

            return QueryResult.update(statement.getUpdateCount());
        }
    }

    public boolean isTimeout(SQLException e) {
        if (e instanceof SQLTimeoutException) {
            return true;
        }
        String message = e.getMessage();
        return message != null && message.toLowerCase().contains("timeout");
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public record QueryResult(boolean hasResultSet, int count, List<String> rows) {
        public static QueryResult select(List<String> rows) {
            return new QueryResult(true, rows.size(), rows);
        }

        public static QueryResult update(int affectedRows) {
            return new QueryResult(false, affectedRows, List.of());
        }
    }
}
