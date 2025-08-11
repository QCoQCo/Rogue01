package com.rogue01.entity;

import com.rogue01.util.InputHandler;
import com.rogue01.item.Inventory;
import java.awt.event.KeyEvent;
import com.rogue01.map.Map;

public class Player extends Entity {
    private InputHandler inputHandler;
    private Inventory inventory;
    
    // 이동 방향을 정의하는 enum
    private enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
        
        private final int dx, dy;
        
        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
        
        public int getDx() { return dx; }
        public int getDy() { return dy; }
    }
    
    // 키 매핑을 위한 배열 (더 간단한 접근)
    private final int[] upKeys = {KeyEvent.VK_W, KeyEvent.VK_UP};
    private final int[] downKeys = {KeyEvent.VK_S, KeyEvent.VK_DOWN};
    private final int[] leftKeys = {KeyEvent.VK_A, KeyEvent.VK_LEFT};
    private final int[] rightKeys = {KeyEvent.VK_D, KeyEvent.VK_RIGHT};
    
    public Player(int x, int y) {
        super(x, y, '@', "Player");
        this.inputHandler = new InputHandler();
        this.inventory = new Inventory();
    }
    
    @Override
    public void update(Map map) {
        // 위쪽 이동
        if (isAnyKeyPressed(upKeys)) {
            if (map.isWalkable(x, y - 1)) {
                y--;
            }
            consumeKeys(upKeys);
        }
        // 아래쪽 이동
        else if (isAnyKeyPressed(downKeys)) {
            if (map.isWalkable(x, y + 1)) {
                y++;
            }
            consumeKeys(downKeys);
        }
        // 왼쪽 이동
        else if (isAnyKeyPressed(leftKeys)) {
            if (map.isWalkable(x - 1, y)) {
                x--;
            }
            consumeKeys(leftKeys);
        }
        // 오른쪽 이동
        else if (isAnyKeyPressed(rightKeys)) {
            if (map.isWalkable(x + 1, y)) {
                x++;
            }
            consumeKeys(rightKeys);
        }
    }
    
    private boolean isAnyKeyPressed(int[] keys) {
        for (int key : keys) {
            if (inputHandler.isKeyPressed(key)) {
                return true;
            }
        }
        return false;
    }
    
    private void consumeKeys(int[] keys) {
        for (int key : keys) {
            inputHandler.consumeKey(key);
        }
    }
    
    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
}