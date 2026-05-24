package revy.com.client.frankfurter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FrankfurterRateResponse(
        BigDecimal rate,
        String base,
        String quote,
        LocalDate date
) {
}