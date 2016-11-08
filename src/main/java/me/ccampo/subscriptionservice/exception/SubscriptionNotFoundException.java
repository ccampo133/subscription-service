package me.ccampo.subscriptionservice.exception;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Chris Campo
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubscriptionNotFoundException extends RuntimeException {
    public SubscriptionNotFoundException(@NotNull final String message) {
        super(message);
    }
}
