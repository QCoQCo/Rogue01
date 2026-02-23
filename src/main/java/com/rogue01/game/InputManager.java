package com.rogue01.game;

import com.rogue01.util.InputHandler;
import com.rogue01.util.KeyBinding;
import com.rogue01.util.KeyBinding.KeyAction;
import com.rogue01.entity.Player;

/**
 * 게임 입력 처리를 담당하는 매니저 클래스
 * 키 바인딩은 {@link KeyBinding}에서 중앙 관리
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
            case BOSS_DOOR_PROMPT:
                // Game에서 F/ESC 처리
                break;
            case GAME_OVER:
                handleGameOverInput();
                break;
            case CHAPTER_TRANSITION:
                handleChapterTransitionInput();
                break;
            case GAME_CLEAR:
                handleGameClearInput();
                break;
        }
    }

    /**
     * 메뉴 상태 입력 처리
     */
    private void handleMenuInput() {
        if (KeyBinding.isPressed(inputHandler, KeyAction.START_GAME)) {
            if (game != null) {
                game.setGameState(GameState.PLAYING);
            }
            inputHandler.clearKeys();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.DIFFICULTY_EASY) && game != null) {
            game.setDifficulty(com.rogue01.game.GameBalance.Difficulty.EASY);
            inputHandler.clearKeys();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.DIFFICULTY_NORMAL) && game != null) {
            game.setDifficulty(com.rogue01.game.GameBalance.Difficulty.NORMAL);
            inputHandler.clearKeys();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.DIFFICULTY_HARD) && game != null) {
            game.setDifficulty(com.rogue01.game.GameBalance.Difficulty.HARD);
            inputHandler.clearKeys();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.QUIT)) {
            System.exit(0);
        }
    }

    /**
     * 게임 진행 중 입력 처리
     */
    private void handlePlayingInput() {
        if (KeyBinding.isPressed(inputHandler, KeyAction.PAUSE)) {
            if (game != null) {
                game.setGameState(GameState.PAUSED);
            }
            inputHandler.clearKeys();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.INVENTORY)) {
            if (game != null) {
                game.setGameState(GameState.INVENTORY);
            }
            inputHandler.clearKeys();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.MAP_VIEW)) {
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
        if (KeyBinding.isPressed(inputHandler, KeyAction.PAUSE)) {
            if (game != null) {
                game.resumeFromPause();
            }
            inputHandler.clearKeys();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.QUIT)) {
            System.exit(0);
        }
    }

    /**
     * 인벤토리 상태 입력 처리
     */
    private void handleInventoryInput() {
        // 아이템 상세 팝업 단축키 (F=장착/사용, T=버리기, Q=취소)
        if (game != null && game.getGameWindow().isItemDetailPopupActive()) {
            int key = KeyBinding.isPressed(inputHandler, KeyAction.ITEM_CANCEL) ? KeyBinding.getPrimaryKey(KeyAction.ITEM_CANCEL)
                    : KeyBinding.isPressed(inputHandler, KeyAction.ITEM_DROP) ? KeyBinding.getPrimaryKey(KeyAction.ITEM_DROP)
                            : KeyBinding.isPressed(inputHandler, KeyAction.ITEM_EQUIP) ? KeyBinding.getPrimaryKey(KeyAction.ITEM_EQUIP)
                                    : 0;
            if (key != 0 && game.getGameWindow().handleItemPopupKey(key)) {
                inputHandler.clearKeys();
                return;
            }
        }

        if (KeyBinding.isPressed(inputHandler, KeyAction.INVENTORY) || KeyBinding.isPressed(inputHandler, KeyAction.CLOSE)) {
            if (game != null) {
                if (game.closeItemDetailPopupIfActive()) {
                    inputHandler.clearKeys();
                    return;
                }
                game.setGameState(GameState.PLAYING);
            }
            inputHandler.clearKeys();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.ITEM_USE)) {
            if (game != null) {
                int slot = game.getGameWindow().getSelectedInventorySlot();
                if (game.useConsumableAtSlot(slot)) {
                    KeyBinding.consumeKeys(inputHandler, KeyAction.ITEM_USE);
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
        if (KeyBinding.isPressed(inputHandler, KeyAction.MAP_VIEW) || KeyBinding.isPressed(inputHandler, KeyAction.CLOSE)) {
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
        if (KeyBinding.isPressed(inputHandler, KeyAction.PAUSE)) {
            if (game != null) {
                game.setGameState(GameState.PAUSED);
            }
            inputHandler.clearKeys();
        }
        // 전투 메뉴 선택 등은 GameWindow/BattleScreen에서 처리
    }

    /**
     * 챕터 전환 연출 상태 입력 처리 ("다음 챕터로 넘어가는중...")
     */
    private void handleChapterTransitionInput() {
        if (game != null && KeyBinding.isPressed(inputHandler, KeyAction.CONTINUE)) {
            game.finishChapterTransition();
            inputHandler.clearKeys();
        }
    }

    /**
     * 게임 클리어 상태 입력 처리
     */
    private void handleGameClearInput() {
        if (game == null)
            return;
        if (KeyBinding.isPressed(inputHandler, KeyAction.RESTART)) {
            game.restartGame();
            inputHandler.clearKeys();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.MAIN_MENU)) {
            game.restartGame();
            game.setGameState(GameState.MENU);
            inputHandler.clearKeys();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.QUIT)) {
            System.exit(0);
        }
    }

    /**
     * 게임 오버 상태 입력 처리
     */
    private void handleGameOverInput() {
        if (game == null)
            return;

        if (KeyBinding.isPressed(inputHandler, KeyAction.RESTART)) {
            game.restartGame();
            inputHandler.clearKeys();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.MAIN_MENU)) {
            game.restartGame();
            game.setGameState(GameState.MENU);
            inputHandler.clearKeys();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.QUIT)) {
            System.exit(0);
        }
    }

    /**
     * 인벤토리 아이템 선택 처리
     */
    private void handleInventorySelection() {
        for (int i = 0; i < 7; i++) {
            KeyAction slotAction = KeyBinding.getItemSlotAction(i);
            if (slotAction != null && KeyBinding.isPressed(inputHandler, slotAction)) {
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
