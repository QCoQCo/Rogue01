package com.rogue01.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.rogue01.map.Map;
import com.rogue01.map.MapGenerationInfo;
import com.rogue01.map.MapGenerator;
import com.rogue01.map.Tile;
import com.rogue01.map.structures.Room;
import com.rogue01.map.utils.RandomUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * 적 랜덤 이동 테스트 - 동시에 생성된 적들이 같은 경로로 움직이지 않는지, 매 턴 실제로 움직이는지
 */
class EnemyRandomTest {
    private static final int SIZE = 401;
    private static final int CENTER = SIZE / 2;
    private static final int STEPS = 100; // 중앙에서 100칸 이내라 맵 경계에 닿지 않음

    private final Map openMap = new Map(SIZE, SIZE, new OpenFloorGenerator());

    @Test
    void enemiesCreatedTogetherWanderDifferently() {
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Enemy enemy = new Enemy(CENTER, CENTER, EnemyType.GOBLIN);
            StringBuilder path = new StringBuilder();
            for (int step = 0; step < 20; step++) {
                enemy.update(openMap);
                path.append(enemy.getX()).append(',').append(enemy.getY()).append(';');
            }
            paths.add(path.toString());
        }

        Set<String> distinct = new HashSet<>(paths);
        assertEquals(paths.size(), distinct.size(), "every enemy should take its own random path");
    }

    @Test
    void wanderingEnemyMovesEveryTurnOnOpenFloor() {
        Enemy enemy = new Enemy(CENTER, CENTER, EnemyType.GOBLIN);
        Set<String> directions = new HashSet<>();
        for (int step = 0; step < STEPS; step++) {
            int beforeX = enemy.getX();
            int beforeY = enemy.getY();
            enemy.update(openMap);
            int dx = enemy.getX() - beforeX;
            int dy = enemy.getY() - beforeY;
            assertEquals(1, Math.abs(dx) + Math.abs(dy), "enemy must move exactly one tile on open floor (step " + step + ")");
            directions.add(dx + "," + dy);
        }
        assertEquals(4, directions.size(), "all four directions should be used");
    }

    @Test
    void defaultRandomUtilsInstancesAreIndependent() {
        RandomUtils first = new RandomUtils();
        RandomUtils second = new RandomUtils();
        StringBuilder a = new StringBuilder();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            a.append(first.nextInt(0, 1000)).append(',');
            b.append(second.nextInt(0, 1000)).append(',');
        }
        assertNotEquals(a.toString(), b.toString());
    }

    /** 전부 바닥인 맵 */
    private static class OpenFloorGenerator implements MapGenerator {
        @Override
        public Tile[][] generate(int width, int height) {
            Tile[][] tiles = new Tile[width][height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    tiles[x][y] = new Tile('.', true);
                }
            }
            return tiles;
        }

        @Override
        public void setSeed(long seed) {
        }

        @Override
        public MapGenerationInfo getGenerationInfo() {
            return new MapGenerationInfo(0, 0, CENTER, CENTER, List.of(), 0);
        }

        @Override
        public List<Room> getRooms() {
            return List.of();
        }
    }
}
