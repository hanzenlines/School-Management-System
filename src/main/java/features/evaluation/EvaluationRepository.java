package features.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.FacultyEvaluation;
import models.enums.Semester;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EvaluationRepository {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static final String BASE_URL = "http://localhost:3000/facultyEvaluations";

    private EvaluationRepository() {}

    public static List<FacultyEvaluation> getAll()
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("[]"))
            return Collections.emptyList();

        return Arrays.asList(
                mapper.readValue(response.body(), FacultyEvaluation[].class));
    }

    public static List<FacultyEvaluation> getByFacultyId(String facultyId)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?facultyId=" + facultyId))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("[]"))
            return Collections.emptyList();

        return Arrays.asList(
                mapper.readValue(response.body(), FacultyEvaluation[].class));
    }

    public static List<FacultyEvaluation> getByStudentId(String studentId)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?studentId=" + studentId))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("[]"))
            return Collections.emptyList();

        return Arrays.asList(
                mapper.readValue(response.body(), FacultyEvaluation[].class));
    }

    /**
     * Check if a student has already evaluated a specific faculty
     * for a given section and semester.
     */
    public static boolean existsByStudentFacultyAndSection(
            String studentId, String facultyId,
            String sectionId, Semester semester)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL
                        + "?studentId=" + studentId
                        + "&facultyId=" + facultyId
                        + "&sectionId=" + sectionId
                        + "&semester=" + semester))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("[]"))
            return false;

        FacultyEvaluation[] results = mapper.readValue(
                response.body(), FacultyEvaluation[].class);
        return results.length > 0;
    }

    public static void save(FacultyEvaluation evaluation)
            throws IOException, InterruptedException {

        String body = mapper.writeValueAsString(evaluation);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}