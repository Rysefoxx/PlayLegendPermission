package io.github.rysefoxx.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.rysefoxx.PlayLegendPermission;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
public class ConnectionManager {

    private final PlayLegendPermission plugin;
    @Getter
    private HikariDataSource dataSource;

    public ConnectionManager(@NotNull PlayLegendPermission plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        setupHikariCP();
    }

    /**
     * Gets a connection from the datasource.
     *
     * @return The connection or null if an error occurred.
     */
    public @Nullable Connection getConnection() {
        try {
            return this.dataSource.getConnection();
        } catch (Exception exception) {
            this.plugin.getLogger().severe("Failed to get connection from datasource!");
            return null;
        }
    }

    /**
     * Closes the connection to the database.
     */
    public void closeConnection() {
        if (this.dataSource == null) return;
        if (this.dataSource.isClosed()) return;
        this.dataSource.close();
    }

    /**
     * Saves the default config to the plugin folder.
     */
    private void saveDefaultConfig() {
        this.plugin.saveResource("database.yml", false);
    }

    /**
     * Sets up the HikariCP datasource.
     */
    private void setupHikariCP() {
        File file = new File(this.plugin.getDataFolder(), "database.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!isValidConfig(config)) {
            this.plugin.getLogger().severe("Failed to load database.yml!");
            // Without a database, nothing works, so we shut down the server.
            Bukkit.shutdown();
            return;
        }

        String url = buildJdbcUrl(config);
        setupDataSource(url, config);
    }

    /**
     * Checks if the database config is valid. Its valid when all required fields are set.
     *
     * @param config The config to check.
     * @return true if valid, false if not.
     */
    private boolean isValidConfig(@NotNull YamlConfiguration config) {
        return config.getString("host") != null
                && config.getString("port") != null
                && config.getString("database") != null
                && config.getString("username") != null
                && config.getString("password") != null;
    }

    /**
     * Builds the jdbc url from the config.
     *
     * @param config The config to build the url from.
     * @return The jdbc url.
     */
    private @NotNull String buildJdbcUrl(@NotNull YamlConfiguration config) {
        String host = config.getString("host");
        String port = config.getString("port");
        String database = config.getString("database");
        return String.format("jdbc:mariadb://%s:%s/%s?useSSL=false", host, port, database);
    }

    /**
     * Sets up the datasource with the given hikari config.
     *
     * @param url    The jdbc url.
     * @param config The config to build the datasource from.
     */
    private void setupDataSource(@NotNull String url, @NotNull YamlConfiguration config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(config.getString("username"));
        hikariConfig.setPassword(config.getString("password"));
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setMaximumPoolSize(20);

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    /**
     * @param query The query to execute.
     * @return the prepared statement or null if an error occurred.
     */
    public @Nullable PreparedStatement prepareStatement(@Nullable Connection connection, @NotNull String query) throws SQLException {
        if (connection == null) {
            this.plugin.getLogger().severe("The connection for the prepared statement is null!");
            return null;
        }

        return connection.prepareStatement(query);
    }
}