package io.github.rysefoxx.command.impl;

import io.github.rysefoxx.command.GroupOperation;
import io.github.rysefoxx.manager.GroupManager;
import io.github.rysefoxx.manager.GroupMemberManager;
import io.github.rysefoxx.manager.LanguageManager;
import io.github.rysefoxx.manager.ScoreboardManager;
import io.github.rysefoxx.util.TimeUtil;
import io.github.rysefoxx.util.UUIDFetcher;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
@RequiredArgsConstructor
public class GroupUserCommand implements GroupOperation {

    private final GroupMemberManager groupMemberManager;
    private final GroupManager groupManager;
    private final LanguageManager languageManager;
    private final ScoreboardManager scoreboardManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        String groupName = args[2];
        Player target = Bukkit.getPlayerExact(args[3]);
        AtomicReference<UUID> targetUuid = new AtomicReference<>();
        if (target == null) {
            UUIDFetcher.getUUID(args[3], uuid -> {
                if (uuid == null) {
                    this.languageManager.sendTranslatedMessage(player, "user_not_found");
                    return;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (!offlinePlayer.hasPlayedBefore()) {
                    this.languageManager.sendTranslatedMessage(player, "user_not_found");
                    return;
                }

                targetUuid.set(uuid);
                processCommand(player, args, targetUuid, groupName, null);
            });
        } else {
            targetUuid.set(target.getUniqueId());
            processCommand(player, args, targetUuid, groupName, target);
        }

        return true;
    }

    /**
     * Processes the command.
     *
     * @param player     The player who executed the command.
     * @param args       The arguments of the command.
     * @param targetUuid The UUID of the player to be added/removed to the group.
     * @param groupName  The group to which the player is to be added/removed.
     * @param target     The player to be added/removed to the group.
     */
    private void processCommand(@NotNull Player player, String @NotNull [] args, @NotNull AtomicReference<UUID> targetUuid, @NotNull String groupName, @Nullable Player target) {
        switch (args[1].toLowerCase()) {
            case "add" -> addUserToGroup(args, player, targetUuid, groupName, target);
            case "remove" -> removeUserFromGroup(player, targetUuid, groupName, target);
        }
    }

    /**
     * Removes a user from a group. If the group is the default group, the player will be informed.
     *
     * @param player     The player who executed the command.
     * @param targetUuid The UUID of the player to be removed from the group.
     * @param groupName  The group from which the player is to be removed.
     * @param target     The player to be removed from the group.
     */
    private void removeUserFromGroup(@NotNull Player player, @NotNull AtomicReference<UUID> targetUuid, @NotNull String groupName, @Nullable Player target) {
        UUID uuid = targetUuid.get();
        if (!this.groupMemberManager.inGroup(uuid, groupName)) {
            this.languageManager.sendTranslatedMessage(player, "group_user_not_in_group");
            return;
        }

        if (groupName.equalsIgnoreCase("default")) {
            this.languageManager.sendTranslatedMessage(player, "group_user_remove_default");
            return;
        }

        if (this.groupManager.findByName(groupName).isEmpty()) {
            this.languageManager.sendTranslatedMessage(player, "group_not_found");
            return;
        }

        this.groupMemberManager.removeMember(uuid, groupName);
        this.groupMemberManager.addToDefaultGroup(uuid);
        this.languageManager.sendTranslatedMessage(player, "group_user_removed");

        if (target == null) return;
        this.scoreboardManager.update(target);
    }


    /**
     * Adds a user to a group. If the group does not exist, the player will be informed.
     *
     * @param args       The arguments of the command. Will be used to check if a duration was specified.
     * @param player     The player who executed the command.
     * @param targetUuid The UUID of the player to be added to the group.
     * @param groupName  The group to which the player is to be added.
     * @param target     The player to be added to the group.
     */
    private void addUserToGroup(@NotNull String @NotNull [] args, Player player, @NotNull AtomicReference<UUID> targetUuid, @NotNull String groupName, @Nullable Player target) {
        UUID uuid = targetUuid.get();

        if (this.groupMemberManager.inGroup(uuid, groupName)) {
            this.languageManager.sendTranslatedMessage(player, "group_user_in_group");
            return;
        }

        if (this.groupManager.findByName(groupName).isEmpty()) {
            this.languageManager.sendTranslatedMessage(player, "group_not_found");
            return;
        }

        LocalDateTime time = null;
        if (args.length == 5) {
            time = TimeUtil.parseDuration(args[4]);
        }

        this.groupMemberManager.addMember(uuid, groupName, time);
        this.languageManager.sendTranslatedMessage(player, "group_user_added");

        if (target == null) return;
        this.scoreboardManager.update(target);
    }
}