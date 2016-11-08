package me.ccampo.subscriptionservice.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import me.ccampo.subscriptionservice.exception.MessageTypeNotSupportedException;
import me.ccampo.subscriptionservice.exception.SubscriptionNotFoundException;
import me.ccampo.subscriptionservice.model.Message;
import me.ccampo.subscriptionservice.model.Subscription;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author Chris Campo
 */
@Service
public class SubscriptionService {

    private final Map<UUID, Subscription> subscriptions = new ConcurrentHashMap<>();

    @NotNull
    public Subscription createSubscription(@NotNull final String name,
            @NotNull final ImmutableSet<String> messageTypes) {
        final Subscription subscription = new Subscription(name, messageTypes);
        subscriptions.put(subscription.id, subscription);
        return subscription;
    }

    @NotNull
    public Subscription getSubscriptionById(@NotNull final UUID id) throws SubscriptionNotFoundException {
        if (!subscriptionExists(id)) {
            throw new SubscriptionNotFoundException("Subscription with ID " + id + " was not found");
        }
        return subscriptions.get(id);
    }

    public boolean subscriptionExists(@NotNull final UUID id) {
        return subscriptions.containsKey(id);
    }

    @NotNull
    public Subscription updateSubscriptionById(@NotNull final UUID id, @NotNull final Optional<String> name,
            @NotNull final ImmutableSet<String> messageTypes) {
        final Subscription current = getSubscriptionById(id);
        /*
         * Here we remove any messages whose types are no longer supported by this subscription.
         * Total judgement call here; we could have just as easily left them alone.
         */
        final ImmutableList<Message> filteredMessages = current.messages.stream()
                .filter(msg -> messageTypes.contains(msg.type.toLowerCase()))
                .collect(collectingAndThen(toList(), ImmutableList::copyOf));
        final Subscription subscription =
                new Subscription(id, name.orElse(current.name), messageTypes, filteredMessages);
        subscriptions.put(id, subscription);
        return subscription;
    }

    @NotNull
    public Message createMessageForSubscription(@NotNull final UUID id, @NotNull final String type,
            @NotNull final String contents) throws SubscriptionNotFoundException, MessageTypeNotSupportedException {
        if (!subscriptionExists(id)) {
            throw new SubscriptionNotFoundException("Subscription with ID " + id + " was not found");
        }

        final Subscription current = getSubscriptionById(id);
        if (!current.messageTypes.contains(type.toLowerCase())) {
            throw new MessageTypeNotSupportedException("Subscription does not support messages of type " + type);
        }

        final Message message = new Message(type, contents);
        final ImmutableList<Message> messages = ImmutableList.<Message>builder()
                .addAll(current.messages)
                .add(message)
                .build();
        final Subscription updated = new Subscription(current.id, current.name, current.messageTypes, messages);
        subscriptions.put(id, updated);
        return message;
    }
}
