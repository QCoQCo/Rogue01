package com.rogue01.map.generators;

import com.rogue01.map.*;
import com.rogue01.map.structures.Room;
import com.rogue01.map.utils.RandomUtils;
import java.util.*;

public class RoomCorridorGenerator implements MapGenerator {
    private long seed;
    private List<Room> rooms;
    private int corridorCount;
    private long generationTime;
    private RandomUtils randomUtils;
    
    public RoomCorridorGenerator() {
        this.rooms = new ArrayList<>();
        this.corridorCount = 0;
        this.randomUtils = new RandomUtils();
    }
    
    public RoomCorridorGenerator(long seed) {
        this.rooms = new ArrayList<>();
        this.corridorCount = 0;
        this.randomUtils = new RandomUtils(seed);
    }
    
    @Override
    public Tile[][] generate(int width, int height) {
        long startTime = System.currentTimeMillis();
        
        // 맵을 벽으로 초기화
        Tile[][] tiles = new Tile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = new Tile('#', false);
            }
        }
        
        // 방 생성
        generateRooms(tiles, width, height);
        
        // 통로 생성
        generateCorridors(tiles);
        
        generationTime = System.currentTimeMillis() - startTime;
        
        return tiles;
    }
    
    private void generateRooms(Tile[][] tiles, int width, int height) {
        int maxRooms = 8;
        int minRoomSize = 5;
        int maxRoomSize = Math.min(12, Math.min(width - 2, height - 2)); // 맵 크기에 맞게 조정
        
        for (int i = 0; i < maxRooms; i++) {
            int roomWidth = randomUtils.nextInt(minRoomSize, maxRoomSize);
            int roomHeight = randomUtils.nextInt(minRoomSize, maxRoomSize);
            
            // 유효한 범위 계산
            int maxX = Math.max(1, width - roomWidth - 1);
            int maxY = Math.max(1, height - roomHeight - 1);
            
            if (maxX <= 1 || maxY <= 1) {
                // 맵이 너무 작으면 방 생성을 중단
                break;
            }
            
            int x = randomUtils.nextInt(1, maxX);
            int y = randomUtils.nextInt(1, maxY);
            
            Room newRoom = new Room(x, y, roomWidth, roomHeight, Room.RoomType.CENTRAL);
            
            // 기존 방과 겹치지 않는지 확인
            boolean failed = false;
            for (Room otherRoom : rooms) {
                if (newRoom.intersects(otherRoom)) {
                    failed = true;
                    break;
                }
            }
            
            if (!failed) {
                createRoom(tiles, newRoom);
                rooms.add(newRoom);
            }
        }
    }
    
    private void createRoom(Tile[][] tiles, Room room) {
        for (int x = room.getX(); x < room.getX() + room.getWidth(); x++) {
            for (int y = room.getY(); y < room.getY() + room.getHeight(); y++) {
                tiles[x][y] = new Tile('.', true);
            }
        }
    }
    
    private void generateCorridors(Tile[][] tiles) {
        for (int i = 0; i < rooms.size() - 1; i++) {
            Room room1 = rooms.get(i);
            Room room2 = rooms.get(i + 1);
            
            // 방 중앙점 계산
            int x1 = room1.getCenterX();
            int y1 = room1.getCenterY();
            int x2 = room2.getCenterX();
            int y2 = room2.getCenterY();
            
            // L자 통로 생성
            if (randomUtils.nextBoolean(0.5)) {
                // 먼저 수평, 그 다음 수직
                createHorizontalCorridor(tiles, x1, x2, y1);
                createVerticalCorridor(tiles, y1, y2, x2);
            } else {
                // 먼저 수직, 그 다음 수평
                createVerticalCorridor(tiles, y1, y2, x1);
                createHorizontalCorridor(tiles, x1, x2, y2);
            }
            corridorCount++;
        }
    }
    
    private void createHorizontalCorridor(Tile[][] tiles, int x1, int x2, int y) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            if (x >= 0 && x < tiles.length && y >= 0 && y < tiles[0].length) {
                tiles[x][y] = new Tile('.', true);
            }
        }
    }
    
    private void createVerticalCorridor(Tile[][] tiles, int y1, int y2, int x) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            if (x >= 0 && x < tiles.length && y >= 0 && y < tiles[0].length) {
                tiles[x][y] = new Tile('.', true);
            }
        }
    }
    
    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        this.randomUtils = new RandomUtils(seed);
    }
    
    @Override
    public MapGenerationInfo getGenerationInfo() {
        int playerStartX = rooms.isEmpty() ? 1 : rooms.get(0).getCenterX();
        int playerStartY = rooms.isEmpty() ? 1 : rooms.get(0).getCenterY();
        
        return new MapGenerationInfo(rooms.size(), corridorCount, playerStartX, playerStartY, rooms, generationTime);
    }
    
    public List<Room> getRooms() {
        return new ArrayList<>(rooms);
    }
} 