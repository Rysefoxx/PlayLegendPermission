package io.github.rysefoxx.manager;

import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.database.AsyncDatabaseManager;
import io.github.rysefoxx.database.ConnectionManager;
import io.github.rysefoxx.model.GroupMemberModel;
import io.github.rysefoxx.model.GroupModel;
import io.github.rysefoxx.model.GroupPermissionModel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
public class GroupPermissionManager {

    private final PlayLegendPermission plugin;
    private final ConnectionManager connectionManager;
    private final AsyncDatabaseManager asyncDatabaseManager;
    private final HashMap<UUID, PermissionAttachment> attachments = new HashMap<>();

    public GroupPermissionManager(@NotNull PlayLegendPermission plugin) {
        this.plugin = plugin;
        this.connectionManager = plugin.getConnectionManager();
        this.asyncDatabaseManager = plugin.getAsyncDatabaseManager();
    }

    /**
     * Adds a permission to a group.
     *
     * @param permission The permission to add
     * @param groupModel The group to add the permission to
     */
    public void addPermission(@NotNull String permission, @NotNull GroupModel groupModel) {
        groupModel.getPermissions().add(new GroupPermissionModel(permission, groupModel));
        save(groupModel, permission);

        reloadPlayerPermissions(groupModel, true);
    }

    /**
     * Removes a permission from a group.
     *
     * @param permission The permission to remove
     * @param groupModel The group to remove the permission from
     */
    public void removePermission(@NotNull String permission, @NotNull GroupModel groupModel) {
        groupModel.getPermissions().removeIf(permissionModel -> permissionModel.getPermission().equalsIgnoreCase(permission));
        delete(groupModel, permission);

        reloadPlayerPermissions(groupModel, false);
    }

    /**
     * Checks if a group has a permission.
     *
     * @param permission The permission to check
     * @param groupModel The group to check
     * @return true if the group has the permission, false otherwise
     */
    public boolean hasPermission(@NotNull String permission, @NotNull GroupModel groupModel) {
        return groupModel.getPermissions().stream().anyMatch(groupPermissionModel -> groupPermissionModel.getPermission().equalsIgnoreCase(permission));
    }

    /**
     * Reloads the permissions of all players in a group.
     *
     * @param groupModel The group to reload the permissions for
     * @param add        True if the permissions should be added, false otherwise
     */
    public void reloadPlayerPermissions(@NotNull GroupModel groupModel, boolean add) {
        for (GroupMemberModel member : groupModel.getMembers()) {
            Player player = Bukkit.getPlayer(member.getUuid());
            if (player == null) continue;

            reloadPlayerPermissions(groupModel, player, add);
        }
    }

    /**
     * Reloads the permissions of a player in a group.
     *
     * @param groupModel The group to reload the permissions for
     * @param player     The player to reload the permissions for
     * @param add        True if the permissions should be added, false otherwise
     */
    public void reloadPlayerPermissions(@NotNull GroupModel groupModel, @Nullable Player player, boolean add) {
        if (player == null) return;

        addPermissionAttachmentIfNotFound(player);
        PermissionAttachment permissionAttachment = this.attachments.get(player.getUniqueId());

        if (!add) {
            permissionAttachment.getPermissions().keySet().forEach(permissionAttachment::unsetPermission);
            return;
        }

        for (GroupPermissionModel permission : groupModel.getPermissions()) {
            permissionAttachment.setPermission(permission.getPermission(), true);
        }
    }

    /**
     * Adds a permission attachment to a player if it does not exist.
     *
     * @param player The player to add the permission attachment to
     */
    private void addPermissionAttachmentIfNotFound(@NotNull Player player) {
        if (this.attachments.containsKey(player.getUniqueId())) return;

        this.attachments.put(player.getUniqueId(), player.addAttachment(this.plugin));
    }

    /**
     * Saves a group permission async to the database.
     *
     * @param groupModel The group to save.
     * @param permission The permission to save.
     */
    public void save(@NotNull GroupModel groupModel, @NotNull String permission) {
        this.asyncDatabaseManager.executeAsync(() -> {
            try (Connection connection = this.connectionManager.getConnection();
                 PreparedStatement preparedStatement = this.connectionManager.prepareStatement(connection,
                         "INSERT INTO legend.group_permission (permission, name) VALUES (?, ?) " +
                                 "ON DUPLICATE KEY UPDATE permission = VALUES(permission), name = VALUES(name)")) {

                if (preparedStatement == null) {
                    this.plugin.getLogger().severe("Failed to save group permission " + permission + " to database, because the prepared statement is null!");
                    return;
                }

                preparedStatement.setString(1, permission);
                preparedStatement.setString(2, groupModel.getName());

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to save group permission " + permission + " to database!", e);
            }
        });
    }

    /**
     * Deletes a group permission async from the database.
     *
     * @param groupModel The group to delete.
     * @param permission The permission to delete.
     */
    public void delete(@NotNull GroupModel groupModel, @NotNull String permission) {
        this.asyncDatabaseManager.executeAsync(() -> {
            try (Connection connection = this.connectionManager.getConnection();
                 PreparedStatement preparedStatement = this.connectionManager.prepareStatement(connection, "DELETE FROM legend.group_permission WHERE permission = ? AND name = ?")) {
                if (preparedStatement == null) {
                    this.plugin.getLogger().severe("Failed to delete group permission " + permission + ", because the prepared statement is null!");
                    return;
                }

                preparedStatement.setString(1, permission);
                preparedStatement.setString(2, groupModel.getName());

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to delete groups permission " + groupModel.getName() + " to database!", e);
            }
        });
    }
}