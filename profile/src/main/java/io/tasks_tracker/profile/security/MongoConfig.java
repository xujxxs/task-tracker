package io.tasks_tracker.profile.security;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.mongo.JdkMongoSessionConverter;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;

@Configuration(proxyBeanMethods = false)
@EnableMongoHttpSession(maxInactiveIntervalInSeconds = 60 * 60 * 24 * 2)
public class MongoConfig
{
    @Bean
	public JdkMongoSessionConverter jdkMongoSessionConverter() {
		return new JdkMongoSessionConverter(Duration.ofMinutes(30)); 
	}
}
