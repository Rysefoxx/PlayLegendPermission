package service;

import be.seeseemelk.mockbukkit.MockBukkit;
import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.manager.GroupManager;
import io.github.rysefoxx.manager.GroupPermissionManager;
import io.github.rysefoxx.model.GroupModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class GroupPermissionManagerTest {

    private GroupPermissionManager groupPermissionManager;
    private GroupManager groupManager;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        PlayLegendPermission plugin = MockBukkit.load(PlayLegendPermission.class);
        this.groupPermissionManager = plugin.getGroupPermissionManager();
        this.groupManager = plugin.getGroupManager();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Tests if the permission was added.
     */
    @Test
    public void addPermission() {
        String groupName = UUID.randomUUID().toString().substring(0, 5);
        GroupModel groupModel = new GroupModel(groupName, groupName + "Prefix");
        this.groupManager.save(groupModel);

        this.groupPermissionManager.addPermission("test.permission", groupModel);
        Assertions.assertTrue(this.groupPermissionManager.hasPermission("test.permission", groupModel));
    }

    /**
     * Tests if the permission was removed.
     */
    @Test
    public void removePermission() {
        String groupName = UUID.randomUUID().toString().substring(0, 5);
        GroupModel groupModel = new GroupModel(groupName, groupName + "Prefix");
        this.groupManager.save(groupModel);

        this.groupPermissionManager.addPermission("test.permission", groupModel);
        Assertions.assertTrue(this.groupPermissionManager.hasPermission("test.permission", groupModel));

        this.groupPermissionManager.removePermission("test.permission", groupModel);
        Assertions.assertFalse(this.groupPermissionManager.hasPermission("test.permission", groupModel));
    }

    /**
     * Tests that the group has permission.
     */
    @Test
    public void hasPermission() {
        String groupName = UUID.randomUUID().toString().substring(0, 5);
        GroupModel groupModel = new GroupModel(groupName, groupName + "Prefix");
        this.groupManager.save(groupModel);

        this.groupPermissionManager.addPermission("test.permission", groupModel);
        Assertions.assertTrue(this.groupPermissionManager.hasPermission("test.permission", groupModel));
    }
}