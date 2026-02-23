package com.rogue01.game;

/**
 * 게임 루프를 관리하는 클래스 - 정확한 FPS 제한과 성능 모니터링 제공
 */
public class GameLoop implements Runnable {
    private static final int TARGET_FPS = 60;
    private static final long TARGET_TIME = 1000000000 / TARGET_FPS;
    private static final int MAX_FRAME_SKIPS = 5;

    private Thread gameThread;
    private boolean running;
    private Game game;

    // 성능 모니터링을 위한 변수들
    private long frameCount = 0;
    private long lastFpsTime = 0;
    private int currentFps = 0;
    private long totalFrameTime = 0;
    private long minFrameTime = Long.MAX_VALUE;
    private long maxFrameTime = 0;

    public void start(Game game) {
        this.game = game;
        this.running = true;
        this.gameThread = new Thread(this, "GameLoop");
        this.gameThread.start();
    }

    public void stop() {
        this.running = false;
        try {
            if (gameThread != null && gameThread.isAlive()) {
                gameThread.join(1000); // 1초 대기
                if (gameThread.isAlive()) {
                    gameThread.interrupt(); // 강제 종료
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long unprocessedTime = 0;
        int frameSkips = 0;

        while (running) {
            long currentTime = System.nanoTime();
            long elapsedTime = currentTime - lastTime;
            lastTime = currentTime;

            unprocessedTime += elapsedTime;

            // 프레임 스킵 방지
            while (unprocessedTime >= TARGET_TIME && frameSkips < MAX_FRAME_SKIPS) {
                // 게임 업데이트
                game.update();
                unprocessedTime -= TARGET_TIME;
                frameSkips++;
            }

            // 화면 렌더링 (항상 실행)
            long renderStartTime = System.nanoTime();
            game.render();
            long renderTime = System.nanoTime() - renderStartTime;

            // 성능 통계 업데이트
            updatePerformanceStats(renderTime);

            // FPS 제한
            long sleepTime = TARGET_TIME - (System.nanoTime() - currentTime);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1000000, (int) (sleepTime % 1000000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            frameSkips = 0; // 프레임 스킵 카운터 리셋
        }
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
            frameCount = 0;
            lastFpsTime = currentTime;

            // 성능 정보 출력 (디버그용)
            if (currentFps < TARGET_FPS * 0.9) {
                System.out.printf("Performance warning: FPS = %d, Avg frame time = %.2fms%n",
                        currentFps, (double) totalFrameTime / 1000000);
            }
        }
    }

    /**
     * 현재 FPS 반환
     */
    public int getCurrentFps() {
        return currentFps;
    }

    /**
     * 평균 프레임 시간 반환 (밀리초)
     */
    public double getAverageFrameTime() {
        return frameCount > 0 ? (double) totalFrameTime / frameCount / 1000000 : 0.0;
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