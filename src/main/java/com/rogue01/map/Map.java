package com.rogue01.map;

public class Map {
    private int width, height;
    private Tile[][] tiles;
    
    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[width][height];
        generateMap();
    }
    
    private void generateMap() {
        // 간단한 맵 생성 - 나중에 프로시저럴 생성으로 확장
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    tiles[x][y] = new Tile('#', false); // 벽
                } else {
                    tiles[x][y] = new Tile('.', true);  // 바닥
                }
            }
        }
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
} 