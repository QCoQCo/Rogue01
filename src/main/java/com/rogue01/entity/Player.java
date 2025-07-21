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
        if (inputHandler.isKeyPressed('w')) {
            y--;
        }
        if (inputHandler.isKeyPressed('s')) {
            y++;
        }
        if (inputHandler.isKeyPressed('a')) {
            x--;
        }
        if (inputHandler.isKeyPressed('d')) {
            x++;
        }
    }
    
    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }
} 