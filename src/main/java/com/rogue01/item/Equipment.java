package com.rogue01.item;

public abstract class Equipment extends Item {
    protected int attack;
    protected int defense;
    protected int durability;
    protected int maxDurability;
    
    public Equipment(String name, String description, ItemType type, int value, char symbol, 
                    int attack, int defense, int durability) {
        super(name, description, type, value, symbol);
        this.attack = attack;
        this.defense = defense;
        this.durability = durability;
        this.maxDurability = durability;
    }
    
    // Getters
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    
    // Setters
    public void setDurability(int durability) { 
        this.durability = Math.max(0, Math.min(durability, maxDurability)); 
    }
    
    public void repair() {
        this.durability = maxDurability;
    }
    
    public boolean isBroken() {
        return durability <= 0;
    }
    
    @Override
    public void use(com.rogue01.entity.Player player) {
        // 장비 착용 로직은 Player 클래스에서 처리
    }
} 