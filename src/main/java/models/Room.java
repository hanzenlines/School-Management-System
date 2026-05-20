package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.RoomType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Room {
    private final String id;
    private final String roomName;
    private final int capacity;
    private final RoomType roomType;
    private boolean isActive;

    @JsonCreator
    public Room(
            @JsonProperty("id")         String id,
            @JsonProperty("roomName")   String roomName,
            @JsonProperty("capacity")   int capacity,
            @JsonProperty("roomType")   RoomType roomType,
            @JsonProperty("isActive")    Boolean isActive
    ) {
        this.id = id;
        this.roomName = roomName;
        this.capacity = capacity;
        this.roomType = roomType;
        this.isActive = isActive;
    }

    public String getId() { return id; }

    public String getRoomName() { return roomName; }

    public int getCapacity() { return capacity; }

    public RoomType getRoomType() { return roomType; }

    public boolean isActive() { return isActive; }

    public void setActive(boolean active) { this.isActive = active; }
}
 