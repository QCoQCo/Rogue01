package com.rogue01.map.generators;

import com.rogue01.map.*;
import com.rogue01.map.structures.Room;
import com.rogue01.map.utils.RandomUtils;
import java.util.*;

public class HybridGenerator implements MapGenerator {
    private long seed;
    private List<Room> rooms;
    private long generationTime;
    private int lastWidth;
    private int lastHeight;
    private int chapter = 1;
    private int level = 1;
    private int lastStairsX = -1;
    private int lastStairsY = -1;
    private final List<int[]> lastSealWalls = new ArrayList<>();
    private final List<int[]> lastBossDoors = new ArrayList<>();

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
        this.lastWidth = width;
        this.lastHeight = height;
        this.rooms.clear();
        this.lastStairsX = -1;
        this.lastStairsY = -1;
        this.lastSealWalls.clear();
        this.lastBossDoors.clear();

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
        generateCentralArea(tiles, centerX - centerSize / 2, centerY - centerSize / 2, centerSize, centerSize);

        // 2. 중간부: 셀룰러 오토마타 (40%)
        generateMiddleArea(tiles, width, height, centerSize);

        // 3. 외곽부: BSP 방식 (30%)
        generateOuterArea(tiles, width, height, centerSize);

        // 4. 영역 간 연결
        connectAreas(tiles, width, height);

        // 5. 플레이어 시작 위치 설정 (중심부)
        setPlayerStartPosition(tiles, centerX, centerY);

        // 6. 계단 및 봉인벽 배치 (1층, 2층에만 - 3층은 보스방만)
        if (level <= 2) {
            placeStairsAndSealWalls(tiles, width, height, centerX, centerY);
        }

        // 7. 보스방 문 배치 (2층: 중간보스 2개 3x4, 3층: 챕터보스 1개 4x12)
        if (level >= 2) {
            placeBossDoors(tiles, width, height, centerX, centerY);
        }

        generationTime = System.currentTimeMillis() - startTime;

