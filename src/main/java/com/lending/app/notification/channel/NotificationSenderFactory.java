package com.lending.app.notification.channel;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Factory class for creating instances of NotificationSender based on the
 * specified notification channel.
 */
@Component
public class NotificationSenderFactory {

    private final Map<NotificationChannel, NotificationSender> senders;

    public NotificationSenderFactory(List<NotificationSender> notificationSenders) {

        this.senders = notificationSenders.stream()
                .collect(Collectors.toMap(NotificationSender::channel, Function.identity()));
    }

    public NotificationSender getSender(NotificationChannel channel) {

        NotificationSender sender = senders.get(channel);

        if (sender == null) {
            throw new IllegalArgumentException("No notification sender configured for channel: " + channel);
        }

        return sender;
    }
}
