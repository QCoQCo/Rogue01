package com.rogue01.item;

public class Weapon extends Equipment {
    private WeaponType weaponType;
    
    public Weapon(String name, String description, ItemType type, int value, char symbol,
                 int attack, int defense, int durability, WeaponType weaponType) {
        super(name, description, type, value, symbol, attack, defense, durability);
        this.weaponType = weaponType;
    }
    
    public WeaponType getWeaponType() {
        return weaponType;
    }
    
    public enum WeaponType {
        SWORD("검"),
        AXE("도끼"),
        MACE("철퇴"),
        DAGGER("단검"),
        BOW("활"),
        STAFF("지팡이");
        
        private final String koreanName;
        
        WeaponType(String koreanName) {
            this.koreanName = koreanName;
        }
        
        public String getKoreanName() {
            return koreanName;
        }
    }
} 