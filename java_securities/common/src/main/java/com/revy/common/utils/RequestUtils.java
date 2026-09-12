//package com.revy.common.utils;
//
//
//import java.util.List;
//
//public final class RequestUtils {
//    private RequestUtils() {
//    }
//    private static final List<String> CANDIDATE_HEADERS = List.of(
//            "X-Forwarded-For",
//            "X-Real-IP",
//            "CF-Connecting-IP",
//            "Forwarded"
//    );
//
//
//    public static String resolve(HttpServletRequest request) {
//        for (String header : CANDIDATE_HEADERS) {
//            String value = request.getHeader(header);
//            if (!StringUtils.hasText(value)) continue;
//
//            String ip = extractIp(header, value);
//            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
//                return ip;
//            }
//        }
//        return request.getRemoteAddr();
//    }
//
//    private static String extractIp(String header, String value) {
//        String v = value.trim();
//
//        // X-Forwarded-For: "client, proxy1, proxy2"
//        if ("X-Forwarded-For".equalsIgnoreCase(header)) {
//            String first = v.split(",")[0].trim();
//            return stripPort(first);
//        }
//
//        // Forwarded: for=203.0.113.43;proto=https;by=...
//        // or for="[2001:db8::1]:1234"
//        if ("Forwarded".equalsIgnoreCase(header)) {
//            // 매우 단순 파싱(운영에선 더 엄격하게 파싱 가능)
//            // for=... 를 찾아 첫 for 값 사용
//            String[] parts = v.split(";");
//            for (String p : parts) {
//                String s = p.trim();
//                if (s.toLowerCase().startsWith("for=")) {
//                    String forVal = s.substring(4).trim();
//                    forVal = forVal.replace("\"", "");
//                    // for="[2001:db8::1]:1234" 케이스
//                    if (forVal.startsWith("[")) {
//                        int end = forVal.indexOf(']');
//                        if (end > 0) return forVal.substring(1, end);
//                    }
//                    // for=203.0.113.43:1234 or for=203.0.113.43
//                    return stripPort(forVal);
//                }
//            }
//        }
//
//        // X-Real-IP / CF-Connecting-IP: 보통 단일 IP
//        return stripPort(v);
//    }
//
//    private static String stripPort(String host) {
//        String h = host.trim();
//        // IPv6는 ':'가 포함되므로 단순 split(':')는 위험.
//        // 여기서는 "x.x.x.x:port" 형태만 제거.
//        int colon = h.lastIndexOf(':');
//        if (colon > 0 && h.indexOf('.') > 0 && h.substring(colon + 1).matches("\\d+")) {
//            return h.substring(0, colon);
//        }
//        return h;
//    }
//}
