package io.github.rysefoxx.scoreboard.impl;

import io.github.rysefoxx.manager.LanguageManager;
import io.github.rysefoxx.scoreboard.AbstractScoreboard;
import io.github.rysefoxx.scoreboard.ScoreboardEntry;
import io.github.rysefoxx.scoreboard.enums.ScoreboardPredefinedValue;
import io.github.rysefoxx.scoreboard.enums.ScoreboardType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
public class DefaultScoreboard extends AbstractScoreboard {

    @Override
    public ScoreboardType getType() {
        return ScoreboardType.DEFAULT;
    }

    @Override
    public String getTitle() {
        return "PlayLegend";
    }

    @Override
    public HashMap<String, ScoreboardEntry> getLines(@NotNull Player player, @NotNull LanguageManager languageManager) {
        HashMap<String, ScoreboardEntry> lines = new HashMap<>();
        lines.put("LEGEND_INTERNAL_GROUP", new ScoreboardEntry("", 0, languageManager.getTranslatedMessage(player, "scoreboard_your_group"), 1, ScoreboardPredefinedValue.GROUP));
        return lines;
    }
}
