package com.rogue01.item;

import java.util.*;

/**
 * 플레이어의 인벤토리를 관리하는 클래스 - 타입 안전성 향상
 */
public class Inventory {
    private static final int INVENTORY_SIZE = 50;
    private final List<Item> items;
    private final Map<ItemType, Equipment> equippedItems;
    
    public Inventory() {
        this.items = new ArrayList<>();
        this.equippedItems = new HashMap<>();
        
        // 기본 장비 슬롯 초기화
        initializeEquipmentSlots();
    }
    
    /**
     * 장비 슬롯 초기화
     */
    private void initializeEquipmentSlots() {
        for (ItemType type : ItemType.values()) {
            if (isEquipmentType(type)) {
                equippedItems.put(type, null);
            }
        }
    }
    
    /**
     * 장비 타입인지 확인
     */
    private boolean isEquipmentType(ItemType type) {
        return type != ItemType.CONSUMABLE && type != ItemType.MATERIAL;
    }
    
    /**
     * 아이템 추가
     */
    public boolean addItem(Item item) {
        if (item == null) {
            return false;
        }
        
        if (items.size() < INVENTORY_SIZE) {
            items.add(item);
            return true;
        }
        return false;
    }
    
    /**
     * 아이템 제거
     */
    public boolean removeItem(Item item) {
        return items.remove(item);
    }
    
    /**
     * 인덱스로 아이템 제거
     */
    public boolean removeItem(int index) {
        if (isValidIndex(index)) {
            items.remove(index);
            return true;
        }
        return false;
    }
    
    /**
     * 인덱스로 아이템 가져오기
     */
    public Item getItem(int index) {
        if (isValidIndex(index)) {
            return items.get(index);
        }
        return null;
    }
    
    /**
     * 특정 타입의 아이템들 가져오기
     */
    @SuppressWarnings("unchecked")
    public <T extends Item> List<T> getItemsByType(Class<T> itemClass) {
        List<T> result = new ArrayList<>();
        for (Item item : items) {
            if (itemClass.isInstance(item)) {
                result.add(itemClass.cast(item));
            }
        }
        return result;
    }
    
    /**
     * 장비 착용
     */
    public boolean equipItem(Item item) {
        if (!(item instanceof Equipment)) {
            return false;
        }
        
        Equipment equipment = (Equipment) item;
        ItemType type = equipment.getType();
        
        if (equippedItems.containsKey(type)) {
            // 기존 장비 해제
            Equipment oldEquipment = equippedItems.get(type);
            if (oldEquipment != null) {
                items.add(oldEquipment);
            }
            
            // 새 장비 착용
            equippedItems.put(type, equipment);
            items.remove(item);
            return true;
        }
        
        return false;
    }
    
    /**
     * 장비 해제
     */
    public boolean unequipItem(ItemType type) {
        if (!isEquipmentType(type)) {
            return false;
        }
        
        Equipment equipped = equippedItems.get(type);
        if (equipped != null && addItem(equipped)) {
            equippedItems.put(type, null);
            return true;
        }
        return false;
    }
    
    /**
     * 착용된 장비 가져오기
     */
    public Equipment getEquippedItem(ItemType type) {
        return equippedItems.get(type);
    }
    
    /**
     * 모든 착용된 장비 가져오기
     */
    public Map<ItemType, Equipment> getEquippedItems() {
        return new HashMap<>(equippedItems);
    }
    
    /**
     * 특정 타입의 착용된 장비 가져오기
     */
    public <T extends Equipment> List<T> getEquippedItemsByType(Class<T> equipmentClass) {
        List<T> result = new ArrayList<>();
        for (Equipment equipment : equippedItems.values()) {
            if (equipment != null && equipmentClass.isInstance(equipment)) {
                result.add(equipmentClass.cast(equipment));
            }
        }
        return result;
    }
    
    /**
     * 인덱스 유효성 검사
     */
    private boolean isValidIndex(int index) {
        return index >= 0 && index < items.size();
    }
    
    /**
     * 아이템 목록 가져오기 (방어적 복사)
     */
    public List<Item> getItems() {
        return new ArrayList<>(items);
    }
    
    /**
     * 인벤토리 크기
     */
    public int getSize() {
        return items.size();
    }
    
    /**
     * 최대 인벤토리 크기
     */
    public int getMaxSize() {
        return INVENTORY_SIZE;
    }
    
    /**
     * 인벤토리가 가득 찼는지 확인
     */
    public boolean isFull() {
        return items.size() >= INVENTORY_SIZE;
    }
    
    /**
     * 인벤토리 비우기
     */
    public void clear() {
        items.clear();
    }
    
    /**
     * 특정 타입의 아이템 개수
     */
    public int getItemCount(ItemType type) {
        int count = 0;
        for (Item item : items) {
            if (item.getType() == type) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 특정 아이템이 있는지 확인
     */
    public boolean containsItem(Item item) {
        return items.contains(item);
    }
    
    /**
     * 특정 이름의 아이템이 있는지 확인
     */
    public boolean containsItemByName(String name) {
        return items.stream().anyMatch(item -> item.getName().equals(name));
    }
} 