package com.integralblue.transparentrouter;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.PurgeQueueInProgressException;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.integralblue.transparentrouter.QueueProperties.RouteConfiguration;
import com.integralblue.transparentrouter.aws.ExtendedAmazonSQS;
import com.integralblue.transparentrouter.entity.PendingReply;
import com.integralblue.transparentrouter.properties.MessageAttributeProperties;
import com.integralblue.transparentrouter.repository.PendingReplyRepository;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/** Test that messages data is sent, stored, and received correctly.
 * Valid SQS credentials must be available and queues must be accessible for this test to work.
 * @author Craig Andrews
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("/application-test.properties")
public class RouteTest {
	@Autowired
	private PendingReplyRepository pendingReplyRepository;

	@Autowired
	private QueueProperties queueProperties;

	@Autowired
	private MessageAttributeProperties messageAttributeProperties;

	@Autowired
	private ExtendedAmazonSQS amazonSQS;

	@Value("${test.queues}")
	private String[] testQueues;

	@Test
	public void sendMessagePendingReplyCreated() throws Exception {
		final String jmsMessageIdPropertyName = messageAttributeProperties.getJmsMessageId();
		final String jmsCorrelationIdPropertyName = messageAttributeProperties.getJmsCorrelationId();
		final String replyToQueueArnPropertyName = messageAttributeProperties.getReplyToQueueArn();
		final int routeConfigurationIndex = 0;
		final RouteConfiguration routeConfiguration = queueProperties.getRoutes()[routeConfigurationIndex];
		final String testQueue = testQueues[routeConfigurationIndex];
		try {
			final String jmsMessageID = UUID.randomUUID().toString();
			final String outgoingMessageBody = "Some outgoing random message body: " + UUID.randomUUID().toString();

			// send a message to the outgoing queue

			amazonSQS.sendMessage(new SendMessageRequest()
					.withQueueUrl(amazonSQS.getQueueUrlFromArn(routeConfiguration.getIncomingArn()).getQueueUrl())
					.addMessageAttributesEntry(jmsMessageIdPropertyName, new MessageAttributeValue().withDataType("String").withStringValue(jmsMessageID))
					.addMessageAttributesEntry(replyToQueueArnPropertyName, new MessageAttributeValue().withDataType("String").withStringValue(testQueue))
					.withMessageBody(outgoingMessageBody)
					);

			// wait to make sure the message is available on SQS and that this application has picked it up
			Thread.sleep(Duration.ofSeconds(queueProperties.getReceiveWaitTimeSeconds()).toMillis());

			// check that the message has been forwarded and is in the outgoing queue
			ReceiveMessageResult receiveOutgoingMessageResult = amazonSQS.receiveMessage(new ReceiveMessageRequest()
					.withQueueUrl(amazonSQS.getQueueUrlFromArn(routeConfiguration.getOutgoingArn()).getQueueUrl())
					.withMessageAttributeNames("All")
					);
			assertThat(receiveOutgoingMessageResult.getMessages()).hasSize(1);
			Message outgoingMessage = receiveOutgoingMessageResult.getMessages().get(0);
			try {
				assertThat(outgoingMessage.getBody()).isEqualTo(outgoingMessageBody);
				assertThat(outgoingMessage.getMessageAttributes().get(jmsMessageIdPropertyName)).isNotNull();
				assertThat(outgoingMessage.getMessageAttributes().get(jmsMessageIdPropertyName).getDataType()).isEqualTo("String");
				assertThat(outgoingMessage.getMessageAttributes().get(jmsMessageIdPropertyName).getStringValue()).isEqualTo(jmsMessageID);
			}
			finally {
				// clean up
				amazonSQS.deleteMessage(amazonSQS.getQueueUrlFromArn(routeConfiguration.getOutgoingArn()).getQueueUrl(), outgoingMessage.getReceiptHandle());
			}

			// check the pending reply exists and is correct
			Optional<PendingReply> optionalPendingReply = pendingReplyRepository.findByJmsCorrelationIdAndReceiveReplyOnQueueArn(jmsMessageID, routeConfiguration.getReplyArn());
			assertThat(optionalPendingReply).isPresent();
			PendingReply pendingReply = optionalPendingReply.get();
			assertThat(pendingReply.getCreated()).isBefore(Instant.now());
			assertThat(pendingReply.getId()).isNotNull();
			assertThat(pendingReply.getJmsCorrelationId()).isEqualTo(jmsMessageID);
			assertThat(pendingReply.getReceiveReplyOnQueueArn()).isEqualTo(routeConfiguration.getReplyArn());
			assertThat(pendingReply.getSendReplyToQueueArn()).isEqualTo(testQueue);

			// send a reply
			final String replyMessageBody = "Some reply random message body: " + UUID.randomUUID().toString();
			amazonSQS.sendMessage(new SendMessageRequest()
					.withQueueUrl(amazonSQS.getQueueUrlFromArn(routeConfiguration.getReplyArn()).getQueueUrl())
					.addMessageAttributesEntry(jmsCorrelationIdPropertyName, new MessageAttributeValue().withDataType("String").withStringValue(jmsMessageID))
					.withMessageBody(replyMessageBody));

			// wait to make sure the message is available on SQS and that this application has picked it up
			Thread.sleep(Duration.ofSeconds(queueProperties.getReceiveWaitTimeSeconds()).toMillis());

			// the pending reply should have been deleted
			assertThat(pendingReplyRepository.findByJmsCorrelationIdAndReceiveReplyOnQueueArn(jmsMessageID, routeConfiguration.getReplyArn())).isNotPresent();

			ReceiveMessageResult receiveTestMessageResult = amazonSQS.receiveMessage(new ReceiveMessageRequest()
					.withQueueUrl(amazonSQS.getQueueUrlFromArn(testQueue).getQueueUrl())
					.withMessageAttributeNames("All")
					);
			assertThat(receiveTestMessageResult.getMessages()).hasSize(1);
			Message testMessage = receiveTestMessageResult.getMessages().get(0);
			try {
				assertThat(testMessage.getBody()).isEqualTo(replyMessageBody);
				assertThat(testMessage.getMessageAttributes().get(jmsCorrelationIdPropertyName)).isNotNull();
				assertThat(testMessage.getMessageAttributes().get(jmsCorrelationIdPropertyName).getDataType()).isEqualTo("String");
				assertThat(testMessage.getMessageAttributes().get(jmsCorrelationIdPropertyName).getStringValue()).isEqualTo(jmsMessageID);
			}
			finally {
				// clean up
				amazonSQS.deleteMessage(amazonSQS.getQueueUrlFromArn(testQueue).getQueueUrl(), testMessage.getReceiptHandle());
			}

			// make sure all queues are empty
			assertThat(getApproximateNumberOfMessages(routeConfiguration.getIncomingArn())).as("routeConfiguration[%s].incomingArn=%s number of messages", routeConfigurationIndex, routeConfiguration.getIncomingArn()).isEqualTo(0);
			assertThat(getApproximateNumberOfMessages(routeConfiguration.getOutgoingArn())).as("routeConfiguration[%s].outgoingArn=%s number of messages", routeConfigurationIndex, routeConfiguration.getOutgoingArn()).isEqualTo(0);
			assertThat(getApproximateNumberOfMessages(routeConfiguration.getReplyArn())).as("routeConfiguration[%s].replyArn=%s number of messages", routeConfigurationIndex, routeConfiguration.getReplyArn()).isEqualTo(0);
			assertThat(getApproximateNumberOfMessages(testQueue)).as("testQueues[%s]=%s number of messages", routeConfigurationIndex, testQueue).isEqualTo(0);

		}
		finally {

			// clean up the queues to ensure future tests can use them
			for (final String queueArn : new String[] {routeConfiguration.getIncomingArn(), routeConfiguration.getOutgoingArn(), routeConfiguration.getReplyArn()}) {
				try {
				amazonSQS.purgeQueue(new PurgeQueueRequest(amazonSQS.getQueueUrlFromArn(queueArn).getQueueUrl()));
				}
				catch (PurgeQueueInProgressException e) {
					//ignore this exception; if there's a purge in progress, that's fine as that's what we're trying to do
				}
			}
		}
	}

	private long getApproximateNumberOfMessages(final String queueArn) {
		return Long.parseLong(amazonSQS.getQueueAttributes(amazonSQS.getQueueUrlFromArn(queueArn).getQueueUrl(), Collections.singletonList("ApproximateNumberOfMessages")).getAttributes().get("ApproximateNumberOfMessages"));
	}
}
