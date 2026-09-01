package com.algaworks.algashop.ordering.infrastructure.adapters.out.messaging.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {

	@Query("select m from OutboxMessage m order by m.id") //uuidv7
	List<OutboxMessage> findBatch(Pageable pageable);
}
