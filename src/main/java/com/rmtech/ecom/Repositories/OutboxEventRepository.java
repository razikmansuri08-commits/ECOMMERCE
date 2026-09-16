package com.rmtech.ecom.Repositories;

import com.rmtech.ecom.Entities.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent,Long> {
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(
            OutboxEvent.OutboxStatus status
    );

}
