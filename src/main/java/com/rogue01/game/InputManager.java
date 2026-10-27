package com.rogue01.game;

import com.rogue01.battle.BattleManager;
import com.rogue01.battle.BattleScreen;
import com.rogue01.util.InputHandler;
import com.rogue01.util.KeyBinding;
import com.rogue01.util.KeyBinding.KeyAction;
import com.rogue01.entity.Player;

/**
 * 게임 입력 처리를 담당하는 매니저 클래스 - 키 입력을 읽는 유일한 곳
 * 상태별로 키를 해석해 Game/GameWindow/BattleScreen의 행동 메서드를 호출
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
                handleBossDoorPromptInput();
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
                // 메뉴에서 고른 난이도로 맵 생성·적 스폰
                game.startNewGame();
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
        } else if (game != null) {
            handlePlayerActionInput();
        }
    }

    /**
     * 탐험 중 플레이어 행동 (상호작용 / 대기 / 이동) - 턴 판정은 Game이 담당
     */
    private void handlePlayerActionInput() {
        if (KeyBinding.isPressed(inputHandler, KeyAction.INTERACT)) {
            KeyBinding.consumeKeys(inputHandler, KeyAction.INTERACT);
            game.interact();
            return;
        }
        if (KeyBinding.isPressed(inputHandler, KeyAction.TURN)) {
            KeyBinding.consumeKeys(inputHandler, KeyAction.TURN);
            game.waitTurn();
            return;
        }

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
            // 한 번 누를 때 한 칸 (누르고 있으면 OS 키 반복으로 계속 이동)
            KeyBinding.consumeKeys(inputHandler, KeyAction.MOVE_UP);
            KeyBinding.consumeKeys(inputHandler, KeyAction.MOVE_DOWN);
            KeyBinding.consumeKeys(inputHandler, KeyAction.MOVE_LEFT);
            KeyBinding.consumeKeys(inputHandler, KeyAction.MOVE_RIGHT);
            game.movePlayer(dx, dy);
        }
    }

    /**
     * 보스방 문 프롬프트 입력 처리 (F=진입, ESC=취소)
     */
    private void handleBossDoorPromptInput() {
        if (game == null) {
            return;
        }
        if (KeyBinding.isPressed(inputHandler, KeyAction.INTERACT)) {
            KeyBinding.consumeKeys(inputHandler, KeyAction.INTERACT);
            game.enterBossDoor();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.CLOSE)) {
            KeyBinding.consumeKeys(inputHandler, KeyAction.CLOSE);
            game.cancelBossDoorPrompt();
        }
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
     * - 결과 화면: 아무 키(ESC 포함)로 계속
     * - 아이템 서브메뉴: ↑↓ 선택, ENTER/SPACE 사용, ESC 닫기 (일시정지 아님)
     * - 행동 메뉴: ←→(↑↓) 선택, ENTER/SPACE 결정, ESC 일시정지
     */
    private void handleBattleInput() {
        if (game == null || game.getBattleManager() == null) {
            return;
        }
        BattleManager battleManager = game.getBattleManager();

        if (battleManager.isBattleEnded()) {
            // 최소 표시 시간 전이면 키를 남겨둬서, 누르고 있는 키로 시간이 지나면 넘어가도록 함
            if (inputHandler.isAnyKeyPressed() && game.acknowledgeBattleResult()) {
                inputHandler.clearKeys();
            }
            return;
        }

        BattleScreen screen = game.getGameWindow().getBattleScreen(battleManager);
        if (screen.isItemSubMenuActive()) {
            if (KeyBinding.isPressed(inputHandler, KeyAction.CLOSE)) {
                KeyBinding.consumeKeys(inputHandler, KeyAction.CLOSE);
                screen.closeItemSubMenu();
            } else if (KeyBinding.isPressed(inputHandler, KeyAction.BATTLE_MENU_UP)) {
                KeyBinding.consumeKeys(inputHandler, KeyAction.BATTLE_MENU_UP);
                screen.moveConsumableSelection(-1);
            } else if (KeyBinding.isPressed(inputHandler, KeyAction.BATTLE_MENU_DOWN)) {
                KeyBinding.consumeKeys(inputHandler, KeyAction.BATTLE_MENU_DOWN);
                screen.moveConsumableSelection(1);
            } else if (KeyBinding.isPressed(inputHandler, KeyAction.BATTLE_CONFIRM)) {
                KeyBinding.consumeKeys(inputHandler, KeyAction.BATTLE_CONFIRM);
                screen.useSelectedConsumable();
            }
            return;
        }

        if (KeyBinding.isPressed(inputHandler, KeyAction.PAUSE)) {
            game.setGameState(GameState.PAUSED);
            inputHandler.clearKeys();
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.BATTLE_MENU_LEFT)
                || KeyBinding.isPressed(inputHandler, KeyAction.BATTLE_MENU_UP)) {
            KeyBinding.consumeKeys(inputHandler, KeyAction.BATTLE_MENU_LEFT);
            KeyBinding.consumeKeys(inputHandler, KeyAction.BATTLE_MENU_UP);
            screen.moveActionSelection(-1);
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.BATTLE_MENU_RIGHT)
                || KeyBinding.isPressed(inputHandler, KeyAction.BATTLE_MENU_DOWN)) {
            KeyBinding.consumeKeys(inputHandler, KeyAction.BATTLE_MENU_RIGHT);
            KeyBinding.consumeKeys(inputHandler, KeyAction.BATTLE_MENU_DOWN);
            screen.moveActionSelection(1);
        } else if (KeyBinding.isPressed(inputHandler, KeyAction.BATTLE_CONFIRM)) {
            KeyBinding.consumeKeys(inputHandler, KeyAction.BATTLE_CONFIRM);
            screen.confirmAction();
        }
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
            // 새 게임은 메뉴에서 시작할 때 startNewGame()으로 생성 (난이도 재선택 반영)
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
            // 새 게임은 메뉴에서 시작할 때 startNewGame()으로 생성 (난이도 재선택 반영)
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
