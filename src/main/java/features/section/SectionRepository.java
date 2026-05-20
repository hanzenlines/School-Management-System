package features.section;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.Section;

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

public class SectionRepository {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static final String BASE_URL = "http://localhost:3000/sections";
    private static final Map<String, Section> cache = new ConcurrentHashMap<>();

    private SectionRepository() {}

    public static List<Section> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("[]"))
            return Collections.emptyList();

        List<Section> sections = Arrays.asList(
                mapper.readValue(response.body(), Section[].class));
        sections.forEach(s -> cache.put(s.getId(), s));
        return sections;
    }

    public static Section getById(String id) throws IOException, InterruptedException {
        if (cache.containsKey(id)) return cache.get(id);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("{}")) return null;

        Section section = mapper.readValue(response.body(), Section.class);
        if (section != null) cache.put(id, section);
        return section;
    }

    public static List<Section> getBySubjectCode(String subjectCode)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?subjectCode=" + subjectCode))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("[]"))
            return Collections.emptyList();

        return Arrays.asList(mapper.readValue(response.body(), Section[].class));
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

    // used by ScheduleService delete guard
    public static List<Section> getByScheduleId(String scheduleId)
            throws IOException, InterruptedException {

        // json-server can't query inside arrays directly,
        // so fetch all and filter client-side
        return getAll().stream()
                .filter(s -> s.getScheduleIds() != null
                        && s.getScheduleIds().contains(scheduleId))
                .collect(java.util.stream.Collectors.toList());
    }

    public static void save(Section section) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(section);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
        cache.put(section.getId(), section);
    }

    public static void update(Section section) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(section);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + section.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body)).build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
        cache.put(section.getId(), section);
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