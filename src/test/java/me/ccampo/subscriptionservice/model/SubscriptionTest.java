package me.ccampo.subscriptionservice.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.*;

/**
 * @author Chris Campo
 */
public class SubscriptionTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionForUnsupportedTypes() {
        final UUID id = UUID.randomUUID();
        final String name = "name";
        final ImmutableSet<String> types = ImmutableSet.of("type1", "type2");
        final ImmutableList<Message> messages = ImmutableList.of(
                new Message("type1", "a"),
                new Message("type2", "b"),
                new Message("type3", "3"));
        @SuppressWarnings("unused")
        final Subscription subscription = new Subscription(id, name, types, messages);
    }
}
