package com.rogue01.ui;

import com.rogue01.game.Game;
import com.rogue01.util.InputHandler;
import com.rogue01.battle.BattleScreen;
import com.rogue01.battle.BattleManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class GameWindow extends JFrame {
    private GamePanel gamePanel;
    private InputHandler inputHandler;
    private Game currentGame; // 현재 게임 인스턴스 참조
    private int selectedInventorySlot = -1; // 선택된 인벤토리 슬롯
    private int selectedEquipmentSlot = -1; // 선택된 장비 슬롯
    private BattleScreen battleScreen; // 전투 화면
    
    public GameWindow() {
        setTitle("Rogue01");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        
        this.inputHandler = new InputHandler();
        this.gamePanel = new GamePanel();
        
        add(gamePanel);
        addKeyListener(inputHandler);
        // 전투 화면 키 입력 처리
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.BATTLE) {
                    handleBattleInput(e);
                }
            }
        });
        // GamePanel에 직접 마우스 리스너 추가
        gamePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
        
        pack();
        setLocationRelativeTo(null);
    }
    
    public void render(Game game) {
        this.currentGame = game; // 현재 게임 인스턴스 저장
        
        // 전투 종료 시 battleScreen 초기화
        if (game.getGameState() != com.rogue01.game.GameState.BATTLE) {
            battleScreen = null;
        }
        
        gamePanel.render(game);
        repaint();
    }
    
    public InputHandler getInputHandler() {
        return inputHandler;
    }
    
    public Game getGame() { return currentGame; }
    
    private void handleMouseClick(int x, int y) {
        if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.INVENTORY) {
            // 장비 슬롯 클릭 처리
            handleEquipmentSlotClick(x, y);
            // 인벤토리 슬롯 클릭 처리
            handleInventorySlotClick(x, y);
        }
    }
    
    private void handleEquipmentSlotClick(int x, int y) {
        int startX = 260;
        int startY = 75;
        int slotSize = 56;
        int spacing = 62;
        
        for (int i = 0; i < 7; i++) {
            int slotX = startX + (i * spacing);
            int slotY = startY;
            
            if (x >= slotX && x <= slotX + slotSize && y >= slotY && y <= slotY + slotSize) {
                selectedEquipmentSlot = i;
                selectedInventorySlot = -1;
                
                // 선택된 장비 해제
                if (currentGame != null) {
                    com.rogue01.entity.Player player = currentGame.getPlayer();
                    com.rogue01.item.Inventory inventory = player.getInventory();
                    
                    com.rogue01.item.ItemType[] slotTypes = {
                        com.rogue01.item.ItemType.HELMET,
                        com.rogue01.item.ItemType.ARMOR,
                        com.rogue01.item.ItemType.BOOTS,
                        com.rogue01.item.ItemType.RING,
                        com.rogue01.item.ItemType.NECKLACE,
                        com.rogue01.item.ItemType.WEAPON_OFF,
                        com.rogue01.item.ItemType.WEAPON_MAIN
                    };
                    
                    if (i < slotTypes.length) {
                        inventory.unequipItem(slotTypes[i]);
                    }
                }
                break;
            }
        }
    }
    
    private void handleInventorySlotClick(int x, int y) {
        int startX = 260;
        int startY = 165;
        int itemsPerRow = 12;
        int slotSize = 38;
        int spacing = 42;
        
        for (int i = 0; i < 50; i++) {
            int row = i / itemsPerRow;
            int col = i % itemsPerRow;
            int slotX = startX + (col * spacing);
            int slotY = startY + (row * spacing);
            
            if (x >= slotX && x <= slotX + slotSize && y >= slotY && y <= slotY + slotSize) {
                selectedInventorySlot = i;
                selectedEquipmentSlot = -1;
                
                // 아이템 착용 시도
                if (currentGame != null) {
                    com.rogue01.entity.Player player = currentGame.getPlayer();
                    com.rogue01.item.Inventory inventory = player.getInventory();
                    com.rogue01.item.Item item = inventory.getItem(i);
                    
                    if (item != null && item instanceof com.rogue01.item.Equipment) {
                        inventory.equipItem(item);
                    }
                }
                break;
            }
        }
    }
    
    private class GamePanel extends JPanel {
        private static final int TILE_SIZE = 20;
        
        public GamePanel() {
            setPreferredSize(new Dimension(1400, 900));
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
                g2d.drawString("Rogue01", getWidth()/2 - 120, getHeight()/2 - 100);
                // 게임 설명
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
                g2d.drawString("Move: WASD / Arrow Keys", getWidth()/2 - 100, getHeight()/2 - 30);
                g2d.drawString("Start: ENTER", getWidth()/2 - 100, getHeight()/2 - 5);
                g2d.drawString("Quit: Q", getWidth()/2 - 100, getHeight()/2 + 20);
                // 선택 커서(예시: ENTER에 강조)
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Monospaced", Font.BOLD, 24));
                g2d.drawString("> Press ENTER to Start <", getWidth()/2 - 150, getHeight()/2 + 70);
                // 제작자 정보
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
                g2d.setColor(Color.GRAY);
                g2d.drawString("Created by SCODA", getWidth()/2 - 80, getHeight()/2 + 130);
                g2d.setColor(Color.WHITE);
            } else if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.PLAYING) {
                // HUD 표시
                g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
                com.rogue01.entity.Player player = currentGame.getPlayer();
                
                // HUD 배경
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, getWidth(), 35);
                g2d.setColor(Color.WHITE);
                
                // 플레이어 정보
                g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
                g2d.drawString("Lv." + player.getLevel(), 20, 22);
                g2d.drawString("HP: " + player.getHealth() + "/" + player.getMaxHealth(), 60, 22);
                g2d.drawString("ATK:" + player.getAttack() + " DEF:" + player.getDefense(), 220, 22);
                int expNext = player.getExpToNextLevel();
                g2d.setColor(Color.CYAN);
                g2d.drawString("EXP: " + player.getExperience() + "/" + expNext, 350, 22);
                g2d.setColor(Color.WHITE);
                
                // 게임 상태
                // g2d.setColor(Color.YELLOW);
                // g2d.drawString("Game State: PLAYING", 600, 25);
                
                // 조작법 (우측 상단)
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
                g2d.drawString("Controls:", getWidth() - 200, 15);
                g2d.drawString("WASD / Arrow Keys: Move", getWidth() - 200, 30);
                g2d.drawString("I: Inventory | M: Map", getWidth() - 200, 45);
                
                // 맵과 플레이어 렌더링
                g2d.setFont(new Font("Monospaced", Font.PLAIN, TILE_SIZE));
                com.rogue01.map.Map map = currentGame.getMap();
                
                // 카메라 시스템: 플레이어를 화면 중앙에 고정
                int screenWidth = getWidth();
                int screenHeight = getHeight();
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
                        
                        // 타일 타입에 따른 색상 설정
                        if (symbol == '#') {
                            g2d.setColor(Color.GRAY); // 벽
                        } else if (symbol == '.') {
                            g2d.setColor(Color.DARK_GRAY); // 바닥
                        } else {
                            g2d.setColor(Color.WHITE); // 기타
                        }
                        
                        // 타일 그리기
                        g2d.drawString(String.valueOf(symbol), offsetX + x * TILE_SIZE, offsetY + (y+1) * TILE_SIZE);
                    }
                }
                
                // 맵 아이템 렌더링
                g2d.setFont(new Font("Monospaced", Font.PLAIN, TILE_SIZE));
                for (com.rogue01.item.MapItem mapItem : currentGame.getMapItems()) {
                    int itemX = mapItem.getX();
                    int itemY = mapItem.getY();
                    
                    if (itemX >= cameraX && itemX < cameraX + visibleTilesX &&
                        itemY >= cameraY && itemY < cameraY + visibleTilesY) {
                        
                        int screenX = offsetX + (itemX - cameraX) * TILE_SIZE;
                        int screenY = offsetY + (itemY - cameraY + 1) * TILE_SIZE;
                        
                        g2d.setColor(Color.YELLOW);
                        g2d.drawString(String.valueOf(mapItem.getItem().getSymbol()), screenX, screenY);
                    }
                }
                
                // 적 렌더링
                g2d.setFont(new Font("Monospaced", Font.PLAIN, TILE_SIZE));
                for (com.rogue01.entity.Enemy enemy : currentGame.getEnemies()) {
                    if (enemy.isDead()) continue;
                    
                    int enemyX = enemy.getX();
                    int enemyY = enemy.getY();
                    
                    // 화면에 보이는 범위 내에 있는지 확인
                    if (enemyX >= cameraX && enemyX < cameraX + visibleTilesX &&
                        enemyY >= cameraY && enemyY < cameraY + visibleTilesY) {
                        
                        int screenX = offsetX + (enemyX - cameraX) * TILE_SIZE;
                        int screenY = offsetY + (enemyY - cameraY + 1) * TILE_SIZE;
                        
                        // 적 색상 설정
                        g2d.setColor(Color.RED);
                        g2d.drawString(String.valueOf(enemy.getSymbol()), screenX, screenY);
                    }
                }
                
                // 플레이어 렌더링 (적 위에 그려서 항상 보이도록)
                if (player.getX() >= cameraX && player.getX() < cameraX + visibleTilesX &&
                    player.getY() >= cameraY && player.getY() < cameraY + visibleTilesY) {
                    int playerScreenX = offsetX + (player.getX() - cameraX) * TILE_SIZE;
                    int playerScreenY = offsetY + (player.getY() - cameraY + 1) * TILE_SIZE;
                    g2d.setColor(Color.GREEN);
                    g2d.drawString(String.valueOf(player.getSymbol()), playerScreenX, playerScreenY);
                }
                
                // 적 정보 표시 (HUD에)
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
                g2d.drawString("Enemies: " + currentGame.getEnemies().size(), 600, 25);
                
                // 하단 정보 패널
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, getHeight() - 35, getWidth(), 35);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
                g2d.drawString("ESC: Pause | Q: Quit | R: Restart", 20, getHeight() - 15);
                g2d.drawString("Map Size: " + map.getWidth() + "x" + map.getHeight(), 400, getHeight() - 15);
                g2d.drawString("FPS: 60", 600, getHeight() - 15);//랜더링 속도 계산
            } else if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.PAUSED) {
                // 일시정지 메뉴
                // 반투명 오버레이
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // 일시정지 메뉴
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.BOLD, 36));
                g2d.drawString("GAME PAUSED", getWidth()/2 - 120, getHeight()/2 - 50);
                
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
                g2d.drawString("ESC: Resume Game", getWidth()/2 - 100, getHeight()/2);
                g2d.drawString("Q: Quit Game", getWidth()/2 - 100, getHeight()/2 + 30);
                
                // 현재 게임 상태 정보
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
                g2d.setColor(Color.LIGHT_GRAY);
                com.rogue01.entity.Player player = currentGame.getPlayer();
                g2d.drawString("Player HP: " + player.getHealth() + "/" + player.getMaxHealth(), getWidth()/2 - 100, getHeight()/2 + 70);
                g2d.drawString("Position: (" + player.getX() + ", " + player.getY() + ")", getWidth()/2 - 100, getHeight()/2 + 90);
            } else if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.INVENTORY) {
                // 인벤토리 창
                drawInventory(g2d);
            } else if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.MAP_VIEW) {
                // 맵 뷰
                drawMapView(g2d);
            } else if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.BATTLE) {
                // 전투 화면
                drawBattleScreen(g2d);
            } else if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.GAME_OVER) {
                // 게임 오버 화면
                drawGameOverScreen(g2d);
            }
        }
        
        private void drawInventory(Graphics2D g2d) {
            // 배경
            g2d.setColor(new Color(30, 30, 40));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            com.rogue01.entity.Player player = currentGame.getPlayer();
            
            // 좌측 스탯 패널
            drawInventoryStatsPanel(g2d, player);
            
            // 중앙: 제목 + 장비 슬롯
            g2d.setColor(new Color(45, 45, 55));
            g2d.fillRect(240, 0, getWidth() - 240, getHeight());
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
            g2d.drawString("인벤토리", 260, 45);
            
            drawEquipmentSlots(g2d);
            drawInventoryItems(g2d);
            drawInventoryItemDetail(g2d, player);
            
            // 조작법
            g2d.setColor(new Color(180, 180, 180));
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g2d.drawString("I/ESC: 닫기 | 클릭: 장비 착용/해제 | 1-7: 빠른 선택", getWidth()/2 - 150, getHeight() - 15);
        }
        
        private void drawInventoryStatsPanel(Graphics2D g2d, com.rogue01.entity.Player player) {
            int panelX = 15;
            int panelY = 50;
            int panelW = 210;
            int panelH = 220;
            
            // 패널 배경
            g2d.setColor(new Color(50, 55, 70));
            g2d.fillRoundRect(panelX, panelY, panelW, panelH, 8, 8);
            g2d.setColor(new Color(80, 90, 110));
            g2d.drawRoundRect(panelX, panelY, panelW, panelH, 8, 8);
            
            g2d.setColor(new Color(200, 210, 230));
            g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
            g2d.drawString("플레이어 스탯", panelX + 15, panelY + 25);
            
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Lv." + player.getLevel(), panelX + 15, panelY + 50);
            
            // HP 바
            g2d.setColor(new Color(80, 80, 90));
            g2d.fillRect(panelX + 15, panelY + 58, 180, 14);
            g2d.setColor(new Color(180, 60, 60));
            g2d.fillRect(panelX + 15, panelY + 58, (int)(180 * player.getHealthRatio()), 14);
            g2d.setColor(Color.WHITE);
            g2d.drawString("HP " + player.getHealth() + "/" + player.getMaxHealth(), panelX + 15, panelY + 85);
            
            // EXP 바
            double expRatio = (double) player.getExperience() / player.getExpToNextLevel();
            g2d.setColor(new Color(60, 60, 80));
            g2d.fillRect(panelX + 15, panelY + 95, 180, 10);
            g2d.setColor(new Color(70, 130, 180));
            g2d.fillRect(panelX + 15, panelY + 95, (int)(180 * Math.min(1, expRatio)), 10);
            g2d.setColor(new Color(180, 200, 220));
            g2d.drawString("EXP " + player.getExperience() + "/" + player.getExpToNextLevel(), panelX + 15, panelY + 120);
            
            // 공격력/방어력
            g2d.setColor(new Color(220, 180, 100));
            g2d.drawString("공격력: " + player.getAttack(), panelX + 15, panelY + 150);
            g2d.setColor(new Color(100, 180, 220));
            g2d.drawString("방어력: " + player.getDefense(), panelX + 15, panelY + 170);
            
            // 기본 스탯 (장비 제외)
            g2d.setColor(new Color(140, 140, 160));
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g2d.drawString("(기본 ATK:" + player.getBaseAttack() + " DEF:" + player.getBaseDefense() + ")", panelX + 15, panelY + 195);
        }
        
        private void drawEquipmentSlots(Graphics2D g2d) {
            int startX = 260;
            int startY = 75;
            int slotSize = 56;
            int spacing = 62;
            
            com.rogue01.entity.Player player = currentGame.getPlayer();
            com.rogue01.item.Inventory inventory = player.getInventory();
            
            g2d.setColor(new Color(180, 185, 200));
            g2d.setFont(new Font("Monospaced", Font.BOLD, 13));
            g2d.drawString("장비 슬롯", startX, startY - 25);
            
            String[] slotNames = {"투구", "갑옷", "신발", "반지", "목걸이", "보조", "주무기"};
            com.rogue01.item.ItemType[] slotTypes = {
                com.rogue01.item.ItemType.HELMET,
                com.rogue01.item.ItemType.ARMOR,
                com.rogue01.item.ItemType.BOOTS,
                com.rogue01.item.ItemType.RING,
                com.rogue01.item.ItemType.NECKLACE,
                com.rogue01.item.ItemType.WEAPON_OFF,
                com.rogue01.item.ItemType.WEAPON_MAIN
            };
            
            for (int i = 0; i < slotNames.length; i++) {
                int x = startX + (i * spacing);
                int y = startY;
                
                if (selectedEquipmentSlot == i) {
                    g2d.setColor(new Color(255, 220, 100));
                    g2d.fillRoundRect(x, y, slotSize, slotSize, 6, 6);
                    g2d.setColor(new Color(255, 180, 50));
                    g2d.drawRoundRect(x, y, slotSize, slotSize, 6, 6);
                } else {
                    g2d.setColor(new Color(60, 65, 80));
                    g2d.fillRoundRect(x, y, slotSize, slotSize, 6, 6);
                    g2d.setColor(new Color(90, 95, 110));
                    g2d.drawRoundRect(x, y, slotSize, slotSize, 6, 6);
                }
                
                g2d.setColor(new Color(200, 205, 220));
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
                g2d.drawString(slotNames[i], x + 4, y - 4);
                
                com.rogue01.item.Equipment equipped = inventory.getEquippedItem(slotTypes[i]);
                if (equipped != null) {
                    g2d.setColor(new Color(255, 230, 150));
                    g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
                    g2d.drawString(String.valueOf(equipped.getSymbol()), x + 16, y + 36);
                    g2d.setColor(new Color(150, 155, 170));
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
                    g2d.drawString(equipped.getDurability() + "/" + equipped.getMaxDurability(), x + 2, y + slotSize + 12);
                }
            }
        }
        
        private void drawInventoryItems(Graphics2D g2d) {
            int startX = 260;
            int startY = 165;
            int itemsPerRow = 12;
            int slotSize = 38;
            int spacing = 42;
            
            com.rogue01.entity.Player player = currentGame.getPlayer();
            com.rogue01.item.Inventory inventory = player.getInventory();
            
            g2d.setColor(new Color(180, 185, 200));
            g2d.setFont(new Font("Monospaced", Font.BOLD, 13));
            g2d.drawString("아이템 (" + inventory.getSize() + "/" + inventory.getMaxSize() + ")", startX, startY - 18);
            
            for (int i = 0; i < inventory.getSize(); i++) {
                int row = i / itemsPerRow;
                int col = i % itemsPerRow;
                int x = startX + (col * spacing);
                int y = startY + (row * spacing);
                
                com.rogue01.item.Item item = inventory.getItem(i);
                
                if (selectedInventorySlot == i) {
                    g2d.setColor(new Color(100, 180, 220));
                    g2d.fillRoundRect(x, y, slotSize, slotSize, 4, 4);
                    g2d.setColor(new Color(80, 160, 200));
                    g2d.drawRoundRect(x, y, slotSize, slotSize, 4, 4);
                } else {
                    g2d.setColor(new Color(55, 60, 75));
                    g2d.fillRoundRect(x, y, slotSize, slotSize, 4, 4);
                    g2d.setColor(new Color(85, 90, 105));
                    g2d.drawRoundRect(x, y, slotSize, slotSize, 4, 4);
                }
                
                if (item != null) {
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
                    g2d.drawString(String.valueOf(item.getSymbol()), x + 10, y + 24);
                    if (item instanceof com.rogue01.item.Equipment) {
                        g2d.setColor(new Color(255, 210, 100));
                    } else {
                        g2d.setColor(new Color(120, 200, 120));
                    }
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
                    g2d.drawString(item.getType().getKoreanName(), x + 2, y + slotSize + 10);
                }
            }
        }
        
        private void drawInventoryItemDetail(Graphics2D g2d, com.rogue01.entity.Player player) {
            com.rogue01.item.Inventory inventory = player.getInventory();
            com.rogue01.item.Item item = null;
            if (selectedInventorySlot >= 0 && selectedInventorySlot < inventory.getSize()) {
                item = inventory.getItem(selectedInventorySlot);
            } else if (selectedEquipmentSlot >= 0 && selectedEquipmentSlot < 7) {
                com.rogue01.item.ItemType[] types = {
                    com.rogue01.item.ItemType.HELMET, com.rogue01.item.ItemType.ARMOR,
                    com.rogue01.item.ItemType.BOOTS, com.rogue01.item.ItemType.RING,
                    com.rogue01.item.ItemType.NECKLACE, com.rogue01.item.ItemType.WEAPON_OFF,
                    com.rogue01.item.ItemType.WEAPON_MAIN
                };
                item = inventory.getEquippedItem(types[selectedEquipmentSlot]);
            }
            
            if (item == null) return;
            
            int detailX = 260;
            int detailY = getHeight() - 95;
            int detailW = getWidth() - 280;
            int detailH = 75;
            
            g2d.setColor(new Color(45, 50, 65));
            g2d.fillRoundRect(detailX, detailY, detailW, detailH, 6, 6);
            g2d.setColor(new Color(100, 105, 120));
            g2d.drawRoundRect(detailX, detailY, detailW, detailH, 6, 6);
            
            g2d.setColor(new Color(255, 245, 200));
            g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
            g2d.drawString(item.getName(), detailX + 15, detailY + 25);
            g2d.setColor(new Color(180, 185, 200));
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g2d.drawString(item.getDescription(), detailX + 15, detailY + 45);
            if (item instanceof com.rogue01.item.Equipment eq) {
                g2d.setColor(new Color(220, 180, 100));
                g2d.drawString("공격+" + eq.getAttack() + " 방어+" + eq.getDefense() + " | 내구도 " + eq.getDurability() + "/" + eq.getMaxDurability(), detailX + 15, detailY + 62);
            }
        }
        
        private void drawMapView(Graphics2D g2d) {
            // 배경
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // 제목
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 24));
            g2d.drawString("WORLD MAP", getWidth()/2 - 80, 50);
            
            // 전체 맵 그리기
            drawFullMap(g2d);
            
            // 플레이어 정보
            drawPlayerInfo(g2d);
            
            // 조작법
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2d.drawString("M/ESC: Close Map", getWidth()/2 - 80, getHeight() - 30);
        }
        
        private void drawFullMap(Graphics2D g2d) {
            com.rogue01.map.Map map = currentGame.getMap();
            com.rogue01.entity.Player player = currentGame.getPlayer();
            
            int mapWidth = map.getWidth();
            int mapHeight = map.getHeight();
            
            // 간략한 맵을 위해 샘플링 간격 설정 (성능 최적화)
            int sampleInterval = Math.max(5, Math.min(mapWidth, mapHeight) / 50);
            
            // 맵을 화면에 맞게 스케일링 (더 큰 스케일로 간략화)
            int maxMapWidth = getWidth() - 100;
            int maxMapHeight = getHeight() - 200;
            
            double scaleX = (double) maxMapWidth / (mapWidth / sampleInterval);
            double scaleY = (double) maxMapHeight / (mapHeight / sampleInterval);
            double scale = Math.min(scaleX, scaleY);
            
            int scaledMapWidth = (int) ((mapWidth / sampleInterval) * scale);
            int scaledMapHeight = (int) ((mapHeight / sampleInterval) * scale);
            
            // 맵을 화면 중앙에 배치
            int mapStartX = (getWidth() - scaledMapWidth) / 2;
            int mapStartY = 100;
            
            // 맵 배경
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(mapStartX - 10, mapStartY - 10, scaledMapWidth + 20, scaledMapHeight + 20);
            
            // 간략한 맵 그리기 (샘플링된 타일만)
            for (int y = 0; y < mapHeight; y += sampleInterval) {
                for (int x = 0; x < mapWidth; x += sampleInterval) {
                    char symbol = map.getTileSymbol(x, y);
                    int pixelX = mapStartX + (int)((x / sampleInterval) * scale);
                    int pixelY = mapStartY + (int)((y / sampleInterval) * scale);
                    int tileSize = (int)(scale * 2); // 더 큰 타일 크기
                    
                    // 플레이어 위치면 플레이어 표시
                    if (Math.abs(player.getX() - x) < sampleInterval && Math.abs(player.getY() - y) < sampleInterval) {
                        g2d.setColor(Color.GREEN);
                        g2d.fillRect(pixelX, pixelY, tileSize, tileSize);
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Monospaced", Font.BOLD, tileSize / 2));
                        g2d.drawString("@", pixelX + tileSize/4, pixelY + tileSize*3/4);
                    } else {
                        // 타일 타입에 따른 색상 (간략화)
                        if (symbol == '#') {
                            g2d.setColor(Color.GRAY); // 벽
                        } else if (symbol == '.') {
                            g2d.setColor(Color.DARK_GRAY); // 바닥
                        } else {
                            g2d.setColor(Color.WHITE); // 기타
                        }
                        g2d.fillRect(pixelX, pixelY, tileSize, tileSize);
                    }
                }
            }
            
            // 플레이어 위치 강조 (더 큰 원으로 표시)
            int playerPixelX = mapStartX + (int)((player.getX() / sampleInterval) * scale);
            int playerPixelY = mapStartY + (int)((player.getY() / sampleInterval) * scale);
            int tileSize = (int)(scale * 2);
            g2d.setColor(Color.RED);
            g2d.setStroke(new java.awt.BasicStroke(3));
            g2d.drawOval(playerPixelX - 3, playerPixelY - 3, tileSize + 6, tileSize + 6);
        }
        
        private void drawPlayerInfo(Graphics2D g2d) {
            com.rogue01.entity.Player player = currentGame.getPlayer();
            
            // 간략한 플레이어 정보 패널
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(20, getHeight() - 80, 250, 60);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
            g2d.drawString("Player: (" + player.getX() + ", " + player.getY() + ") | HP: " + player.getHealth(), 30, getHeight() - 60);
                g2d.drawString("Map: " + currentGame.getMap().getWidth() + "x" + currentGame.getMap().getHeight() + " | Scale: 1:" + Math.max(5, Math.min(currentGame.getMap().getWidth(), currentGame.getMap().getHeight()) / 50), 30, getHeight() - 40);
        }
        
        /**
         * 전투 화면 그리기
         */
        private void drawBattleScreen(Graphics2D g2d) {
            BattleManager battleManager = currentGame.getBattleManager();
            if (battleManager == null) {
                return;
            }
            
            // BattleScreen은 전투 시작 시 한 번만 생성 (선택 상태 유지를 위해)
            if (battleScreen == null) {
                battleScreen = new BattleScreen(battleManager);
            }
            
            battleScreen.render(g2d, getWidth(), getHeight());
        }
        
        /**
         * 게임 오버 화면 그리기
         */
        private void drawGameOverScreen(Graphics2D g2d) {
            // 반투명 배경
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // 게임 오버 타이틀
            g2d.setFont(new Font("Monospaced", Font.BOLD, 56));
            g2d.setColor(Color.RED);
            g2d.drawString("GAME OVER", getWidth() / 2 - 180, getHeight() / 2 - 80);
            
            // 게임 통계
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
            g2d.setColor(Color.WHITE);
            int statsY = getHeight() / 2 - 20;
            g2d.drawString("처치한 적: " + currentGame.getKillCount() + " 마리", getWidth() / 2 - 100, statsY);
            g2d.drawString("생존 시간: " + formatTime(currentGame.getSurvivalTimeSeconds()), getWidth() / 2 - 100, statsY + 30);
            
            // 옵션
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
            g2d.setColor(Color.LIGHT_GRAY);
            int optY = getHeight() / 2 + 50;
            g2d.drawString("R: 재시작", getWidth() / 2 - 80, optY);
            g2d.drawString("M: 메인 메뉴", getWidth() / 2 - 80, optY + 30);
            g2d.drawString("Q: 종료", getWidth() / 2 - 80, optY + 60);
        }
        
        private String formatTime(long seconds) {
            long min = seconds / 60;
            long sec = seconds % 60;
            return String.format("%d분 %d초", min, sec);
        }
    }
    
    /**
     * 키 입력 처리 (전투 화면용)
     */
    public void handleBattleInput(KeyEvent e) {
        if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.BATTLE) {
            if (battleScreen != null) {
                battleScreen.handleInput(e);
            }
            
            // 전투 종료 후 아무 키나 눌러 계속
            BattleManager battleManager = currentGame.getBattleManager();
            if (battleManager != null && battleManager.isBattleEnded()) {
                if (e.getKeyCode() != KeyEvent.VK_UP && e.getKeyCode() != KeyEvent.VK_DOWN) {
                    // 전투 종료 처리 (Game 클래스에서 처리됨)
                }
            }
        }
    }
}