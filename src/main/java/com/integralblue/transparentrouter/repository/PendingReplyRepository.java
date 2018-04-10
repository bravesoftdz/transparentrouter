package com.integralblue.transparentrouter.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.integralblue.transparentrouter.entity.PendingReply;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** {@link Repository} to handle {@link PendingReplyRepository}.
 * @author Craig Andrews
 *
 */
public interface PendingReplyRepository extends CrudRepository<PendingReply, UUID> {
	Optional<PendingReply> findByJmsCorrelationIdAndReceiveReplyOnQueueArn(String jmsCorrelationId, String receiveReplyOnQueueArn);

	Iterable<PendingReply> findByCreatedBefore(Instant instant);
}
