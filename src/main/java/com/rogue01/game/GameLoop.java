package com.rogue01.game;

public class GameLoop implements Runnable {
    private static final int TARGET_FPS = 60;
    private static final long TARGET_TIME = 1000000000 / TARGET_FPS;
    
    private Thread gameThread;
    private boolean running;
    private Game game;
    
    public void start(Game game) {
        this.game = game;
        this.running = true;
        this.gameThread = new Thread(this);
        this.gameThread.start();
    }
    
    public void stop() {
        this.running = false;
        try {
            if (gameThread != null) {
                gameThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        while (running) {
            long startTime = System.nanoTime();
            
            // 게임 업데이트
            game.update();
            
            // 화면 렌더링
            game.render();
            
            // FPS 제한
            long endTime = System.nanoTime();
            long sleepTime = TARGET_TIME - (endTime - startTime);
            
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
} 