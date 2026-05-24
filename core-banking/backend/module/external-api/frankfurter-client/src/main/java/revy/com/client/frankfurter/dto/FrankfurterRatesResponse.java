package revy.com.client.frankfurter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record FrankfurterRatesResponse(
        BigDecimal amount,
        String base,
        LocalDate date,
        Map<String, BigDecimal> rates
) {
}