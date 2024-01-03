package io.github.rysefoxx.model;

import io.github.rysefoxx.util.TimeUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnegative;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
@Getter
public class GroupMemberModel {

    private long id;
    private final UUID uuid;
    private final LocalDateTime expiration;
    private final GroupModel group;

    /**
     * Creates a new group member. The expiration can be null. If the expiration is null, the group member will never expire. <br>
     * This constructor is used when the user command is executed.
     *
     * @param uuid       UUID of the player
     * @param expiration Expiration of the group member
     * @param group      Group of the group member
     */
    public GroupMemberModel(@NotNull UUID uuid, @Nullable LocalDateTime expiration, @NotNull GroupModel group) {
        this.uuid = uuid;
        this.expiration = expiration;
        this.group = group;
    }

    /**
     * Creates a new group member. The expiration can be null. If the expiration is null, the group member will never expire. <br>
     * This constructor is used when loading all group members from the database.
     *
     * @param id         unique id
     * @param uuid       UUID of the player
     * @param expiration Expiration of the group member
     * @param group      Group of the group member
     */
    public GroupMemberModel(@Nonnegative long id, @NotNull UUID uuid, @NotNull LocalDateTime expiration, @NotNull GroupModel group) {
        this.id = id;
        this.uuid = uuid;
        this.expiration = expiration;
        this.group = group;
    }

    @Override
    public String toString() {
        return "GroupMemberModel{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", expiration=" + TimeUtil.toReadableString(expiration) +
                '}';
    }
}