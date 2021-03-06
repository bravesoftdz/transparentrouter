package com.integralblue.transparentrouter.sqsmessagelistener;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.integralblue.transparentrouter.properties.MessageAttributeProperties;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/** Helpful base for {@link SqsMessageListener} implementations.
 * @author Craig Andrews
 *
 */
@Slf4j
public abstract class AbstractSqsMessageListener implements SqsMessageListener {

	@Autowired
	protected MessageAttributeProperties messageAttributeProperties;

	/** Does {@link Message#getMessageAttributes()} contain a {@link MessageAttributeValue} with the given name and a non-blank string value?
	 * @param message the message to inspect
	 * @param messageAttributeValueName The name to look for in {@link Message#getMessageAttributes()}
	 * @return Is there a {@link MessageAttributeValue} with a non-blank string value?
	 */
	protected boolean isMessageAttributeValueValidString(final @NonNull Message message, final @NonNull String messageAttributeValueName) {
		final MessageAttributeValue messageAttributeValue = message.getMessageAttributes().get(messageAttributeValueName);
		if (messageAttributeValue == null) {
			log.warn("No " + messageAttributeValueName + " message attribute was found. SQS Message ID: {}", message.getMessageId());
			return false;
		}
		if (!"String".equals(messageAttributeValue.getDataType())) {
			log.warn(messageAttributeValueName + " message attribute data type is not 'String'. Data Type: {} SQS Message ID: {}", messageAttributeValue.getDataType(), message.getMessageId());
			return false;
		}
		if (!StringUtils.hasText(messageAttributeValue.getStringValue())) {
			log.warn(messageAttributeValueName + " message attribute value is blank (only whitespace). SQS Message ID: {}", message.getMessageId());
			return false;
		}
		return true;
	}
}
