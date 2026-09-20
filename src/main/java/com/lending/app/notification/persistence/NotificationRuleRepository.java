package com.lending.app.notification.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lending.app.notification.domain.NotificationRule;
import com.lending.app.notification.event.NotificationEventType;

public interface NotificationRuleRepository extends JpaRepository<NotificationRule, UUID> {

    List<NotificationRule> findByEventTypeAndEnabledTrue(NotificationEventType eventType);
}
