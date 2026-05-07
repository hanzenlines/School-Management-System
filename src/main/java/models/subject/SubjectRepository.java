package models.subject;

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

public class SubjectRepository {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static final String BASE_URL = "http://localhost:3000/subjects";

    private SubjectRepository() {}

    private static boolean isInvalidResponse(String body) {
        return body == null || body.isBlank() || !body.startsWith("[") && !body.startsWith("{");
    }

    public static List<Subject> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (isInvalidResponse(response.body())) return Collections.emptyList();

        return Arrays.asList(mapper.readValue(response.body(), Subject[].class));
    }

//    public static Subject getByCode(String subjectCode)
//            throws IOException, InterruptedException {
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(BASE_URL + "?subjectCode=" + subjectCode))
//                .GET()
//                .build();
//
//        HttpResponse<String> response = client.send(request,
//                HttpResponse.BodyHandlers.ofString());
//
//        if (isInvalidResponse(response.body())) return null;
//
//        Subject[] results = mapper.readValue(response.body(), Subject[].class);
//        return results.length > 0 ? results[0] : null;
//    }

    public static List<Subject> getByCourseAndYearLevel(String course, int yearLevel)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?course=" + course + "&yearLevel=" + yearLevel))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (isInvalidResponse(response.body())) return Collections.emptyList();

        return Arrays.asList(mapper.readValue(response.body(), Subject[].class));
    }

    public static void save(Subject subject) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(subject);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void update(Subject subject) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(subject);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + subject.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void delete(String id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE()
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static final java.util.Map<String, Subject> cache =
            new java.util.concurrent.ConcurrentHashMap<>();

    public static Subject getByCode(String subjectCode)
            throws IOException, InterruptedException {

        if (cache.containsKey(subjectCode)) return cache.get(subjectCode);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?subjectCode=" + subjectCode))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (isInvalidResponse(response.body())) return null;

        Subject[] results = mapper.readValue(response.body(), Subject[].class);
        Subject subject = results.length > 0 ? results[0] : null;

        if (subject != null) cache.put(subjectCode, subject);
        return subject;
    }
}