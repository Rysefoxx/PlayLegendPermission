package io.github.rysefoxx.manager;

import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.database.AsyncDatabaseManager;
import io.github.rysefoxx.database.ConnectionManager;
import io.github.rysefoxx.model.GroupModel;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
public class GroupManager {

    private final PlayLegendPermission plugin;
    private final ConnectionManager connectionManager;
    private final AsyncDatabaseManager asyncDatabaseManager;

    private final List<GroupModel> groups = new ArrayList<>();

    public GroupManager(@NotNull PlayLegendPermission plugin, @NotNull ConnectionManager connectionManager, @NotNull AsyncDatabaseManager asyncDatabaseManager) {
        this.plugin = plugin;
        this.connectionManager = connectionManager;
        this.asyncDatabaseManager = asyncDatabaseManager;
        onLoad();
    }

    /**
     * This is executed synchronously, as it is executed once when the server is started and not during runtime where users are on the server.
     */
    public void onLoad() {
        cacheAllGroups();
        createDefaultGroup();
    }

    /**
     * Loads all groups from the database and saves them in the cache.
     */
    private void cacheAllGroups() {
        try (Connection connection = this.connectionManager.getConnection();
             PreparedStatement preparedStatement = this.connectionManager.prepareStatement(connection, "SELECT * FROM legend.groups")) {
            if (preparedStatement == null) {
                this.plugin.getLogger().severe("Failed to load groups from database, because the prepared statement is null!");
                return;
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String prefix = resultSet.getString("prefix");
                    int weight = resultSet.getInt("weight");

                    GroupModel groupModel = new GroupModel(name, prefix, weight);
                    this.groups.add(groupModel);

                    this.plugin.getLogger().info("Loaded group " + name + " from database!");
                }
            }
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to load groups from database!", e);
        }
    }

    /**
     * Creates the default group if it does not exist.
     */
    private void createDefaultGroup() {
        if (findByName("default").isPresent()) return;
        save(new GroupModel("default", "default"));
    }

    /**
     * Gets a group by its name.
     *
     * @param name The name of the group
     * @return The group or {@link Optional#empty()} if no group with the given name exists.
     */
    public @NotNull Optional<GroupModel> findByName(@NotNull String name) {
        return this.groups.stream()
                .filter(groupModel -> groupModel.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Saves a group async to the database.
     *
     * @param groupModel The group to save.
     */
    public void save(@NotNull GroupModel groupModel) {
        if (!this.groups.contains(groupModel)) this.groups.add(groupModel);

        this.asyncDatabaseManager.executeAsync(() -> {
            try (Connection connection = this.connectionManager.getConnection();
                 PreparedStatement preparedStatement = this.connectionManager.prepareStatement(connection, "INSERT INTO legend.groups (name, prefix, weight) VALUES (?, ?, ?)")) {
                if (preparedStatement == null) {
                    this.plugin.getLogger().severe("Failed to save group " + groupModel.getName() + " to database, because the prepared statement is null!");
                    return;
                }

                preparedStatement.setString(1, groupModel.getName());
                preparedStatement.setString(2, groupModel.getPrefix());
                preparedStatement.setInt(3, groupModel.getWeight());

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to save group " + groupModel.getName() + " to database!", e);
            }
        });
    }
}