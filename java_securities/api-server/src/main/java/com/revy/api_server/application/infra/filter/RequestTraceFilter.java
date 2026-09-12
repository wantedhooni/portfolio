package com.revy.api_server.application.infra.filter;

import com.revy.common.utils.UuidUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTraceFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_KEY_REQUEST_ID = "requestId";
    public static final String MDC_KEY_TRACE_ID = "traceId";
    public static final String MDC_REQUEST_URL = "requestUrl";
    public static final String MDC_CLIENT_IP = "clientIp";

    /**
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        UUID traceID = UuidUtils.getTimeOrderedEpochUuidV7();
        String requestUri = request.getRequestURI();
        String clientIp = ClientIpResolver.resolveIpv4(request);
        MDC.put(MDC_KEY_TRACE_ID, traceID.toString());
        MDC.put(MDC_REQUEST_URL, requestUri);
        MDC.put(MDC_CLIENT_IP, clientIp);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    public final class ClientIpResolver {

        private ClientIpResolver() {}

        // 우선순위: CDN/리버스프록시가 "클라이언트 IP 단일값"을 보장하는 헤더 먼저
        private static final List<String> HEADERS = List.of(
                "CF-Connecting-IP",
                "X-Real-IP",
                "X-Forwarded-For",
                "Forwarded"
        );

        private static final Pattern IPV4 = Pattern.compile(
                "^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$"
        );

        /**
         * @return client IPv4 or null
         */
        public static String resolveIpv4(HttpServletRequest request) {
            for (String header : HEADERS) {
                String raw = request.getHeader(header);
                if (!StringUtils.hasText(raw)) continue;

                String ip = extractIpv4FromHeader(header, raw);
                if (ip != null) return ip;
            }

            String remote = normalizeHost(request.getRemoteAddr());
            return isIpv4(remote) ? remote : null;
        }

        private static String extractIpv4FromHeader(String header, String raw) {
            String v = raw.trim();

            if ("X-Forwarded-For".equalsIgnoreCase(header)) {
                // "client, proxy1, proxy2" -> leftmost "valid IPv4"
                for (String part : v.split(",")) {
                    String ip = normalizeHost(part);
                    if (isUsableIpv4(ip)) return ip;
                }
                return null;
            }

            if ("Forwarded".equalsIgnoreCase(header)) {
                // RFC7239: Forwarded: for=...;proto=...;by=..., for=...
                for (String element : v.split(",")) {
                    String ip = extractForwardedForIpv4(element);
                    if (ip != null) return ip;
                }
                return null;
            }

            // CF-Connecting-IP / X-Real-IP (가끔 콤마로 여러 값이 들어오는 경우 대비)
            String first = v.split(",")[0];
            String ip = normalizeHost(first);
            return isUsableIpv4(ip) ? ip : null;
        }

        private static String extractForwardedForIpv4(String forwardedElement) {
            // element: for=...;proto=...;by=...
            for (String param : forwardedElement.split(";")) {
                String s = param.trim();
                if (!startsWithIgnoreCase(s, "for=")) continue;

                String forVal = s.substring(4).trim();
                String ip = normalizeForwardedForValue(forVal);
                if (isUsableIpv4(ip)) return ip;
            }
            return null;
        }

        /**
         * Forwarded for= 값은 quoted-string / [ipv6]:port / unknown / obfuscated 등 케이스가 많음
         */
        private static String normalizeForwardedForValue(String raw) {
            String h = normalizeHost(raw);
            if (h == null) return null;

            // RFC: for=_hidden (obfuscated identifier) 같은 건 IP가 아님
            // "_" 로 시작하는 토큰은 실무에서 obfuscated로 많이 나옴
            if (h.startsWith("_")) return null;

            return h;
        }

        private static boolean isUsableIpv4(String ip) {
            return isIpv4(ip) && !isUnknown(ip);
        }

        private static boolean isUnknown(String ip) {
            return ip == null || "unknown".equalsIgnoreCase(ip);
        }

        private static boolean isIpv4(String ip) {
            return ip != null && IPV4.matcher(ip).matches();
        }

        private static boolean startsWithIgnoreCase(String s, String prefix) {
            return s.regionMatches(true, 0, prefix, 0, prefix.length());
        }

        /**
         * Normalize host string:
         * - trim
         * - strip quotes
         * - map ipv6 loopback to 127.0.0.1
         * - [ipv6]:port -> ipv6
         * - ipv4:port -> ipv4
         * - ::ffff:1.2.3.4 (case-insensitive) -> 1.2.3.4
         */
        private static String normalizeHost(String raw) {
            if (!StringUtils.hasText(raw)) return null;
            String h = raw.trim();

            // strip quotes (single/double)
            if ((h.startsWith("\"") && h.endsWith("\"")) || (h.startsWith("'") && h.endsWith("'"))) {
                h = h.substring(1, h.length() - 1).trim();
            }

            // map IPv6 loopback to IPv4 loopback (로컬 개발에서 흔함)
            if ("::1".equals(h) || "0:0:0:0:0:0:0:1".equalsIgnoreCase(h)) {
                return "127.0.0.1";
            }

            // [ipv6]:port  -> ipv6 (strip brackets)
            if (h.startsWith("[")) {
                int end = h.indexOf(']');
                if (end > 0) {
                    h = h.substring(1, end).trim();
                }
            }

            // IPv4-mapped IPv6 (case-insensitive): ::ffff:1.2.3.4 -> 1.2.3.4
            String lower = h.toLowerCase(Locale.ROOT);
            if (lower.startsWith("::ffff:")) {
                h = h.substring(7).trim(); // length("::ffff:") == 7
            }

            // strip ipv4 :port (only if it looks like dotted IPv4-ish host)
            int colon = h.lastIndexOf(':');
            if (colon > 0 && h.indexOf('.') > 0) {
                String maybePort = h.substring(colon + 1);
                if (maybePort.chars().allMatch(Character::isDigit)) {
                    h = h.substring(0, colon);
                }
            }

            h = h.trim();
            return StringUtils.hasText(h) ? h : null;
        }
    }
}


