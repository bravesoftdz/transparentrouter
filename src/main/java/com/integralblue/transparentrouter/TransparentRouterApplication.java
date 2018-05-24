package com.integralblue.transparentrouter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;

/**
 * Declare the Spring Boot application.
 * @author Craig Andrews
 */
@SpringBootApplication
@SuppressWarnings({"checkstyle:hideutilityclassconstructor", "PMD.UseUtilityClass"})
@PropertySource(value = "file:./application-local.properties", ignoreResourceNotFound = true)
public class TransparentRouterApplication extends SpringBootServletInitializer {

	public static void main(final String[] args) {
		SpringApplication.run(TransparentRouterApplication.class, args);
	}
}
