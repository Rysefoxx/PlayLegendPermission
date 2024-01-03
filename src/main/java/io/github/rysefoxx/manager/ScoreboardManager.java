package io.github.rysefoxx.manager;

import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.model.GroupMemberModel;
import io.github.rysefoxx.scoreboard.AbstractScoreboard;
import io.github.rysefoxx.scoreboard.ScoreboardEntry;
import io.github.rysefoxx.scoreboard.enums.ScoreboardPredefinedValue;
import io.github.rysefoxx.scoreboard.impl.DefaultScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
public class ScoreboardManager {

    private final HashMap<UUID, AbstractScoreboard> playerScoreboard = new HashMap<>();
    private final HashMap<UUID, String> playerTeams = new HashMap<>();

    private final GroupMemberManager groupMemberManager;
    private final LanguageManager languageManager;

    public ScoreboardManager(@NotNull PlayLegendPermission plugin) {
        this.groupMemberManager = plugin.getGroupMemberManager();
        this.languageManager = plugin.getLanguageManager();
    }

    /**
     * Creates a scoreboard for a player. When the player already has a scoreboard, nothing will happen.
     *
     * @param player The player to create the scoreboard for.
     */
    public void create(@NotNull Player player) {
        if (hasScoreboard(player)) return;

        String teamName = generateTeamName(player);
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.playerScoreboard.put(player.getUniqueId(), new DefaultScoreboard());
        this.playerTeams.put(player.getUniqueId(), teamName);

        player.setScoreboard(scoreboard);
        createSidebar(player);

        Bukkit.getOnlinePlayers().forEach(this::updateTeamForAllPlayers);
    }


    /**
     * Updates the team for all players.
     *
     * @param player The player to update the team for.
     */
    private void updateTeamForAllPlayers(@NotNull Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = onlinePlayer.getScoreboard();

            String teamName = this.playerTeams.get(player.getUniqueId());
            if (teamName == null) continue;

            Team team = scoreboard.getTeam(teamName);
            if (team != null) team.unregister();

            createTeamForPlayer(player, scoreboard);
        }
    }

    /**
     * Creates a team for a player.
     *
     * @param player     The player to create the team for.
     * @param scoreboard The scoreboard to create the team for.
     */
    private void createTeamForPlayer(@NotNull Player player, @NotNull Scoreboard scoreboard) {
        String teamName = this.playerTeams.get(player.getUniqueId());
        Team team = scoreboard.registerNewTeam(teamName);

        Component prefix = getTabPrefix(player);
        team.prefix(prefix);
        team.addPlayer(player);
    }


    /**
     * Gets the prefix for the tab.
     *
     * @param player The player to get the prefix for.
     * @return The prefix for the tab.
     */
    private @NotNull Component getTabPrefix(@NotNull Player player) {
        Component prefix = getPrefix(player);
        return prefix.append(Component.text(" - ", NamedTextColor.GRAY));
    }


    /**
     * Gets the prefix for a player.
     *
     * @param player The player to get the prefix for.
     * @return The prefix for the player.
     */
    private @NotNull Component getPrefix(@NotNull Player player) {
        GroupMemberModel groupMemberModel = this.groupMemberManager.getOrSetGroup(player.getUniqueId());
        return Component.text(groupMemberModel.getGroup().getPrefix());
    }


    /**
     * Creates the sidebar for a player.
     *
     * @param player The player to create the sidebar for.
     */
    private void createSidebar(@NotNull Player player) {
        AbstractScoreboard abstractScoreboard = this.playerScoreboard.get(player.getUniqueId());
        Scoreboard bukkitScoreboard = player.getScoreboard();

        Objective sidebar = bukkitScoreboard.registerNewObjective("Sidebar", Criteria.DUMMY, Component.text(abstractScoreboard.getTitle()));
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (Map.Entry<String, ScoreboardEntry> entry : abstractScoreboard.getLines(player, this.languageManager).entrySet()) {
            ScoreboardEntry scoreboardEntry = entry.getValue();

            Team team = bukkitScoreboard.registerNewTeam(entry.getKey());
            if (scoreboardEntry.entry() != null)
                team.addEntry(scoreboardEntry.entry());

            sidebar.getScore(scoreboardEntry.display()).setScore(scoreboardEntry.displaySlot());

            if (scoreboardEntry.entry() != null)
                sidebar.getScore(scoreboardEntry.entry()).setScore(scoreboardEntry.entrySlot());
        }
        updateSidebar(player);
    }

    /**
     * Destroys the scoreboard for a player. When the player does not have a scoreboard, nothing will happen.
     *
     * @param player The player to destroy the scoreboard for.
     */
    public void destroy(@NotNull Player player) {
        this.playerScoreboard.remove(player.getUniqueId());
        this.playerTeams.remove(player.getUniqueId());
    }

    /**
     * Updates the scoreboard for a player.
     *
     * @param player The player to update the scoreboard for.
     */
    public void update(@NotNull Player player) {
        if (PlayLegendPermission.isUnitTest()) return;

        updateSidebar(player);
        updateTeamForAllPlayers(player);
    }

    /**
     * Updates the sidebar for a player. When the player does not have a scoreboard, it will be created.
     *
     * @param player The player to update the sidebar for.
     */
    private void updateSidebar(@NotNull Player player) {
        if (!hasScoreboard(player)) {
            create(player);
        }

        AbstractScoreboard abstractScoreboard = this.playerScoreboard.get(player.getUniqueId());
        Scoreboard scoreboard = player.getScoreboard();
        Objective sidebar = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (sidebar == null) {
            return;
        }

        for (Map.Entry<String, ScoreboardEntry> entry : abstractScoreboard.getLines(player, this.languageManager).entrySet()) {
            ScoreboardEntry scoreboardEntry = entry.getValue();

            Team team = scoreboard.getTeam(entry.getKey());
            if (team == null || scoreboardEntry.predefinedValue() == null) {
                continue;
            }

            if (scoreboardEntry.predefinedValue() == ScoreboardPredefinedValue.GROUP) {
                Component prefix = getPrefix(player);
                team.suffix(prefix);
            }
        }
    }


    /**
     * Generates a random team name.
     *
     * @param player The player to generate the team name for.
     * @return The generated team name.
     */
    private @NotNull String generateTeamName(@NotNull Player player) {
        GroupMemberModel groupMemberModel = this.groupMemberManager.getOrSetGroup(player.getUniqueId());
        return groupMemberModel.getGroup().getWeight() + UUID.randomUUID().toString();
    }


    /**
     * Checks if a player has a scoreboard.
     *
     * @param player The player to check.
     * @return {@code true} if the player has a scoreboard, otherwise {@code false}.
     */
    private boolean hasScoreboard(@NotNull Player player) {
        return this.playerScoreboard.containsKey(player.getUniqueId());
    }
}
