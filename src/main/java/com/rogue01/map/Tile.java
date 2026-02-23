package com.rogue01.map;

/**
 * 맵 타일 - 심볼, 이동 가능 여부, 타입(렌더링/로직용)
 */
public class Tile {
    public enum TileType {
        FLOOR,           // 바닥
        WALL,            // 벽
        STAIRS_DOWN,     // 아래 계단
        SEAL_WALL,       // 봉인 벽 (보라색 #, 2층 계단 주변)
        RUBBLE,          // 무너진 벽 (노란색 =, 통과 가능)
        BOSS_DOOR_MID,   // 중간보스방 문 (3x4, 통과 불가)
        BOSS_DOOR_CHAPTER // 챕터 보스방 문 (4x12, 통과 불가)
    }

    private char symbol;
    private boolean walkable;
    private TileType type;

    public Tile(char symbol, boolean walkable) {
        this(symbol, walkable, walkable ? TileType.FLOOR : TileType.WALL);
    }

    public Tile(char symbol, boolean walkable, TileType type) {
        this.symbol = symbol;
        this.walkable = walkable;
        this.type = type;
    }

    public char getSymbol() { return symbol; }
    public boolean isWalkable() { return walkable; }
    public TileType getType() { return type; }

    public void setSymbol(char symbol) { this.symbol = symbol; }
    public void setWalkable(boolean walkable) { this.walkable = walkable; }
    public void setType(TileType type) { this.type = type; }

    public boolean isStairsDown() { return type == TileType.STAIRS_DOWN; }
    public boolean isSealWall() { return type == TileType.SEAL_WALL; }
    public boolean isRubble() { return type == TileType.RUBBLE; }
    public boolean isBossDoorMid() { return type == TileType.BOSS_DOOR_MID; }
    public boolean isBossDoorChapter() { return type == TileType.BOSS_DOOR_CHAPTER; }
    public boolean isBossDoor() { return type == TileType.BOSS_DOOR_MID || type == TileType.BOSS_DOOR_CHAPTER; }

    /** 봉인 벽을 무너뜨림 (SEAL_WALL → RUBBLE) */
    public void breakSeal() {
        if (type == TileType.SEAL_WALL) {
            type = TileType.RUBBLE;
            symbol = '=';
            walkable = true;
        }
    }
}