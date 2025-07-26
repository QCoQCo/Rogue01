package com.rogue01.map.generators;

import com.rogue01.map.*;
import com.rogue01.map.structures.Room;
import com.rogue01.map.utils.RandomUtils;
import java.util.*;

public class CellularGenerator implements MapGenerator {
    private long seed;
    private List<Room> rooms;
    private long generationTime;
    
    public CellularGenerator() {
        this.rooms = new ArrayList<>();
    }
    
    @Override
    public Tile[][] generate(int width, int height) {
        long startTime = System.currentTimeMillis();
        
        // 초기 랜덤 맵 생성
        Tile[][] tiles = new Tile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // 경계는 벽으로 설정
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    tiles[x][y] = new Tile('#', false);
                } else {
                    // 45% 확률로 벽 생성
                    boolean isWall = RandomUtils.nextBoolean(0.45);
                    tiles[x][y] = new Tile(isWall ? '#' : '.', !isWall);
                }
            }
        }
        
        // 셀룰러 오토마타 적용
        for (int iteration = 0; iteration < 4; iteration++) {
            tiles = applyCellularAutomata(tiles, width, height);
        }
        
        // 연결성 보장
        ensureConnectivity(tiles, width, height);
        
        generationTime = System.currentTimeMillis() - startTime;
        
        return tiles;
    }
    
    private Tile[][] applyCellularAutomata(Tile[][] tiles, int width, int height) {
        Tile[][] newTiles = new Tile[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    newTiles[x][y] = new Tile('#', false);
                } else {
                    int wallCount = countAdjacentWalls(tiles, x, y);
                    
                    // 셀룰러 오토마타 규칙
                    if (tiles[x][y].getSymbol() == '#') {
                        // 벽이면 주변에 벽이 4개 이상이면 벽 유지, 아니면 바닥으로
                        newTiles[x][y] = new Tile(wallCount >= 4 ? '#' : '.', wallCount < 4);
                    } else {
                        // 바닥이면 주변에 벽이 5개 이상이면 벽으로, 아니면 바닥 유지
                        newTiles[x][y] = new Tile(wallCount >= 5 ? '#' : '.', wallCount < 5);
                    }
                }
            }
        }
        
        return newTiles;
    }
    
    private int countAdjacentWalls(Tile[][] tiles, int x, int y) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                
                int nx = x + dx;
                int ny = y + dy;
                
                if (nx >= 0 && nx < tiles.length && ny >= 0 && ny < tiles[0].length) {
                    if (tiles[nx][ny].getSymbol() == '#') {
                        count++;
                    }
                } else {
                    count++; // 경계 밖은 벽으로 간주
                }
            }
        }
        return count;
    }
    
    private void ensureConnectivity(Tile[][] tiles, int width, int height) {
        // 간단한 연결성 보장: 중앙에서 시작해서 모든 방향으로 통로 생성
        int centerX = width / 2;
        int centerY = height / 2;
        
        // 중앙에서 4방향으로 통로 생성
        createPath(tiles, centerX, centerY, centerX + 5, centerY);
        createPath(tiles, centerX, centerY, centerX - 5, centerY);
        createPath(tiles, centerX, centerY, centerX, centerY + 5);
        createPath(tiles, centerX, centerY, centerX, centerY - 5);
    }
    
    private void createPath(Tile[][] tiles, int x1, int y1, int x2, int y2) {
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
    
    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        RandomUtils.setSeed(seed);
    }
    
    @Override
    public MapGenerationInfo getGenerationInfo() {
        // 셀룰러 오토마타는 방 개념이 없으므로 빈 리스트
        return new MapGenerationInfo(0, 0, 1, 1, new ArrayList<>(), generationTime);
    }
} 