package me.ccampo.subscriptionservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * @author Chris Campo
 */
public class Subscription {

    public final UUID id;
    public final String name;
    public final ImmutableSet<String> messageTypes;

    @JsonIgnore
    public final ImmutableList<Message> messages;

    public Subscription(@NotNull final UUID id,
            @NotNull final String name,
            @NotNull final ImmutableSet<String> messageTypes,
            @NotNull final ImmutableList<Message> messages) {
        this.id = id;
        this.name = name;
        this.messageTypes = messageTypes;
        // Not a huge fan of logic in the constructor, but it seemed the simplest approach
        if (messages.stream().anyMatch(msg -> !messageTypes.contains(msg.type.toLowerCase()))) {
            throw new IllegalArgumentException("Must only contain messages with types defined in `messageTypes`");
        }
        this.messages = messages;
    }

    public Subscription(@NotNull final String name, @NotNull final ImmutableSet<String> messageTypes) {
        // Start with an auto-generated UUID and empty list
        this(UUID.randomUUID(), name, messageTypes, ImmutableList.of());
    }
}
