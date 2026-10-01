package com.rogue01.map.utils;

import com.rogue01.map.Tile;
import java.util.ArrayDeque;

/**
 * 맵 연결성 검사 유틸리티 (BFS, 4방향 이동 기준)
 */
public final class MapConnectivity {
    private static final int[][] DIRECTIONS = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

    private MapConnectivity() {
    }

    /**
     * 시작 위치에서 걸어서 도달 가능한 타일 표시
     *
     * @param treatSealAsOpen true면 봉인 벽을 무너진 상태(통과 가능)로 간주
     */
    public static boolean[][] reachableFrom(Tile[][] tiles, int startX, int startY, boolean treatSealAsOpen) {
        int width = tiles.length;
        int height = tiles[0].length;
        boolean[][] visited = new boolean[width][height];
        if (!isPassable(tiles, startX, startY, treatSealAsOpen)) {
            return visited;
        }

        ArrayDeque<int[]> queue = new ArrayDeque<>();
        visited[startX][startY] = true;
        queue.add(new int[] { startX, startY });
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            for (int[] d : DIRECTIONS) {
                int nx = current[0] + d[0];
                int ny = current[1] + d[1];
                if (isPassable(tiles, nx, ny, treatSealAsOpen) && !visited[nx][ny]) {
                    visited[nx][ny] = true;
                    queue.add(new int[] { nx, ny });
                }
            }
        }
        return visited;
    }

    /**
     * (x, y)의 4방향 이웃 중 도달 가능한 타일이 있는지 (보스 문처럼 통과 불가 타일 앞에 설 수 있는지)
     */
    public static boolean hasReachableNeighbor(boolean[][] reachable, int x, int y) {
        for (int[] d : DIRECTIONS) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx >= 0 && nx < reachable.length && ny >= 0 && ny < reachable[0].length && reachable[nx][ny]) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPassable(Tile[][] tiles, int x, int y, boolean treatSealAsOpen) {
        if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length) {
            return false;
        }
        Tile tile = tiles[x][y];
        return tile.isWalkable() || (treatSealAsOpen && tile.isSealWall());
    }
}
