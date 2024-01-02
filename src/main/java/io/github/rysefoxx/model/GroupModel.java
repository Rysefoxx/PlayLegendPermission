package io.github.rysefoxx.model;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;

@Getter
public class GroupModel {

    private final String name;

    @Setter
    private String prefix;

    @Setter
    private int weight = 1;

//    private final List<GroupMemberModel> members = new ArrayList<>();
//    private final List<GroupPermissionModel> permissions = new ArrayList<>();

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
}