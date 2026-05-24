package revy.com.client.frankfurter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "frankfurter")
public record FrankfurterProperties(
    String baseUrl,

    Duration connectTimeout,
    Duration readTimeout
) {
}