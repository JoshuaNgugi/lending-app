package com.lending.app.notification.service;

import java.util.List;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.lending.app.customer.domain.Customer;
import com.lending.app.customer.repository.CustomerRepository;
import com.lending.app.notification.channel.NotificationSender;
import com.lending.app.notification.channel.NotificationSenderFactory;
import com.lending.app.notification.domain.NotificationRule;
import com.lending.app.notification.domain.NotificationTemplate;
import com.lending.app.notification.event.NotificationEvent;
import com.lending.app.notification.repository.NotificationTemplateRepository;
import com.lending.app.notification.template.NotificationTemplateRenderer;
import com.lending.app.shared.exception.ResourceNotFoundException;

@Component
public class NotificationEventHandler {

    private final CustomerRepository customerRepository;
    private final NotificationRuleService notificationRuleService;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationTemplateRenderer templateRenderer;
    private final NotificationSenderFactory senderFactory;

    public NotificationEventHandler(
            CustomerRepository customerRepository,
            NotificationRuleService notificationRuleService,
            NotificationTemplateRepository templateRepository,
            NotificationTemplateRenderer templateRenderer,
            NotificationSenderFactory senderFactory) {

        this.customerRepository = customerRepository;
        this.notificationRuleService = notificationRuleService;
        this.templateRepository = templateRepository;
        this.templateRenderer = templateRenderer;
        this.senderFactory = senderFactory;
    }

    @EventListener
    public void handle(NotificationEvent event) {

        Customer customer = customerRepository
                .findById(event.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + event.customerId()));

        List<NotificationRule> rules = notificationRuleService.findApplicableRules(
                event.eventType(),
                customer);

        Map<String, Object> variables = Map.of(
                "firstName", customer.getFirstName(),
                "lastName", customer.getLastName(),
                "loanId", event.loanId());

        for (NotificationRule rule : rules) {

            NotificationTemplate template = templateRepository
                    .findByCodeAndEnabledTrue(rule.getTemplateCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Notification template not found: " + rule.getTemplateCode()));

            String subject = templateRenderer.render(
                    template.getSubject(),
                    variables);

            String message = templateRenderer.render(
                    template.getBody(),
                    variables);

            NotificationSender sender = senderFactory.getSender(rule.getChannel());

            sender.send(customer, subject, message);
        }
    }
}
