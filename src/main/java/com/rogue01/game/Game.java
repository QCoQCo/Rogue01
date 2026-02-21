package com.rogue01.game;

import com.rogue01.ui.GameWindow;
import com.rogue01.map.Map;
import com.rogue01.entity.Player;
import com.rogue01.entity.Enemy;
import com.rogue01.entity.EnemyType;
import com.rogue01.battle.BattleManager;
import com.rogue01.item.ItemFactory;
import com.rogue01.item.Item;
import com.rogue01.item.MapItem;
import com.rogue01.map.structures.Room;
import com.rogue01.map.utils.RandomUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * 게임의 메인 로직을 관리하는 클래스 - 책임을 분리하여 간소화
 */
public class Game {
    private GameState gameState;
    private GameLoop gameLoop;
    private GameWindow gameWindow;
    private Map map;
    private Player player;
    private InputManager inputManager;
    private List<Enemy> enemies;
    private List<MapItem> mapItems;
    private RandomUtils randomUtils;
    private BattleManager battleManager;
    
    // 게임 통계
    private int killCount;
    private long gameStartTime;
    
    // 턴제 시스템
    private TurnPhase turnPhase;
    
    // 일시정지 전 상태 (BATTLE 등에서 복귀용)
    private GameState stateBeforePause;
    
    // 난이도
    private GameBalance.Difficulty difficulty;

    public Game() {
        this.gameState = GameState.MENU;
        this.gameLoop = new GameLoop();
        this.gameWindow = new GameWindow();
        this.map = new Map(750, 450);
        this.player = new Player(375, 225);
        this.enemies = new ArrayList<>();
        this.mapItems = new ArrayList<>();
        this.randomUtils = new RandomUtils();

        // 플레이어에게 GameWindow의 InputHandler 설정
        this.player.setInputHandler(this.gameWindow.getInputHandler());

        // InputManager 초기화
        this.inputManager = new InputManager(this.gameWindow.getInputHandler(), this.player);

        // InputManager에 Game 인스턴스 설정
        this.inputManager.setGame(this);

        // 테스트 아이템 추가
        addTestItems();

        // 난이도 (기본: 보통) - spawnEnemies 등에서 사용하므로 먼저 설정
        this.difficulty = GameBalance.Difficulty.NORMAL;

        // 적 스폰
        spawnEnemies();
        
        // 게임 통계 초기화
        this.killCount = 0;
        this.gameStartTime = System.currentTimeMillis();
        
        // 턴제: 플레이어 턴부터 시작
        this.turnPhase = TurnPhase.PLAYER_TURN;
    }
    
    /**
     * 턴 페이즈 (턴제 로그라이크)
     */
    public enum TurnPhase {
        PLAYER_TURN,   // 플레이어 행동 대기
        ENEMY_TURN     // 적 행동 처리
    }

    public void start() {
        gameWindow.setVisible(true);
        gameLoop.start(this);
    }

    public void update() {
        // InputManager를 통한 입력 처리
        inputManager.handleInput(gameState);

        // 게임 상태에 따른 추가 로직
        switch (gameState) {
            case MENU:
                handleMenuState();
                break;
            case PLAYING:
                handlePlayingState();
                break;
            case PAUSED:
                handlePausedState();
                break;
            case INVENTORY:
                handleInventoryState();
                break;
            case MAP_VIEW:
                handleMapViewState();
                break;
            case BATTLE:
                handleBattleState();
                break;
            case GAME_OVER:
                handleGameOverState();
                break;
        }

        // 맵 업데이트
        map.update();
    }

    /**
     * 메뉴 상태 처리
     */
    private void handleMenuState() {
        // ENTER키 처리는 InputManager에서 담당
        // 여기서는 메뉴 상태에서 필요한 추가 로직만 처리
    }

