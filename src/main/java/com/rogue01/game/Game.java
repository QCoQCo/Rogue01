package com.rogue01.game;

import com.rogue01.ui.GameWindow;
import com.rogue01.map.Map;
import com.rogue01.entity.Player;

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
        this.map = new Map(50, 30);
        this.player = new Player(25, 15);
        
        // 플레이어에게 GameWindow의 InputHandler 설정
        this.player.setInputHandler(this.gameWindow.getInputHandler());
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
                player.update(map);
                map.update();
                break;
            case PAUSED:
                // 게임 일시정지 상태
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
} 