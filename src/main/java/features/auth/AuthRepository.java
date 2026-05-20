package features.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Account;
import models.Admin;
import models.Faculty;
import models.Student;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthRepository {

    private static HttpClient client = HttpClient.newHttpClient();
    private AuthRepository(){}

    public static Account getAccountByEmail(String email) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:3000/accounts?email=" + email))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode array = mapper.readTree(response.body());

        if (!array.isArray() || array.isEmpty()) return null;

        JsonNode node = array.get(0);
        String userTypeStr = node.get("userType").asText();

        return switch (userTypeStr.toUpperCase()) {
            case "ADMIN"   -> mapper.treeToValue(node, Admin.class);
            case "FACULTY" -> mapper.treeToValue(node, Faculty.class);
            case "STUDENT" -> mapper.treeToValue(node, Student.class);
            default        -> null;
        };
    }
}
