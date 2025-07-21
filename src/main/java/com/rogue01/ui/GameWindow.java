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
                g2d.setFont(new Font("Monospaced", Font.BOLD, 40));
                g2d.drawString("Rogue01", 350, 180);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 24));
                g2d.drawString("Press ENTER to Start", 340, 300);
                g2d.drawString("Q: Quit", 420, 340);
            } else {
                g2d.setFont(new Font("Monospaced", Font.PLAIN, TILE_SIZE));
                g2d.drawString("Game Running...", 50, 50);
            }
        }
    }
} 