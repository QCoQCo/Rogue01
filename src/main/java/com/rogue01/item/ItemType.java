package com.rogue01.item;

public enum ItemType {
    HELMET("투구"),
    ARMOR("갑옷"),
    BOOTS("신발"),
    RING("반지"),
    NECKLACE("목걸이"),
    WEAPON_MAIN("주무기"),
    WEAPON_OFF("보조무기"),
    CONSUMABLE("소비아이템"),
    MATERIAL("재료");
    
    private final String koreanName;
    
    ItemType(String koreanName) {
        this.koreanName = koreanName;
    }
    
    public String getKoreanName() {
        return koreanName;
    }
} 