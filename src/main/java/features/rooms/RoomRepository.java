package features.rooms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.Room;

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

public class RoomRepository {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static final String BASE_URL = "http://localhost:3000/rooms";

    // cache
    private static final Map<String, Room> cache = new ConcurrentHashMap<>();

    private RoomRepository() {}

    public static List<Room> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("[]"))
            return Collections.emptyList();

        List<Room> rooms = Arrays.asList(
                mapper.readValue(response.body(), Room[].class));

        // populate cache
        rooms.forEach(r -> cache.put(r.getId(), r));
        return rooms;
    }

    public static Room getById(String id) throws IOException, InterruptedException {
        if (cache.containsKey(id)) return cache.get(id);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET().build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank()
                || response.body().equals("{}"))
            return null;

        Room room = mapper.readValue(response.body(), Room.class);
        if (room != null) cache.put(id, room);
        return room;
    }

    public static void save(Room room) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(room);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
        cache.put(room.getId(), room);
    }

    public static void update(Room room) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(room);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + room.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
        cache.put(room.getId(), room);
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