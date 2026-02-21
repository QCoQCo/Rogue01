package com.rogue01.entity;

/**
 * 적의 타입을 정의하는 enum
 */
public enum EnemyType {
    GOBLIN("고블린", 'g', 45, 8, 4, 2),
    ORC("오크", 'o', 75, 14, 7, 3),
    SKELETON("스켈레톤", 's', 55, 10, 5, 2),
    TROLL("트롤", 'T', 110, 18, 9, 5),
    DRAGON("드래곤", 'D', 180, 26, 12, 8);
    
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
