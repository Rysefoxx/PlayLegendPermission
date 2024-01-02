package io.github.rysefoxx.command.impl;

import io.github.rysefoxx.command.GroupOperation;
import io.github.rysefoxx.manager.GroupMemberManager;
import io.github.rysefoxx.manager.LanguageManager;
import io.github.rysefoxx.model.GroupMemberModel;
import io.github.rysefoxx.model.GroupModel;
import io.github.rysefoxx.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
@RequiredArgsConstructor
public class GroupInformationCommand implements GroupOperation {

    private final GroupMemberManager groupMemberManager;
    private final LanguageManager languageManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        GroupMemberModel member = this.groupMemberManager.getOrSetGroup(player.getUniqueId());

        GroupModel group = member.getGroup();
        LocalDateTime groupExpiration = member.getExpiration();
        String translatedPermanent = this.languageManager.getTranslatedMessage(player, "permanent");
        String translatedMessage = this.languageManager.getTranslatedMessage(player, "group_info",
                group.getName(),
                group.getPrefix(),
                groupExpiration == null ? translatedPermanent : TimeUtil.toReadableString(groupExpiration));

        player.sendMessage(translatedMessage);
        return true;
    }
}