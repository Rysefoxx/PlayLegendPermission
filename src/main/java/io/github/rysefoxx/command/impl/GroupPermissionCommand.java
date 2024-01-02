package io.github.rysefoxx.command.impl;

import io.github.rysefoxx.command.GroupOperation;
import io.github.rysefoxx.manager.GroupManager;
import io.github.rysefoxx.manager.GroupPermissionManager;
import io.github.rysefoxx.manager.LanguageManager;
import io.github.rysefoxx.model.GroupModel;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
@RequiredArgsConstructor
public class GroupPermissionCommand implements GroupOperation {

    private final GroupManager groupManager;
    private final GroupPermissionManager groupPermissionManager;
    private final LanguageManager languageManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        Optional<GroupModel> optional = this.groupManager.findByName(args[2]);

        if (optional.isEmpty()) {
            this.languageManager.sendTranslatedMessage(player, "group_not_found");
            return false;
        }

        GroupModel groupModel = optional.get();
        String permission = args[3];

        switch (args[1].toLowerCase()) {
            case "add":
                addPermission(player, permission, groupModel);
                break;
            case "remove":
                removePermission(player, permission, groupModel);
                break;
        }
        return true;
    }


    /**
     * This method removes a permission from a group. If the permission does not exist, the player will be informed.
     *
     * @param player     The player who executed the command.
     * @param permission The permission to be removed.
     * @param groupModel The group from which the permission is to be removed.
     */
    private void removePermission(@NotNull Player player, @NotNull String permission, @NotNull GroupModel groupModel) {
        if (!this.groupPermissionManager.hasPermission(permission, groupModel)) {
            this.languageManager.sendTranslatedMessage(player, "group_permission_not_exists");
            return;
        }

        this.groupPermissionManager.removePermission(permission, groupModel);
        this.languageManager.sendTranslatedMessage(player, "group_permission_removed");
    }

    /**
     * This method adds a permission to a group. If the permission already exists, the player will be informed.
     *
     * @param player     The player who executed the command.
     * @param permission The permission to be added.
     * @param groupModel The group to which the permission is to be added.
     */
    private void addPermission(@NotNull Player player, @NotNull String permission, @NotNull GroupModel groupModel) {
        if (this.groupPermissionManager.hasPermission(permission, groupModel)) {
            this.languageManager.sendTranslatedMessage(player, "group_permission_already_exists");
            return;
        }

        this.groupPermissionManager.addPermission(permission, groupModel);
        this.languageManager.sendTranslatedMessage(player, "group_permission_added");
    }
}