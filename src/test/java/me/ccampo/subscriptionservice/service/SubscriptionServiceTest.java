package me.ccampo.subscriptionservice.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import me.ccampo.subscriptionservice.exception.SubscriptionNotFoundException;
import me.ccampo.subscriptionservice.model.Message;
import me.ccampo.subscriptionservice.model.Subscription;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author Chris Campo
 */
public class SubscriptionServiceTest {

    private SubscriptionService service;

    private Map<UUID, Subscription> subscriptions;

    @Before
    public void setUp() {
        subscriptions = new HashMap<>();
        service = new SubscriptionService(subscriptions);
    }

    @Test
    public void subscriptionIsSuccessfullyCreated() {
        final String name = "foo";
        final ImmutableSet<String> types = ImmutableSet.of("type1", "type2");
        final Subscription result = service.createSubscription(name, types);
        assertThat(result.messageTypes).containsExactlyInAnyOrder(types.toArray(new String[0]));
        assertThat(result.name).isEqualTo(name);
        assertThat(result.id).isNotNull();
        assertThat(result.messages).isEmpty();
        assertThat(subscriptions).containsOnlyKeys(result.id);

        final Subscription saved = subscriptions.get(result.id);
        assertThat(saved).isEqualToComparingFieldByField(result);
    }

    @Test
    public void subscriptionIsSuccessfullyRetrievedById() {
        final UUID id = UUID.randomUUID();
        final String name = "foo";
        final ImmutableSet<String> types = ImmutableSet.of("type1", "type2");
        final ImmutableList<Message> messages = ImmutableList.of();
        final Subscription subscription = new Subscription(id, name, types, messages);
        subscriptions.put(id, subscription);

        final Subscription result = service.getSubscriptionById(id);
        assertThat(result).isEqualToComparingFieldByField(subscription);
    }

    @Test(expected = SubscriptionNotFoundException.class)
    public void subscriptionIsNotFound() {
        assertThat(subscriptions).isEmpty();
        service.getSubscriptionById(UUID.randomUUID());
    }

    @Test
    public void testSubscriptionExists() {
        final UUID id = UUID.randomUUID();
        final String name = "foo";
        final ImmutableSet<String> types = ImmutableSet.of("type1", "type2");
        final ImmutableList<Message> messages = ImmutableList.of();
        final Subscription subscription = new Subscription(id, name, types, messages);
        subscriptions.put(id, subscription);
        assertThat(service.subscriptionExists(id)).isTrue();
    }

    @Test
    public void testSubscriptionDoesNotExist() {
        final UUID id = UUID.randomUUID();
        assertThat(subscriptions).isEmpty();
        assertThat(service.subscriptionExists(id)).isFalse();
    }

    @Test
    public void testMessagesAreFilteredProperly() {
        final Message msg1 = new Message("t1", "a");
        final Message msg2 = new Message("t2", "b");
        final ImmutableList<Message> messages = ImmutableList.of(msg1, msg2);
        final ImmutableSet<String> types = ImmutableSet.of("t1");
        final ImmutableList<Message> result = SubscriptionService.filterMessages(messages, types);
        assertThat(result).containsOnly(msg1);
    }

    @Test
    public void updateSubscriptionName() {
        final UUID id = UUID.randomUUID();
        final String name = "foo";
        final ImmutableSet<String> types = ImmutableSet.of("type1", "type2");
        final ImmutableList<Message> messages = ImmutableList.of();
        final Subscription subscription = new Subscription(id, name, types, messages);
        subscriptions.put(id, subscription);
        final Subscription result = service.updateSubscriptionById(id, Optional.of("bar"), Optional.empty());
        assertThat(result).isEqualToIgnoringGivenFields(subscription, "name");
        assertThat(result.name).isEqualTo("bar");
    }

    @Test
    public void updateSubscriptionMessageTypes() {
        final UUID id = UUID.randomUUID();
        final String name = "foo";
        final ImmutableSet<String> types = ImmutableSet.of("type1", "type2");
        final ImmutableList<Message> messages = ImmutableList.of();
        final Subscription subscription = new Subscription(id, name, types, messages);
        subscriptions.put(id, subscription);
        final Subscription result =
                service.updateSubscriptionById(id, Optional.empty(), Optional.of(ImmutableSet.of("t3")));
        assertThat(result).isEqualToIgnoringGivenFields(subscription, "messageTypes");
        assertThat(result.messageTypes).containsOnly("t3");
    }

