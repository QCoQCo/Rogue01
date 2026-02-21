package com.rogue01.entity;

import com.rogue01.map.Map;
import com.rogue01.map.utils.RandomUtils;
import java.util.List;

/**
 * 적(몬스터) 엔티티 클래스
 */
public class Enemy extends Entity {
    private EnemyType enemyType;
    private int attack;
    private int defense;
    private int experience;
    private RandomUtils randomUtils;

    // AI 상태
    private boolean hasSeenPlayer;
    private int lastPlayerX;
    private int lastPlayerY;

    public Enemy(int x, int y, EnemyType enemyType) {
        super(x, y, enemyType.getSymbol(), enemyType.getKoreanName());
        this.enemyType = enemyType;
        this.maxHealth = enemyType.getMaxHealth();
        this.health = maxHealth;
        this.attack = enemyType.getAttack();
        this.defense = enemyType.getDefense();
        this.experience = enemyType.getExperience();
        this.randomUtils = new RandomUtils();
        this.hasSeenPlayer = false;
    }

    @Override
    public void update(Map map) {
        if (isDead()) {
            return;
        }
        // 턴제: 호출될 때마다 한 칸 이동 (다른 적 없을 때만 - List<Enemy> 없는 경우 호출 시)
        randomMove(map, List.of());
    }

    /**
     * 플레이어를 추적하는 AI 업데이트 (턴제: 호출될 때마다 한 칸 이동)
     * 
     * @param allEnemies 다른 적과 겹치지 않도록 전체 적 목록 필요
     */
    public void update(Map map, Player player, List<Enemy> allEnemies) {
        if (isDead()) {
            return;
        }

        // 플레이어와의 거리 계산
        int dx = player.getX() - x;
        int dy = player.getY() - y;
        int distance = (int) Math.sqrt(dx * dx + dy * dy);

        // 시야 범위 내에 있으면 추적
        int sightRange = 8;
        if (distance <= sightRange) {
            hasSeenPlayer = true;
            lastPlayerX = player.getX();
            lastPlayerY = player.getY();

            // 플레이어에게 접근
            moveTowards(map, player.getX(), player.getY(), allEnemies);
        } else if (hasSeenPlayer && distance <= sightRange * 2) {
            // 마지막으로 본 위치로 이동
            moveTowards(map, lastPlayerX, lastPlayerY, allEnemies);
        } else {
            // 시야 밖이면 랜덤 이동
            hasSeenPlayer = false;
            randomMove(map, allEnemies);
        }
    }

    /**
     * 목표 위치로 이동 (다른 적이 있는 타일로는 이동하지 않음)
     */
    private void moveTowards(Map map, int targetX, int targetY, List<Enemy> allEnemies) {
        int dx = targetX - x;
        int dy = targetY - y;

        // X 또는 Y 방향으로 이동
        if (Math.abs(dx) > Math.abs(dy)) {
            // X 방향 우선
            if (dx > 0 && canMoveTo(map, x + 1, y, allEnemies)) {
                x++;
            } else if (dx < 0 && canMoveTo(map, x - 1, y, allEnemies)) {
                x--;
            } else if (dy > 0 && canMoveTo(map, x, y + 1, allEnemies)) {
                y++;
            } else if (dy < 0 && canMoveTo(map, x, y - 1, allEnemies)) {
                y--;
            }
        } else {
            // Y 방향 우선
            if (dy > 0 && canMoveTo(map, x, y + 1, allEnemies)) {
                y++;
            } else if (dy < 0 && canMoveTo(map, x, y - 1, allEnemies)) {
                y--;
            } else if (dx > 0 && canMoveTo(map, x + 1, y, allEnemies)) {
                x++;
            } else if (dx < 0 && canMoveTo(map, x - 1, y, allEnemies)) {
                x--;
            }
        }
    }

    /**
     * 랜덤 이동 (다른 적이 있는 타일로는 이동하지 않음)
     */
    private void randomMove(Map map, List<Enemy> allEnemies) {
        int direction = randomUtils.nextInt(4);
        int newX = x;
        int newY = y;

        switch (direction) {
            case 0: // 위
                newY--;
                break;
            case 1: // 아래
                newY++;
                break;
            case 2: // 왼쪽
                newX--;
                break;
            case 3: // 오른쪽
                newX++;
                break;
        }

        if (canMoveTo(map, newX, newY, allEnemies)) {
            x = newX;
            y = newY;
        }
    }

    /**
     * 해당 위치로 이동 가능한지 (맵이 통과 가능하고, 다른 적이 없음)
     * 플레이어 위치는 허용 (전투 시작용)
     */
    private boolean canMoveTo(Map map, int targetX, int targetY, List<Enemy> allEnemies) {
        if (!map.isWalkable(targetX, targetY)) {
            return false;
        }
        for (Enemy other : allEnemies) {
            if (other != this && !other.isDead() && other.getX() == targetX && other.getY() == targetY) {
                return false;
            }
        }
        return true;
    }

    /**
     * 플레이어를 공격
     */
    public void attack(Player player) {
        if (isDead()) {
            return;
        }

        int damage = Math.max(1, attack - player.getDefense());
        player.takeDamage(damage);
    }

    /**
     * 플레이어와 인접해 있는지 확인
     */
    public boolean isAdjacentTo(Player player) {
        int dx = Math.abs(x - player.getX());
        int dy = Math.abs(y - player.getY());
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    // Getters
    public EnemyType getEnemyType() {
        return enemyType;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getExperience() {
        return experience;
    }
}
