package io.github.rysefoxx.command.impl;

import io.github.rysefoxx.command.GroupOperation;
import io.github.rysefoxx.manager.GroupManager;
import io.github.rysefoxx.manager.LanguageManager;
import io.github.rysefoxx.manager.ScoreboardManager;
import io.github.rysefoxx.model.GroupModel;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
@RequiredArgsConstructor
public class GroupPrefixCommand implements GroupOperation {

    private final GroupManager groupManager;
    private final LanguageManager languageManager;
    private final ScoreboardManager scoreboardManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        Optional<GroupModel> optional = this.groupManager.findByName(args[1]);
        if (optional.isEmpty()) {
            this.languageManager.sendTranslatedMessage(player, "group_not_found");
            return false;
        }

        GroupModel groupModel = optional.get();
        groupModel.setPrefix(args[2]);
        this.groupManager.save(groupModel);

        groupModel.getMembers().forEach(member -> {
            Player target = player.getServer().getPlayer(member.getUuid());
            if (target != null) {
                this.scoreboardManager.update(target);
            }
        });

        this.languageManager.sendTranslatedMessage(player, "group_prefix_set");

        return true;
    }

}