        return tiles;
    }

    @Override
    public void setChapterLevel(int chapter, int level) {
        this.chapter = Math.max(1, Math.min(3, chapter));
        this.level = Math.max(1, Math.min(3, level));
    }

    /**
     * 계단 배치 (플레이어와 거리 두고), 2층이면 주변 8칸 봉인벽
     */
    private void placeStairsAndSealWalls(Tile[][] tiles, int width, int height, int playerX, int playerY) {
        Random rand = new Random(seed + 1000);
        int minDist = 15;
        int attempts = 0;
        int stairsX = -1, stairsY = -1;

        while (attempts < 200) {
            int x = 10 + rand.nextInt(width - 20);
            int y = 10 + rand.nextInt(height - 20);
            int dist = (int) Math.sqrt(Math.pow(x - playerX, 2) + Math.pow(y - playerY, 2));
            if (dist >= minDist && tiles[x][y].isWalkable()) {
                stairsX = x;
                stairsY = y;
                break;
            }
            attempts++;
        }

        lastStairsX = stairsX;
        lastStairsY = stairsY;
        lastSealWalls.clear();

        if (stairsX >= 0 && stairsY >= 0) {
            tiles[stairsX][stairsY] = new Tile('>', true, Tile.TileType.STAIRS_DOWN);

            // 2층: 계단 주변 8칸 봉인벽
            if (level == 2) {
                int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
                int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};
                for (int i = 0; i < 8; i++) {
                    int nx = stairsX + dx[i];
                    int ny = stairsY + dy[i];
                    if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                        tiles[nx][ny] = new Tile('#', false, Tile.TileType.SEAL_WALL);
                        lastSealWalls.add(new int[]{nx, ny});
                    }
                }
            }
        }
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
                    Room.RoomType.CENTRAL);
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
                    Room.RoomType.OUTER);
            rooms.add(newRoom);
        }
    }

    private void connectAreas(Tile[][] tiles, int width, int height) {
        // 중심부와 다른 영역들을 연결하는 통로 생성
        int centerX = width / 2;
        int centerY = height / 2;

        // 4방향으로 연결 통로 생성
        createConnectionPath(tiles, centerX, centerY, centerX + width / 4, centerY);
        createConnectionPath(tiles, centerX, centerY, centerX - width / 4, centerY);
        createConnectionPath(tiles, centerX, centerY, centerX, centerY + height / 4);
        createConnectionPath(tiles, centerX, centerY, centerX, centerY - height / 4);
    }

    private void createConnectionPath(Tile[][] tiles, int x1, int y1, int x2, int y2) {
        int x = x1;
        int y = y1;

        while (x != x2 || y != y2) {
            if (x >= 0 && x < tiles.length && y >= 0 && y < tiles[0].length) {
                tiles[x][y] = new Tile('.', true);
            }

            if (x < x2)
                x++;
            else if (x > x2)
                x--;

            if (y < y2)
                y++;
            else if (y > y2)
                y--;
        }
    }

    private void setPlayerStartPosition(Tile[][] tiles, int centerX, int centerY) {
        // 플레이어 시작 위치를 중심부에 설정
        tiles[centerX][centerY] = new Tile('.', true);
    }

    /**
     * 보스방 문 배치: 2층=중간보스 2개(3x4), 3층=챕터보스 1개(4x12)
     * 기존 연결 통로 끝을 연장한 뒤 문 블록 배치
     */
    private void placeBossDoors(Tile[][] tiles, int width, int height, int centerX, int centerY) {
        Random rand = new Random(seed + 2000);
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        List<int[]> used = new ArrayList<>();

        if (level == 2) {
            // 중간보스 문 2개 (3x4) - 서로 다른 방향에 배치
            for (int i = 0; i < 2; i++) {
                for (int attempt = 0; attempt < 4; attempt++) {
                    int[] dir = dirs[(i * 2 + attempt) % 4];
                    if (placeBossDoorBlock(tiles, width, height, centerX, centerY, dir[0], dir[1], 3, 4, true, used)) {
                        used.add(dir);
                        break;
                    }
                }
            }
        } else if (level == 3) {
            // 챕터보스 문 1개 (4x12)
            int[] dir = dirs[rand.nextInt(4)];
            placeBossDoorBlock(tiles, width, height, centerX, centerY, dir[0], dir[1], 4, 12, false, used);
        }
    }

    /**
     * @param dx, dy 방향 (1,0)=오른쪽, (-1,0)=왼쪽, (0,1)=아래, (0,-1)=위
     * @param doorW, doorH 문 크기 (가로x세로)
     * @param isMidBoss true=중간보스(Troll), false=챕터보스(Dragon)
     */
    private boolean placeBossDoorBlock(Tile[][] tiles, int width, int height, int centerX, int centerY,
            int dx, int dy, int doorW, int doorH, boolean isMidBoss, List<int[]> usedDirs) {
        for (int[] u : usedDirs) {
            if (u[0] == dx && u[1] == dy) return false;
        }
        int dist = Math.min(width, height) / 3;
        int endX = centerX + dx * dist;
        int endY = centerY + dy * dist;
        endX = Math.max(2, Math.min(width - 2, endX));
        endY = Math.max(2, Math.min(height - 2, endY));

        // 통로 연장 (끝까지)
        createConnectionPath(tiles, centerX, centerY, endX, endY);
        // 문 앞 한 칸 더
        int doorFrontX = endX + dx;
        int doorFrontY = endY + dy;
        if (doorFrontX < 1 || doorFrontX >= width - 1 || doorFrontY < 1 || doorFrontY >= height - 1) {
            return false;
        }
        tiles[doorFrontX][doorFrontY] = new Tile('.', true);

        // 문 블록: dx>0이면 문이 end 오른쪽에 (doorFront가 문 왼쪽 끝)
        int baseX = doorFrontX + dx;
        int baseY = doorFrontY - doorH / 2;
        if (dy != 0) {
            baseX = doorFrontX - doorW / 2;
            baseY = doorFrontY + dy;
        }
        baseX = Math.max(0, Math.min(width - doorW, baseX));
        baseY = Math.max(0, Math.min(height - doorH, baseY));

        Tile.TileType doorType = isMidBoss ? Tile.TileType.BOSS_DOOR_MID : Tile.TileType.BOSS_DOOR_CHAPTER;
        for (int ox = 0; ox < doorW; ox++) {
            for (int oy = 0; oy < doorH; oy++) {
                int tx = baseX + ox;
                int ty = baseY + oy;
                if (tx >= 0 && tx < width && ty >= 0 && ty < height) {
                    char sym = getBossDoorSymbol(ox, oy, doorW, doorH, isMidBoss);
                    tiles[tx][ty] = new Tile(sym, false, doorType);
                    lastBossDoors.add(new int[]{tx, ty, isMidBoss ? 1 : 0});
                }
            }
        }
        return true;
    }

    /**
     * 보스 문 타일 심볼 - 챕터보스(4x12)는 역십자가 아스키아트, 중간보스는 'D'
     */
    private char getBossDoorSymbol(int ox, int oy, int doorW, int doorH, boolean isMidBoss) {
        if (isMidBoss) {
            return 'D';
        }
        // 챕터보스 4x12: 역십자가 (세로 막대 col 1-2, 가로 막대 row 5-6)
        if (doorW == 4 && doorH == 12) {
            boolean inVertical = (ox == 1 || ox == 2);
            boolean inHorizontal = (oy == 5 || oy == 6);
            if (inHorizontal && inVertical) return '+';
            if (inHorizontal) return '-';
            if (inVertical) return '|';
            return '#';
        }
        return 'D';
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        roomGenerator.setSeed(seed);
        cellularGenerator.setSeed(seed + 1);
        bspGenerator.setSeed(seed + 2);
    }

    @Override
    public MapGenerationInfo getGenerationInfo() {
        int playerStartX = lastWidth / 2;
        int playerStartY = lastHeight / 2;
        MapGenerationInfo info = new MapGenerationInfo(rooms.size(), rooms.size() * 2,
                playerStartX, playerStartY, rooms, generationTime);
        info.setStairs(lastStairsX, lastStairsY);
        for (int[] pos : lastSealWalls) {
            info.addSealWall(pos[0], pos[1]);
        }
        for (int[] pos : lastBossDoors) {
            info.addBossDoorTile(pos[0], pos[1], pos[2] == 1);
        }
        return info;
    }

    public List<Room> getRooms() {
        return new ArrayList<>(rooms);
    }
}