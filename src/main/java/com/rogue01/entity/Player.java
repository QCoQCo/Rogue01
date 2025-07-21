package com.rogue01.entity;

import com.rogue01.util.InputHandler;

public class Player extends Entity {
    private InputHandler inputHandler;
    
    public Player(int x, int y) {
        super(x, y, '@', "Player");
        this.inputHandler = new InputHandler();
    }
    
    @Override
    public void update() {
        // 입력 처리 및 이동 로직
        if (inputHandler.isKeyPressed('w') || inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_UP)) {
            y--;
        }
        if (inputHandler.isKeyPressed('s') || inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_DOWN)) {
            y++;
        }
        if (inputHandler.isKeyPressed('a') || inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_LEFT)) {
            x--;
        }
        if (inputHandler.isKeyPressed('d') || inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_RIGHT)) {
            x++;
        }
    }
    
    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }
} 