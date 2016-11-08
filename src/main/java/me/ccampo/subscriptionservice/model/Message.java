package me.ccampo.subscriptionservice.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Chris Campo
 */
public class Message {

    public final UUID id;
    public final String type;
    public final String content;

    public Message(@NotNull final UUID id, @NotNull final String type, @NotNull final String content) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.content = Objects.requireNonNull(content, "content");
    }

    public Message(@NotNull final String type, @NotNull final String content) {
        // Start with an auto-generated UUID
        this(UUID.randomUUID(), type, content);
    }
}
