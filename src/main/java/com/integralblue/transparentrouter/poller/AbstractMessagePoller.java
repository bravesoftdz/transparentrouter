package com.integralblue.transparentrouter.poller;

import java.time.Duration;

import javax.annotation.PreDestroy;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.integralblue.transparentrouter.QueueProperties;
import com.integralblue.transparentrouter.QueueProperties.RouteConfiguration;
import com.integralblue.transparentrouter.aws.ExtendedAmazonSQS;
import com.integralblue.transparentrouter.sqsmessagelistener.SqsMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * Reads messages from queues and sends them to the given {@link SqsMessageListener}.
 * @author Craig Andrews
 *
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractMessagePoller {
	private final TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

	@Autowired
	protected QueueProperties queueProperties;

	@Autowired
	protected ExtendedAmazonSQS amazonSQS;

	protected final SqsMessageListener sqsMessageListener;

	private boolean shuttingDown;

	@PreDestroy
	public void preDestroy() {
		shuttingDown = true;
	}

	/** Given a {@link RouteConfiguration}, return the ARN of the queue which should be polled for messages.
	 * @param routeConfiguration use a getter on this {@link RouteConfiguration} to get the ARN to return.
	 * @return ARN on which to poll for messages to receive.
	 */
	abstract protected String getListeningQueueArnFromRouteConfiguration(RouteConfiguration routeConfiguration);

	@EventListener(ApplicationReadyEvent.class)
	public void applicationReady() {
		// listen for messages
		for (final RouteConfiguration routeConfiguration : queueProperties.getRoutes()) {
			// assuming that all queues are in the same region
			final String queueUrl = amazonSQS.getQueueUrlFromArn(getListeningQueueArnFromRouteConfiguration(routeConfiguration)).getQueueUrl();
			taskExecutor.execute(new ReceiveMessageTask(routeConfiguration, queueUrl));
		}
	}

	/** Receives messages from an SQS queue and hands them off to an {@link SqsMessageListener}.
	 * @author Craig Andrews
	 *
	 */
	@RequiredArgsConstructor
	private class ReceiveMessageTask implements Runnable {
		private final RouteConfiguration routeConfiguration;
		private final String queueUrl;

		@Override
		public void run() {
			final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl)
					.withMessageAttributeNames("All")
					.withMaxNumberOfMessages(queueProperties.getReceiveMaxNumberOfMessages())
					.withWaitTimeSeconds(queueProperties.getReceiveWaitTimeSeconds());
			while (!shuttingDown) {
				try {
					final ReceiveMessageResult receiveMessageResult = amazonSQS.receiveMessage(receiveMessageRequest);
					for (final Message message : receiveMessageResult.getMessages()) {
						try {
							sqsMessageListener.onMessage(message, routeConfiguration);
						}
						catch (Exception e) { //NOPMD
							log.error("Exception processing message", e);
						}
					}
				}
				catch (final Exception e) { //NOPMD
					log.error("Exception occurred receiving messages. Queue url: {}", queueUrl, e);
					try {
						// wait a little bit; if this is due to a service problem on AWS end or a connection problem on our end, it may resolve after a short wait
						Thread.sleep(Duration.ofSeconds(30).toMillis());
					}
					catch (final InterruptedException e1) {
						// ignore this exception - the while loop with either poll again or shuttingDown will be true and the loop will terminate
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}
}
