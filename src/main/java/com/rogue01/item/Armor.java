package com.rogue01.item;

public class Armor extends Equipment {
    private ArmorType armorType;

    public Armor(String name, String description, ItemType type, int value, char symbol,
            int level, int attack, int defense, int durability, ArmorType armorType) {
        super(name, description, type, value, symbol, level, attack, defense, durability);
        this.armorType = armorType;
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    public enum ArmorType {
        HELMET("투구"),
        CHEST("갑옷"),
        BOOTS("신발"),
        RING("반지"),
        NECKLACE("목걸이");

        private final String koreanName;

        ArmorType(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() {
            return koreanName;
        }
    }
}