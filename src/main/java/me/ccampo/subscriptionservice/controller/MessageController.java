package me.ccampo.subscriptionservice.controller;

import me.ccampo.subscriptionservice.model.Message;
import me.ccampo.subscriptionservice.service.MessageService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @author Chris Campo
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    @Autowired
    public MessageController(@NotNull final MessageService messageService) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
    }

    @NotNull
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Message> createMessage(@RequestParam @NotNull final String type,
            @RequestParam @NotNull final String contents) {
        log.info("POST /messages; type = {}, contents = {}", type, contents);
        final Message message = messageService.createMessage(type, contents);
        log.info("Successfully created message with ID {}", message.id);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    // Not part of the API spec, but useful for debugging at least
    @NotNull
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Message>> getAllMessages() {
        return new ResponseEntity<>(messageService.getMessages(), HttpStatus.OK);
    }
}
