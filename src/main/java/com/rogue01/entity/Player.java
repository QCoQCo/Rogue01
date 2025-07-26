package com.rogue01.entity;

import com.rogue01.util.InputHandler;
import java.awt.event.KeyEvent;
import com.rogue01.map.Map;

public class Player extends Entity {
    private InputHandler inputHandler;
    
    public Player(int x, int y) {//† ß å ∂ ≈ ç √ ∫ å œ ∑
        super(x, y, '†', "Player");
        this.inputHandler = new InputHandler();
    }
    
    @Override
    public void update(Map map) {
        // 입력 처리 및 이동 로직 (keyCode 기반, 한 번만 이동)
        if (inputHandler.isKeyPressed(KeyEvent.VK_W)) {
            if (map.isWalkable(x, y - 1)) y--;
            inputHandler.consumeKey(KeyEvent.VK_W);
        }
        if (inputHandler.isKeyPressed(KeyEvent.VK_UP)) {
            if (map.isWalkable(x, y - 1)) y--;
            inputHandler.consumeKey(KeyEvent.VK_UP);
        }
        if (inputHandler.isKeyPressed(KeyEvent.VK_S)) {
            if (map.isWalkable(x, y + 1)) y++;
            inputHandler.consumeKey(KeyEvent.VK_S);
        }
        if (inputHandler.isKeyPressed(KeyEvent.VK_DOWN)) {
            if (map.isWalkable(x, y + 1)) y++;
            inputHandler.consumeKey(KeyEvent.VK_DOWN);
        }
        if (inputHandler.isKeyPressed(KeyEvent.VK_A)) {
            if (map.isWalkable(x - 1, y)) x--;
            inputHandler.consumeKey(KeyEvent.VK_A);
        }
        if (inputHandler.isKeyPressed(KeyEvent.VK_LEFT)) {
            if (map.isWalkable(x - 1, y)) x--;
            inputHandler.consumeKey(KeyEvent.VK_LEFT);
        }
        if (inputHandler.isKeyPressed(KeyEvent.VK_D)) {
            if (map.isWalkable(x + 1, y)) x++;
            inputHandler.consumeKey(KeyEvent.VK_D);
        }
        if (inputHandler.isKeyPressed(KeyEvent.VK_RIGHT)) {
            if (map.isWalkable(x + 1, y)) x++;
            inputHandler.consumeKey(KeyEvent.VK_RIGHT);
        }
    }
    
    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }
} 