package com.rogue01.map;

import com.rogue01.map.generators.HybridGenerator;
import com.rogue01.map.structures.Room;
import java.util.List;

public class Map {
    private int width, height;
    private Tile[][] tiles;
    private HybridGenerator generator;
    private MapGenerationInfo generationInfo;
    
    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.generator = new HybridGenerator();
        generateMap();
    }
    
    private void generateMap() {
        // 하이브리드 생성기로 맵 생성
        generator.setSeed(System.currentTimeMillis()); // 현재 시간을 시드로 사용
        this.tiles = generator.generate(width, height);
        this.generationInfo = generator.getGenerationInfo();
        
        System.out.println("Generated map: " + generationInfo.toString());
    }
    
    public void update() {
        // 맵 업데이트 로직
    }
    
    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        return tiles[x][y].isWalkable();
    }
    
    public char getTileSymbol(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return '#';
        }
        return tiles[x][y].getSymbol();
    }
    
    // Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Tile[][] getTiles() { return tiles; }
    public List<Room> getRooms() { return generator.getRooms(); }
    public MapGenerationInfo getGenerationInfo() { return generationInfo; }
} 