package models.faculty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

public class FacultyRepository {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static final String BASE_URL = "http://localhost:3000/accounts";

    private static final Map<String, Faculty> cache = new ConcurrentHashMap<>();

    private FacultyRepository() {}

    public static List<Faculty> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?userType=FACULTY"))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("[]"))
            return Collections.emptyList();

        List<Faculty> faculty = Arrays.asList(
                mapper.readValue(response.body(), Faculty[].class));

        faculty.forEach(f -> cache.put(f.getId(), f));
        return faculty;
    }

    public static Faculty getById(String id) throws IOException, InterruptedException {
        if (cache.containsKey(id)) return cache.get(id);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("{}"))
            return null;

        Faculty faculty = mapper.readValue(response.body(), Faculty.class);
        if (faculty != null) cache.put(id, faculty);
        return faculty;
    }

    public static void update(Faculty faculty) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(faculty);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + faculty.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
        cache.put(faculty.getId(), faculty);
    }

    public static void clearCache() { cache.clear(); }
}