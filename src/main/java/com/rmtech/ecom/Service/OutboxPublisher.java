package com.rmtech.ecom.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmtech.ecom.DTOS.OrderCreatedEvent;
import com.rmtech.ecom.Entities.OutboxEvent;
import com.rmtech.ecom.Repositories.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxEventRepository outboxRepository,
            ApplicationEventPublisher eventPublisher,
            ObjectMapper objectMapper) {

        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxRepository
                        .findTop100ByStatusOrderByCreatedAtAsc(
                                OutboxEvent.OutboxStatus.PENDING
                        );

        for (OutboxEvent outboxEvent : events) {

            try {

                publish(outboxEvent);

                outboxEvent.setStatus(
                        OutboxEvent.OutboxStatus.PROCESSED
                );

                outboxEvent.setProcessedAt(
                        LocalDateTime.now()
                );

                outboxRepository.save(outboxEvent);

                log.info(
                        "Outbox event processed: eventId={}, type={}",
                        outboxEvent.getEventId(),
                        outboxEvent.getEventType()
                );

            } catch (Exception e) {

                log.error(
                        "Failed to publish outbox event: eventId={}",
                        outboxEvent.getEventId(),
                        e
                );
            }
        }
    }

    private void publish(OutboxEvent event)
            throws JsonProcessingException {

        if ("ORDER_CREATED".equals(event.getEventType())) {

            OrderCreatedEvent orderEvent =
                    objectMapper.readValue(
                            event.getPayload(),
                            OrderCreatedEvent.class
                    );

            eventPublisher.publishEvent(orderEvent);
        }
    }
}