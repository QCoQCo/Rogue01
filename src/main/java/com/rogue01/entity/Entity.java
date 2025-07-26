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
    
    public void update() {
        // 기본 구현 (아무것도 안 함)
    }
    public abstract void update(Map map);
    
    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }
    
    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }
    
    public boolean isDead() {
        return health <= 0;
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
} 