package com.rogue01.map.generators;

import com.rogue01.map.*;
import com.rogue01.map.structures.Room;
import com.rogue01.map.utils.RandomUtils;
import java.util.*;

public class CellularGenerator implements MapGenerator {
    private long seed;
    private List<Room> rooms;
    private long generationTime;
    private RandomUtils randomUtils;

    public CellularGenerator() {
        this.rooms = new ArrayList<>();
        this.randomUtils = new RandomUtils();
    }

    public CellularGenerator(long seed) {
        this.rooms = new ArrayList<>();
        this.randomUtils = new RandomUtils(seed);
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
                    boolean isWall = randomUtils.nextBoolean(0.45);
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

        // 방 생성 (셀룰러 오토마타 결과를 기반으로)
        generateRoomsFromTiles(tiles, width, height);

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
                if (dx == 0 && dy == 0)
                    continue;

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
        createPath(tiles, centerX, centerY, centerX, 0); // 위쪽
        createPath(tiles, centerX, centerY, centerX, height - 1); // 아래쪽
        createPath(tiles, centerX, centerY, 0, centerY); // 왼쪽
        createPath(tiles, centerX, centerY, width - 1, centerY); // 오른쪽
    }

    private void createPath(Tile[][] tiles, int x1, int y1, int x2, int y2) {
        int x = x1, y = y1;
        while (x != x2 || y != y2) {
            if (x < x2)
                x++;
            else if (x > x2)
                x--;
            if (y < y2)
                y++;
            else if (y > y2)
                y--;

            if (x >= 0 && x < tiles.length && y >= 0 && y < tiles[0].length) {
                tiles[x][y] = new Tile('.', true);
            }
        }
    }

    /**
     * 셀룰러 오토마타 결과를 기반으로 방 생성
     */
    private void generateRoomsFromTiles(Tile[][] tiles, int width, int height) {
        // 바닥 타일들을 그룹화하여 방으로 만들기
        boolean[][] visited = new boolean[width][height];

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (!visited[x][y] && tiles[x][y].isWalkable()) {
                    // 연결된 바닥 타일들을 찾아서 방으로 만들기
                    List<int[]> roomTiles = new ArrayList<>();
                    floodFill(tiles, visited, x, y, roomTiles);

                    if (roomTiles.size() >= 9) { // 최소 3x3 크기
                        createRoomFromTiles(roomTiles);
                    }
                }
            }
        }
    }

    private void floodFill(Tile[][] tiles, boolean[][] visited, int startX, int startY, List<int[]> roomTiles) {
        // 스택 오버플로우 방지를 위해 반복문 사용
        java.util.Stack<int[]> stack = new java.util.Stack<>();
        stack.push(new int[] { startX, startY });

        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int x = current[0];
            int y = current[1];

            if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length ||
                    visited[x][y] || !tiles[x][y].isWalkable()) {
                continue;
            }

            visited[x][y] = true;
            roomTiles.add(new int[] { x, y });

            // 4방향으로 확장
            stack.push(new int[] { x + 1, y });
            stack.push(new int[] { x - 1, y });
            stack.push(new int[] { x, y + 1 });
            stack.push(new int[] { x, y - 1 });
        }
    }

    private void createRoomFromTiles(List<int[]> roomTiles) {
        if (roomTiles.isEmpty())
            return;

        // 방의 경계 계산
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        for (int[] tile : roomTiles) {
            minX = Math.min(minX, tile[0]);
            minY = Math.min(minY, tile[1]);
            maxX = Math.max(maxX, tile[0]);
            maxY = Math.max(maxY, tile[1]);
        }

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        // Room 객체 생성
        Room room = new Room(minX, minY, width, height, Room.RoomType.MIDDLE);
        rooms.add(room);
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

        return new MapGenerationInfo(rooms.size(), 0, playerStartX, playerStartY, rooms, generationTime);
    }

    @Override
    public List<Room> getRooms() {
        return new ArrayList<>(rooms);
    }
}