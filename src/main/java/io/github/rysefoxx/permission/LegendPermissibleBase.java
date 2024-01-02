package io.github.rysefoxx.permission;

import io.github.rysefoxx.manager.GroupMemberManager;
import io.github.rysefoxx.manager.GroupPermissionManager;
import io.github.rysefoxx.model.GroupMemberModel;
import io.github.rysefoxx.model.GroupModel;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
public class LegendPermissibleBase extends PermissibleBase {

    private final GroupPermissionManager groupPermissionManager;
    private final GroupMemberManager groupMemberManager;
    private final Player player;

    /**
     * Creates a new permissible base for the given player.
     *
     * @param player                 The player
     * @param groupPermissionManager The group permission service
     * @param groupMemberManager     The group member service
     */
    public LegendPermissibleBase(@Nullable Player player, @NotNull GroupPermissionManager groupPermissionManager, @NotNull GroupMemberManager groupMemberManager) {
        super(player);
        this.player = player;
        this.groupPermissionManager = groupPermissionManager;
        this.groupMemberManager = groupMemberManager;

    }

    /**
     * Checks if the player has the given permission.
     *
     * @param permission Name of the permission
     * @return true if the player has the permission, otherwise false
     */
    @Override
    public boolean hasPermission(@NotNull String permission) {
        if (this.player == null) return false;
        if (super.isOp()) return true;

        GroupMemberModel groupMemberModel = this.groupMemberManager.getOrSetGroup(this.player.getUniqueId());
        GroupModel groupModel = groupMemberModel.getGroup();
        if (groupModel == null) return false;

        return this.groupPermissionManager.hasPermission("*", groupModel) || this.groupPermissionManager.hasPermission(permission, groupModel);
    }
}