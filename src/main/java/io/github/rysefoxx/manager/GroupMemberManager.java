package io.github.rysefoxx.manager;

import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.database.AsyncDatabaseManager;
import io.github.rysefoxx.database.ConnectionManager;
import io.github.rysefoxx.model.GroupMemberModel;
import io.github.rysefoxx.model.GroupModel;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
public class GroupMemberManager {

    private final PlayLegendPermission plugin;
    private final ConnectionManager connectionManager;
    private final AsyncDatabaseManager asyncDatabaseManager;
    private final GroupManager groupManager;
    private final GroupPermissionManager groupPermissionService;
    private final LanguageManager languageManager;

    public GroupMemberManager(@NotNull PlayLegendPermission plugin) {
        this.plugin = plugin;
        this.connectionManager = plugin.getConnectionManager();
        this.asyncDatabaseManager = plugin.getAsyncDatabaseManager();
        this.groupManager = plugin.getGroupManager();
        this.groupPermissionService = plugin.getGroupPermissionManager();
        this.languageManager = plugin.getLanguageManager();
        scheduler();
    }

    /**
     * Starts the scheduler to check if a {@link GroupMemberModel} is expired. <br>
     * We only check the online players. Because the rank of online players can expire at any time. Offline players, on the other hand, are only checked when they re-enter the server. <br>
     * This ensures better performance as we don't have to go through the entire cache all the time. If a player comes online and his time has expired, he is placed directly in the default group.
     */
    public void scheduler() {
        Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                GroupModel groupModel = getOrSetGroup(onlinePlayer.getUniqueId()).getGroup();
                if (!isExpired(onlinePlayer.getUniqueId())) continue;

                addToDefaultGroup(onlinePlayer.getUniqueId());
                this.languageManager.sendTranslatedMessage(onlinePlayer, "group_user_expired");
                this.plugin.getScoreboardManager().update(onlinePlayer);
                this.plugin.getLogger().info("Removed " + onlinePlayer.getName() + " from group " + groupModel.getName() + " because the group expired!");
            }
        }, 0L, 20L);
    }


    /**
     * Adds a {@link UUID} to a {@link GroupModel}.
     *
     * @param uuid       The {@link UUID} to add.
     * @param name       The name of the {@link GroupModel} to add the {@link UUID} to.
     * @param expiration The expiration of the {@link UUID} in the {@link GroupModel}. Or {@code null} if the group should not expire.
     */
    public void addMember(@NotNull UUID uuid, @NotNull String name, @Nullable LocalDateTime expiration) {
        Optional<GroupModel> optional = this.groupManager.findByName(name);
        if (optional.isEmpty()) return;

        GroupMemberModel groupMemberModel = getGroup(uuid);
        if (groupMemberModel != null) {
            removeMember(uuid, groupMemberModel.getGroup().getName());
        }

        GroupModel model = optional.get();
        GroupMemberModel groupMemberModelToAdd = new GroupMemberModel(uuid, expiration, model);
        model.getMembers().add(groupMemberModelToAdd);
        this.groupPermissionService.reloadPlayerPermissions(model, Bukkit.getPlayer(uuid), true);

        save(model, groupMemberModelToAdd);
    }

    /**
     * Removes a {@link UUID} from a {@link GroupModel}.
     *
     * @param uuid The {@link UUID} to remove.
     * @param name The name of the {@link GroupModel} to remove the {@link UUID} from.
     */
    public void removeMember(@NotNull UUID uuid, @NotNull String name) {
        Optional<GroupModel> optional = this.groupManager.findByName(name);
        if (optional.isEmpty()) return;

        GroupModel groupModel = optional.get();
        this.groupPermissionService.reloadPlayerPermissions(groupModel, Bukkit.getPlayer(uuid), false);
        groupModel.getMembers().removeIf(member -> member.getUuid().equals(uuid));

        delete(groupModel, uuid);
    }

    /**
     * Adds a {@link UUID} to the default {@link GroupModel}.
     *
     * @param uuid The {@link UUID} to add.
     */
    public void addToDefaultGroup(@NotNull UUID uuid) {
        Optional<GroupModel> optional = this.groupManager.findByName("default");
        if (optional.isEmpty()) {
            GroupModel groupModel = new GroupModel("default", "default");
            this.groupManager.save(groupModel);
        }

        addMember(uuid, "default", null);
    }

    /**
     * Get the {@link GroupMemberModel} of a {@link UUID}. Or creates a new {@link GroupMemberModel} if the {@link UUID} is not in a {@link GroupMemberModel}.
     *
     * @param uuid The {@link UUID} to get the {@link GroupMemberModel} of.
     * @return The {@link GroupMemberModel} of the {@link UUID}
     */
    public @NotNull GroupMemberModel getOrSetGroup(@NotNull UUID uuid) {
        GroupMemberModel groupMemberModel = getGroup(uuid);
        if (groupMemberModel != null) return groupMemberModel;

        addToDefaultGroup(uuid);
        return Objects.requireNonNull(getGroup(uuid));
    }


    /**
     * Get the {@link GroupMemberModel} of a {@link UUID}.
     *
     * @param uuid The {@link UUID} to get the {@link GroupMemberModel} of.
     * @return The {@link GroupMemberModel} of the {@link UUID} or {@code null} if the {@link UUID} is not in a {@link GroupMemberModel}.
     */
    public @Nullable GroupMemberModel getGroup(@NotNull UUID uuid) {
        List<GroupModel> groups = this.groupManager.getGroupCache();

        return groups.stream()
                // Get all members of the group
                .map(group -> group.getMembers().stream()
                        // Filter for the uuid
                        .filter(member -> member.getUuid().equals(uuid))
                        // Get the first member
                        .findFirst()
                        // Return null if no member was found
                        .orElse(null)
                )
                // Filter for non null members
                .filter(Objects::nonNull)
                // Get the first member
                .findFirst()
                // Return null if no member was found
                .orElse(null);
    }

    /**
     * Checks if a {@link UUID} is in a {@link GroupModel}.
     *
     * @param uuid The {@link UUID} to check.
     * @param name The name of the {@link GroupModel} to check.
     * @return {@code true} if the {@link UUID} is in the {@link GroupModel}, otherwise {@code false}.
     */
    public boolean inGroup(@NotNull UUID uuid, @NotNull String name) {
        Optional<GroupModel> optional = this.groupManager.findByName(name);
        if (optional.isEmpty()) return false;

        GroupModel groupModel = optional.get();
        return groupModel.getMembers().stream().anyMatch(member -> member.getUuid().equals(uuid) && !isExpired(uuid));
    }


    /**
     * Checks if a {@link UUID} is in any {@link GroupModel}.
     *
     * @param uuid The {@link UUID} to check.
     * @return {@code true} if the {@link UUID} is in any {@link GroupModel}, otherwise {@code false}.
     */
    public boolean hasGroup(@NotNull UUID uuid) {
        List<GroupModel> groupCache = this.groupManager.getGroupCache();
        return groupCache.stream().anyMatch(groupModel -> groupModel.getMembers().stream().anyMatch(member -> member.getUuid().equals(uuid)));
    }

    /**
     * Checks if the group of a {@link UUID} is expired.
     *
     * @param uuid The {@link UUID} to check.
     * @return {@code true} if the group is expired, otherwise {@code false}.
     */
    public boolean isExpired(@NotNull UUID uuid) {
        boolean hasGroup = hasGroup(uuid);
        if (!hasGroup) return false;

        GroupMemberModel groupMemberModel = getOrSetGroup(uuid);
        return groupMemberModel.getExpiration() != null && groupMemberModel.getExpiration().isBefore(LocalDateTime.now());
    }

    /**
     * Saves a {@link GroupMemberModel} async to the database.
     *
     * @param groupModel       The {@link GroupModel} to save.
     * @param groupMemberModel The {@link GroupMemberModel} to save.
     */
    public void save(@NotNull GroupModel groupModel, @NotNull GroupMemberModel groupMemberModel) {
        this.asyncDatabaseManager.executeAsync(() -> {
            try (Connection connection = this.connectionManager.getConnection();
                 PreparedStatement preparedStatement = this.connectionManager.prepareStatement(connection,
                         "INSERT INTO legend.group_member (uuid, name, expiration) VALUES (?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE expiration = VALUES(expiration)")) {

                if (preparedStatement == null) {
                    this.plugin.getLogger().severe("Failed to save group member " + groupMemberModel.getUuid() + " to database, because the prepared statement is null!");
                    return;
                }

                preparedStatement.setObject(1, groupMemberModel.getUuid());
                preparedStatement.setString(2, groupModel.getName());
                preparedStatement.setObject(3, groupMemberModel.getExpiration());

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to save group member " + groupMemberModel.getUuid() + " to database!", e);
            }
        });
    }

    /**
     * Deletes a {@link GroupMemberModel} async from the database.
     *
     * @param groupModel The {@link GroupModel} to delete.
     * @param uuid       The {@link UUID} to delete.
     */
    public void delete(@NotNull GroupModel groupModel, @NotNull UUID uuid) {
        this.asyncDatabaseManager.executeAsync(() -> {
            try (Connection connection = this.connectionManager.getConnection();
                 PreparedStatement preparedStatement = this.connectionManager.prepareStatement(connection, "DELETE FROM legend.group_member WHERE uuid = ? AND name = ?")) {
                if (preparedStatement == null) {
                    this.plugin.getLogger().severe("Failed to delete group member " + uuid + " from database, because the prepared statement is null!");
                    return;
                }

                preparedStatement.setObject(1, uuid);
                preparedStatement.setString(2, groupModel.getName());

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to delete group member " + uuid + " from database!", e);
            }
        });
    }
}