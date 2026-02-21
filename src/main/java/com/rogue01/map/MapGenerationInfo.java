package com.rogue01.map;

import java.util.List;
import com.rogue01.map.structures.Room;

public class MapGenerationInfo {
    private int roomCount;
    private int corridorCount;
    private int playerStartX;
    private int playerStartY;
    private List<Room> rooms;
    private long generationTime;

    public MapGenerationInfo(int roomCount, int corridorCount, int playerStartX, int playerStartY, List<Room> rooms,
            long generationTime) {
        this.roomCount = roomCount;
        this.corridorCount = corridorCount;
        this.playerStartX = playerStartX;
        this.playerStartY = playerStartY;
        this.rooms = rooms;
        this.generationTime = generationTime;
    }

    // Getters
    public int getRoomCount() {
        return roomCount;
    }

    public int getCorridorCount() {
        return corridorCount;
    }

    public int getPlayerStartX() {
        return playerStartX;
    }

    public int getPlayerStartY() {
        return playerStartY;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public long getGenerationTime() {
        return generationTime;
    }

    @Override
    public String toString() {
        return String.format("Map Generation Info: %d rooms, %d corridors, Player at (%d,%d), Generated in %dms",
                roomCount, corridorCount, playerStartX, playerStartY, generationTime);
    }
}