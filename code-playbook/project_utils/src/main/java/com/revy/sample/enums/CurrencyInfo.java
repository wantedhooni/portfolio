import java.util.Currency;

public class CurrencyInfo {
    public static void main(String[] args) {
        Currency.availableCurrencies().forEach(c -> {
            System.out.println(
                String.format("%s(\"%s\", \"%s\", \"%s\", \"%s\");", c.getCurrencyCode(), c.getCurrencyCode(),
                              c.getNumericCode(), c.getSymbol(), c.getDefaultFractionDigits()));
        });
    }
}