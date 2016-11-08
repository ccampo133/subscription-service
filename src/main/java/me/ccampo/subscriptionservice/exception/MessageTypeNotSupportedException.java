package me.ccampo.subscriptionservice.exception;

import org.jetbrains.annotations.NonNls;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Chris Campo
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class MessageTypeNotSupportedException extends RuntimeException {
    public MessageTypeNotSupportedException(@NonNls final String message) {
        super(message);
    }
}
