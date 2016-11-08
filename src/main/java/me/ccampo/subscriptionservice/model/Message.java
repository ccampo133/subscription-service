package me.ccampo.subscriptionservice.model;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * @author Chris Campo
 */
public class Message {

    public final UUID id;
    public final String type;
    public final String contents;

    public Message(@NotNull final UUID id, @NotNull final String type, @NotNull final String contents) {
        this.id = id;
        this.type = type;
        this.contents = contents;
    }

    public Message(@NotNull final String type, @NotNull final String contents) {
        this(UUID.randomUUID(), type, contents);
    }
}
