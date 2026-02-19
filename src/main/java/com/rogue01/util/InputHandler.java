package com.rogue01.util;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class InputHandler extends KeyAdapter {
    private Set<Integer> pressedKeys;
    
    public InputHandler() {
        this.pressedKeys = new HashSet<>();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }
    
    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }
    
    public boolean isKeyPressed(char key) {
        return isKeyPressed(Character.toUpperCase(key));
    }
    
    public void clearKeys() {
        pressedKeys.clear();
    }

    public void consumeKey(int keyCode) {
        pressedKeys.remove(keyCode);
    }
} 