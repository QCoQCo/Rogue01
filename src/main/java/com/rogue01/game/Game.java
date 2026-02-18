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
                startBattle(enemyAtTarget);
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
        updateEnemies();
        removeDeadEnemies();
        
        // 적이 플레이어 타일로 이동했으면 전투 시작
        checkEnemyCollision();
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
                    // 보상 처리
                    battleManager.processRewards();
                    // 아이템 드롭 (적 위치에)
                    dropItemFromEnemy(battleManager.getEnemy());
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
     * 적과의 충돌 체크 (전투 시작)
     */
    private void checkEnemyCollision() {
        if (gameState == GameState.BATTLE) {
            return; // 이미 전투 중이면 체크하지 않음
        }
        
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                continue;
            }
            
            // 플레이어와 적이 같은 위치에 있으면 전투 시작
            if (player.getX() == enemy.getX() && player.getY() == enemy.getY()) {
                startBattle(enemy);
                break;
            }
        }
    }
    
    /**
     * 적 처치 시 아이템 드롭
     */
    private void dropItemFromEnemy(Enemy enemy) {
        Item droppedItem = ItemFactory.createRandomDrop();
        if (droppedItem != null) {
            mapItems.add(new MapItem(enemy.getX(), enemy.getY(), droppedItem));
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
     */
    private void startBattle(Enemy enemy) {
        battleManager = new BattleManager(player, enemy);
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
        
        // 턴제: 플레이어 턴부터 시작
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

            // 플레이어 시작 위치에서 최소 10칸 이상 떨어진 방에만 스폰
            if (distance < 10) {
                continue;
            }

            // 방 크기에 따라 적 수 결정
            int enemyCount = Math.max(1, room.getArea() / 100);

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
        int enemyCount = (map.getWidth() * map.getHeight()) / 500; // 맵 크기에 비례

        for (int i = 0; i < enemyCount; i++) {
            int attempts = 0;
            while (attempts < 50) {
                int x = randomUtils.nextInt(1, map.getWidth() - 2);
                int y = randomUtils.nextInt(1, map.getHeight() - 2);

                // 플레이어 시작 위치와 너무 가까우면 제외
                int playerStartX = map.getGenerationInfo().getPlayerStartX();
                int playerStartY = map.getGenerationInfo().getPlayerStartY();
                int distance = (int) Math.sqrt(
                        Math.pow(x - playerStartX, 2) + Math.pow(y - playerStartY, 2));

                if (map.isWalkable(x, y) && distance >= 10 && getEnemyAt(x, y) == null) {
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
        if (rand < 0.5) {
            return EnemyType.GOBLIN;
        } else if (rand < 0.8) {
            return EnemyType.SKELETON;
        } else if (rand < 0.95) {
            return EnemyType.ORC;
        } else if (rand < 0.99) {
            return EnemyType.TROLL;
        } else {
            return EnemyType.DRAGON;
        }
    }

    /**
     * 적 업데이트
     */
    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            if (!enemy.isDead()) {
                enemy.update(map, player, enemies);
            }
        }
    }

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
    }
}