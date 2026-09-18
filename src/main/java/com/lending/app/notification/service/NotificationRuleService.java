package com.lending.app.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lending.app.customer.domain.CustomerSegment;
import com.lending.app.notification.domain.NotificationRule;
import com.lending.app.notification.event.NotificationEventType;
import com.lending.app.notification.repository.NotificationRuleRepository;

@Service
public class NotificationRuleService {

    private final NotificationRuleRepository ruleRepository;

    public NotificationRuleService(NotificationRuleRepository ruleRepository) {

        this.ruleRepository = ruleRepository;
    }

    public List<NotificationRule> findApplicableRules(NotificationEventType eventType,
            CustomerSegment customerSegment) {
        return ruleRepository
                .findByEventTypeAndEnabledTrue(eventType)
                .stream()
                .filter(rule -> rule.getCustomerSegment() == null || rule.getCustomerSegment() == customerSegment)
                .toList();
    }
}
