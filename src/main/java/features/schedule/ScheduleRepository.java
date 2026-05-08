package features.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.Schedule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ScheduleRepository {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static final String BASE_URL = "http://localhost:3000/schedules";

    private static final Map<String, Schedule> cache = new ConcurrentHashMap<>();

    private ScheduleRepository() {}

    public static List<Schedule> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("[]"))
            return Collections.emptyList();

        List<Schedule> schedules = Arrays.asList(
                mapper.readValue(response.body(), Schedule[].class));

        schedules.forEach(s -> cache.put(s.getId(), s));
        return schedules;
    }

    public static Schedule getById(String id) throws IOException, InterruptedException {
        if (cache.containsKey(id)) return cache.get(id);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("{}"))
            return null;

        Schedule schedule = mapper.readValue(response.body(), Schedule.class);
        if (schedule != null) cache.put(id, schedule);
        return schedule;
    }

    public static List<Schedule> getByRoomId(String roomId)
            throws IOException, InterruptedException {

        // use cache if populated, otherwise fetch all
        if (!cache.isEmpty()) {
            return cache.values().stream()
                    .filter(s -> roomId.equals(s.getRoomId()))
                    .collect(Collectors.toList());
        }

        return getAll().stream()
                .filter(s -> roomId.equals(s.getRoomId()))
                .collect(Collectors.toList());
    }

    public static void save(Schedule schedule) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(schedule);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
        cache.put(schedule.getId(), schedule);
    }

    public static void update(Schedule schedule) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(schedule);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + schedule.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
        cache.put(schedule.getId(), schedule);
    }

    public static void delete(String id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE().build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
        cache.remove(id);
    }

    public static void clearCache() { cache.clear(); }
}