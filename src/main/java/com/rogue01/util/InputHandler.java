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
        // 먼저 등록된 리스너(전투 화면 등)가 처리한 키는 게임 로직에 다시 전달하지 않음
        if (e.isConsumed()) {
            return;
        }
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

    /** 아무 키나 눌려 있는지 ("아무 키나 눌러 계속" 처리용) */
    public boolean isAnyKeyPressed() {
        return !pressedKeys.isEmpty();
    }

    public void clearKeys() {
        pressedKeys.clear();
    }

    public void consumeKey(int keyCode) {
        pressedKeys.remove(keyCode);
    }
}