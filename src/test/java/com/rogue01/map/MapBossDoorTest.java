package com.rogue01.map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rogue01.map.structures.Room;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 보스 문 조회/개방 테스트 (고정 레이아웃)
 *
 * <pre>
 * 중간보스 문 A: (3..5, 2..5)   중간보스 문 B: (12..14, 2..5)   챕터보스 문: (8..11, 7..8)
 * </pre>
 */
class MapBossDoorTest {
    private static final int WIDTH = 20;
    private static final int HEIGHT = 10;

    private Map map;

    @BeforeEach
    void setUp() {
        map = new Map(WIDTH, HEIGHT, new FixedLayoutGenerator());
    }

    @Test
    void findsAdjacentMidBossDoor() {
        assertArrayEquals(new int[] { 3, 3 }, map.getAdjacentBossDoor(2, 3));
        assertEquals(1, map.getAdjacentBossDoorType(2, 3));
    }

    @Test
    void noDoorWhenNotAdjacent() {
        assertNull(map.getAdjacentBossDoor(0, 0));
        assertEquals(0, map.getAdjacentBossDoorType(0, 0));
    }

    @Test
    void chapterDoorTakesPriorityOverMidDoor() {
        // (8, 6): 아래(8,7)는 챕터보스 문. 왼쪽에 중간보스 문을 하나 더 붙여 둘 다 인접하게 만듦
        map.getTiles()[7][6] = new Tile('D', false, Tile.TileType.BOSS_DOOR_MID);
        assertArrayEquals(new int[] { 8, 7 }, map.getAdjacentBossDoor(8, 6));
        assertEquals(2, map.getAdjacentBossDoorType(8, 6));
    }

    @Test
    void openBossDoorOpensWholeBlockOnly() {
        map.openBossDoor(3, 3);

        for (int x = 3; x <= 5; x++) {
            for (int y = 2; y <= 5; y++) {
                Tile tile = map.getTile(x, y);
                assertTrue(tile.isWalkable(), "opened door tile must be walkable at " + x + "," + y);
                assertFalse(tile.isBossDoor(), "opened door tile must not be a door at " + x + "," + y);
            }
        }
        assertEquals(0, map.getAdjacentBossDoorType(2, 3), "defeated door must not prompt again");

        // 다른 문 블록은 그대로
        assertTrue(map.getTile(12, 2).isBossDoorMid());
        assertTrue(map.getTile(14, 5).isBossDoorMid());
        assertTrue(map.getTile(8, 7).isBossDoorChapter());
    }

    @Test
    void openBossDoorIgnoresNonDoorTile() {
        map.openBossDoor(0, 0);
        map.openBossDoor(-1, -1);

        assertTrue(map.getTile(3, 3).isBossDoorMid());
        assertTrue(map.getTile(8, 7).isBossDoorChapter());
    }

    /** 바닥 위에 보스 문 블록 3개를 둔 고정 맵 */
    private static class FixedLayoutGenerator implements MapGenerator {
        @Override
        public Tile[][] generate(int width, int height) {
            Tile[][] tiles = new Tile[width][height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    tiles[x][y] = new Tile('.', true);
                }
            }
            fill(tiles, 3, 2, 3, 4, Tile.TileType.BOSS_DOOR_MID);
            fill(tiles, 12, 2, 3, 4, Tile.TileType.BOSS_DOOR_MID);
            fill(tiles, 8, 7, 4, 2, Tile.TileType.BOSS_DOOR_CHAPTER);
            return tiles;
        }

        private void fill(Tile[][] tiles, int bx, int by, int w, int h, Tile.TileType type) {
            for (int x = bx; x < bx + w; x++) {
                for (int y = by; y < by + h; y++) {
                    tiles[x][y] = new Tile('D', false, type);
                }
            }
        }

        @Override
        public void setSeed(long seed) {
        }

        @Override
        public MapGenerationInfo getGenerationInfo() {
            return new MapGenerationInfo(0, 0, 0, 0, List.of(), 0);
        }

        @Override
        public List<Room> getRooms() {
            return List.of();
        }
    }
}
