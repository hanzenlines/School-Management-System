package features.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.Course;

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

public class CourseRepository {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static final String BASE_URL = "http://localhost:3000/courses";

    private static final Map<String, Course> cacheById   = new ConcurrentHashMap<>();
    private static final Map<String, Course> cacheByCode = new ConcurrentHashMap<>();

    private CourseRepository() {}

    public static List<Course> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("[]"))
            return Collections.emptyList();

        List<Course> courses = Arrays.asList(
                mapper.readValue(response.body(), Course[].class));

        courses.forEach(c -> {
            cacheById.put(c.getId(), c);
            cacheByCode.put(c.getCode(), c);
        });
        return courses;
    }

    public static Course getByCode(String code) throws IOException, InterruptedException {
        if (cacheByCode.containsKey(code)) return cacheByCode.get(code);

        // fetch all to populate cache, then return match
        List<Course> all = getAll();
        return all.stream()
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    public static void clearCache() {
        cacheById.clear();
        cacheByCode.clear();
    }
}