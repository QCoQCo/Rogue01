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
    private int selectedInventorySlot = -1;
    private int selectedEquipmentSlot = -1;
    private BattleScreen battleScreen;

    // 아이템 상세 팝업 (클릭 시 표시)
    private boolean itemDetailPopupActive;
    private int itemPopupSource; // 0=inventory, 1=equipment
    private int itemPopupSlotIndex;

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

    public Game getGame() {
        return currentGame;
    }

    public int getSelectedInventorySlot() {
        return selectedInventorySlot;
    }

    public boolean isItemDetailPopupActive() {
        return itemDetailPopupActive;
    }

    public boolean closeItemDetailPopupIfActive() {
        if (itemDetailPopupActive) {
            itemDetailPopupActive = false;
            selectedInventorySlot = -1;
            selectedEquipmentSlot = -1;
            return true;
        }
        return false;
    }

    /**
     * 아이템 상세 팝업 단축키 처리 (F=장착/사용, T=버리기, Q=취소)
     * 
     * @return true if key was handled
     */
    public boolean handleItemPopupKey(int keyCode) {
        if (!itemDetailPopupActive || currentGame == null)
            return false;

        com.rogue01.entity.Player player = currentGame.getPlayer();
        com.rogue01.item.Inventory inventory = player.getInventory();
        com.rogue01.item.Item item = itemPopupSource == 0
                ? inventory.getItem(itemPopupSlotIndex)
                : inventory.getEquippedItem(getSlotType(itemPopupSlotIndex));
        if (item == null) {
            itemDetailPopupActive = false;
            return true;
        }

        if (keyCode == KeyEvent.VK_Q) {
            itemDetailPopupActive = false;
            selectedInventorySlot = -1;
            selectedEquipmentSlot = -1;
            return true;
        }
        if (keyCode == KeyEvent.VK_T) {
            if (itemPopupSource == 0) {
                inventory.removeItem(item);
            } else {
                inventory.dropEquippedItem(getSlotType(itemPopupSlotIndex));
            }
            itemDetailPopupActive = false;
            selectedInventorySlot = -1;
            selectedEquipmentSlot = -1;
            return true;
        }
        if (keyCode == KeyEvent.VK_F) {
            if (item instanceof com.rogue01.item.Consumable) {
                inventory.useItem(item, player);
            } else if (item instanceof com.rogue01.item.Equipment) {
                if (itemPopupSource == 0) {
                    inventory.equipItem(item);
                } else {
                    inventory.unequipItem(getSlotType(itemPopupSlotIndex));
                }
            }
            itemDetailPopupActive = false;
            selectedInventorySlot = -1;
            selectedEquipmentSlot = -1;
            return true;
        }
        return false;
    }

    public void openItemDetailPopupForSlot(int inventorySlotIndex) {
        itemDetailPopupActive = true;
        itemPopupSource = 0;
        itemPopupSlotIndex = inventorySlotIndex;
        selectedInventorySlot = inventorySlotIndex;
        selectedEquipmentSlot = -1;
    }

    private void handleMouseClick(int x, int y) {
        if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.INVENTORY) {
            if (itemDetailPopupActive) {
                handleItemPopupButtonClick(x, y);
            } else {
                handleEquipmentSlotClick(x, y);
                handleInventorySlotClick(x, y);
            }
        }
    }

    // 인벤토리/장비 레이아웃 상수 (클릭 영역과 그리기 동기화)
    private static final int INV_START_X = 260;
    private static final int EQUIP_START_Y = 75;
    private static final int INV_START_Y = 165;
    private static final int EQUIP_SLOT_SIZE = 56;
    private static final int EQUIP_SPACING = 62;
    private static final int INV_ITEMS_PER_ROW = 12;
    private static final int INV_SLOT_SIZE = 38;
    private static final int INV_SPACING = 42;

    private void handleEquipmentSlotClick(int x, int y) {
        int startX = INV_START_X;
        int startY = EQUIP_START_Y;
        int slotSize = EQUIP_SLOT_SIZE;
        int spacing = EQUIP_SPACING;

        for (int i = 0; i < 7; i++) {
            int slotX = startX + (i * spacing);
            int slotY = startY;

            if (x >= slotX && x <= slotX + slotSize && y >= slotY && y <= slotY + slotSize) {
                com.rogue01.item.Equipment equipped = currentGame.getPlayer().getInventory()
                        .getEquippedItem(getSlotType(i));
                if (equipped != null) {
                    selectedEquipmentSlot = i;
                    selectedInventorySlot = -1;
                    itemDetailPopupActive = true;
                    itemPopupSource = 1;
                    itemPopupSlotIndex = i;
                }
                break;
            }
        }
    }

    private void handleInventorySlotClick(int x, int y) {
        com.rogue01.item.Inventory inventory = currentGame.getPlayer().getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            int row = i / INV_ITEMS_PER_ROW;
            int col = i % INV_ITEMS_PER_ROW;
            int slotX = INV_START_X + (col * INV_SPACING);
            int slotY = INV_START_Y + (row * INV_SPACING);

            if (x >= slotX && x <= slotX + INV_SLOT_SIZE && y >= slotY && y <= slotY + INV_SLOT_SIZE) {
                com.rogue01.item.Item item = inventory.getItem(i);
                if (item != null) {
                    selectedInventorySlot = i;
                    selectedEquipmentSlot = -1;
                    itemDetailPopupActive = true;
                    itemPopupSource = 0;
                    itemPopupSlotIndex = i;
                }
                break;
            }
        }
    }

    private com.rogue01.item.ItemType getSlotType(int slotIndex) {
        com.rogue01.item.ItemType[] types = {
                com.rogue01.item.ItemType.HELMET, com.rogue01.item.ItemType.ARMOR,
                com.rogue01.item.ItemType.BOOTS, com.rogue01.item.ItemType.RING,
                com.rogue01.item.ItemType.NECKLACE, com.rogue01.item.ItemType.WEAPON_OFF,
                com.rogue01.item.ItemType.WEAPON_MAIN
        };
        return types[slotIndex];
    }

    private void handleItemPopupButtonClick(int x, int y) {
        com.rogue01.entity.Player player = currentGame.getPlayer();
        com.rogue01.item.Inventory inventory = player.getInventory();
        com.rogue01.item.Item item = itemPopupSource == 0
                ? inventory.getItem(itemPopupSlotIndex)
                : inventory.getEquippedItem(getSlotType(itemPopupSlotIndex));
        if (item == null) {
            itemDetailPopupActive = false;
            return;
        }

        int panelW = gamePanel.getWidth();
        int panelH = gamePanel.getHeight();
        int popupW = 420;
        int popupH = item instanceof com.rogue01.item.Consumable ? 200 : 260;
        int popupX = (panelW - popupW) / 2;
        int popupY = (panelH - popupH) / 2;
        int btnY = popupY + popupH - 50;
        int btnH = 36;
        int btnW = 100;
        int btnSpacing = 15;
        int totalBtnW = 3 * btnW + 2 * btnSpacing;
        int startBtnX = popupX + (popupW - totalBtnW) / 2;

        // 버튼 순서: [취소] [버리기] [착용/교체/해제/사용]
        int cancelX = startBtnX;
        int dropX = startBtnX + btnW + btnSpacing;
        int actionX = startBtnX + 2 * (btnW + btnSpacing);

        if (y >= btnY && y <= btnY + btnH) {
            if (x >= cancelX && x <= cancelX + btnW) {
                itemDetailPopupActive = false;
                selectedInventorySlot = -1;
                selectedEquipmentSlot = -1;
            } else if (x >= dropX && x <= dropX + btnW) {
                if (itemPopupSource == 0) {
                    inventory.removeItem(item);
                } else {
                    inventory.dropEquippedItem(getSlotType(itemPopupSlotIndex));
                }
                itemDetailPopupActive = false;
                selectedInventorySlot = -1;
                selectedEquipmentSlot = -1;
            } else if (x >= actionX && x <= actionX + btnW) {
                if (item instanceof com.rogue01.item.Consumable) {
                    inventory.useItem(item, player);
                } else if (item instanceof com.rogue01.item.Equipment) {
                    if (itemPopupSource == 0) {
                        inventory.equipItem(item);
                    } else {
                        inventory.unequipItem(getSlotType(itemPopupSlotIndex));
                    }
                }
                itemDetailPopupActive = false;
                selectedInventorySlot = -1;
                selectedEquipmentSlot = -1;
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
                int centerX = getWidth() / 2;
                int baseY = getHeight() / 2;
                FontMetrics fm;

                // 타이틀
                g2d.setFont(new Font("Monospaced", Font.BOLD, 48));
                g2d.setColor(new Color(80, 200, 120));
                fm = g2d.getFontMetrics();
                g2d.drawString("Rogue01", centerX - fm.stringWidth("Rogue01") / 2, baseY - 100);
                // 게임 설명
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
                fm = g2d.getFontMetrics();
                g2d.drawString("Move: WASD / Arrow Keys", centerX - fm.stringWidth("Move: WASD / Arrow Keys") / 2,
                        baseY - 40);
                g2d.drawString("Start: ENTER | Quit: Q", centerX - fm.stringWidth("Start: ENTER | Quit: Q") / 2,
                        baseY - 15);
                String diffStr = "Difficulty: 1-쉬움 2-보통 3-어려움";
                g2d.drawString(diffStr, centerX - fm.stringWidth(diffStr) / 2, baseY + 15);
                com.rogue01.game.GameBalance.Difficulty diff = currentGame.getDifficulty();
                g2d.setColor(Color.YELLOW);
                String currentStr = "현재: " + diff.getKoreanName();
                g2d.drawString(currentStr, centerX - fm.stringWidth(currentStr) / 2, baseY + 45);
                g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
                String startStr = "> Press ENTER to Start <";
                g2d.drawString(startStr, centerX - fm.stringWidth(startStr) / 2, baseY + 85);
                // 제작자 정보
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
                g2d.setColor(Color.GRAY);
                fm = g2d.getFontMetrics();
                String creditStr = "Created by CO_s_MOS";
                g2d.drawString(creditStr, centerX - fm.stringWidth(creditStr) / 2, baseY + 130);
                g2d.setColor(Color.WHITE);
            } else if (currentGame != null && (currentGame.getGameState() == com.rogue01.game.GameState.PLAYING
                    || currentGame.getGameState() == com.rogue01.game.GameState.BOSS_DOOR_PROMPT)) {
                // HUD 표시
                g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
                com.rogue01.entity.Player player = currentGame.getPlayer();

                // HUD 배경
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, getWidth(), 35);
                g2d.setColor(Color.WHITE);

                // 플레이어 정보
                g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
                g2d.drawString("Ch." + currentGame.getCurrentChapter() + "-" + currentGame.getCurrentLevel(), 20, 22);
                g2d.drawString("Lv." + player.getLevel(), 90, 22);
                g2d.drawString("HP: " + player.getHealth() + "/" + player.getMaxHealth(), 130, 22);
                g2d.drawString("ATK:" + player.getAttack() + " DEF:" + player.getDefense(), 290, 22);
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawString("좌표: (" + currentGame.getRelPlayerX() + ", " + currentGame.getRelPlayerY() + ")", 420,
                        22);
                g2d.setColor(Color.CYAN);
                int expNext = player.getExpToNextLevel();
                g2d.drawString("EXP: " + player.getExperience() + "/" + expNext, 550, 22);
                g2d.setColor(Color.WHITE);

                // 적 정보 (EXP와 간격 확보)
                // g2d.setColor(Color.YELLOW);
                // g2d.drawString("Game State: PLAYING", 600, 25);

                // 조작법 (우측 상단)
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
                g2d.drawString("Controls:", getWidth() - 200, 15);
                g2d.drawString("WASD: Move | F: Stairs/Doors", getWidth() - 200, 30);
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
                        com.rogue01.map.Tile tile = map.getTile(mapX, mapY);
                        if (tile == null)
                            continue;
                        char symbol = tile.getSymbol();

                        // 타일 타입에 따른 색상 설정
                        if (tile.isStairsDown()) {
                            g2d.setColor(Color.CYAN); // 계단
                        } else if (tile.isSealWall()) {
                            g2d.setColor(new Color(180, 100, 220)); // 보라색 봉인 벽
                        } else if (tile.isRubble()) {
                            g2d.setColor(Color.YELLOW); // 노란색 무너진 벽
                        } else if (tile.isBossDoorMid()) {
                            g2d.setColor(new Color(200, 180, 80)); // 황금색 중간보스 문
                        } else if (tile.isBossDoorChapter()) {
                            g2d.setColor(new Color(200, 80, 80)); // 붉은색 챕터보스 문
                        } else if (symbol == '#') {
                            g2d.setColor(Color.GRAY); // 벽
                        } else if (symbol == '.') {
                            g2d.setColor(Color.DARK_GRAY); // 바닥
                        } else {
                            g2d.setColor(Color.WHITE); // 기타
                        }

                        g2d.drawString(String.valueOf(symbol), offsetX + x * TILE_SIZE, offsetY + (y + 1) * TILE_SIZE);
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
                    if (enemy.isDead())
                        continue;

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

                // 적 정보 표시 (HUD에, EXP와 간격 확보)
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
                g2d.drawString("Enemies: " + currentGame.getEnemies().size(), 680, 22);

                // 하단 정보 패널
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, getHeight() - 35, getWidth(), 35);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
                g2d.drawString("ESC: Pause | Q: Quit | R: Restart", 20, getHeight() - 15);
                g2d.drawString("Map Size: " + map.getWidth() + "x" + map.getHeight(), 400, getHeight() - 15);
                g2d.drawString("FPS: 60", 600, getHeight() - 15);// 랜더링 속도 계산

                // 보스방 문 프롬프트 오버레이
                if (currentGame.getGameState() == com.rogue01.game.GameState.BOSS_DOOR_PROMPT) {
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Monospaced", Font.BOLD, 24));
                    g2d.drawString("보스방에 들어가시겠습니까?", getWidth() / 2 - 150, getHeight() / 2 - 30);
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
                    g2d.drawString("F: 들어가기  |  ESC: 취소", getWidth() / 2 - 120, getHeight() / 2 + 20);
                }
            } else if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.PAUSED) {
                // 일시정지 메뉴
                // 반투명 오버레이
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // 일시정지 메뉴
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.BOLD, 36));
                g2d.drawString("GAME PAUSED", getWidth() / 2 - 120, getHeight() / 2 - 50);

                g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
                g2d.drawString("ESC: Resume Game", getWidth() / 2 - 100, getHeight() / 2);
                g2d.drawString("Q: Quit Game", getWidth() / 2 - 100, getHeight() / 2 + 30);

                // 현재 게임 상태 정보
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
                g2d.setColor(Color.LIGHT_GRAY);
                com.rogue01.entity.Player player = currentGame.getPlayer();
                g2d.drawString("Player HP: " + player.getHealth() + "/" + player.getMaxHealth(), getWidth() / 2 - 100,
                        getHeight() / 2 + 70);
                g2d.drawString("Position: (" + currentGame.getRelPlayerX() + ", " + currentGame.getRelPlayerY() + ")",
                        getWidth() / 2 - 100, getHeight() / 2 + 90);
                g2d.drawString("Difficulty: " + currentGame.getDifficulty().name(), getWidth() / 2 - 100,
                        getHeight() / 2 + 110);
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
            } else if (currentGame != null
                    && currentGame.getGameState() == com.rogue01.game.GameState.CHAPTER_TRANSITION) {
                // 챕터 전환 연출
                drawChapterTransitionScreen(g2d);
            } else if (currentGame != null && currentGame.getGameState() == com.rogue01.game.GameState.GAME_CLEAR) {
                // 게임 클리어 화면
                drawGameClearScreen(g2d);
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
            g2d.drawString("인벤토리", 260, 38);

            drawEquipmentSlots(g2d);
            drawInventoryItems(g2d);
            if (itemDetailPopupActive) {
                drawItemDetailPopup(g2d, player);
            } else {
                drawInventoryItemDetail(g2d, player);
            }

            // 조작법
            g2d.setColor(new Color(180, 180, 180));
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g2d.drawString("I/ESC: 닫기 | 클릭: 아이템 상세 | 1-7: 선택 | U: 사용 | 팝업: F장착 T버리기 Q취소",
                    getWidth() / 2 - 220, getHeight() - 15);
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
            g2d.fillRect(panelX + 15, panelY + 58, (int) (180 * player.getHealthRatio()), 14);
            g2d.setColor(Color.WHITE);
            g2d.drawString("HP " + player.getHealth() + "/" + player.getMaxHealth(), panelX + 15, panelY + 85);

            // EXP 바
            double expRatio = (double) player.getExperience() / player.getExpToNextLevel();
            g2d.setColor(new Color(60, 60, 80));
            g2d.fillRect(panelX + 15, panelY + 95, 180, 10);
            g2d.setColor(new Color(70, 130, 180));
            g2d.fillRect(panelX + 15, panelY + 95, (int) (180 * Math.min(1, expRatio)), 10);
            g2d.setColor(new Color(180, 200, 220));
            g2d.drawString("EXP " + player.getExperience() + "/" + player.getExpToNextLevel(), panelX + 15,
                    panelY + 120);

            // 공격력/방어력
            g2d.setColor(new Color(220, 180, 100));
            g2d.drawString("공격력: " + player.getAttack(), panelX + 15, panelY + 150);
            g2d.setColor(new Color(100, 180, 220));
            g2d.drawString("방어력: " + player.getDefense(), panelX + 15, panelY + 170);

            // 기본 스탯 (장비 제외)
            g2d.setColor(new Color(140, 140, 160));
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g2d.drawString("(기본 ATK:" + player.getBaseAttack() + " DEF:" + player.getBaseDefense() + ")", panelX + 15,
                    panelY + 195);
        }

        private void drawEquipmentSlots(Graphics2D g2d) {
            int startX = INV_START_X;
            int startY = EQUIP_START_Y;
            int slotSize = EQUIP_SLOT_SIZE;
            int spacing = EQUIP_SPACING;

            com.rogue01.entity.Player player = currentGame.getPlayer();
            com.rogue01.item.Inventory inventory = player.getInventory();

            g2d.setColor(new Color(180, 185, 200));
            g2d.setFont(new Font("Monospaced", Font.BOLD, 13));
            g2d.drawString("장비 슬롯", startX, startY - 15);

            String[] slotNames = { "투구", "갑옷", "신발", "반지", "목걸이", "보조", "주무기" };
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
                g2d.drawString(slotNames[i], x + 4, y + 12);

                com.rogue01.item.Equipment equipped = inventory.getEquippedItem(slotTypes[i]);
                if (equipped != null) {
                    g2d.setColor(new Color(255, 230, 150));
                    g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
                    g2d.drawString(String.valueOf(equipped.getSymbol()), x + 16, y + 36);
                    g2d.setColor(new Color(150, 155, 170));
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
                    g2d.drawString(equipped.getDurability() + "/" + equipped.getMaxDurability(), x + 2,
                            y + slotSize + 12);
                }
            }
        }

        private void drawInventoryItems(Graphics2D g2d) {
            com.rogue01.entity.Player player = currentGame.getPlayer();
            com.rogue01.item.Inventory inventory = player.getInventory();

            g2d.setColor(new Color(180, 185, 200));
            g2d.setFont(new Font("Monospaced", Font.BOLD, 13));
            g2d.drawString("아이템 (" + inventory.getSize() + "/" + inventory.getMaxSize() + ")", INV_START_X,
                    INV_START_Y - 8);

            for (int i = 0; i < inventory.getSize(); i++) {
                int row = i / INV_ITEMS_PER_ROW;
                int col = i % INV_ITEMS_PER_ROW;
                int x = INV_START_X + (col * INV_SPACING);
                int y = INV_START_Y + (row * INV_SPACING);

                com.rogue01.item.Item item = inventory.getItem(i);

                if (selectedInventorySlot == i) {
                    g2d.setColor(new Color(100, 180, 220));
                    g2d.fillRoundRect(x, y, INV_SLOT_SIZE, INV_SLOT_SIZE, 4, 4);
                    g2d.setColor(new Color(80, 160, 200));
                    g2d.drawRoundRect(x, y, INV_SLOT_SIZE, INV_SLOT_SIZE, 4, 4);
                } else {
                    g2d.setColor(new Color(55, 60, 75));
                    g2d.fillRoundRect(x, y, INV_SLOT_SIZE, INV_SLOT_SIZE, 4, 4);
                    g2d.setColor(new Color(85, 90, 105));
                    g2d.drawRoundRect(x, y, INV_SLOT_SIZE, INV_SLOT_SIZE, 4, 4);
                }

                if (item != null) {
                    String iconText;
                    String subText;
                    if (item instanceof com.rogue01.item.Equipment eq) {
                        iconText = String.valueOf(eq.getSymbol()) + "+" + eq.getLevel();
                        subText = eq.getType().getKoreanName();
                    } else if (item instanceof com.rogue01.item.HealthPotion hp) {
                        iconText = String.valueOf(item.getSymbol());
                        subText = hp.getHealAmount() >= 60 ? "체력포션+" : "체력포션";
                    } else {
                        iconText = String.valueOf(item.getSymbol());
                        subText = item.getType().getKoreanName();
                    }
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Monospaced", Font.BOLD, iconText.length() <= 3 ? 14 : 11));
                    g2d.drawString(iconText, x + (INV_SLOT_SIZE - g2d.getFontMetrics().stringWidth(iconText)) / 2,
                            y + 22);
                    if (item instanceof com.rogue01.item.Equipment) {
                        g2d.setColor(new Color(255, 210, 100));
                    } else {
                        g2d.setColor(new Color(120, 200, 120));
                    }
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
                    g2d.drawString(subText, x + 2, y + INV_SLOT_SIZE + 10);
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

            if (item == null)
                return;

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
                g2d.drawString("Lv." + eq.getLevel() + " | 공격+" + eq.getAttack() + " 방어+" + eq.getDefense()
                        + " | 내구도 " + eq.getDurability() + "/" + eq.getMaxDurability(), detailX + 15, detailY + 62);
            } else if (item instanceof com.rogue01.item.HealthPotion hp) {
                g2d.setColor(new Color(120, 200, 120));
                g2d.drawString("HP +" + hp.getHealAmount() + " 회복 | U키로 사용", detailX + 15, detailY + 62);
            }
        }

        private void drawItemDetailPopup(Graphics2D g2d, com.rogue01.entity.Player player) {
            com.rogue01.item.Inventory inventory = player.getInventory();
            com.rogue01.item.Item item = itemPopupSource == 0
                    ? inventory.getItem(itemPopupSlotIndex)
                    : inventory.getEquippedItem(getSlotType(itemPopupSlotIndex));
            if (item == null) {
                return;
            }

            int popupW = 420;
            int popupH = item instanceof com.rogue01.item.Consumable ? 200 : 260;
            int popupX = (getWidth() - popupW) / 2;
            int popupY = (getHeight() - popupH) / 2;

            // 배경 어둡게
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // 팝업 배경
            g2d.setColor(new Color(45, 50, 65));
            g2d.fillRoundRect(popupX, popupY, popupW, popupH, 12, 12);
            g2d.setColor(new Color(100, 110, 130));
            g2d.drawRoundRect(popupX, popupY, popupW, popupH, 12, 12);

            int contentX = popupX + 25;
            int contentY = popupY + 30;

            g2d.setColor(new Color(255, 245, 200));
            g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
            g2d.drawString(item.getName(), contentX, contentY);
            g2d.setColor(new Color(180, 185, 200));
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 13));
            g2d.drawString(item.getDescription(), contentX, contentY + 25);

            if (item instanceof com.rogue01.item.Consumable) {
                if (item instanceof com.rogue01.item.HealthPotion hp) {
                    g2d.setColor(new Color(120, 200, 120));
                    g2d.drawString("효과: HP " + hp.getHealAmount() + " 회복", contentX, contentY + 55);
                }
            } else if (item instanceof com.rogue01.item.Equipment eq) {
                g2d.setColor(new Color(220, 180, 100));
                g2d.drawString("Lv." + eq.getLevel() + " | 공격+" + eq.getAttack() + " 방어+" + eq.getDefense()
                        + " | 내구도 " + eq.getDurability() + "/" + eq.getMaxDurability(), contentX, contentY + 55);

                com.rogue01.item.Equipment equipped = inventory.getEquippedItem(eq.getType());
                if (itemPopupSource == 0 && equipped != null && equipped != eq) {
                    g2d.setColor(new Color(255, 255, 255));
                    g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
                    g2d.drawString("현재 착용 중:", contentX, contentY + 85);
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 13));
                    g2d.setColor(new Color(200, 200, 200));
                    g2d.drawString(
                            equipped.getName() + " (공격+" + equipped.getAttack() + " 방어+" + equipped.getDefense() + ")",
                            contentX, contentY + 105);
                    int attackDiff = eq.getAttack() - equipped.getAttack();
                    int defenseDiff = eq.getDefense() - equipped.getDefense();
                    int lineY = contentY + 125;
                    g2d.setColor(new Color(200, 200, 200));
                    g2d.drawString("→ 교체 시: ", contentX, lineY);
                    int curX = contentX + g2d.getFontMetrics().stringWidth("→ 교체 시: ");
                    // 공격력: +면 초록, -면 빨강
                    String attackStr = "공격 " + (attackDiff >= 0 ? "+" : "") + attackDiff;
                    g2d.setColor(attackDiff >= 0 ? new Color(150, 255, 150) : new Color(255, 120, 120));
                    g2d.drawString(attackStr, curX, lineY);
                    curX += g2d.getFontMetrics().stringWidth(attackStr);
                    g2d.setColor(new Color(200, 200, 200));
                    g2d.drawString(", ", curX, lineY);
                    curX += g2d.getFontMetrics().stringWidth(", ");
                    // 방어력: +면 초록, -면 빨강
                    String defenseStr = "방어 " + (defenseDiff >= 0 ? "+" : "") + defenseDiff;
                    g2d.setColor(defenseDiff >= 0 ? new Color(150, 255, 150) : new Color(255, 120, 120));
                    g2d.drawString(defenseStr, curX, lineY);
                }
            }

            int btnY = popupY + popupH - 50;
            int btnH = 36;
            int btnW = 100;
            int btnSpacing = 15;
            int totalBtnW = 3 * btnW + 2 * btnSpacing;
            int startBtnX = popupX + (popupW - totalBtnW) / 2;

            String[] btnLabels = { "취소", "버리기", "" };
            if (item instanceof com.rogue01.item.Consumable) {
                btnLabels[2] = "사용";
            } else if (itemPopupSource == 1) {
                btnLabels[2] = "해제";
            } else if (item instanceof com.rogue01.item.Equipment eq
                    && inventory.getEquippedItem(eq.getType()) != null) {
                btnLabels[2] = "교체";
            } else {
                btnLabels[2] = "착용";
            }

            for (int i = 0; i < 3; i++) {
                int bx = startBtnX + i * (btnW + btnSpacing);
                g2d.setColor(
                        i == 0 ? new Color(100, 105, 120) : i == 1 ? new Color(150, 80, 80) : new Color(80, 120, 80));
                g2d.fillRoundRect(bx, btnY, btnW, btnH, 6, 6);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
                g2d.drawString(btnLabels[i], bx + (btnW - g2d.getFontMetrics().stringWidth(btnLabels[i])) / 2,
                        btnY + btnH / 2 + 5);
            }
        }

        private void drawMapView(Graphics2D g2d) {
            // 배경
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // 제목
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 24));
            g2d.drawString("WORLD MAP", getWidth() / 2 - 80, 50);

            // 전체 맵 그리기
            drawFullMap(g2d);

            // 플레이어 정보
            drawPlayerInfo(g2d);

            // 범례 (계단·보스방·봉인벽)
            drawMapLegend(g2d);

            // 조작법
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2d.drawString("M/ESC: Close Map", getWidth() / 2 - 80, getHeight() - 30);
        }

        private void drawMapLegend(Graphics2D g2d) {
            int legX = getWidth() - 180;
            int legY = 100;
            int boxSize = 12;
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("범례", legX, legY - 4);
            legY += 18;
            g2d.setColor(Color.GREEN);
            g2d.fillRect(legX, legY - boxSize, boxSize, boxSize);
            g2d.setColor(Color.WHITE);
            g2d.drawString("플레이어", legX + boxSize + 6, legY - 2);
            legY += 18;
            g2d.setColor(Color.CYAN);
            g2d.fillRect(legX, legY - boxSize, boxSize, boxSize);
            g2d.setColor(Color.WHITE);
            g2d.drawString("계단", legX + boxSize + 6, legY - 2);
            legY += 18;
            g2d.setColor(new Color(200, 180, 80));
            g2d.fillRect(legX, legY - boxSize, boxSize, boxSize);
            g2d.setColor(Color.WHITE);
            g2d.drawString("중간보스방", legX + boxSize + 6, legY - 2);
            legY += 18;
            g2d.setColor(new Color(200, 80, 80));
            g2d.fillRect(legX, legY - boxSize, boxSize, boxSize);
            g2d.setColor(Color.WHITE);
            g2d.drawString("챕터보스방", legX + boxSize + 6, legY - 2);
            legY += 18;
            g2d.setColor(new Color(180, 100, 220));
            g2d.fillRect(legX, legY - boxSize, boxSize, boxSize);
            g2d.setColor(Color.WHITE);
            g2d.drawString("봉인벽", legX + boxSize + 6, legY - 2);
            legY += 18;
            g2d.setColor(Color.YELLOW);
            g2d.fillRect(legX, legY - boxSize, boxSize, boxSize);
            g2d.setColor(Color.WHITE);
            g2d.drawString("무너진 벽", legX + boxSize + 6, legY - 2);
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

            // 간략한 맵 그리기 (샘플링된 타일만) - 계단·보스방·봉인벽 구분 표시
            for (int y = 0; y < mapHeight; y += sampleInterval) {
                for (int x = 0; x < mapWidth; x += sampleInterval) {
                    com.rogue01.map.Tile tile = map.getTile(x, y);
                    int pixelX = mapStartX + (int) ((x / sampleInterval) * scale);
                    int pixelY = mapStartY + (int) ((y / sampleInterval) * scale);
                    int tileSize = (int) (scale * 2); // 더 큰 타일 크기

                    // 플레이어 위치면 플레이어 표시
                    if (Math.abs(player.getX() - x) < sampleInterval && Math.abs(player.getY() - y) < sampleInterval) {
                        g2d.setColor(Color.GREEN);
                        g2d.fillRect(pixelX, pixelY, tileSize, tileSize);
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Monospaced", Font.BOLD, tileSize / 2));
                        g2d.drawString("†", pixelX + tileSize / 4, pixelY + tileSize * 3 / 4);
                    } else if (tile != null) {
                        // 타일 타입에 따른 색상 (계단·보스방·봉인벽 구분)
                        if (tile.isStairsDown()) {
                            g2d.setColor(Color.CYAN); // 계단
                        } else if (tile.isSealWall()) {
                            g2d.setColor(new Color(180, 100, 220)); // 보라색 봉인벽
                        } else if (tile.isRubble()) {
                            g2d.setColor(Color.YELLOW); // 노란색 무너진 벽
                        } else if (tile.isBossDoorMid()) {
                            g2d.setColor(new Color(200, 180, 80)); // 황금색 중간보스 문
                        } else if (tile.isBossDoorChapter()) {
                            g2d.setColor(new Color(200, 80, 80)); // 붉은색 챕터보스 문
                        } else if (tile.getSymbol() == '#') {
                            g2d.setColor(Color.GRAY); // 벽
                        } else if (tile.getSymbol() == '.') {
                            g2d.setColor(Color.DARK_GRAY); // 바닥
                        } else {
                            g2d.setColor(Color.WHITE); // 기타
                        }
                        g2d.fillRect(pixelX, pixelY, tileSize, tileSize);
                    } else {
                        g2d.setColor(Color.GRAY);
                        g2d.fillRect(pixelX, pixelY, tileSize, tileSize);
                    }
                }
            }

            // 플레이어 위치 강조 (더 큰 원으로 표시)
            int playerPixelX = mapStartX + (int) ((player.getX() / sampleInterval) * scale);
            int playerPixelY = mapStartY + (int) ((player.getY() / sampleInterval) * scale);
            int tileSize = (int) (scale * 2);
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
            g2d.drawString("Player: (" + currentGame.getRelPlayerX() + ", " + currentGame.getRelPlayerY()
                    + ") | HP: " + player.getHealth(), 30, getHeight() - 60);
            g2d.drawString(
                    "Map: " + currentGame.getMap().getWidth() + "x" + currentGame.getMap().getHeight() + " | Scale: 1:"
                            + Math.max(5,
                                    Math.min(currentGame.getMap().getWidth(), currentGame.getMap().getHeight()) / 50),
                    30, getHeight() - 40);
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
        private void drawChapterTransitionScreen(Graphics2D g2d) {
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setFont(new Font("Monospaced", Font.BOLD, 28));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("다음 챕터로 넘어가는중...", getWidth() / 2 - 180, getHeight() / 2 - 20);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("ENTER: 계속", getWidth() / 2 - 50, getHeight() / 2 + 30);
        }

        private void drawGameClearScreen(Graphics2D g2d) {
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setFont(new Font("Monospaced", Font.BOLD, 48));
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawString("GAME CLEAR!", getWidth() / 2 - 140, getHeight() / 2 - 60);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
            g2d.setColor(Color.WHITE);
            g2d.drawString("R: 재시작  |  M: 메인 메뉴  |  Q: 종료", getWidth() / 2 - 180, getHeight() / 2 + 20);
        }

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
            g2d.drawString("생존 시간: " + formatTime(currentGame.getSurvivalTimeSeconds()), getWidth() / 2 - 100,
                    statsY + 30);

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