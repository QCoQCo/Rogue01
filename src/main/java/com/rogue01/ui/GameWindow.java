package com.rogue01.ui;

import com.rogue01.game.Game;
import com.rogue01.util.InputHandler;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private GamePanel gamePanel;
    private InputHandler inputHandler;
    private Game currentGame; // 현재 게임 인스턴스 참조
    private int selectedInventorySlot = -1; // 선택된 인벤토리 슬롯
    private int selectedEquipmentSlot = -1; // 선택된 장비 슬롯
    
    public GameWindow() {
        setTitle("Rogue01");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        
        this.inputHandler = new InputHandler();
        this.gamePanel = new GamePanel();
        
        add(gamePanel);
        addKeyListener(inputHandler);
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
        int startX = 50;
        int startY = 100;
        int slotSize = 60;
        int spacing = 70; // 슬롯 간격을 줄여서 더 정확한 클릭 영역 확보
        
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
        int startX = 50;
        int startY = 250;
        int itemsPerRow = 10;
        int slotSize = 40;
        int spacing = 45; // 슬롯 간격을 줄여서 더 정확한 클릭 영역 확보
        
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
                g2d.drawString("HP: " + player.getHealth() + "/" + player.getMaxHealth(), 20, 25);
                g2d.drawString("Position: (" + player.getX() + ", " + player.getY() + ")", 200, 25);
                g2d.drawString("Player: " + player.getName(), 450, 25);
                
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
                        
                        // 플레이어 위치면 플레이어 심볼로 덮어씀
                        if (player.getX() == mapX && player.getY() == mapY) {
                            g2d.setColor(Color.GREEN);
                            g2d.drawString(String.valueOf(player.getSymbol()), offsetX + x * TILE_SIZE, offsetY + (y+1) * TILE_SIZE);
                        } else {
                            g2d.drawString(String.valueOf(symbol), offsetX + x * TILE_SIZE, offsetY + (y+1) * TILE_SIZE);
                        }
                    }
                }
                
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
            }
        }
        
        private void drawInventory(Graphics2D g2d) {
            // 배경
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // 제목
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 24));
            g2d.drawString("INVENTORY", getWidth()/2 - 80, 50);
            
            // 장비 슬롯 (상단)
            drawEquipmentSlots(g2d);
            
            // 인벤토리 아이템 (하단)
            drawInventoryItems(g2d);
            
            // 조작법
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2d.drawString("I/ESC: Close Inventory | Click: Equip/Unequip | 1-7: Quick Select", getWidth()/2 - 200, getHeight() - 30);
        }
        
        private void drawEquipmentSlots(Graphics2D g2d) {
            int startX = 50;
            int startY = 100;
            int slotSize = 60;
            int spacing = 70; // 클릭 처리와 동일한 간격
            
            com.rogue01.entity.Player player = currentGame.getPlayer();
            com.rogue01.item.Inventory inventory = player.getInventory();
            
            // 장비 슬롯 그리기
            String[] slotNames = {"투구", "갑옷", "신발", "반지", "목걸이", "왼손", "오른손"};
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
                
                // 슬롯 배경
                if (selectedEquipmentSlot == i) {
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(x, y, slotSize, slotSize);
                    g2d.setColor(Color.ORANGE);
                    g2d.drawRect(x, y, slotSize, slotSize);
                } else {
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.fillRect(x, y, slotSize, slotSize);
                    g2d.setColor(Color.GRAY);
                    g2d.drawRect(x, y, slotSize, slotSize);
                }
                
                // 슬롯 이름
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
                g2d.drawString(slotNames[i], x, y - 5);
                
                // 장착된 아이템 표시
                com.rogue01.item.Equipment equipped = inventory.getEquippedItem(slotTypes[i]);
                if (equipped != null) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
                    g2d.drawString(String.valueOf(equipped.getSymbol()), x + 20, y + 35);
                    
                    // 내구도 표시
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
                    g2d.drawString(equipped.getDurability() + "/" + equipped.getMaxDurability(), x, y + slotSize + 15);
                }
            }
        }
        
        private void drawInventoryItems(Graphics2D g2d) {
            int startX = 50;
            int startY = 250;
            int itemsPerRow = 10;
            int slotSize = 40;
            int spacing = 45; // 클릭 처리와 동일한 간격
            
            com.rogue01.entity.Player player = currentGame.getPlayer();
            com.rogue01.item.Inventory inventory = player.getInventory();
            
            // 제목
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
            g2d.drawString("Inventory (" + inventory.getSize() + "/" + inventory.getMaxSize() + ")", startX, startY - 20);
            
            // 아이템 그리드
            for (int i = 0; i < inventory.getSize(); i++) {
                int row = i / itemsPerRow;
                int col = i % itemsPerRow;
                int x = startX + (col * spacing);
                int y = startY + (row * spacing);
                
                com.rogue01.item.Item item = inventory.getItem(i);
                
                // 슬롯 배경
                if (selectedInventorySlot == i) {
                    g2d.setColor(Color.CYAN);
                    g2d.fillRect(x, y, slotSize, slotSize);
                    g2d.setColor(Color.BLUE);
                    g2d.drawRect(x, y, slotSize, slotSize);
                } else {
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.fillRect(x, y, slotSize, slotSize);
                    g2d.setColor(Color.GRAY);
                    g2d.drawRect(x, y, slotSize, slotSize);
                }
                
                if (item != null) {
                    // 아이템 심볼
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
                    g2d.drawString(String.valueOf(item.getSymbol()), x + 12, y + 25);
                    
                    // 아이템 타입에 따른 색상
                    if (item instanceof com.rogue01.item.Equipment) {
                        g2d.setColor(Color.YELLOW);
                    } else {
                        g2d.setColor(Color.GREEN);
                    }
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 8));
                    g2d.drawString(item.getType().getKoreanName(), x, y + slotSize + 12);
                }
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
    }
} 