
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
@RequestMapping("/shorturl")
public class UrlShortenerApplication {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerApplication.class);
    private Map<String, UrlData> urlMap = new HashMap<>();
    private Map<String, Integer> clickData = new HashMap<>();
    private Map<String, String> authCredentials = new HashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
        // Preload example credentials
        authCredentials.put("user1", "pass123");
        authCredentials.put("user2", "secret456");
    }

    static class UrlData {
        String originalUrl;
        Instant expiry;

        UrlData(String originalUrl, Instant expiry) {
            this.originalUrl = originalUrl;
            this.expiry = expiry;
        }
    }

    // Login endpoint to authenticate and issue Bearer token
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> credentials) {
        logger.info("Login attempt with credentials: {}", credentials);
        String clientID = credentials.get("clientID");
        String clientSecret = credentials.get("clientSecret");

        if (clientID != null && clientSecret != null && authCredentials.containsKey(clientID) && authCredentials.get(clientID).equals(clientSecret)) {
            String token = "Bearer " + UUID.randomUUID().toString();
            return Map.of(
                "token_type", "Bearer",
                "access_token", token,
                "expires_in", "3600"
            );
        }
        return Map.of("error", "Invalid clientID or clientSecret");
    }

    private String generateShortCode() {
        return UUID.randomUUID().toString().split("-")[0].substring(0, 6);
    }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void log(String level, String packageName, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJNYXBDbGFpbXMiOnsiYXVkIjoiaHR0cDovLzIwLjI0NC41Ni4xNDQvZXZhbHVhdGlvbi1zZXJ2aWNlIiwiZW1haWwiOiIyMjE1MDExMzVAcmFqYWxha3NobWkuZWR1LmluIiwiZXhwIjoxNzUxNjk5NTU1LCJpYXQiOjE3NTE2OTg2NTUsImlzcyI6IkFmZm9yZCBNZWRpY2FsIFRlY2hub2xvZ2llcyBQcml2YXRlIExpbWl0ZWQiLCJqdGkiOiJlNjVkODY1Yy0yNGJkLTQ0NWQtOGExMi1lODU4NDU1MDY4M2MiLCJsb2NhbGUiOiJlbi1JTiIsIm5hbWUiOiJzaHJpbml0aGkgcyIsInN1YiI6IjAxNjBkYTQ3LTYzZTEtNDJjNC05YWQ3LTgzZTJiYmQ0YmIwNyJ9LCJlbWFpbCI6IjIyMTUwMTEzNUByYWphbGFrc2htaS5lZHUuaW4iLCJuYW1lIjoic2hyaW5pdGhpIHMiLCJyb2xsTm8iOiIyMjE1MDExMzUiLCJhY2Nlc3NDb2RlIjoiY1d5YVhXIiwiY2xpZW50SUQiOiIwMTYwZGE0Ny02M2UxLTQyYzQtOWFkNy04M2UyYmJkNGJiMDciLCJjbGllbnRTZWNyZXQiOiJIY1h3dHRNZnJCRnp5a2t6In0.8ZlcAyY0gAOvTKQncOgVWCs9QJG7iIVRPmSZ2mzQfmg");
        Map<String, String> body = Map.of(
            "stack", "backend",
            "level", level,
            "package", packageName,
            "message", message.length() > 48 ? message.substring(0, 48) : message
        );
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForObject("http://20.244.56.144/evaluation-service/logs", entity, String.class);
    }

    @PostMapping
    public Map<String, String> createShortUrl(@RequestBody Map<String, Object> request) {
        logger.info("Received POST request with body: {}", request);
        String url = (String) request.get("url");
        Integer validity = (Integer) request.getOrDefault("validity", 30);
        String shortcode = (String) request.get("shortcode");

        if (!isValidUrl(url)) {
            log("error", "handler", "Invalid URL: " + url.substring(0, 20));
            throw new IllegalArgumentException("Invalid URL");
        }

        if (shortcode != null && !shortcode.matches("^[a-zA-Z0-9]{1,10}$")) {
            log("warn", "handler", "Invalid shortcode: " + shortcode);
            throw new IllegalArgumentException("Invalid shortcode format");
        }

        String code = shortcode != null && !urlMap.containsKey(shortcode) ? shortcode : generateShortCode();
        while (urlMap.containsKey(code)) {
            code = generateShortCode();
        }

        Instant expiry = Instant.now().plus(validity, ChronoUnit.MINUTES);
        urlMap.put(code, new UrlData(url, expiry));
        log("info", "handler", "URL shortened for " + url.substring(0, 20));

        return Map.of(
            "shortlink", "http://localhost:8080/shorturl/" + code,
            "expiry", expiry.toString()
        );
    }

    @GetMapping("/{shortcode}")
    public Object redirectOrStats(@PathVariable String shortcode, @RequestParam(required = false) String stats) {
        logger.info("Received GET request for shortcode: {}", shortcode);
        UrlData data = urlMap.get(shortcode);

        if (data == null) {
            log("error", "route", "Shortcode not found: " + shortcode);
            return Map.of("error", "Shortcode not found");
        }

        if (Instant.now().isAfter(data.expiry)) {
            log("warn", "route", "Link expired: " + shortcode);
            return Map.of("error", "Link expired");
        }

        if ("true".equals(stats)) {
            clickData.put(shortcode, clickData.getOrDefault(shortcode, 0) + 1);
            log("info", "route", "Stats for shortcode: " + shortcode);
            return Map.of(
                "totalClicks", clickData.get(shortcode),
                "originalUrl", data.originalUrl,
                "creationDate", Instant.now().minus(30, ChronoUnit.MINUTES).toString(),
                "expiryDate", data.expiry.toString(),
                "clickDetails", Map.of(
                    "timestamp", Instant.now().toString(),
                    "source", "Unknown",
                    "location", "Unknown"
                )
            );
        }

        clickData.put(shortcode, clickData.getOrDefault(shortcode, 0) + 1);
        log("info", "route", "Redirected: " + shortcode + " to " + data.originalUrl.substring(0, 20));
        return "Redirecting to: " + data.originalUrl;
    }
}
