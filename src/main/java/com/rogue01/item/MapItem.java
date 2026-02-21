package com.rogue01.item;

/**
 * 맵 위에 떨어져 있는 아이템을 나타내는 클래스
 */
public class MapItem {
    private int x;
    private int y;
    private Item item;

    public MapItem(int x, int y, Item item) {
        this.x = x;
        this.y = y;
        this.item = item;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Item getItem() {
        return item;
    }
}
