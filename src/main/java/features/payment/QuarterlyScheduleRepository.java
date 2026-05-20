package features.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.QuarterlySchedule;
import models.Section;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QuarterlyScheduleRepository {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String BASE_URL = "http://localhost:3000/quarterlySchedules";

    private QuarterlyScheduleRepository() {}

    public static List<Section> getAll() throws IOException, InterruptedException  {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return Arrays.asList(mapper.readValue(response.body(), Section[].class));
    }

    public static Section getById(String id) throws IOException, InterruptedException  {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.body().equals("{}") || response.body().isBlank()) return null;

        return mapper.readValue(response.body(), Section.class);
    }

    public static void update(QuarterlySchedule schedule) throws IOException, InterruptedException  {
        String body = mapper.writeValueAsString(schedule);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + schedule.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void save(QuarterlySchedule schedule) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(schedule);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static List<QuarterlySchedule> getByBalanceId(String balanceId)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?balanceId=" + balanceId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("[]"))
            return Collections.emptyList();

        return Arrays.asList(mapper.readValue(response.body(), QuarterlySchedule[].class));
    }
}

