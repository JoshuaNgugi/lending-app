package com.lending.app.notification.domain;

import java.util.UUID;

import com.lending.app.customer.domain.Customer;
import com.lending.app.notification.channel.NotificationChannel;
import com.lending.app.notification.event.NotificationEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "customer_notification_preferences", uniqueConstraints = {
        @UniqueConstraint(name = "uq_customer_notification_preference", columnNames = { "customer_id", "event_type",
                "channel" })
})
public class CustomerNotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private NotificationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(nullable = false)
    private boolean enabled;

    protected CustomerNotificationPreference() {
    }

    public CustomerNotificationPreference(
            Customer customer,
            NotificationEventType eventType,
            NotificationChannel channel,
            boolean enabled) {

        this.customer = customer;
        this.eventType = eventType;
        this.channel = channel;
        this.enabled = enabled;
    }

    public NotificationEventType getEventType() {
        return eventType;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
