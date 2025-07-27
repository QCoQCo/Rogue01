package com.rogue01.game;

import com.rogue01.ui.GameWindow;
import com.rogue01.map.Map;
import com.rogue01.entity.Player;
import com.rogue01.item.ItemFactory;

public class Game {
    private GameState gameState;
    private GameLoop gameLoop;
    private GameWindow gameWindow;
    private Map map;
    private Player player;
    
    public Game() {
        this.gameState = GameState.MENU;
        this.gameLoop = new GameLoop();
        this.gameWindow = new GameWindow();
        this.map = new Map(750, 450);
        this.player = new Player(375, 225);
        
        // 플레이어에게 GameWindow의 InputHandler 설정
        this.player.setInputHandler(this.gameWindow.getInputHandler());
        
        // 테스트 아이템 추가
        addTestItems();
    }
    
    public void start() {
        gameWindow.setVisible(true);
        gameLoop.start(this);
    }
    
    public void update() {
        switch (gameState) {
            case MENU:
                // ENTER 키가 눌리면 게임 시작
                if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_ENTER)) {
                    setGameState(GameState.PLAYING);
                    gameWindow.getInputHandler().clearKeys();
                }
                else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_Q)) {
                    System.exit(0);
                }
                break;
            case PLAYING:
                // ESC 키가 눌리면 일시정지
                if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_ESCAPE)) {
                    setGameState(GameState.PAUSED);
                    gameWindow.getInputHandler().clearKeys();
                }
                // I 키가 눌리면 인벤토리 창
                else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_I)) {
                    setGameState(GameState.INVENTORY);
                    gameWindow.getInputHandler().clearKeys();
                }
                player.update(map);
                map.update();
                break;
            case PAUSED:
                // ESC 키가 눌리면 게임 재개
                if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_ESCAPE)) {
                    setGameState(GameState.PLAYING);
                    gameWindow.getInputHandler().clearKeys();
                }
                // Q 키가 눌리면 게임 종료
                else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_Q)) {
                    System.exit(0);
                }
                break;
            case INVENTORY:
                // I 키가 눌리면 게임으로 돌아가기
                if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_I)) {
                    setGameState(GameState.PLAYING);
                    gameWindow.getInputHandler().clearKeys();
                }
                // ESC 키가 눌리면 게임으로 돌아가기
                else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_ESCAPE)) {
                    setGameState(GameState.PLAYING);
                    gameWindow.getInputHandler().clearKeys();
                }
                // 숫자 키로 인벤토리 아이템 선택
                else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_1)) {
                    handleInventorySelection(0);
                    gameWindow.getInputHandler().clearKeys();
                }
                else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_2)) {
                    handleInventorySelection(1);
                    gameWindow.getInputHandler().clearKeys();
                }
                else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_3)) {
                    handleInventorySelection(2);
                    gameWindow.getInputHandler().clearKeys();
                }
                else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_4)) {
                    handleInventorySelection(3);
                    gameWindow.getInputHandler().clearKeys();
                }
                else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_5)) {
                    handleInventorySelection(4);
                    gameWindow.getInputHandler().clearKeys();
                }
                else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_6)) {
                    handleInventorySelection(5);
                    gameWindow.getInputHandler().clearKeys();
                }
                else if (gameWindow.getInputHandler().isKeyPressed(java.awt.event.KeyEvent.VK_7)) {
                    handleInventorySelection(6);
                    gameWindow.getInputHandler().clearKeys();
                }
                break;
            case GAME_OVER:
                // 게임 오버 상태
                break;
        }
    }
    
    public void render() {
        gameWindow.render(this);
    }
    
    // Getters
    public GameState getGameState() { return gameState; }
    public Map getMap() { return map; }
    public Player getPlayer() { return player; }
    public GameWindow getGameWindow() { return gameWindow; }
    
    // Setters
    public void setGameState(GameState gameState) { this.gameState = gameState; }
    
    private void addTestItems() {
        // 테스트 아이템들을 플레이어 인벤토리에 추가
        player.getInventory().addItem(ItemFactory.createSword());
        player.getInventory().addItem(ItemFactory.createDagger());
        player.getInventory().addItem(ItemFactory.createLeatherArmor());
        player.getInventory().addItem(ItemFactory.createIronHelmet());
        player.getInventory().addItem(ItemFactory.createLeatherBoots());
        player.getInventory().addItem(ItemFactory.createRing());
        player.getInventory().addItem(ItemFactory.createNecklace());
    }
    
    private void handleInventorySelection(int index) {
        if (index < player.getInventory().getSize()) {
            com.rogue01.item.Item item = player.getInventory().getItem(index);
            if (item != null && item instanceof com.rogue01.item.Equipment) {
                player.getInventory().equipItem(item);
            }
        }
    }
} 