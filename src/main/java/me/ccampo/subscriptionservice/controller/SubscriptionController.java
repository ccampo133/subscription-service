package me.ccampo.subscriptionservice.controller;

import com.google.common.collect.ImmutableSet;
import me.ccampo.subscriptionservice.model.Message;
import me.ccampo.subscriptionservice.model.Subscription;
import me.ccampo.subscriptionservice.model.resource.SubscriptionResponseResource;
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
        this.subscriptionService = subscriptionService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<SubscriptionResponseResource> createSubscription(@RequestParam @NotNull final String name,
            @RequestParam @NotNull final List<String> messageTypes) {
        log.info("POST /subscriptions; name = {}, messageTypes = {}", name, messageTypes);
        final ImmutableSet<String> types = ImmutableSet.copyOf(messageTypes);
        final Subscription subscription = subscriptionService.createSubscription(name, types);
        final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(subscription.id).toUri();
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        final SubscriptionResponseResource resource = SubscriptionResponseResource.fromSubscription(subscription);
        log.info("Subscription successfully created with ID = {}", subscription.id);
        return new ResponseEntity<>(resource, headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<SubscriptionResponseResource> getSubscriptionById(@PathVariable @NotNull final String id) {
        log.info("GET /subscriptions/{}", id);
        final Subscription subscription = subscriptionService.getSubscriptionById(UUID.fromString(id));
        final SubscriptionResponseResource resource = SubscriptionResponseResource.fromSubscription(subscription);
        log.info("Successfully retrieved subscription with ID {}", id);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /*
     * There's a lot of debate about whether or not you should support partial
     * updates with HTTP PUT. PUT should remain idempotent according to the
     * HTTP spec, so technically you should PUT the entire resource on every
     * request. Practically it comes down to an API design decision whether or
     * not to support partial updates. Here, I allow it, however the method
     * still remains idempotent.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<SubscriptionResponseResource> updateSubscriptionById(@PathVariable @NotNull final String id,
            @RequestParam @NotNull final Optional<String> name,
            @RequestParam(required = false, defaultValue = "") @NotNull final List<String> messageTypes) {
        log.info("PUT /subscriptions/{}; name = {}, messageTypes = {}", id, name, messageTypes);
        final ImmutableSet<String> types = ImmutableSet.copyOf(messageTypes);
        final Subscription subscription = subscriptionService.updateSubscriptionById(UUID.fromString(id), name, types);
        final SubscriptionResponseResource resource = SubscriptionResponseResource.fromSubscription(subscription);
        log.info("Successfully updated subscription with ID {}", id);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/messages", method = RequestMethod.POST)
    public ResponseEntity<Message> createMessageForSubscription(@PathVariable @NotNull final String id,
            @RequestParam @NotNull final String type,
            @RequestParam @NotNull final String contents) {
        log.info("POST /subscriptions/{}/messages; type = {}, contents = {}", id, type, contents);
        final Message message = subscriptionService.createMessageForSubscription(UUID.fromString(id), type, contents);
        log.info("Successfully created message with ID {} on subscription {}", message.id, id);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }
}
