package io.github.rysefoxx.command.impl;

import io.github.rysefoxx.command.GroupOperation;
import io.github.rysefoxx.manager.GroupManager;
import io.github.rysefoxx.manager.GroupMemberManager;
import io.github.rysefoxx.manager.LanguageManager;
import io.github.rysefoxx.manager.ScoreboardManager;
import io.github.rysefoxx.model.GroupMemberModel;
import io.github.rysefoxx.model.GroupModel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
@RequiredArgsConstructor
public class GroupDeleteCommand implements GroupOperation {

    private final GroupManager groupManager;
    private final GroupMemberManager groupMemberManager;
    private final LanguageManager languageManager;
    private final ScoreboardManager scoreboardManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        String name = args[1];
        Optional<GroupModel> optional = this.groupManager.findByName(name);
        if (optional.isEmpty()) {
            this.languageManager.sendTranslatedMessage(player, "group_not_found");
            return false;
        }

        GroupModel groupModel = optional.get();
        if (groupModel.getName().equalsIgnoreCase("default")) {
            this.languageManager.sendTranslatedMessage(player, "group_cant_delete_default");
            return false;
        }


        List<GroupMemberModel> members = groupModel.getMembers();
        //No for each loop because of concurrent modification
        for (int i = 0; i < members.size(); i++) {
            GroupMemberModel member = members.get(i);
            this.groupMemberManager.addToDefaultGroup(member.getUuid());
            Player target = Bukkit.getPlayer(member.getUuid());
            if (target != null) {
                this.scoreboardManager.update(target);
                this.languageManager.sendTranslatedMessage(target, "group_user_reset");
            }
        }

        this.groupManager.delete(groupModel);
        this.languageManager.sendTranslatedMessage(player, "group_deleted");
        return true;
    }
}