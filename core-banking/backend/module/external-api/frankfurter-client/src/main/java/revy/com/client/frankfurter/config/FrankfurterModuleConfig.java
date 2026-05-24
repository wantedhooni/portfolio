package revy.com.client.frankfurter.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FrankfurterProperties.class)
public class FrankfurterModuleConfig {
}