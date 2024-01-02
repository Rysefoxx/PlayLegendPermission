package command;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.command.impl.GroupDeleteCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * This class tests the {@link GroupDeleteCommand}.
 */
class GroupDeleteCommandTest {

    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        ServerMock server = MockBukkit.mock();
        MockBukkit.load(PlayLegendPermission.class);
        this.player = server.addPlayer();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Tests if the group was deleted.
     */
    @Test
    public void deleteGroup() {
        String groupName = UUID.randomUUID().toString().substring(0, 19);
        this.player.performCommand("group create " + groupName);
        this.player.assertSaid("The group was created.");

        this.player.performCommand("group delete " + groupName);
        this.player.assertSaid("The group has been deleted.");
    }

    /**
     * Tests if the group does not exist.
     */
    @Test
    public void deleteGroupNotFound() {
        this.player.performCommand("group delete " + UUID.randomUUID().toString().substring(0, 19));
        this.player.assertSaid("The group does not exist.");
    }

}