    @Test
    public void updateSubscriptionNameAndMessageTypes() {
        final UUID id = UUID.randomUUID();
        final String name = "foo";
        final ImmutableSet<String> types = ImmutableSet.of("type1", "type2");
        final ImmutableList<Message> messages = ImmutableList.of();
        final Subscription subscription = new Subscription(id, name, types, messages);
        subscriptions.put(id, subscription);
        final Subscription result =
                service.updateSubscriptionById(id, Optional.of("bar"), Optional.of(ImmutableSet.of("t3")));
        assertThat(result).isEqualToIgnoringGivenFields(subscription, "name", "messageTypes");
        assertThat(result.name).isEqualTo("bar");
        assertThat(result.messageTypes).containsOnly("t3");
    }

    @Test(expected = SubscriptionNotFoundException.class)
    public void updateSubscriptionFailsForNonExistingSubscription() {
        service.updateSubscriptionById(UUID.randomUUID(), Optional.of("bar"), Optional.of(ImmutableSet.of("t3")));
    }

    @Test
    public void messageIsSentToMultipleSubscriptions() {
        final UUID id1 = UUID.randomUUID();
        final String name1 = "foo";
        final ImmutableSet<String> types1 = ImmutableSet.of("t1");
        final ImmutableList<Message> messages1 = ImmutableList.of();
        final Subscription sub1 = new Subscription(id1, name1, types1, messages1);
        subscriptions.put(id1, sub1);

        final UUID id2 = UUID.randomUUID();
        final String name2 = "bar";
        final ImmutableSet<String> types2 = ImmutableSet.of("t2");
        final ImmutableList<Message> messages2 = ImmutableList.of();
        final Subscription sub2 = new Subscription(id2, name2, types2, messages2);
        subscriptions.put(id2, sub2);

        final UUID id3 = UUID.randomUUID();
        final String name3 = "baz";
        final ImmutableSet<String> types3 = ImmutableSet.of("t3");
        final ImmutableList<Message> messages3 = ImmutableList.of();
        final Subscription sub3 = new Subscription(id3, name3, types3, messages3);
        subscriptions.put(id3, sub3);

        final Message msg1 = new Message("t1", "a");
        final Message msg2 = new Message("t2", "b");
        final Message msg3 = new Message("t9", "c");

        service.sendMessageToSupportingSubscriptions(msg1);
        service.sendMessageToSupportingSubscriptions(msg2);
        service.sendMessageToSupportingSubscriptions(msg3);

        final Subscription res1 = subscriptions.get(id1);
        final Subscription res2 = subscriptions.get(id2);
        final Subscription res3 = subscriptions.get(id3);
        assertThat(res1.messages).containsOnly(msg1);
        assertThat(res2.messages).containsOnly(msg2);
        assertThat(res3.messages).isEmpty();
    }

    @Test
    public void testGetAllSubscriptions() {
        final UUID id1 = UUID.randomUUID();
        final String name1 = "foo";
        final ImmutableSet<String> types1 = ImmutableSet.of("t1");
        final ImmutableList<Message> messages1 = ImmutableList.of();
        final Subscription sub1 = new Subscription(id1, name1, types1, messages1);
        subscriptions.put(id1, sub1);

        final UUID id2 = UUID.randomUUID();
        final String name2 = "bar";
        final ImmutableSet<String> types2 = ImmutableSet.of("t2");
        final ImmutableList<Message> messages2 = ImmutableList.of();
        final Subscription sub2 = new Subscription(id2, name2, types2, messages2);
        subscriptions.put(id2, sub2);

        final UUID id3 = UUID.randomUUID();
        final String name3 = "baz";
        final ImmutableSet<String> types3 = ImmutableSet.of("t3");
        final ImmutableList<Message> messages3 = ImmutableList.of();
        final Subscription sub3 = new Subscription(id3, name3, types3, messages3);
        subscriptions.put(id3, sub3);

        final ImmutableList<Subscription> result = service.getSubscriptions();

        assertThat(result).containsExactlyElementsOf(ImmutableList.copyOf(subscriptions.values()));
    }
}
