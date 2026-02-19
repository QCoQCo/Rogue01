package com.rogue01.item;

import com.rogue01.entity.Player;

/**
 * 소비 아이템 기본 클래스
 */
public abstract class Consumable extends Item {

    public Consumable(String name, String description, int value, char symbol) {
        super(name, description, ItemType.CONSUMABLE, value, symbol);
    }

    @Override
    public abstract void use(Player player);
}
