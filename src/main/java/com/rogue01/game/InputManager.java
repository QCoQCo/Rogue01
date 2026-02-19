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
            // 게임 재개 (이전 상태로: PLAYING 또는 BATTLE)
            if (game != null) {
                game.resumeFromPause();
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
            if (game != null) {
                if (game.closeItemDetailPopupIfActive()) {
                    inputHandler.clearKeys();
                    return;
                }
                game.setGameState(GameState.PLAYING);
            }
            inputHandler.clearKeys();
        } else if (inputHandler.isKeyPressed(KeyEvent.VK_U)) {
            // U: 선택한 소비 아이템 사용
            if (game != null) {
                int slot = game.getGameWindow().getSelectedInventorySlot();
                if (game.useConsumableAtSlot(slot)) {
                    inputHandler.consumeKey(KeyEvent.VK_U);
                }
            }
            inputHandler.clearKeys();
        } else {
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
        if (inputHandler.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            // 일시정지 화면으로
            if (game != null) {
                game.setGameState(GameState.PAUSED);
            }
            inputHandler.clearKeys();
        }
        // 전투 메뉴 선택 등은 GameWindow/BattleScreen에서 처리
    }

    /**
     * 게임 오버 상태 입력 처리
     */
    private void handleGameOverInput() {
        if (game == null) return;
        
        if (inputHandler.isKeyPressed(KeyEvent.VK_R)) {
            // 재시작
            game.restartGame();
            inputHandler.clearKeys();
        } else if (inputHandler.isKeyPressed(KeyEvent.VK_M)) {
            // 메인 메뉴로 (게임 초기화 후)
            game.restartGame();
            game.setGameState(GameState.MENU);
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
     * 특정 인덱스의 인벤토리 아이템 선택 처리 (팝업 표시)
     */
    private void handleInventoryItemSelection(int index) {
        if (game != null && index < player.getInventory().getSize()) {
            var item = player.getInventory().getItem(index);
            if (item != null) {
                game.getGameWindow().openItemDetailPopupForSlot(index);
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
