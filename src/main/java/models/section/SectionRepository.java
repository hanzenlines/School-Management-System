package models.section;

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

public class SectionRepository {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String BASE_URL = "http://localhost:3000/sections";

    private SectionRepository() {}

    public static List<Section> getAll() throws IOException, InterruptedException  {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return Arrays.asList(mapper.readValue(response.body(), Section[].class));
    }

//    public static Section getById(String id) throws IOException, InterruptedException  {
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(BASE_URL + "/" + id))
//                .GET()
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        if (response.body().equals("{}") || response.body().isBlank()) return null;
//
//        return mapper.readValue(response.body(), Section.class);
//    }

    public static List<Section> getBySubjectCode(String subjectCode)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?subjectCode=" + subjectCode))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return Arrays.asList(mapper.readValue(response.body(), Section[].class));
    }

    public static void update(Section section) throws IOException, InterruptedException  {
        String body = mapper.writeValueAsString(section);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + section.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void save(Section section) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(section);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static List<Section> getByFacultyId(String facultyId)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?facultyId=" + facultyId))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("[]"))
            return Collections.emptyList();

        return Arrays.asList(mapper.readValue(response.body(), Section[].class));
    }

    private static final java.util.Map<String, Section> cache =
            new java.util.concurrent.ConcurrentHashMap<>();

    public static Section getById(String id)
            throws IOException, InterruptedException {

        if (cache.containsKey(id)) return cache.get(id);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body().equals("{}") || response.body().isBlank()) return null;

        Section section = mapper.readValue(response.body(), Section.class);

        if (section != null) cache.put(id, section);
        return section;
    }
}
