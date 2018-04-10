package com.integralblue.transparentrouter.sqsmessagelistener;

import com.amazonaws.services.sqs.model.Message;
import com.integralblue.transparentrouter.QueueProperties.RouteConfiguration;

/** Handle SQS messages.
 * @author Craig Andrews
 *
 */
@FunctionalInterface
public interface SqsMessageListener {
	/** Handle a message.
	 * @param message the message
	 * @param routeConfiguration the {@link RouteConfiguration} that received the message
	 */
	void onMessage(Message message, RouteConfiguration routeConfiguration);
}
