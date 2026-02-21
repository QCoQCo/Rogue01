package com.rogue01.item;

import com.rogue01.game.GameBalance;
import java.util.Random;

public class ItemFactory {
    private static final Random RANDOM = new Random();

    private static int rollInRange(int min, int max) {
        if (min >= max)
            return min;
        return min + RANDOM.nextInt(max - min + 1);
    }

    /**
     * 적 처치 시 랜덤 아이템 드롭 (난이도 반영, 장비는 레벨 1~9 가중치 드롭)
     */
    public static Item createRandomDrop(GameBalance.Difficulty difficulty) {
        double dropChance = GameBalance.DROP_CHANCE_BASE * GameBalance.getDropChanceMultiplier(difficulty);
        if (RANDOM.nextDouble() > dropChance) {
            return null;
        }

        if (RANDOM.nextDouble() < GameBalance.CONSUMABLE_RATIO) {
            return createRandomConsumable();
        }

        int level = GameBalance.rollItemLevel(RANDOM);
        int roll = RANDOM.nextInt(7);
        return switch (roll) {
            case 0 -> createSword(level);
            case 1 -> createDagger(level);
            case 2 -> createLeatherArmor(level);
            case 3 -> createIronHelmet(level);
            case 4 -> createLeatherBoots(level);
            case 5 -> createRing(level);
            case 6 -> createNecklace(level);
            default -> createSword(level);
        };
    }

    /**
     * 랜덤 소비 아이템 생성
     */
    public static Consumable createRandomConsumable() {
        return RANDOM.nextBoolean() ? createHealthPotion() : createGreaterHealthPotion();
    }

    public static HealthPotion createHealthPotion() {
        return new HealthPotion("체력 포션", "HP를 30 회복합니다.", 50, 'P', 30);
    }

    public static HealthPotion createGreaterHealthPotion() {
        return new HealthPotion("상급 체력 포션", "HP를 80 회복합니다.", 120, 'Q', 80);
    }

    /** 레벨 1 검 (테스트/시작용) */
    public static Weapon createSword() {
        return createSword(1);
    }

    public static Weapon createSword(int level) {
        int[] atk = GameBalance.getWeaponAttackRange(level);
        int[] def = GameBalance.getWeaponDefenseRange(level);
        int attack = rollInRange(atk[0], atk[1]);
        int defense = rollInRange(def[0], def[1]);
        int durability = 80 + level * 15;
        int value = 80 + level * 25;
        String name = level > 1 ? "철검 +" + level : "철검";
        return new Weapon(name, "기본적인 철검입니다. (Lv." + level + ")", ItemType.WEAPON_MAIN, value, 'S',
                level, attack, defense, durability, Weapon.WeaponType.SWORD);
    }

    public static Weapon createDagger() {
        return createDagger(1);
    }

    public static Weapon createDagger(int level) {
        int[] atk = GameBalance.getWeaponAttackRange(level);
        int[] def = GameBalance.getWeaponDefenseRange(level);
        int attack = rollInRange(atk[0], atk[1]);
        int defense = rollInRange(def[0], def[1]);
        int durability = 60 + level * 12;
        int value = 60 + level * 20;
        String name = level > 1 ? "단검 +" + level : "단검";
        return new Weapon(name, "빠르고 가벼운 단검입니다. (Lv." + level + ")", ItemType.WEAPON_OFF, value, 'D',
                level, attack, defense, durability, Weapon.WeaponType.DAGGER);
    }

    public static Armor createLeatherArmor() {
        return createLeatherArmor(1);
    }

    public static Armor createLeatherArmor(int level) {
        int[] def = GameBalance.getArmorDefenseRange(level);
        int[] atk = GameBalance.getArmorAttackRange(level);
        int defense = rollInRange(def[0], def[1]);
        int attack = rollInRange(atk[0], atk[1]);
        int durability = 90 + level * 12;
        int value = 100 + level * 25;
        String name = level > 1 ? "가죽 갑옷 +" + level : "가죽 갑옷";
        return new Armor(name, "가죽으로 만든 기본 갑옷입니다. (Lv." + level + ")", ItemType.ARMOR, value, 'A',
                level, attack, defense, durability, Armor.ArmorType.CHEST);
    }

    public static Armor createIronHelmet() {
        return createIronHelmet(1);
    }

    public static Armor createIronHelmet(int level) {
        int[] def = GameBalance.getArmorDefenseRange(level);
        int[] atk = GameBalance.getArmorAttackRange(level);
        int defense = rollInRange(def[0], def[1]);
        int attack = rollInRange(atk[0], atk[1]);
        int durability = 100 + level * 15;
        int value = 120 + level * 30;
        String name = level > 1 ? "철 투구 +" + level : "철 투구";
        return new Armor(name, "철로 만든 튼튼한 투구입니다. (Lv." + level + ")", ItemType.HELMET, value, 'H',
                level, attack, defense, durability, Armor.ArmorType.HELMET);
    }

    public static Armor createLeatherBoots() {
        return createLeatherBoots(1);
    }

    public static Armor createLeatherBoots(int level) {
        int[] def = GameBalance.getArmorDefenseRange(level);
        int[] atk = GameBalance.getArmorAttackRange(level);
        int defense = rollInRange(def[0], def[1]);
        int attack = rollInRange(atk[0], atk[1]);
        int durability = 60 + level * 10;
        int value = 50 + level * 20;
        String name = level > 1 ? "가죽 신발 +" + level : "가죽 신발";
        return new Armor(name, "가죽으로 만든 신발입니다. (Lv." + level + ")", ItemType.BOOTS, value, 'B',
                level, attack, defense, durability, Armor.ArmorType.BOOTS);
    }

    public static Armor createRing() {
        return createRing(1);
    }

    public static Armor createRing(int level) {
        int[] def = GameBalance.getArmorDefenseRange(level);
        int[] atk = GameBalance.getArmorAttackRange(level);
        int defense = rollInRange(def[0], def[1]);
        int attack = rollInRange(atk[0], atk[1]);
        int durability = 40 + level * 8;
        int value = 150 + level * 40;
        String name = level > 1 ? "마법 반지 +" + level : "마법 반지";
        return new Armor(name, "마법의 힘이 깃든 반지입니다. (Lv." + level + ")", ItemType.RING, value, 'R',
                level, attack, defense, durability, Armor.ArmorType.RING);
    }

    public static Armor createNecklace() {
        return createNecklace(1);
    }

    public static Armor createNecklace(int level) {
        int[] def = GameBalance.getArmorDefenseRange(level);
        int[] atk = GameBalance.getArmorAttackRange(level);
        int defense = rollInRange(def[0], def[1]);
        int attack = rollInRange(atk[0], atk[1]);
        int durability = 50 + level * 10;
        int value = 140 + level * 35;
        String name = level > 1 ? "보호 목걸이 +" + level : "보호 목걸이";
        return new Armor(name, "보호의 힘이 깃든 목걸이입니다. (Lv." + level + ")", ItemType.NECKLACE, value, 'N',
                level, attack, defense, durability, Armor.ArmorType.NECKLACE);
    }
}