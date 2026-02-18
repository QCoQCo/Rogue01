package com.rogue01.battle;

import com.rogue01.entity.Player;
import com.rogue01.entity.Enemy;
import com.rogue01.map.utils.RandomUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * 전투를 관리하는 매니저 클래스 (JRPG 스타일)
 */
public class BattleManager {
    private Player player;
    private Enemy enemy;
    private BattleState battleState;
    private RandomUtils randomUtils;
    private List<String> battleLog;
    private int turnNumber;
    private boolean playerTurn;
    private boolean battleEnded;
    private BattleResult result;
    
    public enum BattleState {
        STARTING,       // 전투 시작
        PLAYER_TURN,    // 플레이어 턴
        ENEMY_TURN,     // 적 턴
        ANIMATING,      // 애니메이션 중
        VICTORY,        // 승리
        DEFEAT,         // 패배
        ESCAPED         // 도망 성공
    }
    
    public enum BattleResult {
        VICTORY,
        DEFEAT,
        ESCAPED
    }
    
    public enum BattleAction {
        ATTACK,
        DEFEND,
        ITEM,
        ESCAPE
    }
    
    public BattleManager(Player player, Enemy enemy) {
        this.player = player;
        this.enemy = enemy;
        this.randomUtils = new RandomUtils();
        this.battleLog = new ArrayList<>();
        this.turnNumber = 0;
        this.battleState = BattleState.STARTING;
        this.battleEnded = false;
        this.playerTurn = true; // 플레이어가 먼저 시작
        
        addLog("전투 시작! " + enemy.getName() + "와(과) 맞섰다!");
    }
    
    /**
     * 전투 업데이트
     */
    public void update() {
        if (battleEnded) {
            return;
        }
        
        switch (battleState) {
            case STARTING:
                battleState = BattleState.PLAYER_TURN;
                addLog("당신의 턴입니다!");
                break;
                
            case PLAYER_TURN:
                // 플레이어 입력 대기 (UI에서 처리)
                break;
                
            case ENEMY_TURN:
                executeEnemyTurn();
                break;
                
            case ANIMATING:
                // 애니메이션 처리 (나중에 구현)
                battleState = BattleState.PLAYER_TURN;
                break;
                
            case VICTORY:
            case DEFEAT:
            case ESCAPED:
                battleEnded = true;
                break;
        }
        
        // 승리/패배 체크
        checkBattleEnd();
    }
    
    /**
     * 플레이어 액션 실행
     */
    public void executePlayerAction(BattleAction action) {
        if (battleState != BattleState.PLAYER_TURN || battleEnded) {
            return;
        }
        
        switch (action) {
            case ATTACK:
                executePlayerAttack();
                break;
            case DEFEND:
                executePlayerDefend();
                break;
            case ITEM:
                // 아이템 사용 (나중에 구현)
                addLog("아이템을 사용했습니다.");
                break;
            case ESCAPE:
                executeEscape();
                break;
        }
        
        // 적 턴으로 전환
        if (!battleEnded && battleState == BattleState.PLAYER_TURN) {
            battleState = BattleState.ENEMY_TURN;
            turnNumber++;
        }
    }
    
    /**
     * 플레이어 공격 실행
     */
    private void executePlayerAttack() {
        int playerAttack = player.getAttack();
        int enemyDefense = enemy.getDefense();
        
        // 크리티컬 체크 (10% 확률)
        boolean isCritical = randomUtils.nextBoolean(0.1);
        int damage = calculateDamage(playerAttack, enemyDefense, isCritical);
        
        enemy.takeDamage(damage);
        
        String message = isCritical ? 
            "크리티컬 히트! " + damage + "의 데미지!" :
            player.getName() + "의 공격! " + damage + "의 데미지!";
        addLog(message);
        
        if (enemy.isDead()) {
            addLog(enemy.getName() + "를(을) 쓰러뜨렸다!");
            battleState = BattleState.VICTORY;
            result = BattleResult.VICTORY;
        }
    }
    
