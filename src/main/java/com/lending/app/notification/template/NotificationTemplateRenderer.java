package com.lending.app.notification.template;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class NotificationTemplateRenderer {

    private static final Pattern VARIABLE = Pattern.compile("\\{\\{(\\w+)}}");

    public String render(String template, Map<String, Object> variables) {

        Matcher matcher = VARIABLE.matcher(template);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {

            String variableName = matcher.group(1);

            Object value = variables.get(variableName);

            if (value == null) {
                throw new IllegalArgumentException("Missing template variable: " + variableName);
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(value.toString()));
        }

        matcher.appendTail(result);

        return result.toString();
    }
}