    /**
     * 게임 진행 중 상태 처리 (턴제)
     */
    private void handlePlayingState() {
        // 메뉴 키는 턴과 무관하게 항상 처리
        if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_ESCAPE)) {
            setGameState(GameState.PAUSED);
            return;
        } else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_I)) {
            setGameState(GameState.INVENTORY);
            return;
        } else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_M)) {
            setGameState(GameState.MAP_VIEW);
            return;
        }

        if (turnPhase == TurnPhase.PLAYER_TURN) {
            // 플레이어 턴: 이동 또는 대기
            if (processPlayerTurn()) {
                turnPhase = TurnPhase.ENEMY_TURN;
            }
        } else {
            // 적 턴: 모든 적이 한 칸씩 이동
            processEnemyTurn();
            turnPhase = TurnPhase.PLAYER_TURN;
        }
    }
    
    /**
     * 플레이어 턴 처리 - 이동 또는 대기
     * @return true if player acted (moved or waited), false otherwise
     */
    private boolean processPlayerTurn() {
        var inputHandler = gameWindow.getInputHandler();
        
        // 대기 (턴 넘기기) - Space
        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_SPACE)) {
            inputHandler.consumeKey(java.awt.event.KeyEvent.VK_SPACE);
            return true;
        }
        
        // 이동 처리
        int dx = 0, dy = 0;
        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_W) || inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_UP)) {
            dy = -1;
        } else if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_S) || inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_DOWN)) {
            dy = 1;
        } else if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_A) || inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_LEFT)) {
            dx = -1;
        } else if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_D) || inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_RIGHT)) {
            dx = 1;
        }
        
        if (dx != 0 || dy != 0) {
            int targetX = player.getX() + dx;
            int targetY = player.getY() + dy;
            
            // 이동 키 소비
            inputHandler.consumeKey(java.awt.event.KeyEvent.VK_W);
            inputHandler.consumeKey(java.awt.event.KeyEvent.VK_UP);
            inputHandler.consumeKey(java.awt.event.KeyEvent.VK_S);
            inputHandler.consumeKey(java.awt.event.KeyEvent.VK_DOWN);
            inputHandler.consumeKey(java.awt.event.KeyEvent.VK_A);
            inputHandler.consumeKey(java.awt.event.KeyEvent.VK_LEFT);
            inputHandler.consumeKey(java.awt.event.KeyEvent.VK_D);
            inputHandler.consumeKey(java.awt.event.KeyEvent.VK_RIGHT);
            
            // 목표 타일에 적이 있으면 전투 시작
            Enemy enemyAtTarget = getEnemyAt(targetX, targetY);
            if (enemyAtTarget != null) {
                startBattle(enemyAtTarget, targetX, targetY);
                return true; // 행동함 (전투 시작)
            }
            
            // 이동 가능하면 이동
            if (map.isWalkable(targetX, targetY)) {
                player.setPosition(targetX, targetY);
                checkItemPickup();
                return true;
            }
            return false; // 벽이라 이동 못함 - 턴 소모 안 함
        }
        
        return false; // 아무 입력 없음
    }
    
    /**
     * 적 턴 처리 - 모든 적이 한 칸씩 이동
     */
    private void processEnemyTurn() {
        updateEnemiesWithCollisionCheck();
        removeDeadEnemies();
    }
    
    /**
     * 지정된 위치에 있는 적 반환
     */
    private Enemy getEnemyAt(int x, int y) {
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && enemy.getX() == x && enemy.getY() == y) {
                return enemy;
            }
        }
        return null;
    }
    
    /**
     * 전투 상태 처리
     */
    private void handleBattleState() {
        if (battleManager != null) {
            battleManager.update();
            
            // 전투 종료 체크
            if (battleManager.isBattleEnded()) {
                BattleManager.BattleResult result = battleManager.getResult();
                
                if (result == BattleManager.BattleResult.VICTORY) {
                    battleManager.processRewards();
                    dropItemFromEnemy(battleManager.getEnemy(), battleManager.getDropX(), battleManager.getDropY());
                    // 전투 중인 적 제거
                    enemies.remove(battleManager.getEnemy());
                    killCount++;
                } else if (result == BattleManager.BattleResult.DEFEAT) {
                    // 게임 오버
                    setGameState(GameState.GAME_OVER);
                } else if (result == BattleManager.BattleResult.ESCAPED) {
                    // 도망 성공: 플레이어를 적 반대 방향으로 한 칸 이동
                    movePlayerAwayFromEnemy(battleManager.getEnemy());
                    turnPhase = TurnPhase.PLAYER_TURN; // 플레이어 턴부터 시작
                }
                
                // 전투 종료 후 게임으로 복귀
                if (result != BattleManager.BattleResult.DEFEAT) {
                    setGameState(GameState.PLAYING);
                    battleManager = null;
                }
            }
        }
    }
    
    /**
     * 적 업데이트 및 충돌 체크 (적이 플레이어 타일로 이동 시 전투)
     * 이동 전 위치를 저장하여 아이템 드롭 시 사용
     */
    private void updateEnemiesWithCollisionCheck() {
        if (gameState == GameState.BATTLE) {
            return;
        }
        
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;
            
            int dropX = enemy.getX();
            int dropY = enemy.getY();
            enemy.update(map, player, enemies);
            
            if (player.getX() == enemy.getX() && player.getY() == enemy.getY()) {
                startBattle(enemy, dropX, dropY);
                return;
            }
        }
    }
    
    /**
     * 적과의 충돌 체크 (플레이어가 적 타일로 이동한 경우 - processPlayerTurn에서 처리)
     */
    private void checkEnemyCollision() {
        if (gameState == GameState.BATTLE) return;
        
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;
            if (player.getX() == enemy.getX() && player.getY() == enemy.getY()) {
                startBattle(enemy, enemy.getX(), enemy.getY());
                break;
            }
        }
    }
    
    /**
     * 적 처치 시 아이템 드롭 (적이 있던 위치에)
     */
    private void dropItemFromEnemy(Enemy enemy, int dropX, int dropY) {
        Item droppedItem = ItemFactory.createRandomDrop(difficulty);
        if (droppedItem != null) {
            mapItems.add(new MapItem(dropX, dropY, droppedItem));
        }
    }
    
    /**
     * 아이템 획득 체크 (플레이어가 아이템 위에 있을 때 자동 획득)
     */
    private void checkItemPickup() {
        int playerX = player.getX();
        int playerY = player.getY();
        
        mapItems.removeIf(mapItem -> {
            if (mapItem.getX() == playerX && mapItem.getY() == playerY) {
                if (!player.getInventory().isFull()) {
                    player.getInventory().addItem(mapItem.getItem());
                    return true; // 제거
                }
                return false; // 인벤토리 가득 참
            }
            return false;
        });
    }
    
    /**
     * 전투 시작
     * @param dropX, dropY 아이템 드롭 위치 (적이 죽은 위치)
     */
    private void startBattle(Enemy enemy, int dropX, int dropY) {
        battleManager = new BattleManager(player, enemy, dropX, dropY, difficulty);
        setGameState(GameState.BATTLE);
    }
    
    /**
     * 도망 시 플레이어를 적 반대 방향으로 한 칸 이동
     */
    private void movePlayerAwayFromEnemy(Enemy enemy) {
        int dx = player.getX() - enemy.getX();
        int dy = player.getY() - enemy.getY();
        // 같은 타일(적이 플레이어에게 온 경우): 4방향 중 빈 타일 탐색
        if (dx == 0 && dy == 0) {
            int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] d : dirs) {
                int ex = player.getX() + d[0];
                int ey = player.getY() + d[1];
                if (map.isWalkable(ex, ey) && getEnemyAt(ex, ey) == null) {
                    player.setPosition(ex, ey);
                    return;
                }
            }
            return;
        }
        // 인접한 경우: 적 반대 방향으로 이동
        if (dx != 0) dx = dx > 0 ? 1 : -1;
        if (dy != 0) dy = dy > 0 ? 1 : -1;
        int escapeX = player.getX() + dx;
        int escapeY = player.getY() + dy;
        if (map.isWalkable(escapeX, escapeY) && getEnemyAt(escapeX, escapeY) == null) {
            player.setPosition(escapeX, escapeY);
        }
    }

    /**
     * 일시정지 상태 처리
     */
    private void handlePausedState() {
        if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_ESCAPE)) {
            setGameState(GameState.PLAYING);
        }
    }

    /**
     * 인벤토리 상태 처리
     */
    private void handleInventoryState() {
        if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_I) ||
                gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_ESCAPE)) {
            setGameState(GameState.PLAYING);
        }
    }

    /**
     * 맵 뷰 상태 처리
     */
    private void handleMapViewState() {
        if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_M) ||
                gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_ESCAPE)) {
            setGameState(GameState.PLAYING);
        }
    }

    /**
     * 게임 오버 상태 처리
     */
    private void handleGameOverState() {
        // 입력 처리는 InputManager에서 담당 (재시작, 메뉴, 종료)
    }
    
    /**
     * 게임 재시작
     */
    public void restartGame() {
        // 맵 재생성
        map.regenerate(Map.MapGeneratorType.HYBRID);
        
        // 플레이어 초기화
        int startX = map.getGenerationInfo().getPlayerStartX();
        int startY = map.getGenerationInfo().getPlayerStartY();
        player.setPosition(startX, startY);
        player.resetLevelAndStats();
        player.getInventory().clear();
        addTestItems();
        
        // 적 초기화
        enemies.clear();
        mapItems.clear();
        spawnEnemies();
        
        // 전투 상태 초기화
        battleManager = null;
        
        // 게임 통계 초기화
        killCount = 0;
        gameStartTime = System.currentTimeMillis();
        
        turnPhase = TurnPhase.PLAYER_TURN;
        
        setGameState(GameState.PLAYING);
    }

    public void render() {
        gameWindow.render(this);
    }

    /**
     * 게임 상태 변경
     */
    public void setGameState(GameState gameState) {
        if (gameState == GameState.PAUSED) {
            this.stateBeforePause = this.gameState;
        }
        this.gameState = gameState;
        gameWindow.getInputHandler().clearKeys();
    }
    
    /**
     * 일시정지에서 복귀 (이전 상태로)
     */
    public void resumeFromPause() {
        if (stateBeforePause != null) {
            setGameState(stateBeforePause);
            stateBeforePause = null;
        } else {
            setGameState(GameState.PLAYING);
        }
    }

    /**
     * 적 스폰 시스템
     */
    private void spawnEnemies() {
        List<Room> rooms = map.getRooms();
        if (rooms.isEmpty()) {
            // 방이 없으면 랜덤 위치에 스폰
            spawnEnemiesRandomly();
            return;
        }

        // 각 방에 적 스폰
        for (Room room : rooms) {
            // 플레이어 시작 위치와 너무 가까운 방은 제외
            int playerStartX = map.getGenerationInfo().getPlayerStartX();
            int playerStartY = map.getGenerationInfo().getPlayerStartY();
            int roomCenterX = room.getCenterX();
            int roomCenterY = room.getCenterY();

            int distance = (int) Math.sqrt(
                    Math.pow(roomCenterX - playerStartX, 2) +
                            Math.pow(roomCenterY - playerStartY, 2));

            if (distance < GameBalance.SPAWN_MIN_DISTANCE) {
                continue;
            }

            int baseCount = Math.max(1, room.getArea() / GameBalance.ROOM_ENEMY_DIVISOR);
            int enemyCount = Math.max(1, (int)(baseCount * GameBalance.getSpawnDensityMultiplier(difficulty)));

            for (int i = 0; i < enemyCount; i++) {
                for (int attempt = 0; attempt < 10; attempt++) {
                    int spawnX = randomUtils.nextInt(room.getX() + 1, room.getX() + room.getWidth() - 2);
                    int spawnY = randomUtils.nextInt(room.getY() + 1, room.getY() + room.getHeight() - 2);
                    if (map.isWalkable(spawnX, spawnY) && getEnemyAt(spawnX, spawnY) == null) {
                        EnemyType enemyType = getRandomEnemyType();
                        Enemy enemy = new Enemy(spawnX, spawnY, enemyType);
                        enemies.add(enemy);
                        break;
                    }
                }
            }
        }

        System.out.println("Spawned " + enemies.size() + " enemies");
    }

    /**
     * 랜덤 위치에 적 스폰 (방이 없을 때)
     */
    private void spawnEnemiesRandomly() {
        int baseCount = (map.getWidth() * map.getHeight()) / GameBalance.RANDOM_SPAWN_DIVISOR;
        int enemyCount = Math.max(1, (int)(baseCount * GameBalance.getSpawnDensityMultiplier(difficulty)));

        for (int i = 0; i < enemyCount; i++) {
            int attempts = 0;
            while (attempts < 50) {
                int x = randomUtils.nextInt(1, map.getWidth() - 2);
                int y = randomUtils.nextInt(1, map.getHeight() - 2);

                int playerStartX = map.getGenerationInfo().getPlayerStartX();
                int playerStartY = map.getGenerationInfo().getPlayerStartY();
                int distance = (int) Math.sqrt(
                        Math.pow(x - playerStartX, 2) + Math.pow(y - playerStartY, 2));

                if (map.isWalkable(x, y) && distance >= GameBalance.SPAWN_MIN_DISTANCE && getEnemyAt(x, y) == null) {
                    EnemyType enemyType = getRandomEnemyType();
                    Enemy enemy = new Enemy(x, y, enemyType);
                    enemies.add(enemy);
                    break;
                }
                attempts++;
            }
        }
    }

    /**
     * 랜덤 적 타입 선택 (약한 적 위주)
     */
    private EnemyType getRandomEnemyType() {
        double rand = randomUtils.getRandom().nextDouble();
        if (rand < GameBalance.SPAWN_GOBLIN) {
            return EnemyType.GOBLIN;
        } else if (rand < GameBalance.SPAWN_SKELETON) {
            return EnemyType.SKELETON;
        } else if (rand < GameBalance.SPAWN_ORC) {
            return EnemyType.ORC;
        } else if (rand < GameBalance.SPAWN_TROLL) {
            return EnemyType.TROLL;
        } else {
            return EnemyType.DRAGON;
        }
    }

    /**
     * 적 업데이트
     */
    /**
     * 사망한 적 제거
     */
    private void removeDeadEnemies() {
        enemies.removeIf(Enemy::isDead);
    }

    // Getters
    public GameState getGameState() {
        return gameState;
    }

    public Map getMap() {
        return map;
    }

    public Player getPlayer() {
        return player;
    }

    /** 스폰 지점 기준 상대 X (오른쪽이 +) */
    public int getRelPlayerX() {
        return player.getX() - map.getGenerationInfo().getPlayerStartX();
    }

    /** 스폰 지점 기준 상대 Y (위가 +) */
    public int getRelPlayerY() {
        return map.getGenerationInfo().getPlayerStartY() - player.getY();
    }

    public GameWindow getGameWindow() {
        return gameWindow;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public List<Enemy> getEnemies() {
        return new ArrayList<>(enemies);
    }
    
    public List<MapItem> getMapItems() {
        return new ArrayList<>(mapItems);
    }
    
    public BattleManager getBattleManager() {
        return battleManager;
    }
    
    public int getKillCount() {
        return killCount;
    }
    
    public long getGameStartTime() {
        return gameStartTime;
    }
    
    public GameBalance.Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(GameBalance.Difficulty difficulty) { this.difficulty = difficulty != null ? difficulty : GameBalance.Difficulty.NORMAL; }
    
    /**
     * 인벤토리에서 소비 아이템 사용 (필드)
     * @param slotIndex 인벤토리 슬롯 인덱스
     * @return true if used, false otherwise
     */
    public boolean closeItemDetailPopupIfActive() {
        return gameWindow.closeItemDetailPopupIfActive();
    }
    
    public boolean useConsumableAtSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= player.getInventory().getSize()) {
            return false;
        }
        var item = player.getInventory().getItem(slotIndex);
        return player.getInventory().useItem(item, player);
    }
    
    /**
     * 생존 시간 (초)
     */
    public long getSurvivalTimeSeconds() {
        return (System.currentTimeMillis() - gameStartTime) / 1000;
    }

    /**
     * 테스트 아이템들을 플레이어 인벤토리에 추가
     */
    private void addTestItems() {
        player.getInventory().addItem(ItemFactory.createSword());
        player.getInventory().addItem(ItemFactory.createDagger());
        player.getInventory().addItem(ItemFactory.createLeatherArmor());
        player.getInventory().addItem(ItemFactory.createIronHelmet());
        player.getInventory().addItem(ItemFactory.createLeatherBoots());
        player.getInventory().addItem(ItemFactory.createRing());
        player.getInventory().addItem(ItemFactory.createNecklace());
        player.getInventory().addItem(ItemFactory.createHealthPotion());
        player.getInventory().addItem(ItemFactory.createHealthPotion());
        player.getInventory().addItem(ItemFactory.createGreaterHealthPotion());
    }
}