package command;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.github.rysefoxx.PlayLegendPermission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class GroupWeightCommandTest {

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
    public void modifyGroupWeightGroupNotFound() {
        this.player.performCommand("group weight " + UUID.randomUUID().toString().substring(0, 19) + " 1");
        this.player.assertSaid("The group does not exist.");
    }

    /**
     * Tests if the weight is not an integer.
     */
    @Test
    public void modifyGroupWeightNotAInteger() {
        String groupName = UUID.randomUUID().toString().substring(0, 19);
        this.player.performCommand("group create " + groupName);
        this.player.assertSaid("The group was created.");

        this.player.performCommand("group weight " + groupName + " DasIstKeineZahl");
        this.player.assertSaid("The weight must be an integer.");
    }

    /**
     * Tests if the weight has been set.
     */
    @Test
    public void modifyGroupWeight() {
        String groupName = UUID.randomUUID().toString().substring(0, 19);
        this.player.performCommand("group create " + groupName);
        this.player.assertSaid("The group was created.");

        this.player.performCommand("group weight " + groupName + " 50");
        this.player.assertSaid("The group weight has been set.");
    }
}