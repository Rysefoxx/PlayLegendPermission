package io.github.rysefoxx.listener;

import io.github.rysefoxx.manager.GroupMemberManager;
import io.github.rysefoxx.model.GroupMemberModel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
@RequiredArgsConstructor
public class SignListener implements Listener {

    private static final PlainTextComponentSerializer SERIALIZER = PlainTextComponentSerializer.plainText();
    private final GroupMemberManager groupMemberManager;

    @EventHandler
    public void onSignChange(@NotNull SignChangeEvent event) {
        Player player = event.getPlayer();
        List<Component> lines = event.lines();

        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            if (line == null) continue;

            String text = SERIALIZER.serialize(line);
            if (text.startsWith("%NAME%")) {
                event.line(i, Component.text(text.replace("%NAME%", player.getName())));
                continue;
            }

            if (text.startsWith("%GROUP%")) {
                GroupMemberModel member = this.groupMemberManager.getOrSetGroup(player.getUniqueId());
                event.line(i, Component.text(text.replace("%GROUP%", member.getGroup().getName())));
            }
        }
    }
}