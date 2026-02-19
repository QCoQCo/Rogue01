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
        int before = player.getHealth();
        player.heal(healAmount);
        int actualHeal = player.getHealth() - before;
        // 실제 회복량은 최대 HP를 넘지 않음 (heal 내부에서 처리됨)
    }

    public int getHealAmount() {
        return healAmount;
    }
}
