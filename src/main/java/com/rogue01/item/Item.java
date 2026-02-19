package com.rogue01.item;

public abstract class Item {
    protected String name;
    protected String description;
    protected ItemType type;
    protected int value;
    protected char symbol;
    
    public Item(String name, String description, ItemType type, int value, char symbol) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.value = value;
        this.symbol = symbol;
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ItemType getType() { return type; }
    public int getValue() { return value; }
    public char getSymbol() { return symbol; }
    
    public abstract void use(com.rogue01.entity.Player player);
    
    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
} 