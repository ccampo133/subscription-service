package me.ccampo.subscriptionservice.model.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import me.ccampo.subscriptionservice.model.Message;
import me.ccampo.subscriptionservice.model.Subscription;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author Chris Campo
 */
public class SubscriptionResourceTest {

    @Test
    public void fromSubscription() {
        final UUID id = UUID.randomUUID();
        final String name = "foo";
        final ImmutableSet<String> types = ImmutableSet.of("type1", "type2", "type3");
        final ImmutableList<Message> messages = ImmutableList.of(
                new Message("type1", "a"),
                new Message("type1", "b"),
                new Message("type1", "c"),
                new Message("type2", "d"),
                new Message("type2", "e"),
                new Message("type3", "f"));
        final Subscription subscription = new Subscription(id, name, types, messages);
        final SubscriptionResource resource = SubscriptionResource.fromSubscription(subscription);

        assertThat(resource.messageCountsByType)
                .containsOnly(entry("type1", 3L), entry("type2", 2L), entry("type3", 1L));
    }

    @Test
    public void fromSubscriptions() {
        final UUID id1 = UUID.randomUUID();
        final String name1 = "foo";
        final ImmutableSet<String> types1 = ImmutableSet.of("type1", "type2", "type3");
        final ImmutableList<Message> msgs1 = ImmutableList.of(
                new Message("type1", "a"),
                new Message("type1", "b"),
                new Message("type1", "c"),
                new Message("type2", "d"),
                new Message("type2", "e"),
                new Message("type3", "f"));
        final Subscription sub1 = new Subscription(id1, name1, types1, msgs1);

        final UUID id2 = UUID.randomUUID();
        final String name2 = "bar";
        final ImmutableSet<String> types2 = ImmutableSet.of("type4");
        final ImmutableList<Message> msgs2 = ImmutableList.of(new Message("type4", "g"));
        final Subscription sub2 = new Subscription(id2, name2, types2, msgs2);

        final ImmutableList<SubscriptionResource> result =
                SubscriptionResource.fromSubscriptions(ImmutableList.of(sub1, sub2));

        assertThat(result).hasSize(2);

        final SubscriptionResource res1 = result.get(0);
        final SubscriptionResource res2 = result.get(1);
        assertThat(res1.messageCountsByType).containsOnly(entry("type1", 3L), entry("type2", 2L), entry("type3", 1L));
        assertThat(res2.messageCountsByType).containsOnly(entry("type4", 1L));
    }
}
