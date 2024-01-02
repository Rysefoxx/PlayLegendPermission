package command;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.command.impl.GroupUserCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * This class tests the {@link GroupUserCommand}.
 */
class GroupUserCommandTest {

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
     * Tests if the group does not exist. This happens when add is used.
     */
    @Test
    public void addGroupNotFound() {
        this.player.performCommand("group user add " + UUID.randomUUID().toString().substring(0, 19) + " " + this.player.getName());
        this.player.assertSaid("The group does not exist.");
    }

    /**
     * Tests if the user has been added to the group.
     */
    @Test
    public void addGroupUser() {
        String groupName = UUID.randomUUID().toString().substring(0, 19);
        this.player.performCommand("group create " + groupName);
        this.player.assertSaid("The group was created.");

        this.player.performCommand("group user add " + groupName + " " + this.player.getName());
        this.player.assertSaid("The user has been added to the group.");
    }

    /**
     * Tests if the user is already in the group.
     */
    @Test
    public void addGroupUserInGroup() {
        String groupName = UUID.randomUUID().toString().substring(0, 19);
        this.player.performCommand("group create " + groupName);
        this.player.assertSaid("The group was created.");

        this.player.performCommand("group user add " + groupName + " " + this.player.getName());
        this.player.assertSaid("The user has been added to the group.");

        this.player.performCommand("group user add " + groupName + " " + this.player.getName());
        this.player.assertSaid("The user is already in the group.");
    }

    /**
     * Tests if the user has been removed from the group.
     */
    @Test
    public void removeGroupUser() {
        String groupName = UUID.randomUUID().toString().substring(0, 19);
        this.player.performCommand("group create " + groupName);
        this.player.assertSaid("The group was created.");

        this.player.performCommand("group user add " + groupName + " " + this.player.getName());
        this.player.assertSaid("The user has been added to the group.");

        this.player.performCommand("group user remove " + groupName + " " + this.player.getName());
        this.player.assertSaid("The user has been removed from the group.");
    }

    /**
     * Tests if the user is not in the group.
     */
    @Test
    public void removeGroupUserNotInGroup() {
        String groupName = UUID.randomUUID().toString().substring(0, 19);
        this.player.performCommand("group create " + groupName);
        this.player.assertSaid("The group was created.");

        this.player.performCommand("group user remove " + groupName + " " + this.player.getName());
        this.player.assertSaid("The user is not in the group.");
    }
}