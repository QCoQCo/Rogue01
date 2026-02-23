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
import com.rogue01.util.KeyBinding;
import com.rogue01.util.KeyBinding.KeyAction;
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

    // 챕터/레벨 (3챕터 × 3레벨)
    private int currentChapter; // 1~3
    private int currentLevel; // 1~3
    private int midBossDefeatedCount; // 2층 전용: 중간보스 처치 수 (1 이상이면 계단 활성화)
    /** BOSS_DOOR_PROMPT 시 대기 중인 보스 타입: 1=중간보스, 2=챕터보스 */
    private int pendingBossDoorType;

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

        // 챕터/레벨 초기화 및 맵 재생성 (계단 배치)
        this.currentChapter = 1;
        this.currentLevel = 1;
        this.midBossDefeatedCount = 0;
        map.regenerate(Map.MapGeneratorType.HYBRID, 1, 1);
        player.setPosition(map.getGenerationInfo().getPlayerStartX(), map.getGenerationInfo().getPlayerStartY());

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
        PLAYER_TURN, // 플레이어 행동 대기
        ENEMY_TURN // 적 행동 처리
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
            case BOSS_DOOR_PROMPT:
                handleBossDoorPromptState();
                break;
            case GAME_OVER:
                handleGameOverState();
                break;
            case CHAPTER_TRANSITION:
            case GAME_CLEAR:
                // 입력만 처리, 별도 로직 없음
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
     * 메뉴 키(ESC/I/M)는 InputManager에서 먼저 처리됨
     */
    private void handlePlayingState() {
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
     * 
     * @return true if player acted (moved or waited), false otherwise
     */
    private boolean processPlayerTurn() {
        var inputHandler = gameWindow.getInputHandler();

        // 상호작용 (F키)
        if (KeyBinding.isPressed(inputHandler, KeyAction.INTERACT)) {
            KeyBinding.consumeKeys(inputHandler, KeyAction.INTERACT);
            int doorType = map.getAdjacentBossDoorType(player.getX(), player.getY());
            if (doorType > 0) {
                pendingBossDoorType = doorType;
                setGameState(GameState.BOSS_DOOR_PROMPT);
                return true;
            }
            if (tryUseStairs()) {
                return true;
            }
        }

        // 대기 (턴 넘기기)
        if (KeyBinding.isPressed(inputHandler, KeyAction.TURN)) {
            KeyBinding.consumeKeys(inputHandler, KeyAction.TURN);
            return true;
        }

        // 이동 처리
        int dx = 0, dy = 0;
        if (KeyBinding.isPressed(inputHandler, KeyAction.MOVE_UP)) {
            dy = -1;
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.MOVE_DOWN)) {
            dy = 1;
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.MOVE_LEFT)) {
            dx = -1;
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.MOVE_RIGHT)) {
            dx = 1;
        }

        if (dx != 0 || dy != 0) {
            int targetX = player.getX() + dx;
            int targetY = player.getY() + dy;

            KeyBinding.consumeKeys(inputHandler, KeyAction.MOVE_UP);
            KeyBinding.consumeKeys(inputHandler, KeyAction.MOVE_DOWN);
            KeyBinding.consumeKeys(inputHandler, KeyAction.MOVE_LEFT);
            KeyBinding.consumeKeys(inputHandler, KeyAction.MOVE_RIGHT);

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
     * 계단 사용 시도 (플레이어가 계단 위에 있을 때 F키)
     * 
     * @return true if acted (used stairs or tried)
     */
    private boolean tryUseStairs() {
        int px = player.getX();
        int py = player.getY();
        com.rogue01.map.Tile tile = map.getTile(px, py);
        if (tile == null || !tile.isStairsDown()) {
            return false; // 계단 위가 아님
        }

        // 2층: 중간보스 1마리 이상 처치해야 계단 활성화
        if (currentLevel == 2 && midBossDefeatedCount < 1) {
            return false; // 계단 봉인됨 (메시지는 UI에서 처리 가능)
        }

        // 1층→2층, 2층→3층 (3층에는 계단 없음, 보스 처치로 진행)
        currentLevel++;
        midBossDefeatedCount = 0;
        advanceToNextFloor();
        return true;
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
                    enemies.remove(battleManager.getEnemy());
                    killCount++;

                    Enemy defeatedEnemy = battleManager.getEnemy();

                    // 2층: 중간보스 처치 시에만 계단 봉인 해제
                    if (currentLevel == 2 && defeatedEnemy.getType().isMidBoss()) {
                        incrementMidBossDefeatedCount();
                    }

                    // 3층: 챕터 보스 처치 시 → 챕터 전환 또는 게임 클리어
                    if (currentLevel == 3 && defeatedEnemy.getType().isChapterBoss()) {
                        if (currentChapter == 3) {
                            setGameState(GameState.GAME_CLEAR);
                        } else {
                            setGameState(GameState.CHAPTER_TRANSITION);
                        }
                        battleManager = null;
                        return;
                    }
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
            if (enemy.isDead())
                continue;

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
        if (gameState == GameState.BATTLE)
            return;

        for (Enemy enemy : enemies) {
            if (enemy.isDead())
                continue;
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
     * 
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
            int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
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
        if (dx != 0)
            dx = dx > 0 ? 1 : -1;
        if (dy != 0)
            dy = dy > 0 ? 1 : -1;
        int escapeX = player.getX() + dx;
        int escapeY = player.getY() + dy;
        if (map.isWalkable(escapeX, escapeY) && getEnemyAt(escapeX, escapeY) == null) {
            player.setPosition(escapeX, escapeY);
        }
    }

    /**
     * 보스방 문 프롬프트: F=진입(전투 시작), ESC=취소
     */
    private void handleBossDoorPromptState() {
        if (KeyBinding.isPressed(gameWindow.getInputHandler(), KeyAction.INTERACT)) {
            KeyBinding.consumeKeys(gameWindow.getInputHandler(), KeyAction.INTERACT);
            Enemy boss = createBossFromDoorType(pendingBossDoorType);
            if (boss != null) {
                int dropX = player.getX();
                int dropY = player.getY();
                startBattle(boss, dropX, dropY);
            }
        } else if (KeyBinding.isPressed(gameWindow.getInputHandler(), KeyAction.PAUSE)
                || KeyBinding.isPressed(gameWindow.getInputHandler(), KeyAction.CLOSE)) {
            KeyBinding.consumeKeys(gameWindow.getInputHandler(), KeyAction.PAUSE);
            KeyBinding.consumeKeys(gameWindow.getInputHandler(), KeyAction.CLOSE);
            setGameState(GameState.PLAYING);
        }
    }

    /**
     * 보스 문 타입에 따른 보스 생성 (맵에 스폰하지 않음, 전투용)
     */
    private Enemy createBossFromDoorType(int doorType) {
        EnemyType type = doorType == 2 ? EnemyType.DRAGON : EnemyType.TROLL;
        double scale = GameBalance.getEnemyStatScale(currentChapter, currentLevel);
        return new Enemy(0, 0, type, scale);
    }

    /**
     * 일시정지 상태 처리 (InputManager에서 ESC 처리, 여기서는 추가 로직만)
     */
    private void handlePausedState() {
        if (KeyBinding.isPressed(gameWindow.getInputHandler(), KeyAction.PAUSE)) {
            resumeFromPause();
        }
    }

    /**
     * 인벤토리 상태 처리 (InputManager에서 I/ESC 처리)
     */
    private void handleInventoryState() {
        if (KeyBinding.isPressed(gameWindow.getInputHandler(), KeyAction.INVENTORY)
                || KeyBinding.isPressed(gameWindow.getInputHandler(), KeyAction.CLOSE)) {
            setGameState(GameState.PLAYING);
        }
    }

    /**
     * 맵 뷰 상태 처리 (InputManager에서 M/ESC 처리)
     */
    private void handleMapViewState() {
        if (KeyBinding.isPressed(gameWindow.getInputHandler(), KeyAction.MAP_VIEW)
                || KeyBinding.isPressed(gameWindow.getInputHandler(), KeyAction.CLOSE)) {
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
        // 맵 재생성 (챕터 1-1로 초기화)
        map.regenerate(Map.MapGeneratorType.HYBRID, 1, 1);

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

        // 챕터/레벨 초기화
        currentChapter = 1;
        currentLevel = 1;
        midBossDefeatedCount = 0;

        setGameState(GameState.PLAYING);
    }

    /**
     * 챕터 전환 연출 완료 후 다음 챕터 1층으로 이동
     */
    public void finishChapterTransition() {
        currentChapter++;
        currentLevel = 1;
        midBossDefeatedCount = 0;
        advanceToNextFloor();
    }

    /**
     * 다음 층으로 이동 (맵 재생성, 플레이어 유지)
     */
    public void advanceToNextFloor() {
        map.regenerate(Map.MapGeneratorType.HYBRID, currentChapter, currentLevel);

        int startX = map.getGenerationInfo().getPlayerStartX();
        int startY = map.getGenerationInfo().getPlayerStartY();
        player.setPosition(startX, startY);

        enemies.clear();
        mapItems.clear();
        spawnEnemies();

        battleManager = null;
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
            int enemyCount = Math.max(1, (int) (baseCount * GameBalance.getSpawnDensityMultiplier(difficulty)));

            for (int i = 0; i < enemyCount; i++) {
                for (int attempt = 0; attempt < 10; attempt++) {
                    int spawnX = randomUtils.nextInt(room.getX() + 1, room.getX() + room.getWidth() - 2);
                    int spawnY = randomUtils.nextInt(room.getY() + 1, room.getY() + room.getHeight() - 2);
                    if (map.isWalkable(spawnX, spawnY) && getEnemyAt(spawnX, spawnY) == null) {
                        EnemyType enemyType = GameBalance.rollEnemyType(
                                randomUtils.getRandom(), currentChapter, currentLevel);
                        double scale = GameBalance.getEnemyStatScale(currentChapter, currentLevel);
                        enemies.add(new Enemy(spawnX, spawnY, enemyType, scale));
                        break;
                    }
                }
            }
        }

        // 보스는 보스방 문 진입 시에만 스폰 (맵에 직접 스폰하지 않음)
        System.out.println("Spawned " + enemies.size() + " enemies");
    }

    /**
     * 랜덤 위치에 적 스폰 (방이 없을 때)
     */
    private void spawnEnemiesRandomly() {
        int baseCount = (map.getWidth() * map.getHeight()) / GameBalance.RANDOM_SPAWN_DIVISOR;
        int enemyCount = Math.max(1, (int) (baseCount * GameBalance.getSpawnDensityMultiplier(difficulty)));

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
                    EnemyType enemyType = GameBalance.rollEnemyType(
                            randomUtils.getRandom(), currentChapter, currentLevel);
                    double scale = GameBalance.getEnemyStatScale(currentChapter, currentLevel);
                    enemies.add(new Enemy(x, y, enemyType, scale));
                    break;
                }
                attempts++;
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

    public GameBalance.Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(GameBalance.Difficulty difficulty) {
        this.difficulty = difficulty != null ? difficulty : GameBalance.Difficulty.NORMAL;
    }

    public int getCurrentChapter() {
        return currentChapter;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getMidBossDefeatedCount() {
        return midBossDefeatedCount;
    }

    /** 중간보스 처치 시 호출 (2층 계단 봉인 해제) */
    public void incrementMidBossDefeatedCount() {
        midBossDefeatedCount++;
        map.breakStairsSeal();
    }

    /**
     * 인벤토리에서 소비 아이템 사용 (필드)
     * 
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