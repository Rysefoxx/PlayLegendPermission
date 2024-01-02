package io.github.rysefoxx.manager;

import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.database.AsyncDatabaseManager;
import io.github.rysefoxx.database.ConnectionManager;
import io.github.rysefoxx.model.GroupMemberModel;
import io.github.rysefoxx.model.GroupModel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
public class GroupManager {

    private final PlayLegendPermission plugin;
    private final ConnectionManager connectionManager;
    private final AsyncDatabaseManager asyncDatabaseManager;
    private final GroupPermissionManager groupPermissionManager;

    @Getter
    private final List<GroupModel> groupCache = new ArrayList<>();

    public GroupManager(@NotNull PlayLegendPermission plugin,
                        @NotNull ConnectionManager connectionManager,
                        @NotNull AsyncDatabaseManager asyncDatabaseManager,
                        @NotNull GroupPermissionManager groupPermissionManager) {
        this.plugin = plugin;
        this.connectionManager = connectionManager;
        this.asyncDatabaseManager = asyncDatabaseManager;
        this.groupPermissionManager = groupPermissionManager;
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
                    groupModel.setMembers(findGroupMembers(groupModel));
                    groupModel.setPermissions(this.groupPermissionManager.findAllByGroupName(groupModel));

                    this.groupCache.add(groupModel);
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
     * Gets a group from the cache by its name.
     *
     * @param name The name of the group
     * @return The group or {@link Optional#empty()} if no group with the given name exists.
     */
    public @NotNull Optional<GroupModel> findByName(@NotNull String name) {
        return this.groupCache.stream()
                .filter(groupModel -> groupModel.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Saves a group async to the database.
     *
     * @param groupModel The group to save.
     */
    public void save(@NotNull GroupModel groupModel) {
        if (!this.groupCache.contains(groupModel)) this.groupCache.add(groupModel);

        this.asyncDatabaseManager.executeAsync(() -> {
            try (Connection connection = this.connectionManager.getConnection();
                 PreparedStatement preparedStatement = this.connectionManager.prepareStatement(connection,
                         "INSERT INTO legend.groups (name, prefix, weight) VALUES (?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE prefix = VALUES(prefix), weight = VALUES(weight)")) {

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

    /**
     * Removes a group async from the database.
     *
     * @param groupModel The group to remove.
     */
    public void delete(@NotNull GroupModel groupModel) {
        this.groupCache.remove(groupModel);
        this.asyncDatabaseManager.executeAsync(() -> {
            try (Connection connection = this.connectionManager.getConnection();
                 PreparedStatement preparedStatement = this.connectionManager.prepareStatement(connection, "DELETE FROM legend.groups WHERE name = ?")) {
                if (preparedStatement == null) {
                    this.plugin.getLogger().severe("Failed to remove group " + groupModel.getName() + " from database, because the prepared statement is null!");
                    return;
                }

                preparedStatement.setString(1, groupModel.getName());

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to remove group " + groupModel.getName() + " from database!", e);
            }
        });
    }

    /**
     * This method is executed synchronously as it is only executed once when the server is started and not during runtime when users are on the server.
     *
     * @param groupModel The group to cache all group members
     * @return A list of all group members. Cant be null.
     */
    private @NotNull List<GroupMemberModel> findGroupMembers(@NotNull GroupModel groupModel) {
        List<GroupMemberModel> groupMemberModels = new ArrayList<>();
        try (Connection connection = this.connectionManager.getConnection();
             PreparedStatement preparedStatement = this.connectionManager.prepareStatement(connection, "SELECT id, uuid, expiration FROM legend.group_member WHERE name = ?")) {
            if (preparedStatement == null) {
                this.plugin.getLogger().severe("Failed to load groups_members from database, because the prepared statement is null!");
                return groupMemberModels;
            }

            preparedStatement.setString(1, groupModel.getName());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    long id = resultSet.getLong("id");
                    UUID uuid = resultSet.getObject("uuid", UUID.class);
                    LocalDateTime expiration = resultSet.getObject("expiration", LocalDateTime.class);

                    GroupMemberModel groupMemberModel = new GroupMemberModel(id, uuid, expiration, groupModel);
                    groupMemberModels.add(groupMemberModel);

                    this.plugin.getLogger().info("Loaded group member " + uuid + " with id " + id + " from database!");
                }
            }
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to load group members from database!", e);
        }

        return groupMemberModels;
    }
}