package com.rogue01.entity;

import com.rogue01.map.Map;

public abstract class Entity {
    protected int x, y;
    protected char symbol;
    protected String name;
    protected int health;
    protected int maxHealth;
    
    public Entity(int x, int y, char symbol, String name) {
        this.x = x;
        this.y = y;
        this.symbol = symbol;
        this.name = name;
        this.health = 100;
        this.maxHealth = 100;
    }
    
    /**
     * 기본 업데이트 메서드 - 맵과 상관없는 기본 로직
     */
    public void update() {
        // 기본 구현 - 하위 클래스에서 오버라이드 가능
    }
    
    /**
     * 맵을 고려한 업데이트 메서드 - 추상 메서드
     */
    public abstract void update(Map map);
    
    /**
     * 데미지를 받는 메서드
     */
    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }
    
    /**
     * 체력을 회복하는 메서드
     */
    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }
    
    /**
     * 사망 여부를 확인하는 메서드
     */
    public boolean isDead() {
        return health <= 0;
    }
    
    /**
     * 체력 비율을 반환하는 메서드 (0.0 ~ 1.0)
     */
    public double getHealthRatio() {
        return maxHealth > 0 ? (double) health / maxHealth : 0.0;
    }
    
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public char getSymbol() { return symbol; }
    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    
    // Setters
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setHealth(int health) { this.health = Math.max(0, Math.min(maxHealth, health)); }
    public void setMaxHealth(int maxHealth) { 
        this.maxHealth = Math.max(1, maxHealth);
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }
} 