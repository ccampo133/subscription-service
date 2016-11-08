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
public class SubscriptionResponseResourceTest {

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
        final SubscriptionResponseResource resource = SubscriptionResponseResource.fromSubscription(subscription);

        assertThat(resource.messageCountsByType)
                .containsOnly(entry("type1", 3L), entry("type2", 2L), entry("type3", 1L));
    }

}
