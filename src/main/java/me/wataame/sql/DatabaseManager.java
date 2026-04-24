package me.wataame.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

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
            if (!dbFile.getParentFile().exists() && !dbFile.getParentFile().mkdirs()) {
                plugin.getLogger().warning("Failed to create plugin data folder for SQLite.");
            }

            hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
            hikariConfig.setMaximumPoolSize(5);
        }

        hikariConfig.setPoolName("SimpleSQL-Hikari");
        hikariConfig.setMaximumPoolSize(config.getInt("pool.maximum-pool-size", 10));
        hikariConfig.setMinimumIdle(config.getInt("pool.minimum-idle", 2));
        hikariConfig.setConnectionTimeout(config.getLong("pool.connection-timeout-ms", 10000L));

        dataSource = new HikariDataSource(hikariConfig);
    }

    public QueryResult executeRaw(String query) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            boolean hasResultSet = statement.execute(query);
            if (hasResultSet) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    int rowCount = 0;
                    while (resultSet.next()) {
                        rowCount++;
                    }
                    return QueryResult.select(rowCount);
                }
            }

            return QueryResult.update(statement.getUpdateCount());
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public record QueryResult(boolean hasResultSet, int count) {
        public static QueryResult select(int rows) {
            return new QueryResult(true, rows);
        }

        public static QueryResult update(int affectedRows) {
            return new QueryResult(false, affectedRows);
        }
    }
}
