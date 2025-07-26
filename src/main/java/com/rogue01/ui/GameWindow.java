package com.rogue01.ui;

import com.rogue01.game.Game;
import com.rogue01.util.InputHandler;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private GamePanel gamePanel;
    private InputHandler inputHandler;
    private Game currentGame; // 현재 게임 인스턴스 참조
    
    public GameWindow() {
        setTitle("Rogue01");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        this.inputHandler = new InputHandler();
        this.gamePanel = new GamePanel();
        
        add(gamePanel);
        addKeyListener(inputHandler);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    public void render(Game game) {
        this.currentGame = game; // 현재 게임 인스턴스 저장
        gamePanel.render(game);
        repaint();
    }
    
    public InputHandler getInputHandler() {
        return inputHandler;
    }
    
    public Game getGame() { return currentGame; }
    
    private class GamePanel extends JPanel {
        private static final int TILE_SIZE = 20;
        
        public GamePanel() {
            setPreferredSize(new Dimension(1000, 600));
            setBackground(Color.BLACK);
        }
        
        public void render(Game game) {
            // 게임 렌더링 로직이 여기에 구현됨
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // 게임 화면 그리기
            drawGame(g2d);
        }
        
        private void drawGame(Graphics2D g2d) {
            g2d.setColor(Color.WHITE);
            if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.MENU) {
                // 타이틀
                g2d.setFont(new Font("Monospaced", Font.BOLD, 48));
                g2d.setColor(new Color(80, 200, 120));
                g2d.drawString("Rogue01", 320, 150);
                // 게임 설명
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
                g2d.drawString("Move: WASD / Arrow Keys", 340, 220);
                g2d.drawString("Start: ENTER", 340, 245);
                g2d.drawString("Quit: Q", 340, 270);
                // 선택 커서(예시: ENTER에 강조)
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Monospaced", Font.BOLD, 24));
                g2d.drawString("> Press ENTER to Start <", 320, 320);
                // 제작자 정보
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
                g2d.setColor(Color.GRAY);
                g2d.drawString("Created by SCODA", 370, 380);
                g2d.setColor(Color.WHITE);
            } else if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.PLAYING) {
                // HUD 표시
                g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
                com.rogue01.entity.Player player = currentGame.getPlayer();
                
                // HUD 배경
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, 1000, 35);
                g2d.setColor(Color.WHITE);
                
                // 플레이어 정보
                g2d.drawString("HP: " + player.getHealth() + "/" + player.getMaxHealth(), 20, 25);
                g2d.drawString("Position: (" + player.getX() + ", " + player.getY() + ")", 200, 25);
                g2d.drawString("Player: " + player.getName(), 400, 25);
                
                // 게임 상태
                // g2d.setColor(Color.YELLOW);
                // g2d.drawString("Game State: PLAYING", 600, 25);
                
                // 조작법 (우측 상단)
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
                g2d.drawString("Controls:", 800, 15);
                g2d.drawString("WASD / Arrow Keys: Move", 800, 30);
                
                // 맵과 플레이어 렌더링
                g2d.setFont(new Font("Monospaced", Font.PLAIN, TILE_SIZE));
                com.rogue01.map.Map map = currentGame.getMap();
                
                // 카메라 시스템: 플레이어를 화면 중앙에 고정
                int screenWidth = 1000;
                int screenHeight = 600;
                int visibleTilesX = (screenWidth - 80) / TILE_SIZE; // 좌우 여백 제외
                int visibleTilesY = (screenHeight - 120) / TILE_SIZE; // 상하 여백 제외
                
                // 플레이어 위치를 기준으로 카메라 위치 계산
                int cameraX = player.getX() - visibleTilesX / 2;
                int cameraY = player.getY() - visibleTilesY / 2;
                
                // 카메라가 맵 경계를 벗어나지 않도록 제한
                cameraX = Math.max(0, Math.min(cameraX, map.getWidth() - visibleTilesX));
                cameraY = Math.max(0, Math.min(cameraY, map.getHeight() - visibleTilesY));
                
                int offsetX = 40, offsetY = 60; // HUD 아래로 맵 위치 조정
                
                // 화면에 보이는 맵 영역만 렌더링
                for (int y = 0; y < visibleTilesY && y + cameraY < map.getHeight(); y++) {
                    for (int x = 0; x < visibleTilesX && x + cameraX < map.getWidth(); x++) {
                        int mapX = x + cameraX;
                        int mapY = y + cameraY;
                        char symbol = map.getTileSymbol(mapX, mapY);
                        
                        // 플레이어 위치면 플레이어 심볼로 덮어씀
                        if (player.getX() == mapX && player.getY() == mapY) {
                            g2d.setColor(Color.GREEN);
                            g2d.drawString(String.valueOf(player.getSymbol()), offsetX + x * TILE_SIZE, offsetY + (y+1) * TILE_SIZE);
                            g2d.setColor(Color.WHITE);
                        } else {
                            g2d.drawString(String.valueOf(symbol), offsetX + x * TILE_SIZE, offsetY + (y+1) * TILE_SIZE);
                        }
                    }
                }
                
                // 하단 정보 패널
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 565, 1000, 35);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
                g2d.drawString("ESC: Pause | Q: Quit | R: Restart", 20, 585);
                g2d.drawString("Map Size: " + map.getWidth() + "x" + map.getHeight(), 400, 585);
                g2d.drawString("FPS: 60", 600, 585);//랜더링 속도 계산
            } else if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.PAUSED) {
                // 일시정지 메뉴
                // 반투명 오버레이
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, 1000, 600);
                
                // 일시정지 메뉴
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.BOLD, 36));
                g2d.drawString("GAME PAUSED", 350, 200);
                
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
                g2d.drawString("ESC: Resume Game", 380, 250);
                g2d.drawString("Q: Quit Game", 380, 280);
                
                // 현재 게임 상태 정보
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
                g2d.setColor(Color.LIGHT_GRAY);
                com.rogue01.entity.Player player = currentGame.getPlayer();
                g2d.drawString("Player HP: " + player.getHealth() + "/" + player.getMaxHealth(), 380, 320);
                g2d.drawString("Position: (" + player.getX() + ", " + player.getY() + ")", 380, 340);
            }
        }
    }
} 