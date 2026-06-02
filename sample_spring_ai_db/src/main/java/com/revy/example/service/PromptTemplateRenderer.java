package com.revy.example.service;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PromptTemplateRenderer {

    public String render(String template, Map<String, Object> variables) {
        if (template == null || template.isBlank()) {
            throw new IllegalArgumentException("Prompt template is empty.");
        }

        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String rendered = template;

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() == null
                ? ""
                : String.valueOf(entry.getValue());

            rendered = rendered.replace(placeholder, value);
        }

        return rendered;
    }
}