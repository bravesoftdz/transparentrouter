package com.integralblue.transparentrouter.sqsmessagelistener;

import java.util.Optional;

import javax.transaction.Transactional;

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

/** Process a message received on a reply queue.
 * The message's JMSCorrelationID is used to look up a {@link PendingReply} and determine on which queue to forward the message.
 * @author Craig Andrews
 *
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ReplySqsMessageListener extends AbstractSqsMessageListener {
	private final PendingReplyRepository pendingReplyRepository;
	private final ExtendedAmazonSQS amazonSQS;

	@Override
	@Transactional
	public void onMessage(final @NonNull Message message, final @NonNull RouteConfiguration routeConfiguration) {
		// the message was received on the routeConfiguration.replyArn queue

		if (isMessageAttributeValueValidString(message, messageAttributeProperties.getJmsCorrelationId())) {
			final String jmsCorrelationId = message.getMessageAttributes().get(messageAttributeProperties.getJmsCorrelationId()).getStringValue();
			final Optional<PendingReply> optionalPendingReply = pendingReplyRepository.findByJmsCorrelationIdAndReceiveReplyOnQueueArn(jmsCorrelationId, routeConfiguration.getReplyArn());
			if (optionalPendingReply.isPresent()) {
				final PendingReply pendingReply = optionalPendingReply.get();

				amazonSQS.forwardMessage(message, pendingReply.getSendReplyToQueueArn());
				pendingReplyRepository.delete(pendingReply);
				amazonSQS.deleteMessage(
						new DeleteMessageRequest()
							.withQueueUrl(amazonSQS.getQueueUrlFromArn(routeConfiguration.getReplyArn()).getQueueUrl())
							.withReceiptHandle(message.getReceiptHandle()));
			}
			else {
				log.warn("No reply was expected on this queue with this JMSCorrelationID. There is no way to know where to send this message; it is being dropped. JMSCorrelationID: {} Reply Queue ARN {} SQS Message ID: {}", jmsCorrelationId, routeConfiguration.getReplyArn(), message.getMessageId());
			}
		}
		else {
			log.warn(messageAttributeProperties.getJmsCorrelationId() + " not present. There is no way to know where to send this message; it is being dropped. SQS Message ID: {}", message.getMessageId());
		}
	}

}
