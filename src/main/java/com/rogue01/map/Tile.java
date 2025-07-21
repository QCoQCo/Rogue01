package com.rogue01.map;

public class Tile {
    private char symbol;
    private boolean walkable;
    
    public Tile(char symbol, boolean walkable) {
        this.symbol = symbol;
        this.walkable = walkable;
    }
    
    // Getters
    public char getSymbol() { return symbol; }
    public boolean isWalkable() { return walkable; }
    
    // Setters
    public void setSymbol(char symbol) { this.symbol = symbol; }
    public void setWalkable(boolean walkable) { this.walkable = walkable; }
} 