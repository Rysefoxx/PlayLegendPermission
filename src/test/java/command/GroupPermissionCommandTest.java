package command;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.command.impl.GroupPermissionCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * This class tests the {@link GroupPermissionCommand}.
 */
class GroupPermissionCommandTest {

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

    /*+
     * Tests if the group does not exist. This happens when add is used.
     */
    @Test
    public void addGroupPermissionNotFound() {
        String groupName = UUID.randomUUID().toString().substring(0, 19);
        this.player.performCommand("group permission add " + groupName + " test.permission");
        this.player.assertSaid("The group does not exist.");
    }

    /**
     * Tests if the group does not exist. This happens when remove is used.
     */
    @Test
    public void removeGroupPermissionNotFound() {
        this.player.performCommand("group permission remove " + UUID.randomUUID().toString().substring(0, 19) + " test.permission");
        this.player.assertSaid("The group does not exist.");
    }

    /**
     * Tests if the permission has been added to the group.
     */
    @Test
    public void addGroupPermission() {
        String groupName = UUID.randomUUID().toString().substring(0, 19);
        this.player.performCommand("group create " + groupName);
        this.player.assertSaid("The group was created.");

        this.player.performCommand("group permission add " + groupName + " test.permission");
        this.player.assertSaid("The permission has been added to the group.");
    }

    /**
     * Tests if the group already has the permission.
     */
    @Test
    public void addGroupPermissionAlreadyHasPermission() {
        String groupName = UUID.randomUUID().toString().substring(0, 19);
        this.player.performCommand("group create " + groupName);
        this.player.assertSaid("The group was created.");

        this.player.performCommand("group permission add " + groupName + " test.permission");
        this.player.assertSaid("The permission has been added to the group.");

        this.player.performCommand("group permission add " + groupName + " test.permission");
        this.player.assertSaid("The group has already this permission.");
    }

    /**
     * Tests if the permission has been removed from the group.
     */
    @Test
    public void removeGroupPermission() {
        String groupName = UUID.randomUUID().toString().substring(0, 19);
        this.player.performCommand("group create " + groupName);
        this.player.assertSaid("The group was created.");

        this.player.performCommand("group permission add " + groupName + " test.permission");
        this.player.assertSaid("The permission has been added to the group.");

        this.player.performCommand("group permission remove " + groupName + " test.permission");
        this.player.assertSaid("The permission has been removed from the group.");
    }

    /**
     * Tests if the group does not have the permission.
     */
    @Test
    public void removeGroupPermissionAlreadyHasPermission() {
        String groupName = UUID.randomUUID().toString().substring(0, 19);
        this.player.performCommand("group create " + groupName);
        this.player.assertSaid("The group was created.");

        this.player.performCommand("group permission add " + groupName + " test.permission");
        this.player.assertSaid("The permission has been added to the group.");

        this.player.performCommand("group permission remove " + groupName + " test.permission");
        this.player.assertSaid("The permission has been removed from the group.");

        this.player.performCommand("group permission remove " + groupName + " test.permission");
        this.player.assertSaid("The group does not have this permission.");
    }

}