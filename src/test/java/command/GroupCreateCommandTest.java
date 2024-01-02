package command;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.command.impl.GroupCreateCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * This class tests the {@link GroupCreateCommand}.
 */
public class GroupCreateCommandTest {

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
     * Tests if the group was created.
     */
    @Test
    public void createGroup() {
        this.player.performCommand("group create " + UUID.randomUUID().toString().substring(0, 19));
        this.player.assertSaid("The group was created.");
    }

    /**
     * Tests if the group name is too long.
     */
    @Test
    public void createGroupTooLong() {
        this.player.performCommand("group create " + UUID.randomUUID());
        this.player.assertSaid("The group name is too long.");
    }

    /**
     * Tests if the group already exists.
     */
    @Test
    public void createGroupExists() {
        String groupToCreate = UUID.randomUUID().toString().substring(0, 19);
        this.player.performCommand("group create " + groupToCreate);
        this.player.assertSaid("The group was created.");

        this.player.performCommand("group create " + groupToCreate);
        this.player.assertSaid("The group already exists.");
    }
}
