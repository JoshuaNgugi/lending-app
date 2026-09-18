package com.lending.app.notification.domain;

import java.util.UUID;

import com.lending.app.customer.domain.CustomerSegment;
import com.lending.app.notification.channel.NotificationChannel;
import com.lending.app.notification.event.NotificationEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_rules")
public class NotificationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private NotificationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_segment", length = 20)
    private CustomerSegment customerSegment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "template_code", nullable = false, length = 100)
    private String templateCode;

    @Column(nullable = false)
    private boolean enabled;

    protected NotificationRule() {
    }

    public NotificationRule(
            NotificationEventType eventType,
            CustomerSegment customerSegment,
            NotificationChannel channel,
            String templateCode) {

        this.eventType = eventType;
        this.customerSegment = customerSegment;
        this.channel = channel;
        this.templateCode = templateCode;
        this.enabled = true;
    }

    public UUID getId() {
        return id;
    }

    public NotificationEventType getEventType() {
        return eventType;
    }

    public CustomerSegment getCustomerSegment() {
        return customerSegment;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
