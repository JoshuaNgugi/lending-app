package com.lending.app.notification.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lending.app.notification.channel.NotificationChannel;
import com.lending.app.notification.domain.CustomerNotificationPreference;
import com.lending.app.notification.event.NotificationEventType;

public interface CustomerNotificationPreferenceRepository
        extends JpaRepository<CustomerNotificationPreference, UUID> {

    Optional<CustomerNotificationPreference> findByCustomerIdAndEventTypeAndChannel(
            UUID customerId,
            NotificationEventType eventType,
            NotificationChannel channel);
}
