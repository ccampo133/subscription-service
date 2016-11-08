package me.ccampo.subscriptionservice.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import me.ccampo.subscriptionservice.exception.SubscriptionNotFoundException;
import me.ccampo.subscriptionservice.model.Message;
import me.ccampo.subscriptionservice.model.Subscription;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author Chris Campo
 */
@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final Map<UUID, Subscription> subscriptions;

    public SubscriptionService() {
        subscriptions = new ConcurrentHashMap<>();
    }

    // Primarily used for testing
    protected SubscriptionService(@NotNull final Map<UUID, Subscription> subscriptions) {
        this.subscriptions = Objects.requireNonNull(subscriptions, "subscriptions");
    }

    @NotNull
    public Subscription createSubscription(@NotNull final String name,
            @NotNull final ImmutableSet<String> messageTypes) {
        log.info("Creating subscription with name = {} and messageTypes = {}", name, messageTypes);
        final Subscription subscription = new Subscription(name, messageTypes);
        subscriptions.put(subscription.id, subscription);
        return subscription;
    }

    @NotNull
    public Subscription getSubscriptionById(@NotNull final UUID id) throws SubscriptionNotFoundException {
        if (!subscriptionExists(id)) {
            log.info("Subscription with ID {} was not found", id);
            throw new SubscriptionNotFoundException("Subscription with ID " + id + " was not found");
        }
        return subscriptions.get(id);
    }

    public boolean subscriptionExists(@NotNull final UUID id) {
        return subscriptions.containsKey(id);
    }

    @NotNull
    public Subscription updateSubscriptionById(@NotNull final UUID id, @NotNull final Optional<String> name,
            @NotNull final Optional<ImmutableSet<String>> messageTypes) {
        final Subscription current = getSubscriptionById(id);
         // Here we remove any messages whose types are no longer supported by this subscription.
         // This is a complete judgement call; we could have just as easily left them alone.
        final ImmutableList<Message> filteredMessages = messageTypes
                .map(types -> filterMessages(current.messages, types))
                .orElse(current.messages);
        final Subscription updated =
                new Subscription(id, name.orElse(current.name), messageTypes.orElse(current.messageTypes),
                        filteredMessages);
        subscriptions.put(id, updated);
        return updated;
    }

    @NotNull
    @Contract(pure = true)
    private static ImmutableList<Message> filterMessages(@NotNull final ImmutableList<Message> messages,
            @NotNull final ImmutableSet<String> messageTypes) {
        return messages.stream()
                .filter(msg -> messageTypes.contains(msg.type))
                .collect(collectingAndThen(toList(), ImmutableList::copyOf));
    }

    public void sendMessageToSupportingSubscriptions(@NotNull final Message message) {
        for (final Map.Entry<UUID, Subscription> entry : subscriptions.entrySet()) {
            final Subscription current = entry.getValue();
            if (current.supportsType(message.type)) {
                log.info("Sending message to subscription {}", current.id);
                final ImmutableList<Message> messages = ImmutableList.<Message>builder()
                        .addAll(current.messages)
                        .add(message)
                        .build();
                final Subscription updated = new Subscription(current.id, current.name, current.messageTypes, messages);
                entry.setValue(updated);
            }
        }
    }

    @NotNull
    public ImmutableList<Subscription> getSubscriptions() {
        return ImmutableList.copyOf(subscriptions.values());
    }
}
