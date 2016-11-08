package me.ccampo.subscriptionservice.controller;

import com.google.common.collect.ImmutableSet;
import me.ccampo.subscriptionservice.model.Subscription;
import me.ccampo.subscriptionservice.model.resource.SubscriptionResource;
import me.ccampo.subscriptionservice.service.SubscriptionService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Chris Campo
 */
@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(@NotNull final SubscriptionService subscriptionService) {
        this.subscriptionService = Objects.requireNonNull(subscriptionService, "subscriptionService");
    }

    /**
     * Creates a subscription, given a name and the list of messageTypes it should receive.
     *
     * @param name         The name of the subscription
     * @param messageTypes The list of messageTypes to receive
     * @return An HTTP response containing the new subscription object
     */
    @NotNull
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<SubscriptionResource> createSubscription(@RequestParam @NotNull final String name,
            @RequestParam @NotNull final List<String> messageTypes) {
        log.info("POST /subscriptions; name = {}, messageTypes = {}", name, messageTypes);
        final ImmutableSet<String> types = ImmutableSet.copyOf(messageTypes);
        final Subscription subscription = subscriptionService.createSubscription(name, types);
        final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(subscription.id).toUri();
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        final SubscriptionResource resource = SubscriptionResource.fromSubscription(subscription);
        log.info("Subscription successfully created with ID = {}", subscription.id);
        return new ResponseEntity<>(resource, headers, HttpStatus.CREATED);
    }

    // Not part of the API spec, but useful for debugging at least
    @NotNull
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<SubscriptionResource>> getAllSubscriptions() {
        final List<SubscriptionResource> resources =
                SubscriptionResource.fromSubscriptions(subscriptionService.getSubscriptions());
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    /**
     * Gets a single subscription by its unique identifier
     *
     * @param id the UUID of the subscription
     * @return an HTTP response containing the existing subscription object
     */
    @NotNull
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<SubscriptionResource> getSubscriptionById(@PathVariable @NotNull final String id) {
        log.info("GET /subscriptions/{}", id);
        final Subscription subscription = subscriptionService.getSubscriptionById(UUID.fromString(id));
        final SubscriptionResource resource = SubscriptionResource.fromSubscription(subscription);
        log.info("Successfully retrieved subscription with ID {}", id);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Updates an existing subscription object
     *
     * @param id           the UUID of the subscription
     * @param name         the new name of the subscription (optional)
     * @param messageTypes the new list of supported message types (optional)
     * @return An HTTP entity containing the update subscription object
     */
    /* (non-Javadoc)
     *
     * There's a lot of debate about whether or not you should support partial
     * updates with HTTP PUT. PUT should remain idempotent according to the
     * HTTP spec, so technically you should PUT the entire resource on every
     * request. Practically it comes down to an API design decision whether or
     * not to support partial updates. Here, I allow it, however the method
     * still remains idempotent.
     */
    @NotNull
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<SubscriptionResource> updateSubscriptionById(@PathVariable @NotNull final String id,
            @RequestParam @NotNull final Optional<String> name,
            // Note: usually I'd prefer defaulting to an empty collection instead of an optional,
            // but this allows for the case where we want to remove all supported message types
            // by passing an empty list here, as opposed to not changing the message types at all
            // by omitting the parameter altogether.
            @RequestParam @NotNull final Optional<List<String>> messageTypes) {
        log.info("PUT /subscriptions/{}; name = {}, messageTypes = {}", id, name, messageTypes);
        if (!name.isPresent() && !messageTypes.isPresent()) {
            // Nothing to do
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        final Optional<ImmutableSet<String>> types = messageTypes.map(ImmutableSet::copyOf);
        final Subscription subscription = subscriptionService.updateSubscriptionById(UUID.fromString(id), name, types);
        final SubscriptionResource resource = SubscriptionResource.fromSubscription(subscription);
        log.info("Successfully updated subscription with ID {}", id);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }
}
