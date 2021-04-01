package io.bosch.measurement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import io.bosch.measurement.ditto.AuthenticationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ AuthenticationProperties.class })
public class IotSuitePerformanceMeterApplication {

    public static void main(final String[] args) {
        SpringApplication.run(IotSuitePerformanceMeterApplication.class, args);
    }

}
