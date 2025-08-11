package com.rogue01.game;

import com.rogue01.ui.GameWindow;
import com.rogue01.map.Map;
import com.rogue01.entity.Player;
import com.rogue01.item.ItemFactory;

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
    
    public Game() {
        this.gameState = GameState.MENU;
        this.gameLoop = new GameLoop();
        this.gameWindow = new GameWindow();
        this.map = new Map(750, 450);
        this.player = new Player(375, 225);
        
        // 플레이어에게 GameWindow의 InputHandler 설정
        this.player.setInputHandler(this.gameWindow.getInputHandler());
        
        // InputManager 초기화
        this.inputManager = new InputManager(this.gameWindow.getInputHandler(), this.player);
        
        // 테스트 아이템 추가
        addTestItems();
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
        if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_ENTER)) {
            setGameState(GameState.PLAYING);
        }
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
    
    // Getters
    public GameState getGameState() { return gameState; }
    public Map getMap() { return map; }
    public Player getPlayer() { return player; }
    public GameWindow getGameWindow() { return gameWindow; }
    public InputManager getInputManager() { return inputManager; }
    
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