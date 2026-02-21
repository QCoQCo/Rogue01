package com.rogue01.entity;

import com.rogue01.util.InputHandler;
import com.rogue01.item.Inventory;
import java.awt.event.KeyEvent;
import com.rogue01.map.Map;

public class Player extends Entity {
    private InputHandler inputHandler;
    private Inventory inventory;

    // 레벨/스탯 시스템
    private int level;
    private int experience;
    private int baseAttack; // 레벨에 따른 기본 공격력
    private int baseDefense; // 레벨에 따른 기본 방어력

    // 이동 방향을 정의하는 enum
    private enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

        private final int dx, dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public int getDx() {
            return dx;
        }

        public int getDy() {
            return dy;
        }
    }

    // 키 매핑을 위한 배열 (더 간단한 접근)
    private final int[] upKeys = { KeyEvent.VK_W, KeyEvent.VK_UP };
    private final int[] downKeys = { KeyEvent.VK_S, KeyEvent.VK_DOWN };
    private final int[] leftKeys = { KeyEvent.VK_A, KeyEvent.VK_LEFT };
    private final int[] rightKeys = { KeyEvent.VK_D, KeyEvent.VK_RIGHT };

    public Player(int x, int y) {
        super(x, y, '@', "Player");
        this.inputHandler = new InputHandler();
        this.inventory = new Inventory();
        this.level = 1;
        this.experience = 0;
        applyLevelStats();
    }

    /**
     * 레벨에 따른 기본 스탯 적용
     */
    private void applyLevelStats() {
        this.baseAttack = com.rogue01.game.GameBalance.PLAYER_BASE_ATTACK
                + level * com.rogue01.game.GameBalance.PLAYER_ATTACK_PER_LEVEL;
        this.baseDefense = com.rogue01.game.GameBalance.PLAYER_BASE_DEFENSE
                + level * com.rogue01.game.GameBalance.PLAYER_DEFENSE_PER_LEVEL;
        this.maxHealth = com.rogue01.game.GameBalance.PLAYER_BASE_HP
                + level * com.rogue01.game.GameBalance.PLAYER_HP_PER_LEVEL;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }

    /**
     * 경험치 추가 및 레벨업 처리
     */
    public void addExperience(int exp) {
        if (exp <= 0)
            return;
        this.experience += exp;

        while (experience >= getExpToNextLevel()) {
            experience -= getExpToNextLevel();
            levelUp();
        }
    }

    /**
     * 레벨업 시 스탯 상승
     */
    private void levelUp() {
        level++;
        applyLevelStats();
        health = maxHealth; // 풀 회복
    }

    /**
     * 다음 레벨까지 필요한 경험치
     */
    public int getExpToNextLevel() {
        return com.rogue01.game.GameBalance.getExpToNextLevel(level);
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

    /**
     * 플레이어의 총 방어력 계산 (기본 방어력 + 장비 방어력)
     */
    public int getDefense() {
        int totalDefense = baseDefense;
        var equippedItems = inventory.getEquippedItems();
        for (var equipment : equippedItems.values()) {
            if (equipment != null) {
                totalDefense += equipment.getDefense();
            }
        }
        return totalDefense;
    }

    /**
     * 플레이어의 총 공격력 계산 (기본 공격력 + 장비 공격력)
     */
    public int getAttack() {
        int totalAttack = baseAttack;
        var equippedItems = inventory.getEquippedItems();
        for (var equipment : equippedItems.values()) {
            if (equipment != null) {
                totalAttack += equipment.getAttack();
            }
        }
        return totalAttack;
    }

    /**
     * 레벨/경험치 초기화 (재시작 시)
     */
    public void resetLevelAndStats() {
        this.level = 1;
        this.experience = 0;
        applyLevelStats();
        this.health = this.maxHealth;
    }

    // 레벨/스탯 getters
    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public int getBaseDefense() {
        return baseDefense;
    }
}