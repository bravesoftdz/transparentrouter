package com.integralblue.transparentrouter;

import java.time.Duration;

import javax.sql.DataSource;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.ScheduledLockConfiguration;
import net.javacrumbs.shedlock.spring.ScheduledLockConfigurationBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Scheduling configuration and scheduled tasks.
 * @author Craig Andrews
 *
 */
@EnableScheduling
public class SchedulingConfig {
	@Value("${schedlock.schema}")
	private String schedLockSchema;

	@Bean
	public LockProvider lockProvider(final DataSource dataSource) {
		return new JdbcTemplateLockProvider(dataSource, schedLockSchema + ".shedlock");
	}

	@Bean
	public ScheduledLockConfiguration taskScheduler(final LockProvider lockProvider) {
		return ScheduledLockConfigurationBuilder
			.withLockProvider(lockProvider)
			.withPoolSize(10)
			.withDefaultLockAtMostFor(Duration.ofMinutes(10))
			.build();
	}

}
