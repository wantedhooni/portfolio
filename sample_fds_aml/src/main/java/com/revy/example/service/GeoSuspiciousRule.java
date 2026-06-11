package com.revy.example.service;

@Component
@Slf4j
public class GeoSuspiciousRule implements FdsRule {

    @Value("${fds.rules.geo.suspicious-countries}")
    private List<String> suspiciousCountries;

    @Override
    public FdsRuleCode getRuleCode() {
        return FdsRuleCode.GEO_SUSPICIOUS;
    }

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public RuleResult evaluate(RuleContext context) {
        Transaction txn = context.currentTransaction();
        GeoLocationEmbeddable geo = txn.getGeoLocation();

        if (geo == null || geo.getCountryCode() == null) {
            return RuleResult.notTriggered(getRuleCode());
        }

        if (suspiciousCountries.contains(geo.getCountryCode())) {
            String evidence = """
                {"rule": "GEO_SUSPICIOUS", "country": "%s", "suspiciousList": %s}
                """.formatted(geo.getCountryCode(), suspiciousCountries);

            return RuleResult.triggered(getRuleCode(), 85, evidence);
        }

        return RuleResult.notTriggered(getRuleCode());
    }
}

@Component
@Slf4j
public class ImpossibleTravelRule implements FdsRule {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double MAX_SPEED_KMH = 1000.0; // 비행기 속도 기준

    @Override
    public FdsRuleCode getRuleCode() {
        return FdsRuleCode.GEO_IMPOSSIBLE_TRAVEL;
    }

    @Override
    public int getPriority() {
        return 25;
    }

    @Override
    public RuleResult evaluate(RuleContext context) {
        Transaction current = context.currentTransaction();
        List<Transaction> recent = context.recentTransactions();

        if (recent.isEmpty() || current.getGeoLocation() == null) {
            return RuleResult.notTriggered(getRuleCode());
        }

        // 가장 최근 거래와 비교
        Transaction prev = recent.get(0);
        if (prev.getGeoLocation() == null) {
            return RuleResult.notTriggered(getRuleCode());
        }

        double distanceKm = haversine(
            prev.getGeoLocation().getLatitude(), prev.getGeoLocation().getLongitude(),
            current.getGeoLocation().getLatitude(), current.getGeoLocation().getLongitude()
        );

        long minutesBetween = ChronoUnit.MINUTES.between(
            prev.getCreatedAt(), current.getCreatedAt() == null ? LocalDateTime.now() : LocalDateTime.now()
        );

        if (minutesBetween > 0) {
            double speedKmh = (distanceKm / minutesBetween) * 60;
            if (speedKmh > MAX_SPEED_KMH) {
                String evidence = """
                    {"rule": "GEO_IMPOSSIBLE_TRAVEL", "distanceKm": %.1f,
                     "minutesBetween": %d, "estimatedSpeedKmh": %.1f}
                    """.formatted(distanceKm, minutesBetween, speedKmh);

                return RuleResult.triggered(getRuleCode(), 90, evidence);
            }
        }

        return RuleResult.notTriggered(getRuleCode());
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}