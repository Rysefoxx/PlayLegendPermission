package io.github.rysefoxx.command;

import io.github.rysefoxx.command.impl.GroupCreateCommand;
import io.github.rysefoxx.manager.GroupManager;
import io.github.rysefoxx.manager.LanguageManager;
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

    public CommandGroup(@NotNull GroupManager groupManager, @NotNull LanguageManager languageManager) {
        this.operations.put("create", new GroupCreateCommand(groupManager, languageManager));
//        this.operations.put("delete", new GroupDeleteCommand());
//        this.operations.put("user", new GroupUserCommand());
//        this.operations.put("info", new GroupInformationCommand());
//        this.operations.put("prefix", new GroupPrefixCommand());
//        this.operations.put("permission", new GroupPermissionCommand());
//        this.operations.put("weight", new GroupWeightCommand());
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
        return null;
    }
}