package com.rogue01.map;

import com.rogue01.map.generators.*;
import com.rogue01.map.structures.Room;
import java.util.List;

/**
 * 게임 맵을 관리하는 클래스
 */
public class Map {
    private int width, height;
    private Tile[][] tiles;
    private MapGenerator generator;
    private MapGenerationInfo generationInfo;
    
    /**
     * 기본 생성자 - 하이브리드 생성기 사용
     */
    public Map(int width, int height) {
        this(width, height, MapGeneratorType.HYBRID);
    }
    
    /**
     * 생성기 타입을 지정하는 생성자
     */
    public Map(int width, int height, MapGeneratorType generatorType) {
        this.width = width;
        this.height = height;
        this.generator = createGenerator(generatorType);
        generateMap();
    }
    
    /**
     * 특정 생성기를 직접 지정하는 생성자
     */
    public Map(int width, int height, MapGenerator generator) {
        this.width = width;
        this.height = height;
        this.generator = generator;
        generateMap();
    }
    
    /**
     * 생성기 타입에 따른 생성기 인스턴스 생성
     */
    private MapGenerator createGenerator(MapGeneratorType type) {
        switch (type) {
            case ROOM_CORRIDOR:
                return new RoomCorridorGenerator();
            case CELLULAR:
                return new CellularGenerator();
            case BSP:
                return new BSPGenerator();
            case HYBRID:
            default:
                return new HybridGenerator();
        }
    }
    
    /**
     * 맵 생성
     */
    private void generateMap() {
        long seed = System.currentTimeMillis();
        generator.setSeed(seed);
        this.tiles = generator.generate(width, height);
        this.generationInfo = generator.getGenerationInfo();
        
        System.out.println("Generated map: " + generationInfo.toString());
    }
    
    /**
     * 새로운 생성기로 맵 재생성
     */
    public void regenerate(MapGeneratorType generatorType) {
        this.generator = createGenerator(generatorType);
        generateMap();
    }
    
    /**
     * 특정 시드로 맵 재생성
     */
    public void regenerate(long seed) {
        generator.setSeed(seed);
        this.tiles = generator.generate(width, height);
        this.generationInfo = generator.getGenerationInfo();
    }
    
    /**
     * 맵 업데이트 로직
     */
    public void update() {
        // 맵 업데이트 로직
    }
    
    /**
     * 지정된 위치가 이동 가능한지 확인
     */
    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        return tiles[x][y].isWalkable();
    }
    
    /**
     * 지정된 위치의 타일 심볼 반환
     */
    public char getTileSymbol(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return '#';
        }
        return tiles[x][y].getSymbol();
    }
    
    /**
     * 지정된 위치의 타일 반환
     */
    public Tile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }
        return tiles[x][y];
    }
    
    /**
     * 맵 경계 내에 있는지 확인
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    // Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Tile[][] getTiles() { return tiles; }
    public List<Room> getRooms() { return generator.getRooms(); }
    public MapGenerationInfo getGenerationInfo() { return generationInfo; }
    public MapGenerator getGenerator() { return generator; }
    
    // Setters
    public void setGenerator(MapGenerator generator) { 
        this.generator = generator; 
    }
    
    /**
     * 맵 생성기 타입을 정의하는 enum
     */
    public enum MapGeneratorType {
        ROOM_CORRIDOR,
        CELLULAR,
        BSP,
        HYBRID
    }
} 