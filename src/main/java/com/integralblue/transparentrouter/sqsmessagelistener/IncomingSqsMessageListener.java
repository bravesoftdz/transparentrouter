package com.integralblue.transparentrouter.sqsmessagelistener;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.integralblue.transparentrouter.QueueProperties.RouteConfiguration;
import com.integralblue.transparentrouter.aws.ExtendedAmazonSQS;
import com.integralblue.transparentrouter.entity.PendingReply;
import com.integralblue.transparentrouter.repository.PendingReplyRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/** Process messages received on an incoming queue.
 * Information about the expected reply is saved using {@link PendingReply} and the message is send out on an outgoing queue.
 * @author Craig Andrews
 *
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class IncomingSqsMessageListener extends AbstractSqsMessageListener {
	private final PendingReplyRepository pendingReplyRepository;
	private final ExtendedAmazonSQS amazonSQS;

	@Override
	@Transactional
	public void onMessage(final @NonNull Message message, final @NonNull RouteConfiguration routeConfiguration) {
		// the message was received on the routeConfiguration.incomingArn queue

		final String jmsCorrelationId;
		if (isMessageAttributeValueValidString(message, messageAttributeProperties.getJmsCorrelationId())) {
			jmsCorrelationId = message.getMessageAttributes().get(messageAttributeProperties.getJmsCorrelationId()).getStringValue();
		}
		else if (isMessageAttributeValueValidString(message, messageAttributeProperties.getJmsMessageId())) {
			jmsCorrelationId = message.getMessageAttributes().get(messageAttributeProperties.getJmsMessageId()).getStringValue();
		}
		else {
			jmsCorrelationId = null;
		}
		if (jmsCorrelationId != null && isMessageAttributeValueValidString(message, messageAttributeProperties.getReplyToQueueArn())) {
			final String replyToQueueArn = message.getMessageAttributes().get(messageAttributeProperties.getReplyToQueueArn()).getStringValue();

			final PendingReply pendingReply = new PendingReply();
			pendingReply.setJmsCorrelationId(jmsCorrelationId);
			pendingReply.setReceiveReplyOnQueueArn(routeConfiguration.getReplyArn());
			pendingReply.setSendReplyToQueueArn(replyToQueueArn);
			try {
				pendingReplyRepository.save(pendingReply);
			}
			catch (final ConstraintViolationException e) {
				log.info("Duplicate message received. SQS Message ID: {}, + Message/Correlation ID: {}", message.getMessageId(), jmsCorrelationId, e);
				return; // do not forward the duplicate message along
			}
		}
		else {
			log.warn("Both of " + messageAttributeProperties.getJmsMessageId() + " and " + messageAttributeProperties.getReplyToQueueArn() + " message attributes must be present but were not; the message is being forwarded but there is no way to wait for a reply. SQS Message ID: {}", message.getMessageId());
		}
		amazonSQS.forwardMessage(message, routeConfiguration.getOutgoingArn());
		amazonSQS.deleteMessage(
				new DeleteMessageRequest()
					.withQueueUrl(amazonSQS.getQueueUrlFromArn(routeConfiguration.getIncomingArn()).getQueueUrl())
					.withReceiptHandle(message.getReceiptHandle()));
	}

}
