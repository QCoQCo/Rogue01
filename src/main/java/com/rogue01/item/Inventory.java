package com.rogue01.item;

import java.util.*;

public class Inventory {
    private static final int INVENTORY_SIZE = 50;
    private List<Item> items;
    private Map<ItemType, Equipment> equippedItems;
    
    public Inventory() {
        this.items = new ArrayList<>();
        this.equippedItems = new HashMap<>();
        
        // 기본 장비 슬롯 초기화
        for (ItemType type : ItemType.values()) {
            if (type != ItemType.CONSUMABLE && type != ItemType.MATERIAL) {
                equippedItems.put(type, null);
            }
        }
    }
    
    public boolean addItem(Item item) {
        if (items.size() < INVENTORY_SIZE) {
            items.add(item);
            return true;
        }
        return false;
    }
    
    public boolean removeItem(Item item) {
        return items.remove(item);
    }
    
    public boolean removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            return true;
        }
        return false;
    }
    
    public Item getItem(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return null;
    }
    
    public boolean equipItem(Item item) {
        if (item instanceof Equipment) {
            Equipment equipment = (Equipment) item;
            ItemType type = equipment.getType();
            
            // 장비 타입에 따라 착용
            if (type == ItemType.WEAPON_MAIN || type == ItemType.WEAPON_OFF) {
                equippedItems.put(type, equipment);
                items.remove(item);
                return true;
            } else if (equippedItems.containsKey(type)) {
                equippedItems.put(type, equipment);
                items.remove(item);
                return true;
            }
        }
        return false;
    }
    
    public boolean unequipItem(ItemType type) {
        Equipment equipped = equippedItems.get(type);
        if (equipped != null && addItem(equipped)) {
            equippedItems.put(type, null);
            return true;
        }
        return false;
    }
    
    public Equipment getEquippedItem(ItemType type) {
        return equippedItems.get(type);
    }
    
    public List<Item> getItems() {
        return new ArrayList<>(items);
    }
    
    public Map<ItemType, Equipment> getEquippedItems() {
        return new HashMap<>(equippedItems);
    }
    
    public int getSize() {
        return items.size();
    }
    
    public int getMaxSize() {
        return INVENTORY_SIZE;
    }
    
    public boolean isFull() {
        return items.size() >= INVENTORY_SIZE;
    }
    
    public void clear() {
        items.clear();
    }
} 