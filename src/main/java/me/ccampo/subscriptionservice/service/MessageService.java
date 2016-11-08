package me.ccampo.subscriptionservice.service;

import com.google.common.collect.ImmutableList;
import me.ccampo.subscriptionservice.model.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Chris Campo
 */
@Service
public class MessageService {

    private final Queue<Message> messages;
    private final SubscriptionService subscriptionService;

    // Primarily used for testing
    protected MessageService(@NotNull final Queue<Message> messages,
            @NotNull final SubscriptionService subscriptionService) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.subscriptionService = Objects.requireNonNull(subscriptionService, "subscriptionService");
    }

    @Autowired
    public MessageService(@NotNull final SubscriptionService subscriptionService) {
        this(new ConcurrentLinkedQueue<>(), subscriptionService);
    }

    @NotNull
    public Message createMessage(@NotNull final String type, @NotNull final String contents) {
        final Message message = new Message(type, contents);
        messages.offer(message);
        subscriptionService.sendMessageToSupportingSubscriptions(message);
        return message;
    }

    /*
     * Note: this is weakly consistent in concurrent environments. We're only
     * guaranteed to get the collection of messages as they exist at the time
     * of the `toArray` call. If any elements are added (or removed) during
     * the processing of `toArray` by another thread, they will not be
     * reflected in this output.
     *
     * It's nothing to worry about in most cases, but worth noting just for
     * completeness.
     */
    @NotNull
    public List<Message> getMessages() {
        return ImmutableList.copyOf(messages.toArray(new Message[0]));
    }
}
