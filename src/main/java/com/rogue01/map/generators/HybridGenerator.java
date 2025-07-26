package com.rogue01.map.generators;

import com.rogue01.map.*;
import com.rogue01.map.structures.Room;
import com.rogue01.map.utils.RandomUtils;
import java.util.*;

public class HybridGenerator implements MapGenerator {
    private long seed;
    private List<Room> rooms;
    private long generationTime;
    
    // 각 생성기
    private RoomCorridorGenerator roomGenerator;
    private CellularGenerator cellularGenerator;
    private BSPGenerator bspGenerator;
    
    public HybridGenerator() {
        this.rooms = new ArrayList<>();
        this.roomGenerator = new RoomCorridorGenerator();
        this.cellularGenerator = new CellularGenerator();
        this.bspGenerator = new BSPGenerator();
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
        
        // 영역별 생성
        int centerSize = Math.min(width, height) / 3;
        int centerX = width / 2;
        int centerY = height / 2;
        
        // 1. 중심부: 방-통로 방식 (30%)
        generateCentralArea(tiles, centerX - centerSize/2, centerY - centerSize/2, centerSize, centerSize);
        
        // 2. 중간부: 셀룰러 오토마타 (40%)
        generateMiddleArea(tiles, width, height, centerSize);
        
        // 3. 외곽부: BSP 방식 (30%)
        generateOuterArea(tiles, width, height, centerSize);
        
        // 4. 영역 간 연결
        connectAreas(tiles, width, height);
        
        // 5. 플레이어 시작 위치 설정 (중심부)
        setPlayerStartPosition(tiles, centerX, centerY);
        
        generationTime = System.currentTimeMillis() - startTime;
        
        return tiles;
    }
    
    private void generateCentralArea(Tile[][] tiles, int startX, int startY, int sizeX, int sizeY) {
        // 중심부 영역에 방-통로 방식 적용
        Tile[][] centralTiles = roomGenerator.generate(sizeX, sizeY);
        
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                int mapX = startX + x;
                int mapY = startY + y;
                
                if (mapX >= 0 && mapX < tiles.length && mapY >= 0 && mapY < tiles[0].length) {
                    tiles[mapX][mapY] = centralTiles[x][y];
                }
            }
        }
        
        // 방 정보 복사
        for (Room room : roomGenerator.getRooms()) {
            Room newRoom = new Room(
                room.getX() + startX, 
                room.getY() + startY, 
                room.getWidth(), 
                room.getHeight(), 
                Room.RoomType.CENTRAL
            );
            rooms.add(newRoom);
        }
    }
    
    private void generateMiddleArea(Tile[][] tiles, int width, int height, int centerSize) {
        // 중간부 영역에 셀룰러 오토마타 적용
        int middleSize = centerSize * 2;
        int startX = Math.max(0, (width - middleSize) / 2);
        int startY = Math.max(0, (height - middleSize) / 2);
        
        Tile[][] middleTiles = cellularGenerator.generate(middleSize, middleSize);
        
        for (int x = 0; x < middleSize; x++) {
            for (int y = 0; y < middleSize; y++) {
                int mapX = startX + x;
                int mapY = startY + y;
                
                if (mapX >= 0 && mapX < tiles.length && mapY >= 0 && mapY < tiles[0].length) {
                    // 중심부와 겹치지 않는 경우에만 적용
                    if (tiles[mapX][mapY].getSymbol() == '#') {
                        tiles[mapX][mapY] = middleTiles[x][y];
                    }
                }
            }
        }
    }
    
    private void generateOuterArea(Tile[][] tiles, int width, int height, int centerSize) {
        // 외곽부 영역에 BSP 방식 적용
        int outerSize = Math.min(width, height);
        int startX = Math.max(0, (width - outerSize) / 2);
        int startY = Math.max(0, (height - outerSize) / 2);
        
        Tile[][] outerTiles = bspGenerator.generate(outerSize, outerSize);
        
        for (int x = 0; x < outerSize; x++) {
            for (int y = 0; y < outerSize; y++) {
                int mapX = startX + x;
                int mapY = startY + y;
                
                if (mapX >= 0 && mapX < tiles.length && mapY >= 0 && mapY < tiles[0].length) {
                    // 기존 영역과 겹치지 않는 경우에만 적용
                    if (tiles[mapX][mapY].getSymbol() == '#') {
                        tiles[mapX][mapY] = outerTiles[x][y];
                    }
                }
            }
        }
        
        // BSP 방 정보 복사
        for (Room room : bspGenerator.getRooms()) {
            Room newRoom = new Room(
                room.getX() + startX, 
                room.getY() + startY, 
                room.getWidth(), 
                room.getHeight(), 
                Room.RoomType.OUTER
            );
            rooms.add(newRoom);
        }
    }
    
    private void connectAreas(Tile[][] tiles, int width, int height) {
        // 중심부와 다른 영역들을 연결하는 통로 생성
        int centerX = width / 2;
        int centerY = height / 2;
        
        // 4방향으로 연결 통로 생성
        createConnectionPath(tiles, centerX, centerY, centerX + width/4, centerY);
        createConnectionPath(tiles, centerX, centerY, centerX - width/4, centerY);
        createConnectionPath(tiles, centerX, centerY, centerX, centerY + height/4);
        createConnectionPath(tiles, centerX, centerY, centerX, centerY - height/4);
    }
    
    private void createConnectionPath(Tile[][] tiles, int x1, int y1, int x2, int y2) {
        int x = x1;
        int y = y1;
        
        while (x != x2 || y != y2) {
            if (x >= 0 && x < tiles.length && y >= 0 && y < tiles[0].length) {
                tiles[x][y] = new Tile('.', true);
            }
            
            if (x < x2) x++;
            else if (x > x2) x--;
            
            if (y < y2) y++;
            else if (y > y2) y--;
        }
    }
    
    private void setPlayerStartPosition(Tile[][] tiles, int centerX, int centerY) {
        // 플레이어 시작 위치를 중심부에 설정
        tiles[centerX][centerY] = new Tile('.', true);
    }
    
    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        RandomUtils.setSeed(seed);
        roomGenerator.setSeed(seed);
        cellularGenerator.setSeed(seed + 1);
        bspGenerator.setSeed(seed + 2);
    }
    
    @Override
    public MapGenerationInfo getGenerationInfo() {
        // 맵 크기는 생성 시점에 알 수 없으므로 기본값 사용
        int playerStartX = 50; // 기본 맵 크기의 중앙
        int playerStartY = 30;
        
        return new MapGenerationInfo(rooms.size(), rooms.size() * 2, playerStartX, playerStartY, rooms, generationTime);
    }
    
    public List<Room> getRooms() {
        return new ArrayList<>(rooms);
    }
} 