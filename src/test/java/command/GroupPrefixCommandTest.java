package command;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.command.impl.GroupPrefixCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * This class tests the {@link GroupPrefixCommand}.
 */
class GroupPrefixCommandTest {

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
     * Tests if the group does not exist.
     */
    @Test
    public void modifyPrefixGroupNotFound() {
        this.player.performCommand("group prefix " + UUID.randomUUID().toString().substring(0, 19) + " NeuerPrefix");
        this.player.assertSaid("The group does not exist.");
    }

    /**
     * Tests if the group prefix has been set.
     */
    @Test
    public void modifyPrefixGroup() {
        String groupName = UUID.randomUUID().toString().substring(0, 19);

        this.player.performCommand("group create " + groupName);
        this.player.assertSaid("The group was created.");

        this.player.performCommand("group prefix " + groupName + " NeuerPrefix");
        this.player.assertSaid("The group prefix has been set.");
    }
}