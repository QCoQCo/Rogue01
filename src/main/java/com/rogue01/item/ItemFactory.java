package com.rogue01.item;

public class ItemFactory {
    
    public static Weapon createSword() {
        return new Weapon("철검", "기본적인 철검입니다.", ItemType.WEAPON_MAIN, 100, 'S', 15, 0, 100, Weapon.WeaponType.SWORD);
    }
    
    public static Weapon createDagger() {
        return new Weapon("단검", "빠르고 가벼운 단검입니다.", ItemType.WEAPON_OFF, 80, 'D', 8, 0, 80, Weapon.WeaponType.DAGGER);
    }
    
    public static Armor createLeatherArmor() {
        return new Armor("가죽 갑옷", "가죽으로 만든 기본 갑옷입니다.", ItemType.ARMOR, 120, 'A', 0, 10, 90, Armor.ArmorType.CHEST);
    }
    
    public static Armor createIronHelmet() {
        return new Armor("철 투구", "철로 만든 튼튼한 투구입니다.", ItemType.HELMET, 150, 'H', 0, 8, 120, Armor.ArmorType.HELMET);
    }
    
    public static Armor createLeatherBoots() {
        return new Armor("가죽 신발", "가죽으로 만든 신발입니다.", ItemType.BOOTS, 60, 'B', 0, 3, 70, Armor.ArmorType.BOOTS);
    }
    
    public static Armor createRing() {
        return new Armor("마법 반지", "마법의 힘이 깃든 반지입니다.", ItemType.RING, 200, 'R', 5, 5, 50, Armor.ArmorType.RING);
    }
    
    public static Armor createNecklace() {
        return new Armor("보호 목걸이", "보호의 힘이 깃든 목걸이입니다.", ItemType.NECKLACE, 180, 'N', 0, 7, 60, Armor.ArmorType.NECKLACE);
    }
} 