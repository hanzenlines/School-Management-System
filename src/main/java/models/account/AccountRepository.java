package models.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.faculty.Faculty;
import models.section.Section;
import models.student.Student;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class AccountRepository {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String BASE_URL = "http://localhost:3000/accounts";

    private AccountRepository() {}

    public static List<Section> getAll() throws IOException, InterruptedException  {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return Arrays.asList(mapper.readValue(response.body(), Section[].class));
    }

//    public static Account getById(String id) throws IOException, InterruptedException  {
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(BASE_URL + "/" + id))
//                .GET()
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        if (response.body().equals("{}") || response.body().isBlank()) return null;
//
//        return mapper.readValue(response.body(), Account.class);
//    }

    public static Student getStudentById(String id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body().equals("{}") || response.body().isBlank()) return null;

        return mapper.readValue(response.body(), Student.class);
    }

    public static void update(Student student) throws IOException, InterruptedException  {
        String body = mapper.writeValueAsString(student);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + student.getId()))
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

    private static final java.util.Map<String, Account> cache =
            new java.util.concurrent.ConcurrentHashMap<>();

    public static Account getById(String id)
            throws IOException, InterruptedException {

        if (cache.containsKey(id)) return cache.get(id);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body().equals("{}") || response.body().isBlank()) return null;

        Account account = mapper.readValue(response.body(), Account.class);

        if (account != null) cache.put(id, account);
        return account;
    }

    // add a getFacultyById that deserializes to Faculty
    public static Faculty getFacultyById(String id)
            throws IOException, InterruptedException {

        if (cache.containsKey(id)) {
            Account cached = cache.get(id);
            // re-deserialize as Faculty if needed
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body().equals("{}") || response.body().isBlank()) return null;

        Faculty faculty = mapper.readValue(response.body(), Faculty.class);
        if (faculty != null) cache.put(id, faculty);
        return faculty;
    }
}

