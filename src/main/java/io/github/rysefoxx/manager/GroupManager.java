package io.github.rysefoxx.manager;

import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.database.AsyncDatabaseManager;
import io.github.rysefoxx.database.ConnectionManager;
import io.github.rysefoxx.model.GroupMemberModel;
import io.github.rysefoxx.model.GroupModel;
import io.github.rysefoxx.model.GroupPermissionModel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
public class GroupManager {

    private final PlayLegendPermission plugin;
    private final ConnectionManager connectionManager;
    private final AsyncDatabaseManager asyncDatabaseManager;

    @Getter
    private final List<GroupModel> groupCache = new ArrayList<>();

    public GroupManager(@NotNull PlayLegendPermission plugin) {
        this.plugin = plugin;
        this.connectionManager = plugin.getConnectionManager();
        this.asyncDatabaseManager = plugin.getAsyncDatabaseManager();
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
        String query = "SELECT g.name, g.prefix, g.weight, " +
                "gm.id as member_id, gm.uuid as member_uuid, gm.expiration as member_expiration, " +
                "gp.id as permission_id, gp.permission " +
                "FROM legend.groups g " +
                "LEFT JOIN legend.group_member gm ON g.name = gm.name " +
                "LEFT JOIN legend.group_permission gp ON g.name = gp.name";

        try (Connection connection = this.connectionManager.getConnection();
             PreparedStatement preparedStatement = this.connectionManager.prepareStatement(connection, query)) {
            if (preparedStatement == null) {
                this.plugin.getLogger().severe("Failed to load groups from database, because the prepared statement is null!");
                return;
            }

            Map<String, GroupModel> groupMap = new HashMap<>();

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String prefix = resultSet.getString("prefix");
                    int weight = resultSet.getInt("weight");
                    GroupModel groupModel = groupMap.computeIfAbsent(name, k -> new GroupModel(name, prefix, weight));

                    if (resultSet.getObject("member_uuid") != null) {
                        UUID uuid = (UUID) resultSet.getObject("member_uuid");
                        LocalDateTime expiration = resultSet.getObject("member_expiration", LocalDateTime.class);
                        GroupMemberModel member = new GroupMemberModel(resultSet.getLong("member_id"), uuid, expiration, groupModel);
                        groupModel.getMembers().add(member);
                    }

                    if (resultSet.getString("permission") != null) {
                        GroupPermissionModel permission = new GroupPermissionModel(resultSet.getLong("permission_id"), resultSet.getString("permission"), groupModel);
                        groupModel.getPermissions().add(permission);
                    }
                }
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to load groups from database!", e);
            }

            groupMap.values().forEach(groupModel -> {
                this.groupCache.add(groupModel);
                this.plugin.getLogger().info("Loaded group " + groupModel.toString() + " from database!");
            });

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
}