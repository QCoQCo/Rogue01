package com.rogue01.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

/**
 * 게임 루프 스레드 검사 - update/render가 키 입력·그리기와 같은 EDT에서만 실행되는지
 * (게임 상태를 한 스레드에서만 다루므로 동기화 없이 경쟁 상태가 생기지 않음)
 */
@DisabledIf("java.awt.GraphicsEnvironment#isHeadless")
class GameLoopThreadTest {

    /** update/render 호출 스레드를 기록하는 Game */
    static class RecordingGame extends Game {
        final AtomicInteger updates = new AtomicInteger();
        final AtomicInteger renders = new AtomicInteger();
        final AtomicInteger offEdtCalls = new AtomicInteger();

        @Override
        public void update() {
            record(updates);
            super.update();
        }

        @Override
        public void render() {
            record(renders);
            super.render();
        }

        private void record(AtomicInteger counter) {
            counter.incrementAndGet();
            if (!SwingUtilities.isEventDispatchThread()) {
                offEdtCalls.incrementAndGet();
            }
        }
    }

    @Test
    void updateAndRenderRunOnlyOnEventDispatchThread() throws Exception {
        AtomicReference<RecordingGame> ref = new AtomicReference<>();
        AtomicReference<GameLoop> loopRef = new AtomicReference<>();

        // 창은 띄우지 않고 루프만 시작 (Main과 동일하게 EDT에서 생성·시작)
        SwingUtilities.invokeAndWait(() -> {
            RecordingGame game = new RecordingGame();
            GameLoop loop = gameLoopOf(game);
            loop.start(game);
            ref.set(game);
            loopRef.set(loop);
        });
        RecordingGame game = ref.get();

        // 다른 스레드에서 키 이벤트를 넣는 상황 (AWT는 EDT로 전달) - 메뉴에서 게임 시작 후 이동
        SwingUtilities.invokeLater(() -> pressOnEdt(game, KeyEvent.VK_ENTER));
        Thread.sleep(300);
        for (int i = 0; i < 20; i++) {
            int key = i % 2 == 0 ? KeyEvent.VK_RIGHT : KeyEvent.VK_LEFT;
            SwingUtilities.invokeLater(() -> pressOnEdt(game, key));
            Thread.sleep(30);
        }
        Thread.sleep(1200);

        SwingUtilities.invokeAndWait(() -> loopRef.get().stop());
        int fps = loopRef.get().getCurrentFps();
        SwingUtilities.invokeAndWait(() -> game.getGameWindow().dispose());

        // 이동 중 적과 만나 전투가 될 수도 있으므로 "메뉴를 벗어났는지"만 확인
        assertNotEquals(GameState.MENU, game.getGameState(), "ENTER from another thread reached the game");
        assertEquals(0, game.offEdtCalls.get(), "update/render must never run off the EDT");
        assertTrue(game.updates.get() > 60, "loop should have run for ~2s, updates=" + game.updates.get());
        assertEquals(game.updates.get(), game.renders.get(), "each frame updates then renders once");
        assertTrue(fps >= 40, "frame rate should stay near 60fps, was " + fps);
    }

    private static void pressOnEdt(Game game, int keyCode) {
        var window = game.getGameWindow();
        long now = System.currentTimeMillis();
        window.getInputHandler().keyPressed(new KeyEvent(window, KeyEvent.KEY_PRESSED, now, 0, keyCode, KeyEvent.CHAR_UNDEFINED));
    }

    private static GameLoop gameLoopOf(Game game) {
        try {
            Field field = Game.class.getDeclaredField("gameLoop");
            field.setAccessible(true);
            return (GameLoop) field.get(game);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
