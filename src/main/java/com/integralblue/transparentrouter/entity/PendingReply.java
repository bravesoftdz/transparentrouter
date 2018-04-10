package com.integralblue.transparentrouter.entity;

import java.time.Instant;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;

import org.springframework.data.annotation.CreatedDate;

/** Entity that holds information about pending replies.
 * Instances are created when a reply is expected.
 * Indicates that in the future,
 * a message with a JMS correlation ID {@link #getJmsCorrelationId()} will be received
 *  on queue {@link #getReceiveReplyOnQueueArn()}.
 *  That message should then be sent on queue {@link #getSendReplyToQueueArn()}.
 * @author Craig Andrews
 *
 */
@Entity
@Getter
@Setter
@Immutable // can be inserted, but not updated or deleted
@Table
public class PendingReply {
	private final static String ARN_REGEX = "^arn:aws:sqs:(?<region>.+?):(?<accountId>.+?):(?<queuename>.+)$";

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@Column(nullable = false)
	@SuppressWarnings("PMD.ShortVariable")
	private UUID id;

	/**
	 * The JMS Correlation ID of the expected reply message.
	 */
	@NotBlank
	@Column(nullable = false)
	private String jmsCorrelationId;

	/**
	 * ARN of the queue on which the reply will be received.
	 */
	@NotNull
	@Pattern(regexp = ARN_REGEX)
	private String receiveReplyOnQueueArn;

	/**
	 * When the reply is received, the ARN of the queue to forward the reply to.
	 */
	@NotNull
	@Pattern(regexp = ARN_REGEX)
	private String sendReplyToQueueArn;

	/**
	 * The time when waiting for the reply started.
	 */
	@Column(nullable = false, updatable = false)
	@CreatedDate
	@Setter(AccessLevel.PRIVATE)
	private Instant created = Instant.now();
}
