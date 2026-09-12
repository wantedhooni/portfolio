package com.revy.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Slf4j
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        return getMessage(code, null, locale);
    }

    public String getMessage(String code, Object[] args) {
        Locale locale = LocaleContextHolder.getLocale();
        return getMessage(code, args, locale);
    }

    public String getMessage(String code, Object[] args, Locale locale) {
        try {
            log.debug("Resolving message for code: {}, args: {}, locale: {}", code, args, locale);
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            // REVY - 운영에서는 메시지키 누락 확인을 위해 로그를 남겨둔다.
            log.warn("Message not found for code: {}, args: {}, locale: {}", code, args, locale);
            return messageSource.getMessage(code, args, code, locale);
        }
    }
}