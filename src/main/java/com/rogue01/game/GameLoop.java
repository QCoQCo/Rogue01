package com.rogue01.game;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;

/**
 * 게임 루프 - 매 프레임 update/render를 EDT(Event Dispatch Thread)에서 실행
 *
 * 키 입력·마우스 클릭·화면 그리기도 모두 EDT에서 실행되므로, 게임 상태를 한 스레드에서만
 * 읽고 쓰게 되어 별도 동기화 없이 경쟁 상태가 생기지 않음 (턴제라 프레임당 작업량이 작음)
 *
 * 프레임 간격은 고정 주기 스케줄러(pacer 스레드)가 맞추고, pacer는 EDT에 프레임 실행만 요청함
 * (javax.swing.Timer는 틱마다 지연이 누적되어 60fps 목표에서 52fps 정도로 떨어짐)
 */
public class GameLoop {
    private static final int TARGET_FPS = 60;
    private static final long FRAME_NANOS = 1_000_000_000L / TARGET_FPS;

    private ScheduledExecutorService pacer;
    /** EDT에 요청한 프레임이 아직 실행 전이면 true (EDT가 바쁠 때 프레임 요청이 쌓이지 않도록) */
    private final AtomicBoolean framePending = new AtomicBoolean();
    private volatile boolean running;
    private Game game;

    // 성능 모니터링 (1초 단위로 집계 후 초기화)
    private long frameCount = 0;
    private long lastFpsTime = 0;
    private int currentFps = 0;
    private long totalFrameTime = 0;
    private double lastAverageFrameTimeMs = 0.0;
    private long minFrameTime = Long.MAX_VALUE;
    private long maxFrameTime = 0;

    /**
     * 루프 시작 (EDT에서 호출해야 함)
     */
    public void start(Game game) {
        this.game = game;
        this.lastFpsTime = System.currentTimeMillis();
        this.running = true;
        this.pacer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "GameLoop-Pacer");
            thread.setDaemon(true);
            return thread;
        });
        pacer.scheduleAtFixedRate(this::requestFrame, 0, FRAME_NANOS, TimeUnit.NANOSECONDS);
    }

    public void stop() {
        running = false;
        if (pacer != null) {
            pacer.shutdownNow();
        }
    }

    /** pacer 스레드: 게임 상태에는 손대지 않고 EDT에 프레임 실행만 요청 */
    private void requestFrame() {
        if (running && framePending.compareAndSet(false, true)) {
            SwingUtilities.invokeLater(() -> {
                try {
                    if (running) {
                        tick();
                    }
                } finally {
                    framePending.set(false);
                }
            });
        }
    }

    /**
     * 한 프레임: 게임 갱신 후 다시 그리기 요청 (항상 EDT)
     */
    private void tick() {
        long frameStart = System.nanoTime();
        game.update();
        game.render();
        updatePerformanceStats(System.nanoTime() - frameStart);
    }

    /**
     * 성능 통계 업데이트
     */
    private void updatePerformanceStats(long frameTime) {
        frameCount++;
        totalFrameTime += frameTime;

        if (frameTime < minFrameTime)
            minFrameTime = frameTime;
        if (frameTime > maxFrameTime)
            maxFrameTime = frameTime;

        // FPS 계산 (1초마다)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFpsTime >= 1000) {
            currentFps = (int) frameCount;
            lastAverageFrameTimeMs = (double) totalFrameTime / frameCount / 1000000;
            frameCount = 0;
            totalFrameTime = 0;
            lastFpsTime = currentTime;

            // 성능 정보 출력 (디버그용)
            if (currentFps < TARGET_FPS * 0.9) {
                System.out.printf("Performance warning: FPS = %d, Avg frame time = %.2fms%n",
                        currentFps, lastAverageFrameTimeMs);
            }
        }
    }

    /**
     * 현재 FPS 반환 (직전 1초 기준)
     */
    public int getCurrentFps() {
        return currentFps;
    }

    /**
     * 평균 프레임 시간 반환 (밀리초, 직전 1초 기준)
     */
    public double getAverageFrameTime() {
        return lastAverageFrameTimeMs;
    }

    /**
     * 최소 프레임 시간 반환 (밀리초)
     */
    public double getMinFrameTime() {
        return minFrameTime == Long.MAX_VALUE ? 0.0 : (double) minFrameTime / 1000000;
    }

    /**
     * 최대 프레임 시간 반환 (밀리초)
     */
    public double getMaxFrameTime() {
        return (double) maxFrameTime / 1000000;
    }

    /**
     * 게임 루프가 실행 중인지 확인
     */
    public boolean isRunning() {
        return running;
    }
}
