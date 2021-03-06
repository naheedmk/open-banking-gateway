package de.adorsys.opba.protocol.xs2a.tests.e2e;

import com.tngtech.jgiven.integration.spring.EnableJGiven;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = JGivenConfig.class)
@EnableJGiven
public class JGivenConfig {
}
