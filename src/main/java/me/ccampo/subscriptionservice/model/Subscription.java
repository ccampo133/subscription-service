package me.ccampo.subscriptionservice.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Chris Campo
 */
public class Subscription {

    public final UUID id;
    public final String name;
    public final ImmutableSet<String> messageTypes;
    public final ImmutableList<Message> messages;

    public Subscription(@NotNull final UUID id,
            @NotNull final String name,
            @NotNull final ImmutableSet<String> messageTypes,
            @NotNull final ImmutableList<Message> messages) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.messageTypes = Objects.requireNonNull(messageTypes, "messageTypes");
        // I'm not a huge fan of logic in constructors, and it's probably not needed, but I figured it couldn't hurt.
        if (messages.stream().anyMatch(msg -> !messageTypes.contains(msg.type))) {
            throw new IllegalArgumentException("Must only contain messages with types defined in `messageTypes`");
        }
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    public Subscription(@NotNull final String name, @NotNull final ImmutableSet<String> messageTypes) {
        // Start with an auto-generated UUID and empty list
        this(UUID.randomUUID(), name, messageTypes, ImmutableList.of());
    }

    public boolean supportsType(@NotNull final String type) {
        return messageTypes.contains(type);
    }
}
