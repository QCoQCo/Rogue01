package com.rogue01.util;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * 게임 전체 키 바인딩을 중앙에서 관리하는 클래스.
 * 액션별 키 매핑을 정의하고, 향후 설정 메뉴에서 변경 가능하도록 설계.
 */
public final class KeyBinding {
    private static final Map<KeyAction, int[]> BINDINGS = new HashMap<>();

    static {
        // 메뉴
        bind(KeyAction.START_GAME, KeyEvent.VK_ENTER);
        bind(KeyAction.DIFFICULTY_EASY, KeyEvent.VK_1);
        bind(KeyAction.DIFFICULTY_NORMAL, KeyEvent.VK_2);
        bind(KeyAction.DIFFICULTY_HARD, KeyEvent.VK_3);
        bind(KeyAction.QUIT, KeyEvent.VK_Q);

        // 공통
        bind(KeyAction.PAUSE, KeyEvent.VK_ESCAPE);
        bind(KeyAction.INVENTORY, KeyEvent.VK_I);
        bind(KeyAction.MAP_VIEW, KeyEvent.VK_M);
        bind(KeyAction.CLOSE, KeyEvent.VK_ESCAPE);

        // 플레이 (이동, 상호작용)
        bind(KeyAction.INTERACT, KeyEvent.VK_F);
        bind(KeyAction.TURN, KeyEvent.VK_SPACE);
        bind(KeyAction.MOVE_UP, KeyEvent.VK_W, KeyEvent.VK_UP);
        bind(KeyAction.MOVE_DOWN, KeyEvent.VK_S, KeyEvent.VK_DOWN);
        bind(KeyAction.MOVE_LEFT, KeyEvent.VK_A, KeyEvent.VK_LEFT);
        bind(KeyAction.MOVE_RIGHT, KeyEvent.VK_D, KeyEvent.VK_RIGHT);

        // 인벤토리 팝업
        bind(KeyAction.ITEM_EQUIP, KeyEvent.VK_F);
        bind(KeyAction.ITEM_DROP, KeyEvent.VK_T);
        bind(KeyAction.ITEM_CANCEL, KeyEvent.VK_Q);
        bind(KeyAction.ITEM_USE, KeyEvent.VK_U);

        // 인벤토리 슬롯 1~7
        bind(KeyAction.ITEM_SLOT_1, KeyEvent.VK_1);
        bind(KeyAction.ITEM_SLOT_2, KeyEvent.VK_2);
        bind(KeyAction.ITEM_SLOT_3, KeyEvent.VK_3);
        bind(KeyAction.ITEM_SLOT_4, KeyEvent.VK_4);
        bind(KeyAction.ITEM_SLOT_5, KeyEvent.VK_5);
        bind(KeyAction.ITEM_SLOT_6, KeyEvent.VK_6);
        bind(KeyAction.ITEM_SLOT_7, KeyEvent.VK_7);

        // 챕터 전환
        bind(KeyAction.CONTINUE, KeyEvent.VK_ENTER);

        // 게임 오버/클리어
        bind(KeyAction.RESTART, KeyEvent.VK_R);
        bind(KeyAction.MAIN_MENU, KeyEvent.VK_M);

        // 전투 메뉴
        bind(KeyAction.BATTLE_MENU_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_A);
        bind(KeyAction.BATTLE_MENU_RIGHT, KeyEvent.VK_RIGHT, KeyEvent.VK_D);
        bind(KeyAction.BATTLE_MENU_UP, KeyEvent.VK_UP, KeyEvent.VK_W);
        bind(KeyAction.BATTLE_MENU_DOWN, KeyEvent.VK_DOWN, KeyEvent.VK_S);
        bind(KeyAction.BATTLE_CONFIRM, KeyEvent.VK_ENTER, KeyEvent.VK_SPACE);
    }

    private static void bind(KeyAction action, int... keyCodes) {
        BINDINGS.put(action, keyCodes);
    }

    private KeyBinding() {
    }

    /**
     * 해당 액션에 바인딩된 키 중 하나라도 눌려 있으면 true
     */
    public static boolean isPressed(InputHandler handler, KeyAction action) {
        int[] keys = BINDINGS.get(action);
        if (keys == null)
            return false;
        for (int k : keys) {
            if (handler.isKeyPressed(k))
                return true;
        }
        return false;
    }

    /**
     * KeyEvent의 키코드가 해당 액션에 해당하면 true (BattleScreen 등 KeyEvent 직접 사용 시)
     */
    public static boolean matches(KeyEvent e, KeyAction action) {
        int[] keys = BINDINGS.get(action);
        if (keys == null)
            return false;
        int code = e.getKeyCode();
        for (int k : keys) {
            if (code == k)
                return true;
        }
        return false;
    }

    /**
     * 액션에 바인딩된 키 코드 배열 반환 (consumeKey 등에 사용)
     */
    public static int[] getKeyCodes(KeyAction action) {
        return BINDINGS.getOrDefault(action, new int[0]);
    }

    /**
     * 인벤토리 슬롯 인덱스(0~6)에 해당하는 액션
     */
    public static KeyAction getItemSlotAction(int slotIndex) {
        return switch (slotIndex) {
            case 0 -> KeyAction.ITEM_SLOT_1;
            case 1 -> KeyAction.ITEM_SLOT_2;
            case 2 -> KeyAction.ITEM_SLOT_3;
            case 3 -> KeyAction.ITEM_SLOT_4;
            case 4 -> KeyAction.ITEM_SLOT_5;
            case 5 -> KeyAction.ITEM_SLOT_6;
            case 6 -> KeyAction.ITEM_SLOT_7;
            default -> null;
        };
    }

    /**
     * 액션에 바인딩된 키들을 InputHandler에서 소비(제거)
     */
    public static void consumeKeys(InputHandler handler, KeyAction action) {
        for (int k : getKeyCodes(action)) {
            handler.consumeKey(k);
        }
    }

    /**
     * 액션의 첫 번째(기본) 키 코드 반환
     */
    public static int getPrimaryKey(KeyAction action) {
        int[] keys = getKeyCodes(action);
        return keys.length > 0 ? keys[0] : 0;
    }

    /**
     * 게임 내 키 액션 정의
     */
    public enum KeyAction {
        // 메뉴
        START_GAME,
        DIFFICULTY_EASY,
        DIFFICULTY_NORMAL,
        DIFFICULTY_HARD,
        QUIT,

        // 공통
        PAUSE,
        INVENTORY,
        MAP_VIEW,
        CLOSE,

        // 플레이
        INTERACT,
        TURN,
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,

        // 인벤토리
        ITEM_EQUIP,
        ITEM_DROP,
        ITEM_CANCEL,
        ITEM_USE,
        ITEM_SLOT_1,
        ITEM_SLOT_2,
        ITEM_SLOT_3,
        ITEM_SLOT_4,
        ITEM_SLOT_5,
        ITEM_SLOT_6,
        ITEM_SLOT_7,

        // 챕터 전환
        CONTINUE,

        // 게임 오버/클리어
        RESTART,
        MAIN_MENU,

        // 전투
        BATTLE_MENU_LEFT,
        BATTLE_MENU_RIGHT,
        BATTLE_MENU_UP,
        BATTLE_MENU_DOWN,
        BATTLE_CONFIRM,
    }
}
