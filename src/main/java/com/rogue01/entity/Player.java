package com.rogue01.entity;

import com.rogue01.item.Inventory;
import com.rogue01.map.Map;

public class Player extends Entity {
    private Inventory inventory;

    // 레벨/스탯 시스템
    private int level;
    private int experience;
    private int baseAttack; // 레벨에 따른 기본 공격력
    private int baseDefense; // 레벨에 따른 기본 방어력

    public Player(int x, int y) {
        super(x, y, '†', "Player");
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

    /**
     * 플레이어 이동은 턴제로 Game.movePlayer가 처리 (입력은 InputManager 전담)
     */
    @Override
    public void update(Map map) {
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