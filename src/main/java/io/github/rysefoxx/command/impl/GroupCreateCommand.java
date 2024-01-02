package io.github.rysefoxx.command.impl;

import io.github.rysefoxx.command.GroupOperation;
import io.github.rysefoxx.manager.GroupManager;
import io.github.rysefoxx.manager.LanguageManager;
import io.github.rysefoxx.model.GroupModel;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@RequiredArgsConstructor
public class GroupCreateCommand implements GroupOperation {

    private final GroupManager groupManager;
    private final LanguageManager languageManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        String name = args[1];

        Optional<GroupModel> optional = this.groupManager.findByName(name);
        if (optional.isPresent()) {
            this.languageManager.sendTranslatedMessage(player, "group_exists");
            return false;
        }

        if (name.length() > 20) {
            this.languageManager.sendTranslatedMessage(player, "group_name_too_long");
            return false;
        }

        GroupModel groupModel = new GroupModel(name, name);
        this.groupManager.save(groupModel);
        this.languageManager.sendTranslatedMessage(player, "group_created");
        return true;
    }
}