package me.ccampo.subscriptionservice.model.resource;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.ImmutableList;
import me.ccampo.subscriptionservice.model.Subscription;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author Chris Campo
 */
public class SubscriptionResource {

    @JsonUnwrapped
    public final Subscription subscription;
    public final Map<String, Long> messageCountsByType;

    // Private constructor because we only want to be able to create these via the static factory method below
    private SubscriptionResource(@NotNull final Subscription subscription,
            @NotNull final Map<String, Long> messageCountsByType) {
        this.subscription = subscription;
        this.messageCountsByType = messageCountsByType;
    }

    @NotNull
    @Contract(pure = true)
    public static SubscriptionResource fromSubscription(@NotNull final Subscription subscription) {
        final Map<String, Long> messageCountsByType = subscription.messages.stream()
                .collect(Collectors.groupingBy(msg -> msg.type, Collectors.counting()));
        // The stream collector just returns an empty map if there are zero
        // messages for certain types, so we have to populate those manually.
        subscription.messageTypes.forEach(type -> messageCountsByType.putIfAbsent(type, 0L));
        return new SubscriptionResource(subscription, messageCountsByType);
    }

    @NotNull
    public static ImmutableList<SubscriptionResource> fromSubscriptions(
            @NotNull final ImmutableList<Subscription> subscriptions) {
        return subscriptions.stream()
                .map(SubscriptionResource::fromSubscription)
                .collect(collectingAndThen(toList(), ImmutableList::copyOf));
    }
}
