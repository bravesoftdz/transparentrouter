package com.integralblue.transparentrouter;

import java.time.Instant;

import com.integralblue.transparentrouter.entity.PendingReply;
import com.integralblue.transparentrouter.properties.CleanupOldPendingRepliesProperties;
import com.integralblue.transparentrouter.repository.PendingReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/** Configure the {@link #cleanupOldPendingReplies()} job.
 * @author Craig Andrews
 *
 */
@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class CleanupOldPendingRepliesConfig {
	private final PendingReplyRepository pendingReplyRepository;
	private final CleanupOldPendingRepliesProperties cleanupOldPendingRepliesProperties;

	/**
	 * Every hour, delete {@link PendingReply} records that were created more than {@link #deletePendingRepliesOlderThan} ago.
	 */
	@Scheduled(fixedRateString = "PT1H")
	@SchedulerLock(name = "cleanupOldPendingReplies")
	public void cleanupOldPendingReplies() {
		for (final PendingReply pendingReply : pendingReplyRepository.findByCreatedBefore(Instant.now().minus(cleanupOldPendingRepliesProperties.getDeletePendingRepliesOlderThan()))) {
			log.warn("Waited for reply for more than {} but never received it. JmsCorrelationId: {} ReceiveReplyOnQueueArn: {} SendReplyToQueueArn: {} Created: {}", cleanupOldPendingRepliesProperties.getDeletePendingRepliesOlderThan(), pendingReply.getJmsCorrelationId(), pendingReply.getReceiveReplyOnQueueArn(), pendingReply.getSendReplyToQueueArn(), pendingReply.getCreated());
			pendingReplyRepository.delete(pendingReply);
		}
	}

}
