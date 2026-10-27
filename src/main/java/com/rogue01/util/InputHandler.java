package com.rogue01.util;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * 눌린 키 상태 저장소. 키 이벤트와 게임 루프(GameLoop)가 모두 EDT에서 실행되므로 동기화가 필요 없음
 * 키 해석은 {@link com.rogue01.game.InputManager}가 전담
 */
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