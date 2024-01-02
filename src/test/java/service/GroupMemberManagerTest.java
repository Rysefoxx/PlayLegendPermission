package service;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.manager.GroupManager;
import io.github.rysefoxx.manager.GroupMemberManager;
import io.github.rysefoxx.model.GroupMemberModel;
import io.github.rysefoxx.model.GroupModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

class GroupMemberManagerTest {

    private GroupMemberManager groupMemberManager;
    private GroupManager groupManager;
    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        ServerMock server = MockBukkit.mock();
        PlayLegendPermission plugin = MockBukkit.load(PlayLegendPermission.class);
        this.player = server.addPlayer();
        this.groupMemberManager = plugin.getGroupMemberManager();
        this.groupManager = plugin.getGroupManager();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Tests if the group member expire is permanent.
     */
    @Test
    public void addMemberPermanent() {
        String groupName = UUID.randomUUID().toString().substring(0, 5);
        GroupModel groupModel = new GroupModel(groupName, groupName + "Prefix");
        this.groupManager.save(groupModel);
        this.groupMemberManager.addMember(this.player.getUniqueId(), groupName, null);

        GroupMemberModel groupMemberModel = this.groupMemberManager.getGroup(this.player.getUniqueId());
        Assertions.assertNotNull(groupMemberModel);
        Assertions.assertNull(groupMemberModel.getExpiration());
    }


    /**
     * Tests if the isExpired method works.
     */
    @Test
    public void isExpired() {
        String groupName = UUID.randomUUID().toString().substring(0, 5);
        GroupModel groupModel = new GroupModel(groupName, groupName + "Prefix");
        this.groupManager.save(groupModel);

        this.groupMemberManager.addMember(this.player.getUniqueId(), groupName, LocalDateTime.now().plusSeconds(3));

        boolean inGroup = this.groupMemberManager.inGroup(this.player.getUniqueId(), groupName);
        Assertions.assertTrue(inGroup);

        try {
            Thread.sleep(3L * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        inGroup = this.groupMemberManager.inGroup(this.player.getUniqueId(), groupName);
        Assertions.assertFalse(inGroup);
    }

}