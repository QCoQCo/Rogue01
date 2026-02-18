package com.rogue01.game;

import com.rogue01.util.InputHandler;
import com.rogue01.entity.Player;
import java.awt.event.KeyEvent;

/**
 * 게임 입력 처리를 담당하는 매니저 클래스
 */
public class InputManager {
    private InputHandler inputHandler;
    private Player player;
    private Game game; // Game 인스턴스 참조 추가
    
    public InputManager(InputHandler inputHandler, Player player) {
        this.inputHandler = inputHandler;
        this.player = player;
    }
    
    /**
     * Game 인스턴스 설정
     */
    public void setGame(Game game) {
        this.game = game;
    }
    
    /**
     * 게임 상태에 따른 입력 처리
     */
    public void handleInput(GameState gameState) {
        switch (gameState) {
            case MENU:
                handleMenuInput();
                break;
            case PLAYING:
                handlePlayingInput();
                break;
            case PAUSED:
                handlePausedInput();
                break;
            case INVENTORY:
                handleInventoryInput();
                break;
            case MAP_VIEW:
                handleMapViewInput();
                break;
            case BATTLE:
                handleBattleInput();
                break;
            case GAME_OVER:
                handleGameOverInput();
                break;
        }
    }
    
    /**
     * 메뉴 상태 입력 처리
     */
    private void handleMenuInput() {
        if (inputHandler.isKeyPressed(KeyEvent.VK_ENTER)) {
            // 게임 시작 신호 - 게임 상태를 PLAYING으로 변경
            if (game != null) {
                game.setGameState(GameState.PLAYING);
            }
            inputHandler.clearKeys();
        } else if (inputHandler.isKeyPressed(KeyEvent.VK_Q)) {
            System.exit(0);
        }
    }
    
    /**
     * 게임 진행 중 입력 처리
     */
    private void handlePlayingInput() {
        if (inputHandler.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            // 일시정지 신호
            if (game != null) {
                game.setGameState(GameState.PAUSED);
            }
            inputHandler.clearKeys();
        } else if (inputHandler.isKeyPressed(KeyEvent.VK_I)) {
            // 인벤토리 창 신호
            if (game != null) {
                game.setGameState(GameState.INVENTORY);
            }
            inputHandler.clearKeys();
        } else if (inputHandler.isKeyPressed(KeyEvent.VK_M)) {
            // 맵 뷰 신호
            if (game != null) {
                game.setGameState(GameState.MAP_VIEW);
            }
            inputHandler.clearKeys();
        }
        
        // 플레이어 이동은 Game 클래스에서 처리하므로 여기서는 하지 않음
    }
    
    /**
     * 일시정지 상태 입력 처리
     */
    private void handlePausedInput() {
        if (inputHandler.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            // 게임 재개 신호
            if (game != null) {
                game.setGameState(GameState.PLAYING);
            }
            inputHandler.clearKeys();
        } else if (inputHandler.isKeyPressed(KeyEvent.VK_Q)) {
            System.exit(0);
        }
    }
    
    /**
     * 인벤토리 상태 입력 처리
     */
    private void handleInventoryInput() {
        if (inputHandler.isKeyPressed(KeyEvent.VK_I) || 
            inputHandler.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            // 게임으로 돌아가기 신호
            if (game != null) {
                game.setGameState(GameState.PLAYING);
            }
            inputHandler.clearKeys();
        } else {
            // 숫자 키로 인벤토리 아이템 선택
            handleInventorySelection();
        }
    }
    
    /**
     * 맵 뷰 상태 입력 처리
     */
    private void handleMapViewInput() {
        if (inputHandler.isKeyPressed(KeyEvent.VK_M) || 
            inputHandler.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            // 게임으로 돌아가기 신호
            if (game != null) {
                game.setGameState(GameState.PLAYING);
            }
            inputHandler.clearKeys();
        }
    }
    
    /**
     * 전투 상태 입력 처리
     */
    private void handleBattleInput() {
        // 전투 입력은 GameWindow에서 직접 처리
        // 여기서는 특별한 처리가 필요 없음
    }
    
    /**
     * 게임 오버 상태 입력 처리
     */
    private void handleGameOverInput() {
        // 게임 오버 상태에서의 입력 처리
        if (inputHandler.isKeyPressed(KeyEvent.VK_ENTER)) {
            // 게임 재시작 신호
            if (game != null) {
                game.setGameState(GameState.PLAYING);
            }
            inputHandler.clearKeys();
        } else if (inputHandler.isKeyPressed(KeyEvent.VK_Q)) {
            System.exit(0);
        }
    }
    
    /**
     * 인벤토리 아이템 선택 처리
     */
    private void handleInventorySelection() {
        for (int i = 0; i < 7; i++) {
            int keyCode = KeyEvent.VK_1 + i;
            if (inputHandler.isKeyPressed(keyCode)) {
                handleInventoryItemSelection(i);
                inputHandler.clearKeys();
                break;
            }
        }
    }
    
    /**
     * 특정 인덱스의 인벤토리 아이템 선택 처리
     */
    private void handleInventoryItemSelection(int index) {
        if (index < player.getInventory().getSize()) {
            var item = player.getInventory().getItem(index);
            if (item != null && item instanceof com.rogue01.item.Equipment) {
                player.getInventory().equipItem(item);
            }
        }
    }
    
    /**
     * 입력 핸들러 설정
     */
    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }
    
    /**
     * 플레이어 설정
     */
    public void setPlayer(Player player) {
        this.player = player;
    }
}
