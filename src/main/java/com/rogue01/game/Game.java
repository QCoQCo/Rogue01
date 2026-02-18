package com.rogue01.game;

import com.rogue01.ui.GameWindow;
import com.rogue01.map.Map;
import com.rogue01.entity.Player;
import com.rogue01.entity.Enemy;
import com.rogue01.entity.EnemyType;
import com.rogue01.battle.BattleManager;
import com.rogue01.item.ItemFactory;
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
    private RandomUtils randomUtils;
    private BattleManager battleManager;

    public Game() {
        this.gameState = GameState.MENU;
        this.gameLoop = new GameLoop();
        this.gameWindow = new GameWindow();
        this.map = new Map(750, 450);
        this.player = new Player(375, 225);
        this.enemies = new ArrayList<>();
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
     * 게임 진행 중 상태 처리
     */
    private void handlePlayingState() {
        if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_ESCAPE)) {
            setGameState(GameState.PAUSED);
        } else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_I)) {
            setGameState(GameState.INVENTORY);
        } else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_M)) {
            setGameState(GameState.MAP_VIEW);
        }

        // 플레이어 업데이트 (맵과 함께)
        player.update(map);

        // 적과의 충돌 체크 (전투 시작)
        checkEnemyCollision();

        // 적 업데이트
        updateEnemies();

        // 사망한 적 제거
        removeDeadEnemies();
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
                    // 전투 중인 적 제거
                    enemies.remove(battleManager.getEnemy());
                } else if (result == BattleManager.BattleResult.DEFEAT) {
                    // 게임 오버
                    setGameState(GameState.GAME_OVER);
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
     * 전투 시작
     */
    private void startBattle(Enemy enemy) {
        battleManager = new BattleManager(player, enemy);
        setGameState(GameState.BATTLE);
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
        // 게임 오버 상태에서의 추가 로직
    }

    public void render() {
        gameWindow.render(this);
    }

    /**
     * 게임 상태 변경
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        gameWindow.getInputHandler().clearKeys();
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
                // 방 내부의 랜덤 위치 찾기
                int spawnX = randomUtils.nextInt(room.getX() + 1, room.getX() + room.getWidth() - 2);
                int spawnY = randomUtils.nextInt(room.getY() + 1, room.getY() + room.getHeight() - 2);

                // 이동 가능한 위치인지 확인
                if (map.isWalkable(spawnX, spawnY)) {
                    // 적 타입 랜덤 선택 (약한 적 위주)
                    EnemyType enemyType = getRandomEnemyType();
                    Enemy enemy = new Enemy(spawnX, spawnY, enemyType);
                    enemies.add(enemy);
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

                if (map.isWalkable(x, y) && distance >= 10) {
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
                enemy.update(map, player);
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
    
    public BattleManager getBattleManager() {
        return battleManager;
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