    /**
     * 플레이어 방어 실행
     */
    private void executePlayerDefend() {
        addLog(player.getName() + "는(은) 방어 자세를 취했다!");
        // 방어 시 다음 턴에 받는 데미지 감소 (50%)
        // 이는 임시 버프로 구현 가능 (나중에 확장)
    }
    
    /**
     * 도망 시도
     */
    private void executeEscape() {
        // 도망 성공 확률: 70%
        boolean escaped = randomUtils.nextBoolean(0.7);
        
        if (escaped) {
            addLog("무사히 도망쳤다!");
            battleState = BattleState.ESCAPED;
            result = BattleResult.ESCAPED;
        } else {
            addLog("도망에 실패했다!");
        }
    }
    
    /**
     * 적 턴 실행
     */
    private void executeEnemyTurn() {
        if (enemy.isDead()) {
            return;
        }
        
        // 적은 항상 공격
        int enemyAttack = enemy.getAttack();
        int playerDefense = player.getDefense();
        
        // 크리티컬 체크 (5% 확률)
        boolean isCritical = randomUtils.nextBoolean(0.05);
        int damage = calculateDamage(enemyAttack, playerDefense, isCritical);
        
        player.takeDamage(damage);
        
        String message = isCritical ?
            "크리티컬 히트! " + damage + "의 데미지!" :
            enemy.getName() + "의 공격! " + damage + "의 데미지!";
        addLog(message);
        
        if (player.isDead()) {
            addLog(player.getName() + "는(은) 쓰러졌다...");
            battleState = BattleState.DEFEAT;
            result = BattleResult.DEFEAT;
        } else {
            // 플레이어 턴으로 전환
            battleState = BattleState.PLAYER_TURN;
            addLog("당신의 턴입니다!");
        }
    }
    
    /**
     * 데미지 계산
     */
    private int calculateDamage(int attack, int defense, boolean isCritical) {
        int baseDamage = Math.max(1, attack - defense);
        
        if (isCritical) {
            baseDamage = (int)(baseDamage * 1.5);
        }
        
        // 랜덤 변동 (80%~120%)
        double variance = 0.8 + randomUtils.getRandom().nextDouble() * 0.4;
        return Math.max(1, (int)(baseDamage * variance));
    }
    
    /**
     * 전투 종료 체크
     */
    private void checkBattleEnd() {
        if (player.isDead()) {
            battleState = BattleState.DEFEAT;
            result = BattleResult.DEFEAT;
            battleEnded = true;
        } else if (enemy.isDead()) {
            battleState = BattleState.VICTORY;
            result = BattleResult.VICTORY;
            battleEnded = true;
        }
    }
    
    /**
     * 전투 로그 추가
     */
    public void addLog(String message) {
        battleLog.add(message);
        // 최대 10개까지만 유지
        if (battleLog.size() > 10) {
            battleLog.remove(0);
        }
    }
    
    /**
     * 전투 종료 후 보상 처리
     */
    public void processRewards() {
        if (result == BattleResult.VICTORY) {
            // 경험치 획득 (나중에 레벨 시스템과 연동)
            int exp = enemy.getExperience();
            addLog("경험치 " + exp + "를 획득했다!");
            // TODO: 플레이어 경험치 추가
        }
    }
    
    // Getters
    public Player getPlayer() {
        return player;
    }
    
    public Enemy getEnemy() {
        return enemy;
    }
    
    public BattleState getBattleState() {
        return battleState;
    }
    
    public List<String> getBattleLog() {
        return new ArrayList<>(battleLog);
    }
    
    public int getTurnNumber() {
        return turnNumber;
    }
    
    public boolean isBattleEnded() {
        return battleEnded;
    }
    
    public BattleResult getResult() {
        return result;
    }
    
    public boolean isPlayerTurn() {
        return battleState == BattleState.PLAYER_TURN;
    }
}
