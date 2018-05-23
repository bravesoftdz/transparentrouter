package com.integralblue.transparentrouter.properties;

import javax.validation.constraints.NotNull;

import com.integralblue.transparentrouter.CleanupOldPendingRepliesConfig;
import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/** Configuration for {@link CleanupOldPendingRepliesConfig}.
 * @author Craig Andrews
 *
 */
@Configuration
@ConfigurationProperties(prefix = "message.attribute-names")
@Validated
@Getter
@Setter
public class MessageAttributeProperties {

	/**
	 * The name of the SQS message attribute from which to get the JMS Correlation ID.
	 */
	@NotNull
	private String jmsCorrelationId = "JMSCorrelationID";

	/**
	 * The name of the SQS message attribute from which to get the JMS Message ID.
	 */
	@NotNull
	private String jmsMessageId = "JMSMessageID";

	/**
	 * The name of the SQS message attribute from which to get the reply to queue arn.
	 */
	@NotNull
	private String replyToQueueArn = "ReplyToQueueArn";
}
