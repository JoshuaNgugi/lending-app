package com.lending.app.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lending.app.customer.domain.Customer;
import com.lending.app.notification.channel.NotificationChannel;
import com.lending.app.notification.domain.NotificationRule;
import com.lending.app.notification.event.NotificationEventType;
import com.lending.app.notification.persistence.CustomerNotificationPreferenceRepository;
import com.lending.app.notification.persistence.NotificationRuleRepository;

@Service
public class NotificationRuleService {

    private final NotificationRuleRepository ruleRepository;
    private final CustomerNotificationPreferenceRepository preferenceRepository;

    public NotificationRuleService(NotificationRuleRepository ruleRepository,
            CustomerNotificationPreferenceRepository preferenceRepository) {

        this.ruleRepository = ruleRepository;
        this.preferenceRepository = preferenceRepository;
    }

    public List<NotificationRule> findApplicableRules(NotificationEventType eventType,
            Customer customer) {

        return ruleRepository
                .findByEventTypeAndEnabledTrue(eventType)
                .stream()
                .filter(rule -> rule.getCustomerSegment() == null || rule.getCustomerSegment() == customer.getSegment())
                .filter(rule -> isChannelEnabled(customer, eventType, rule.getChannel()))
                .toList();
    }

    private boolean isChannelEnabled(Customer customer, NotificationEventType eventType, NotificationChannel channel) {

        return preferenceRepository
                .findByCustomerIdAndEventTypeAndChannel(customer.getId(), eventType, channel)
                .map(pref -> pref.isEnabled())
                .orElse(true);
    }
}
