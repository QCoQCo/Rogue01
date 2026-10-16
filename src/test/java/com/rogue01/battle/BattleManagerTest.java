package com.rogue01.battle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rogue01.battle.BattleManager.BattleAction;
import com.rogue01.battle.BattleManager.BattleState;
import com.rogue01.entity.Enemy;
import com.rogue01.entity.EnemyType;
import com.rogue01.entity.Player;
import com.rogue01.game.GameBalance;
import com.rogue01.map.utils.RandomUtils;
import org.junit.jupiter.api.Test;

/**
 * 전투 행동 테스트 - 방어 자세의 피해 감소와 지속 시간
 * 크리티컬/데미지 변동이 있어 시드를 고정한 여러 전투의 평균으로 비교한다.
 */
class BattleManagerTest {
    private static final int SEEDS = 300;

    @Test
    void defendHalvesNextEnemyHitOnly() {
        double undefended = 0;
        double defended = 0;
        double afterDefend = 0;

        for (long seed = 0; seed < SEEDS; seed++) {
            // 방어하지 않은 경우: 공격 후 적 턴 피해
            Fight plain = new Fight(seed);
            undefended += plain.takeTurn(BattleAction.ATTACK);

            // 방어한 경우: 방어 후 적 턴 피해, 그 다음 공격 후 적 턴 피해(방어 효과 소멸 확인)
            Fight guarded = new Fight(seed);
            defended += guarded.takeTurn(BattleAction.DEFEND);
            afterDefend += guarded.takeTurn(BattleAction.ATTACK);
        }
        undefended /= SEEDS;
        defended /= SEEDS;
        afterDefend /= SEEDS;

        double ratio = defended / undefended;
        assertTrue(ratio > 0.4 && ratio < 0.6,
                String.format("defended hit should be about half: %.2f vs %.2f (ratio %.2f)", defended, undefended, ratio));
        double afterRatio = afterDefend / undefended;
        assertTrue(afterRatio > 0.85 && afterRatio < 1.15,
                String.format("defend must last one enemy hit only: next hit %.2f vs %.2f", afterDefend, undefended));
    }

    @Test
    void defendedHitIsLoggedAndNeverBelowOne() {
        Fight fight = new Fight(42);
        int damage = fight.takeTurn(BattleAction.DEFEND);

        assertTrue(damage >= 1);
        assertTrue(fight.battle.getBattleLog().stream().anyMatch(line -> line.contains("방어로 피해 감소")),
                "battle log should mention the reduced hit");
    }

    @Test
    void defendDoesNotEndPlayerTurnEarlyOrSkipEnemy() {
        Fight fight = new Fight(7);
        fight.battle.executePlayerAction(BattleAction.DEFEND);
        assertEquals(BattleState.ENEMY_TURN, fight.battle.getBattleState());
        fight.battle.update();
        assertEquals(BattleState.PLAYER_TURN, fight.battle.getBattleState());
    }

    /** Lv1 플레이어(장비 없음) vs 드래곤 - 적 공격이 방어력보다 충분히 커서 배율 차이가 드러남 */
    private static class Fight {
        final Player player = new Player(0, 0);
        final BattleManager battle;

        Fight(long seed) {
            Enemy dragon = new Enemy(1, 0, EnemyType.DRAGON);
            battle = new BattleManager(player, dragon, 1, 0, GameBalance.Difficulty.NORMAL, new RandomUtils(seed));
            battle.update(); // STARTING → PLAYER_TURN
        }

        /** 플레이어 행동 1회 + 적 턴 1회 후 플레이어가 받은 피해 */
        int takeTurn(BattleAction action) {
            int before = player.getHealth();
            battle.executePlayerAction(action);
            battle.update(); // ENEMY_TURN 실행
            return before - player.getHealth();
        }
    }
}
