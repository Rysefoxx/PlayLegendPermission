package service;

import be.seeseemelk.mockbukkit.MockBukkit;
import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.manager.GroupManager;
import io.github.rysefoxx.model.GroupModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

class GroupManagerTest {

    private GroupManager groupService;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        PlayLegendPermission plugin = MockBukkit.load(PlayLegendPermission.class);
        this.groupService = plugin.getGroupManager();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Tests if the group get saved in the database.
     */
    @Test
    public void save() {
        String groupName = UUID.randomUUID().toString().substring(0, 5);
        GroupModel groupModel = new GroupModel(groupName, groupName + "Prefix");
        this.groupService.save(groupModel);

        Optional<GroupModel> optional = this.groupService.findByName(groupName);
        Assertions.assertTrue(optional.isPresent());
    }

    /**
     * Tests if the group get deleted from the database.
     */
    @Test
    public void delete() {
        String groupName = UUID.randomUUID().toString().substring(0, 5);
        GroupModel groupModel = new GroupModel(groupName, groupName + "Prefix");
        this.groupService.save(groupModel);

        Optional<GroupModel> optional = this.groupService.findByName(groupName);
        Assertions.assertTrue(optional.isPresent());

        this.groupService.delete(groupModel);

        optional = this.groupService.findByName(groupName);
        Assertions.assertFalse(optional.isPresent());
    }

    /**
     * Tests if the group exists in the database.
     */
    @Test
    public void exists() {
        boolean exists = this.groupService.findByName(UUID.randomUUID().toString().substring(0, 19)).isPresent();
        Assertions.assertFalse(exists);

        String groupName = UUID.randomUUID().toString().substring(0, 5);
        GroupModel groupModel = new GroupModel(groupName, groupName + "Prefix");
        this.groupService.save(groupModel);

        exists = this.groupService.findByName(groupName).isPresent();
        Assertions.assertTrue(exists);
    }

}