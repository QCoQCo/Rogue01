package com.rogue01.entity;

/**
 * 적의 타입을 정의하는 enum
 */
public enum EnemyType {
    GOBLIN("고블린", 'g', 50, 10, 5, 1),
    ORC("오크", 'o', 80, 15, 8, 2),
    SKELETON("스켈레톤", 's', 60, 12, 6, 1),
    TROLL("트롤", 'T', 120, 20, 10, 3),
    DRAGON("드래곤", 'D', 200, 30, 15, 5);
    
    private final String koreanName;
    private final char symbol;
    private final int maxHealth;
    private final int attack;
    private final int defense;
    private final int experience;
    
    EnemyType(String koreanName, char symbol, int maxHealth, int attack, int defense, int experience) {
        this.koreanName = koreanName;
        this.symbol = symbol;
        this.maxHealth = maxHealth;
        this.attack = attack;
        this.defense = defense;
        this.experience = experience;
    }
    
    public String getKoreanName() {
        return koreanName;
    }
    
    public char getSymbol() {
        return symbol;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public int getAttack() {
        return attack;
    }
    
    public int getDefense() {
        return defense;
    }
    
    public int getExperience() {
        return experience;
    }
}
