package io.github.rysefoxx.command;

import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.command.impl.*;
import io.github.rysefoxx.manager.*;
import io.github.rysefoxx.model.GroupModel;
import io.github.rysefoxx.model.GroupPermissionModel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
public class CommandGroup implements CommandExecutor, TabCompleter {

    private final HashMap<String, GroupOperation> operations = new HashMap<>();
    private final GroupManager groupManager;

    public CommandGroup(@NotNull PlayLegendPermission plugin) {
        LanguageManager languageManager = plugin.getLanguageManager();
        GroupMemberManager groupMemberManager = plugin.getGroupMemberManager();
        GroupPermissionManager groupPermissionManager = plugin.getGroupPermissionManager();
        ScoreboardManager scoreboardManager = plugin.getScoreboardManager();

        this.groupManager = plugin.getGroupManager();
        this.operations.put("create", new GroupCreateCommand(groupManager, languageManager));
        this.operations.put("delete", new GroupDeleteCommand(groupManager, groupMemberManager, languageManager, scoreboardManager));
        this.operations.put("user", new GroupUserCommand(groupMemberManager, groupManager, languageManager, scoreboardManager));
        this.operations.put("info", new GroupInformationCommand(groupMemberManager, languageManager));
        this.operations.put("prefix", new GroupPrefixCommand(groupManager, languageManager, scoreboardManager));
        this.operations.put("permission", new GroupPermissionCommand(groupManager, groupPermissionManager, languageManager));
        this.operations.put("weight", new GroupWeightCommand(groupManager, languageManager));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            GroupOperation operation = this.operations.get(args[0].toLowerCase());
            if (operation == null) return false;

            return operation.onCommand(sender, command, label, args);
        } catch (IndexOutOfBoundsException | NullPointerException ex) {
            sender.sendMessage("/Group create <Name>",
                    "/Group delete <Name>",
                    "/Group prefix <Name> <Prefix>",
                    "/Group weight <Name> <Weight>",
                    "/Group user add <Name> <User> (<Dauer> Format: 1d2h3m4s)",
                    "/Group user remove <Name> <User>",
                    "/Group permission add <Name> <Permission>",
                    "/Group permission remove <Name> <Permission>",
                    "/Group info");
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("create", "delete", "user", "info", "prefix", "weight", "permission");
        }

        List<GroupModel> groupCache = this.groupManager.getGroupCache();
        List<String> groupNames = groupCache.stream().map(GroupModel::getName).toList();

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "create":
                    return List.of("<Name>");
                case "delete":
                case "prefix":
                case "weight":
                    return groupNames;
            }

            if (args[0].equalsIgnoreCase("user") || args[0].equalsIgnoreCase("permission")) {
                return List.of("add", "remove");
            }
        }

        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "prefix":
                    return List.of("<Prefix>");
                case "weight":
                    return List.of("<Weight>");
                case "user":
                case "permission":
                    return groupNames;
            }
        }

        if (args.length == 4) {
            switch (args[0].toLowerCase()) {
                case "user":
                    return null;
                case "permission":
                    if (args[1].equalsIgnoreCase("add")) {
                        return List.of("<Permission>");
                    }
                    if (args[1].equalsIgnoreCase("remove")) {
                        return groupCache.stream()
                                .map(GroupModel::getPermissions)
                                .flatMap(List::stream)
                                .map(GroupPermissionModel::getPermission).
                                toList();
                    }
            }
        }

        if (args.length == 5 && args[0].equalsIgnoreCase("user") && args[1].equalsIgnoreCase("add")) {
            return List.of("<Dauer>");
        }

        return List.of();
    }
}