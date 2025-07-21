package com.rogue01.ui;

import com.rogue01.game.Game;
import com.rogue01.util.InputHandler;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private GamePanel gamePanel;
    private InputHandler inputHandler;
    
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
        gamePanel.render(game);
        repaint();
    }
    
    public InputHandler getInputHandler() {
        return inputHandler;
    }
    
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
            // 맵과 엔티티 그리기
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, TILE_SIZE));
            
            // 여기에 실제 게임 렌더링 로직 구현
            g2d.drawString("Rogue01 Game", 50, 50);
        }
    }
} 