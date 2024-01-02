package io.github.rysefoxx.model;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
@Getter
public class GroupPermissionModel {

    private long id;
    private final String permission;
    private final GroupModel group;

    /**
     * Creates a new group permission.
     *
     * @param permission The permission to add
     * @param group      The group to add the permission to
     */
    public GroupPermissionModel(@NotNull String permission, @NotNull GroupModel group) {
        this.permission = permission;
        this.group = group;
    }

    /**
     * Creates a new group permission. This constructor is used when loading all group permissions from the database.
     *
     * @param id         unique id
     * @param permission The permission to add
     * @param group      The group to add the permission to
     */
    public GroupPermissionModel(@Nonnegative long id, @NotNull String permission, @NotNull GroupModel group) {
        this.id = id;
        this.permission = permission;
        this.group = group;
    }
}