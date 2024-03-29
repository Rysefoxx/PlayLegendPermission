package io.github.rysefoxx.model;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
@Getter
@Setter
public class GroupModel {

    private final String name;

    private String prefix;
    private int weight = 1;
    private List<GroupMemberModel> members = new ArrayList<>();
    private List<GroupPermissionModel> permissions = new ArrayList<>();

    /**
     * Creates a new GroupModel with no members and no permissions. This constructor is called when using the create command.
     *
     * @param name   The name of the group
     * @param prefix The prefix of the group
     */
    public GroupModel(@NotNull String name, @NotNull String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    /**
     * Creates a new GroupModel with no members and no permissions. This constructor is called when loading all groups from the database.
     *
     * @param name   The name of the group
     * @param prefix The prefix of the group
     * @param weight The weight of the group
     */
    public GroupModel(@NotNull String name, @NotNull String prefix, @Nonnegative int weight) {
        this.name = name;
        this.prefix = prefix;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "GroupModel{" +
                "name='" + name + '\'' +
                ", prefix='" + prefix + '\'' +
                ", weight=" + weight +
                ", members=" + formatMembers() +
                ", permissions=" + formatPermissions() +
                '}';
    }

    /**
     * Formats the permissions of the group to a readable string.
     *
     * @return The formatted permissions
     */
    private @NotNull String formatPermissions() {
        StringBuilder permissions = new StringBuilder();
        for (GroupPermissionModel permission : this.permissions) {
            permissions.append(permission.getPermission()).append(", ");
        }
        return permissions.toString();
    }

    /**
     * Formats the members of the group to a readable string.
     *
     * @return The formatted members
     */
    private @NotNull String formatMembers() {
        StringBuilder members = new StringBuilder();
        for (GroupMemberModel member : this.members) {
            members.append(member.toString()).append(", ");
        }
        return members.toString();
    }
}