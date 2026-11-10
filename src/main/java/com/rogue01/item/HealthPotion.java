package com.rogue01.item;

import com.rogue01.entity.Player;

/**
 * 체력 회복 포션
 */
public class HealthPotion extends Consumable {
    private final int healAmount;

    public HealthPotion(String name, String description, int value, char symbol, int healAmount) {
        super(name, description, value, symbol);
        this.healAmount = healAmount;
    }

    @Override
    public void use(Player player) {
        player.heal(healAmount); // 최대 HP를 넘지 않음 (Entity.heal)
    }

    public int getHealAmount() {
        return healAmount;
    }
}
