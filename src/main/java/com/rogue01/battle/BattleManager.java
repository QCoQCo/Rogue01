package com.rogue01.battle;

import com.rogue01.entity.Player;
import com.rogue01.entity.Enemy;
import com.rogue01.game.GameBalance;
import com.rogue01.item.Consumable;
import com.rogue01.item.Item;
import com.rogue01.map.utils.RandomUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * 전투를 관리하는 매니저 클래스 (JRPG 스타일)
 */
public class BattleManager {
    private Player player;
    private Enemy enemy;
    private int dropX;
    private int dropY;
    private GameBalance.Difficulty difficulty;
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
    
    public BattleManager(Player player, Enemy enemy, int dropX, int dropY, GameBalance.Difficulty difficulty) {
        this.player = player;
        this.enemy = enemy;
        this.dropX = dropX;
        this.dropY = dropY;
        this.difficulty = difficulty != null ? difficulty : GameBalance.Difficulty.NORMAL;
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
                // 아이템은 BattleScreen에서 소비아이템 선택 후 executeUseConsumable 호출
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
        double escapeChance = GameBalance.getEscapeChance(difficulty);
        boolean escaped = randomUtils.nextBoolean(escapeChance);
        
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
        
        int enemyAttack = (int)(enemy.getAttack() * GameBalance.getEnemyAttackMultiplier(difficulty));
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
     * 소비 아이템 사용 (전투 중)
     * @param consumableListIndex getConsumables() 목록 기준 인덱스
     * @return true if used, false if failed (취소 또는 없음)
     */
    public boolean executeUseConsumable(int consumableListIndex) {
        if (battleState != BattleState.PLAYER_TURN || battleEnded) {
            return false;
        }
        var consumables = player.getInventory().getConsumables();
        if (consumableListIndex < 0 || consumableListIndex >= consumables.size()) {
            return false;
        }
        Item item = consumables.get(consumableListIndex);
        if (!(item instanceof Consumable)) {
            return false;
        }
        int hpBefore = player.getHealth();
        boolean used = player.getInventory().useItem(item, player);
        if (!used) {
            return false;
        }
        int healed = player.getHealth() - hpBefore;
        if (healed > 0) {
            addLog(player.getName() + "는(은) " + item.getName() + "을(를) 사용했다! HP +" + healed);
        } else {
            addLog(player.getName() + "는(은) " + item.getName() + "을(를) 사용했다!");
        }
        battleState = BattleState.ENEMY_TURN;
        turnNumber++;
        return true;
    }
    
    /**
     * 소비 아이템 목록 (전투 중 사용 가능)
     */
    public List<Item> getConsumables() {
        return player.getInventory().getConsumables();
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
            int exp = enemy.getExperience();
            int prevLevel = player.getLevel();
            player.addExperience(exp);
            addLog("경험치 " + exp + "를 획득했다!");
            if (player.getLevel() > prevLevel) {
                addLog("레벨 업! Lv." + player.getLevel() + "!");
            }
        }
    }
    
    // Getters
    public Player getPlayer() {
        return player;
    }
    
    public Enemy getEnemy() {
        return enemy;
    }
    
    public int getDropX() { return dropX; }
    public int getDropY() { return dropY; }
    
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
