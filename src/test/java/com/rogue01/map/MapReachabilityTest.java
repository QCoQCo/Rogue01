package com.rogue01.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rogue01.map.generators.HybridGenerator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * 생성된 맵에서 계단과 보스 문에 실제로 도달할 수 있는지 검사 (게임 진행 불가 방지)
 * 생성기 내부 검증과 독립적으로 BFS를 직접 수행한다.
 */
class MapReachabilityTest {
    private static final int WIDTH = 750;
    private static final int HEIGHT = 450;
    private static final int SEEDS_PER_LEVEL = 60;
    private static final int[][] DIRS = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

    @ParameterizedTest(name = "chapter 1, level {0}")
    @ValueSource(ints = { 1, 2, 3 })
    void stairsAndBossDoorsAreReachable(int level) {
        for (long seed = 0; seed < SEEDS_PER_LEVEL; seed++) {
            HybridGenerator generator = new HybridGenerator();
            generator.setChapterLevel(1, level);
            generator.setSeed(seed * 7919L);
            Tile[][] tiles = generator.generate(WIDTH, HEIGHT);
            MapGenerationInfo info = generator.getGenerationInfo();
            int startX = info.getPlayerStartX();
            int startY = info.getPlayerStartY();
            String context = "level=" + level + " seed=" + (seed * 7919L);

            assertTrue(tiles[startX][startY].isWalkable(), "start tile must be walkable: " + context);

            if (level <= 2) {
                int sx = info.getStairsX();
                int sy = info.getStairsY();
                assertTrue(sx >= 0 && tiles[sx][sy].isStairsDown(), "stairs missing: " + context);
                assertTrue(flood(tiles, startX, startY, true)[sx][sy], "stairs unreachable: " + context);
                if (level == 2) {
                    assertFalse(flood(tiles, startX, startY, false)[sx][sy],
                            "stairs must stay sealed until mid-boss is defeated: " + context);
                }
            }

            if (level >= 2) {
                boolean[][] reachable = flood(tiles, startX, startY, false);
                List<List<int[]>> doors = doorBlocks(tiles);
                assertFalse(doors.isEmpty(), "boss door missing: " + context);
                if (level == 3) {
                    assertEquals(1, doors.size(), "level 3 must have one chapter boss door: " + context);
                }
                for (List<int[]> door : doors) {
                    assertTrue(door.stream().anyMatch(t -> hasReachableNeighbor(reachable, t[0], t[1])),
                            "boss door at " + door.get(0)[0] + "," + door.get(0)[1] + " unreachable: " + context);
                }
            }
        }
    }

    private static boolean[][] flood(Tile[][] tiles, int sx, int sy, boolean sealOpen) {
        boolean[][] visited = new boolean[tiles.length][tiles[0].length];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        visited[sx][sy] = true;
        queue.add(new int[] { sx, sy });
        while (!queue.isEmpty()) {
            int[] c = queue.poll();
            for (int[] d : DIRS) {
                int x = c[0] + d[0];
                int y = c[1] + d[1];
                if (x < 0 || y < 0 || x >= tiles.length || y >= tiles[0].length || visited[x][y]) {
                    continue;
                }
                if (tiles[x][y].isWalkable() || (sealOpen && tiles[x][y].isSealWall())) {
                    visited[x][y] = true;
                    queue.add(new int[] { x, y });
                }
            }
        }
        return visited;
    }

    /** 맞닿은 보스 문 타일끼리 묶어 문 블록 단위로 반환 */
    private static List<List<int[]>> doorBlocks(Tile[][] tiles) {
        boolean[][] seen = new boolean[tiles.length][tiles[0].length];
        List<List<int[]>> blocks = new ArrayList<>();
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                if (!tiles[x][y].isBossDoor() || seen[x][y]) {
                    continue;
                }
                List<int[]> block = new ArrayList<>();
                ArrayDeque<int[]> queue = new ArrayDeque<>();
                seen[x][y] = true;
                queue.add(new int[] { x, y });
                while (!queue.isEmpty()) {
                    int[] c = queue.poll();
                    block.add(c);
                    for (int[] d : DIRS) {
                        int nx = c[0] + d[0];
                        int ny = c[1] + d[1];
                        if (nx >= 0 && ny >= 0 && nx < tiles.length && ny < tiles[0].length
                                && !seen[nx][ny] && tiles[nx][ny].isBossDoor()) {
                            seen[nx][ny] = true;
                            queue.add(new int[] { nx, ny });
                        }
                    }
                }
                blocks.add(block);
            }
        }
        return blocks;
    }

    private static boolean hasReachableNeighbor(boolean[][] reachable, int x, int y) {
        for (int[] d : DIRS) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx >= 0 && ny >= 0 && nx < reachable.length && ny < reachable[0].length && reachable[nx][ny]) {
                return true;
            }
        }
        return false;
    }
}
