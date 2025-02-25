package com.hungover;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

/**
 * The main entry point for the HungOver application.
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@EnableJpaRepositories(basePackages = { "com.hungover" })
@EntityScan(basePackages = { "com.hungover" })
@ComponentScan(basePackages = { "com.hungover" })
public class HungOverMasterEntry extends SpringBootServletInitializer {

	private static final Logger hungoverMasterEntryLogger = LoggerFactory.getLogger(HungOverMasterEntry.class);

	public static void main(String[] args) {
		hungoverMasterEntryLogger.info("HungOver Master Entry Application Started::::::::::::::::::::::::");
		SpringApplication.run(HungOverMasterEntry.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			SpringApplicationBuilder application) {
		return application.sources(HungOverMasterEntry.class);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames("classpath:/messages/api_messages");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
