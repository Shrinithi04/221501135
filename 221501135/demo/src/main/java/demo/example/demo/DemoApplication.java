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

@SpringBootApplication
@RestController
@RequestMapping("/shorturl")
public class UrlShortenerApplication {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerApplication.class);
    private Map<String, UrlData> urlMap = new HashMap<>();
    private Map<String, Integer> clickData = new HashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }

    static class UrlData {
        String originalUrl;
        Instant expiry;

        UrlData(String originalUrl, Instant expiry) {
            this.originalUrl = originalUrl;
            this.expiry = expiry;
        }
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

    @PostMapping
    public Map<String, String> createShortUrl(@RequestBody Map<String, Object> request) {
        logger.info("Received POST request with body: {}", request);
        String url = (String) request.get("url");
        Integer validity = (Integer) request.getOrDefault("validity", 30);
        String shortcode = (String) request.get("shortcode");

        if (!isValidUrl(url)) {
            throw new IllegalArgumentException("Invalid URL");
        }

        if (shortcode != null && !shortcode.matches("^[a-zA-Z0-9]{1,10}$")) {
            throw new IllegalArgumentException("Invalid shortcode format");
        }

        String code = shortcode != null && !urlMap.containsKey(shortcode) ? shortcode : generateShortCode();
        while (urlMap.containsKey(code)) {
            code = generateShortCode();
        }

        Instant expiry = Instant.now().plus(validity, ChronoUnit.MINUTES);
        urlMap.put(code, new UrlData(url, expiry));

        Map<String, String> response = new HashMap<>();
        response.put("shortlink", "http://localhost:8080/shorturl/" + code);
        response.put("expiry", expiry.toString());
        return response;
    }

    @GetMapping("/{shortcode}")
    public Object redirectOrStats(@PathVariable String shortcode, @RequestParam(required = false) String stats) {
        logger.info("Received GET request for shortcode: {}", shortcode);
        UrlData data = urlMap.get(shortcode);

        if (data == null) {
            return Map.of("error", "Shortcode not found");
        }

        if (Instant.now().isAfter(data.expiry)) {
            return Map.of("error", "Link expired");
        }

        if ("true".equals(stats)) {
            clickData.put(shortcode, clickData.getOrDefault(shortcode, 0) + 1);
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
        return "Redirecting to: " + data.originalUrl;
    }
}