package com.integralblue.transparentrouter.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;

/** Adds some additional useful methods to {@link AmazonSQS}.
 * @author Craig Andrews
 *
 */
public interface ExtendedAmazonSQS extends AmazonSQS {
	/** Get the URL of a queue given its ARN.
	 * @param arn ARN of the queue.
	 * @return URL of the queue.
	 */
	GetQueueUrlResult getQueueUrlFromArn(String arn);

	/** Sends a received message on a different queue.
	 * @param message message to send
	 * @param queueArn queue to send the message on
	 */
	void forwardMessage(Message message, String queueArn);
}
