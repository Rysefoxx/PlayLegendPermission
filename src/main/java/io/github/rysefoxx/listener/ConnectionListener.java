package io.github.rysefoxx.listener;

import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.manager.GroupMemberManager;
import io.github.rysefoxx.manager.GroupPermissionManager;
import io.github.rysefoxx.manager.LanguageManager;
import io.github.rysefoxx.manager.ScoreboardManager;
import io.github.rysefoxx.model.GroupMemberModel;
import io.github.rysefoxx.model.GroupModel;
import io.github.rysefoxx.util.CraftBukkitImplementation;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
public class ConnectionListener implements Listener {

    private final PlayLegendPermission plugin;
    private final GroupMemberManager groupMemberManager;
    private final GroupPermissionManager groupPermissionManager;
    private final LanguageManager languageManager;
    private final ScoreboardManager scoreboardManager;

    public ConnectionListener(@NotNull PlayLegendPermission plugin) {
        this.plugin = plugin;
        this.groupMemberManager = plugin.getGroupMemberManager();
        this.groupPermissionManager = plugin.getGroupPermissionManager();
        this.languageManager = plugin.getLanguageManager();
        this.scoreboardManager = plugin.getScoreboardManager();
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        if (PlayLegendPermission.isUnitTest()) return;
        Player player = event.getPlayer();

        if (!this.groupMemberManager.hasGroup(player.getUniqueId()))
            this.groupMemberManager.addToDefaultGroup(player.getUniqueId());

        GroupModel group = updatePermissionsBasedOnGroup(player);
        injectCustomPermissibleBase(player);
        createScoreboard(event, player);
        displayJoinMessage(event, player, group);
    }


    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        this.scoreboardManager.destroy(event.getPlayer());
    }

    /**
     * Injects the custom permissible base into the player
     *
     * @param player Player to inject
     */
    private void injectCustomPermissibleBase(@NotNull Player player) {
        try {
            CraftBukkitImplementation.injectEntity(player, this.groupPermissionManager, this.groupMemberManager);
        } catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not inject entity", e);
        }
    }

    /**
     * Updates the permissions based on the group
     *
     * @param player Player to update
     * @return GroupModel of the player
     */
    private @NotNull GroupModel updatePermissionsBasedOnGroup(@NotNull Player player) {
        GroupMemberModel member = this.groupMemberManager.getOrSetGroup(player.getUniqueId());
        GroupModel group = member.getGroup();
        this.groupPermissionManager.reloadPlayerPermissions(group, player, true);
        return group;
    }

    /**
     * Creates the scoreboard for the player
     *
     * @param event  PlayerJoinEvent
     * @param player Player to create the scoreboard for
     */
    private void createScoreboard(@NotNull PlayerJoinEvent event, @NotNull Player player) {
        this.scoreboardManager.create(event.getPlayer());
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.scoreboardManager.update(player), 40L);
    }

    /**
     * Displays the join message
     *
     * @param event  PlayerJoinEvent
     * @param player Player name to display
     * @param group  GroupModel of the player
     */
    private void displayJoinMessage(@NotNull PlayerJoinEvent event, @NotNull Player player, @NotNull GroupModel group) {
        String translatedJoinMessage = this.languageManager.getTranslatedMessage(player, "user_join");
        event.joinMessage(Component.text(group.getPrefix())
                .append(Component.text(" " + player.getName() + " "))
                .append(Component.text(translatedJoinMessage)));
    }
}