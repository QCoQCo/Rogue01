package com.rogue01.map.structures;

public class Room {
    private int x, y, width, height;
    private RoomType type;
    
    public enum RoomType {
        CENTRAL,    // 중심부 (방-통로 방식)
        MIDDLE,     // 중간부 (셀룰러 오토마타)
        OUTER       // 외곽부 (BSP)
    }
    
    public Room(int x, int y, int width, int height, RoomType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }
    
    public Room(int x, int y, int width, int height) {
        this(x, y, width, height, RoomType.CENTRAL);
    }
    
    // 다른 방과 겹치는지 확인
    public boolean intersects(Room other) {
        return (x <= other.x + other.width && x + width >= other.x &&
                y <= other.y + other.height && y + height >= other.y);
    }
    
    // 방의 중앙점 X 좌표 반환
    public int getCenterX() {
        return x + width / 2;
    }
    
    // 방의 중앙점 Y 좌표 반환
    public int getCenterY() {
        return y + height / 2;
    }
    
    // 방의 면적 반환
    public int getArea() {
        return width * height;
    }
    
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public RoomType getType() { return type; }
    
    // Setters
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setType(RoomType type) { this.type = type; }
    
    @Override
    public String toString() {
        return String.format("Room(%d,%d,%dx%d,%s)", x, y, width, height, type);
    }
} 