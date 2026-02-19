package com.rogue01.map.generators;

import com.rogue01.map.*;
import com.rogue01.map.structures.Room;
import com.rogue01.map.utils.RandomUtils;
import java.util.*;

public class BSPGenerator implements MapGenerator {
    private long seed;
    private List<Room> rooms;
    private long generationTime;
    private RandomUtils randomUtils;
    
    public BSPGenerator() {
        this.rooms = new ArrayList<>();
        this.randomUtils = new RandomUtils();
    }
    
    public BSPGenerator(long seed) {
        this.rooms = new ArrayList<>();
        this.randomUtils = new RandomUtils(seed);
    }
    
    @Override
    public Tile[][] generate(int width, int height) {
        long startTime = System.currentTimeMillis();
        
        // 맵을 벽으로 초기화
        Tile[][] tiles = new Tile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = new Tile('#', false);
            }
        }
        
        // BSP 트리 생성
        BSPNode root = new BSPNode(1, 1, width - 2, height - 2);
        splitNode(root, 4); // 4번 분할
        
        // 각 리프 노드에 방 생성
        createRoomsFromBSP(tiles, root);
        
        // 방들을 통로로 연결
        connectRooms(tiles);
        
        generationTime = System.currentTimeMillis() - startTime;
        
        return tiles;
    }
    
    private void splitNode(BSPNode node, int depth) {
        if (depth <= 0 || node.width < 8 || node.height < 8) {
            return;
        }
        
        // 수평 또는 수직 분할 결정
        boolean horizontal = randomUtils.nextBoolean(0.5);
        
        if (horizontal && node.height > 8) {
            // 수평 분할 - 유효한 범위 확인
            int minSplit = node.y + 4;
            int maxSplit = node.y + node.height - 4;
            if (maxSplit > minSplit) {
                int splitY = randomUtils.nextInt(minSplit, maxSplit);
                node.left = new BSPNode(node.x, node.y, node.width, splitY - node.y);
                node.right = new BSPNode(node.x, splitY, node.width, node.y + node.height - splitY);
            }
        } else if (node.width > 8) {
            // 수직 분할 - 유효한 범위 확인
            int minSplit = node.x + 4;
            int maxSplit = node.x + node.width - 4;
            if (maxSplit > minSplit) {
                int splitX = randomUtils.nextInt(minSplit, maxSplit);
                node.left = new BSPNode(node.x, node.y, splitX - node.x, node.height);
                node.right = new BSPNode(splitX, node.y, node.x + node.width - splitX, node.height);
            }
        }
        
        // 재귀적으로 분할
        if (node.left != null) splitNode(node.left, depth - 1);
        if (node.right != null) splitNode(node.right, depth - 1);
    }
    
    private void createRoomsFromBSP(Tile[][] tiles, BSPNode node) {
        if (node.left == null && node.right == null) {
            // 리프 노드에 방 생성 - 유효한 범위 확인
            int maxRoomWidth = Math.min(8, node.width - 2);
            int maxRoomHeight = Math.min(8, node.height - 2);
            
            if (maxRoomWidth >= 4 && maxRoomHeight >= 4) {
                int roomWidth = randomUtils.nextInt(4, maxRoomWidth);
                int roomHeight = randomUtils.nextInt(4, maxRoomHeight);
                
                int maxRoomX = node.x + node.width - roomWidth;
                int maxRoomY = node.y + node.height - roomHeight;
                
                if (maxRoomX >= node.x && maxRoomY >= node.y) {
                    int roomX = randomUtils.nextInt(node.x, maxRoomX);
                    int roomY = randomUtils.nextInt(node.y, maxRoomY);
                    
                    Room room = new Room(roomX, roomY, roomWidth, roomHeight, Room.RoomType.OUTER);
                    createRoom(tiles, room);
                    rooms.add(room);
                }
            }
        } else {
            // 재귀적으로 자식 노드 처리
            if (node.left != null) createRoomsFromBSP(tiles, node.left);
            if (node.right != null) createRoomsFromBSP(tiles, node.right);
        }
    }
    
    private void createRoom(Tile[][] tiles, Room room) {
        for (int x = room.getX(); x < room.getX() + room.getWidth(); x++) {
            for (int y = room.getY(); y < room.getY() + room.getHeight(); y++) {
                if (x >= 0 && x < tiles.length && y >= 0 && y < tiles[0].length) {
                    tiles[x][y] = new Tile('.', true);
                }
            }
        }
    }
    
    private void connectRooms(Tile[][] tiles) {
        if (rooms.size() < 2) return;
        
        // 간단한 연결: 첫 번째 방을 기준으로 다른 방들과 연결
        Room baseRoom = rooms.get(0);
        for (int i = 1; i < rooms.size(); i++) {
            Room targetRoom = rooms.get(i);
            connectTwoRooms(tiles, baseRoom, targetRoom);
        }
    }
    
    private void connectTwoRooms(Tile[][] tiles, Room room1, Room room2) {
        int x1 = room1.getCenterX();
        int y1 = room1.getCenterY();
        int x2 = room2.getCenterX();
        int y2 = room2.getCenterY();
        
        // L자 통로 생성
        if (randomUtils.nextBoolean(0.5)) {
            // 먼저 수평, 그 다음 수직
            createHorizontalCorridor(tiles, x1, x2, y1);
            createVerticalCorridor(tiles, y1, y2, x2);
        } else {
            // 먼저 수직, 그 다음 수평
            createVerticalCorridor(tiles, y1, y2, x1);
            createHorizontalCorridor(tiles, x1, x2, y2);
        }
    }
    
    private void createHorizontalCorridor(Tile[][] tiles, int x1, int x2, int y) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            if (x >= 0 && x < tiles.length && y >= 0 && y < tiles[0].length) {
                tiles[x][y] = new Tile('.', true);
            }
        }
    }
    
    private void createVerticalCorridor(Tile[][] tiles, int y1, int y2, int x) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            if (x >= 0 && x < tiles.length && y >= 0 && y < tiles[0].length) {
                tiles[x][y] = new Tile('.', true);
            }
        }
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
    
    private static class BSPNode {
        int x, y, width, height;
        BSPNode left, right;
        
        BSPNode(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
} 