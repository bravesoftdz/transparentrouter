package com.integralblue.transparentrouter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;


/**
 * Holds the configuration information for the queues.
 * @author Craig Andrews
 *
 */
@Configuration
@ConfigurationProperties(prefix = "queues")
@Getter
@Setter
@Validated
public class QueueProperties {

	@NotNull
	@Size(min = 1)
	private RouteConfiguration[] routes;

	/**
	 * The duration (in seconds) for which the call waits for a message to arrive in the queue before returning.
	 * @see com.amazonaws.services.sqs.model.ReceiveMessageRequest#withWaitTimeSeconds(Integer)
	 */
	@Min(0)
	private int receiveWaitTimeSeconds = 10;

	/**
	 * Maximum number of messages to receive in polling attempt.
	 * @see com.amazonaws.services.sqs.model.ReceiveMessageRequest#withMaxNumberOfMessages(Integer)
	 */
	@Min(1)
	private int receiveMaxNumberOfMessages = 10;

	/** Specifies the configuration for one queue route.
	 * @author Craig Andrews
	 *
	 */
	@Getter
	@Setter
	@Validated
	public static class RouteConfiguration {
		private final static String ARN_REGEX = "^arn:(?<partition>.+?):sqs:(?<region>.+?):(?<accountId>.+?):(?<queuename>.+)$";
		private final static String ARN_INVALID_MESSAGE = "Invalid SQS ARN";

		/**
		 * <a href="https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">ARN</a> of the incoming queue.
		 * This queue is the one that messages being sent from a client to the processing system are placed.
		 */
		@NotBlank
		@Pattern(regexp = ARN_REGEX, message = ARN_INVALID_MESSAGE)
		private String incomingArn;

		/**
		 * <a href="https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">ARN</a> of the outgoing queue.
		 * This queue is the one that the system being interfaced with is checking.
		 */
		@NotBlank
		@Pattern(regexp = ARN_REGEX, message = ARN_INVALID_MESSAGE)
		private String outgoingArn;

		/**
		 * <a href="https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">ARN</a> of the reply queue.
		 * This queue is the one that the system being interfaced with places replies to messages it receives on {@link #outgoingArn}.
		 */
		@NotBlank
		@Pattern(regexp = ARN_REGEX, message = ARN_INVALID_MESSAGE)
		private String replyArn;
	}
}
