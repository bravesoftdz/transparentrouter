package com.integralblue.transparentrouter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Test {@link SqsQueueInfo}.
 * @author Craig Andrews
 *
 */
public class SqsQueueInfoTest {

	@Test
	public void testFromUsEast1Arn() {
		final SqsQueueInfo actual = SqsQueueInfo.fromArn("arn:aws:sqs:us-east-1:711033084107:transparentrouter-test-1");
		assertThat(actual.getPartition()).isEqualTo("aws");
		assertThat(actual.getAccountId()).isEqualTo("711033084107");
		assertThat(actual.getQueueName()).isEqualTo("transparentrouter-test-1");
		assertThat(actual.getRegion()).isEqualTo("us-east-1");
	}

	@Test
	public void testToUsEast1Arn() {
		final SqsQueueInfo actual = new SqsQueueInfo("aws", "us-east-1", "711033084107", "transparentrouter-test-1");
		assertThat(actual.toArn()).isEqualTo("arn:aws:sqs:us-east-1:711033084107:transparentrouter-test-1");
	}

	@Test
	public void testFromUsGovWest1Arn() {
		final SqsQueueInfo actual = SqsQueueInfo.fromArn("arn:aws-us-gov:sqs:us-gov-west-1:429415595364:QUEUE_NAME");
		assertThat(actual.getPartition()).isEqualTo("aws-us-gov");
		assertThat(actual.getAccountId()).isEqualTo("429415595364");
		assertThat(actual.getQueueName()).isEqualTo("QUEUE_NAME");
		assertThat(actual.getRegion()).isEqualTo("us-gov-west-1");
	}

	@Test
	public void testToUsGovWest1Arn() {
		final SqsQueueInfo actual = new SqsQueueInfo("aws-us-gov", "us-gov-west-1", "429415595364", "QUEUE_NAME");
		assertThat(actual.toArn()).isEqualTo("arn:aws-us-gov:sqs:us-gov-west-1:429415595364:QUEUE_NAME");
	}

}
