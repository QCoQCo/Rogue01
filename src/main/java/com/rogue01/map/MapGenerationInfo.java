package com.rogue01.map;

import java.util.ArrayList;
import java.util.List;
import com.rogue01.map.structures.Room;

public class MapGenerationInfo {
    private int roomCount;
    private int corridorCount;
    private int playerStartX;
    private int playerStartY;
    private List<Room> rooms;
    private long generationTime;

    /** 아래 계단 위치 (없으면 -1,-1) */
    private int stairsX = -1;
    private int stairsY = -1;

    /** 봉인 벽 8칸 좌표 (2층 계단 주변, midBossDefeated 시 breakSeal) */
    private final List<int[]> sealWallPositions = new ArrayList<>();

    /** 보스방 문 타일 좌표 (각 int[] = {x, y, isMidBoss: 1=중간보스, 0=챕터보스}) */
    private final List<int[]> bossDoorPositions = new ArrayList<>();

    public MapGenerationInfo(int roomCount, int corridorCount, int playerStartX, int playerStartY, List<Room> rooms,
            long generationTime) {
        this.roomCount = roomCount;
        this.corridorCount = corridorCount;
        this.playerStartX = playerStartX;
        this.playerStartY = playerStartY;
        this.rooms = rooms;
        this.generationTime = generationTime;
    }

    public int getRoomCount() { return roomCount; }
    public int getCorridorCount() { return corridorCount; }
    public int getPlayerStartX() { return playerStartX; }
    public int getPlayerStartY() { return playerStartY; }
    public List<Room> getRooms() { return rooms; }
    public long getGenerationTime() { return generationTime; }

    public int getStairsX() { return stairsX; }
    public int getStairsY() { return stairsY; }
    public void setStairs(int x, int y) { this.stairsX = x; this.stairsY = y; }

    public List<int[]> getSealWallPositions() { return sealWallPositions; }
    public void addSealWall(int x, int y) { sealWallPositions.add(new int[]{x, y}); }

    public List<int[]> getBossDoorPositions() { return bossDoorPositions; }
    public void addBossDoorTile(int x, int y, boolean isMidBoss) {
        bossDoorPositions.add(new int[]{x, y, isMidBoss ? 1 : 0});
    }

    @Override
    public String toString() {
        return String.format("Map Generation Info: %d rooms, %d corridors, Player at (%d,%d), Stairs at (%d,%d), Generated in %dms",
                roomCount, corridorCount, playerStartX, playerStartY, stairsX, stairsY, generationTime);
    }
}