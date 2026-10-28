package com.rogue01.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rogue01.entity.EnemyType;
import com.rogue01.item.Armor;
import com.rogue01.item.Equipment;
import com.rogue01.item.Item;
import com.rogue01.item.ItemFactory;
import com.rogue01.item.Weapon;
import java.util.HashMap;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * 밸런스 공식 테스트 (docs/bugAndFix.md 6단계: 10·11·17번)
 */
class GameBalanceTest {

    @Test
    void ratioDamageShrinksWithDefenseButNeverReachesFloorOfOne() {
        assertEquals(30, GameBalance.baseDamage(30, 0), "no defense → full attack");
        assertEquals(15, GameBalance.baseDamage(30, 33), "defense 33 → half");
        int heavyArmor = GameBalance.baseDamage(41, 66);
        assertTrue(heavyArmor >= 12 && heavyArmor <= 14,
                "late boss vs heavy armor should still hurt (old formula gave 1): " + heavyArmor);
        assertEquals(1, GameBalance.baseDamage(1, 500), "minimum 1");
        assertTrue(GameBalance.baseDamage(30, 10) > GameBalance.baseDamage(30, 20), "more defense → less damage");
    }

    @Test
    void stochasticRoundingKeepsExpectedValue() {
        Random rng = new Random(1);
        for (double value : new double[] { 0.7, 1.0, 1.3, 2.5 }) {
            int samples = 20000;
            long sum = 0;
            for (int i = 0; i < samples; i++) {
                int rounded = GameBalance.roundStochastic(value, rng);
                assertTrue(rounded == Math.floor(value) || rounded == Math.floor(value) + 1);
                sum += rounded;
            }
            assertEquals(value, (double) sum / samples, 0.02, "mean of rounded " + value);
        }
    }

    @Test
    void difficultySpawnMultipliersDifferForOneEnemyRooms() {
        // 방 대부분이 기본 1마리라, 반올림 후 기댓값이 난이도별로 달라야 함 (17번)
        Random rng = new Random(2);
        double easy = 0;
        double hard = 0;
        for (int i = 0; i < 10000; i++) {
            easy += GameBalance.roundStochastic(GameBalance.getSpawnDensityMultiplier(GameBalance.Difficulty.EASY), rng);
            hard += GameBalance.roundStochastic(GameBalance.getSpawnDensityMultiplier(GameBalance.Difficulty.HARD), rng);
        }
        assertEquals(0.7, easy / 10000, 0.03);
        assertEquals(1.3, hard / 10000, 0.03);
    }

    @Test
    void dropLevelFollowsFloorNumber() {
        Random rng = new Random(3);
        for (int chapter = 1; chapter <= 3; chapter++) {
            for (int level = 1; level <= 3; level++) {
                int floor = (chapter - 1) * 3 + level;
                var counts = new HashMap<Integer, Integer>();
                for (int i = 0; i < 4000; i++) {
                    counts.merge(GameBalance.rollItemLevel(rng, chapter, level), 1, Integer::sum);
                }
                for (int itemLevel : counts.keySet()) {
                    assertTrue(Math.abs(itemLevel - floor) <= 1 && itemLevel >= 1 && itemLevel <= 9,
                            "floor " + floor + " dropped Lv" + itemLevel);
                }
                int mostCommon = counts.entrySet().stream().max(java.util.Map.Entry.comparingByValue()).get().getKey();
                assertEquals(Math.max(1, Math.min(9, floor)), mostCommon, "most drops match the floor number");
            }
        }
    }

    @Test
    void dropsOnFirstFloorAreLowLevelEquipment() {
        for (int i = 0; i < 2000; i++) {
            Item item = ItemFactory.createRandomDrop(GameBalance.Difficulty.EASY, 1, 1);
            if (item instanceof Equipment equipment) {
                assertTrue(equipment.getLevel() <= 2, "1-1 drop level " + equipment.getLevel());
            }
        }
    }

    @Test
    void levelOneEquipmentStatsAreHalvedRanges() {
        for (int i = 0; i < 200; i++) {
            Weapon sword = ItemFactory.createSword(1);
            Armor armor = ItemFactory.createLeatherArmor(1);
            assertTrue(sword.getAttack() >= 5 && sword.getAttack() <= 8, "Lv1 sword attack " + sword.getAttack());
            assertTrue(armor.getDefense() >= 5 && armor.getDefense() <= 8, "Lv1 armor defense " + armor.getDefense());
            Weapon top = ItemFactory.createSword(9);
            assertTrue(top.getAttack() >= 26 && top.getAttack() <= 28, "Lv9 sword attack " + top.getAttack());
        }
    }

    @Test
    void enemyScalingAndExperienceCurve() {
        assertEquals(1.0, GameBalance.getEnemyStatScale(1, 1), 1e-9);
        assertEquals(1.24, GameBalance.getEnemyStatScale(1, 3), 1e-9);
        assertEquals(1.40, GameBalance.getEnemyStatScale(2, 1), 1e-9);
        assertEquals(2.04, GameBalance.getEnemyStatScale(3, 3), 1e-9);

        // Lv1→2(80 EXP)에 고블린 14마리 이내 (기존 40마리)
        int goblinsForLevelTwo = (int) Math.ceil(80.0 / EnemyType.GOBLIN.getExperience());
        assertTrue(goblinsForLevelTwo <= 14, "goblins needed for Lv2: " + goblinsForLevelTwo);
    }
}
