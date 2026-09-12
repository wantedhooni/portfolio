package com.revy.example.controller;

import com.revy.example.service.MessageService;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/hello")
    public String hello() {
        return messageService.getMessage("common.hello");
    }

    @GetMapping("/welcome")
    public String welcome() {
        return messageService.getMessage("common.welcome");
    }

    @GetMapping("/search/{messageKey}")
    public String search(@PathVariable String messageKey) {
        return messageService.getMessage(messageKey);
    }
}