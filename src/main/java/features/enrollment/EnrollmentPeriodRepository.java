package features.enrollment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.EnrollmentPeriod;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class EnrollmentPeriodRepository {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String BASE_URL = "http://localhost:3000/enrollmentPeriods";

    private EnrollmentPeriodRepository() {}

    public static List<EnrollmentPeriod> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("PERIOD RAW RESPONSE:");
        System.out.println(response.body());

        return Arrays.asList(mapper.readValue(response.body(), EnrollmentPeriod[].class));
    }

    public static EnrollmentPeriod getById(String id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.body().equals("{}") || response.body().isBlank()) return null;

        return mapper.readValue(response.body(), EnrollmentPeriod.class);
    }

    public static EnrollmentPeriod getActive() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?isOpen=true"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.body().equals("{}") || response.body().isBlank()
                || response.body().equals("[]")) return null;

        EnrollmentPeriod[] results = mapper.readValue(response.body(), EnrollmentPeriod[].class);
        return results.length > 0 ? results[0] : null;
    }

    public static void save(EnrollmentPeriod period) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(period);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void update(EnrollmentPeriod period) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(period);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + period.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
