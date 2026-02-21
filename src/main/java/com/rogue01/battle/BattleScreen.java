package com.rogue01.battle;

import com.rogue01.entity.Player;
import com.rogue01.entity.Enemy;
import com.rogue01.item.HealthPotion;
import com.rogue01.item.Item;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * 전투 화면 UI 클래스 (JRPG 스타일)
 */
public class BattleScreen {
    private BattleManager battleManager;
    private int selectedActionIndex;
    private String[] actionMenu = { "공격", "방어", "아이템", "도망" };
    private boolean itemSubMenuActive;
    private int selectedConsumableIndex;

    public BattleScreen(BattleManager battleManager) {
        this.battleManager = battleManager;
        this.selectedActionIndex = 0;
    }

    /**
     * 전투 화면 렌더링
     */
    public void render(Graphics2D g2d, int width, int height) {
        // 배경
        g2d.setColor(new Color(20, 20, 40));
        g2d.fillRect(0, 0, width, height);

        // 플레이어 정보 패널 (왼쪽 하단)
        drawPlayerInfo(g2d, width, height);

        // 적 정보 패널 (오른쪽 상단)
        drawEnemyInfo(g2d, width, height);

        // 액션 메뉴 또는 아이템 서브메뉴
        if (itemSubMenuActive) {
            drawItemSubMenu(g2d, width, height);
        } else {
            drawActionMenu(g2d, width, height);
        }

        // 전투 로그 (상단)
        drawBattleLog(g2d, width, height);

        // 전투 상태 메시지
        drawBattleStatus(g2d, width, height);
    }

    /**
     * 플레이어 정보 그리기
     */
    private void drawPlayerInfo(Graphics2D g2d, int width, int height) {
        Player player = battleManager.getPlayer();

        int panelX = 50;
        int panelY = height - 200;
        int panelWidth = 400;
        int panelHeight = 150;

        // 패널 배경
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(panelX, panelY, panelWidth, panelHeight);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(panelX, panelY, panelWidth, panelHeight);

        // 플레이어 이름
        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2d.setColor(Color.CYAN);
        g2d.drawString(player.getName(), panelX + 20, panelY + 30);

        // HP 바
        drawHPBar(g2d, panelX + 20, panelY + 50, panelWidth - 40, 20,
                player.getHealth(), player.getMaxHealth(), Color.GREEN);

        // HP 텍스트
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString("HP: " + player.getHealth() + " / " + player.getMaxHealth(),
                panelX + 20, panelY + 90);

        // 공격력/방어력
        g2d.drawString("공격력: " + player.getAttack() + " | 방어력: " + player.getDefense(),
                panelX + 20, panelY + 110);
    }

    /**
     * 적 정보 그리기
     */
    private void drawEnemyInfo(Graphics2D g2d, int width, int height) {
        Enemy enemy = battleManager.getEnemy();

        int panelX = width - 450;
        int panelY = 120;
        int panelWidth = 400;
        int panelHeight = 150;

        // 패널 배경
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(panelX, panelY, panelWidth, panelHeight);
        g2d.setColor(Color.RED);
        g2d.drawRect(panelX, panelY, panelWidth, panelHeight);

        // 적 이름
        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2d.setColor(Color.RED);
        g2d.drawString(enemy.getName(), panelX + 20, panelY + 30);

        // HP 바 (아이콘보다 먼저 그려서 겹치지 않도록)
        drawHPBar(g2d, panelX + 20, panelY + 50, panelWidth - 120, 20,
                enemy.getHealth(), enemy.getMaxHealth(), Color.RED);

        // HP 텍스트
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString("HP: " + enemy.getHealth() + " / " + enemy.getMaxHealth(),
                panelX + 20, panelY + 90);

        // 공격력/방어력
        g2d.drawString("공격력: " + enemy.getAttack() + " | 방어력: " + enemy.getDefense(),
                panelX + 20, panelY + 110);

        // 적 심볼 (HP 바 옆 오른쪽에 배치, 마지막에 그려서 가려지지 않도록)
        g2d.setFont(new Font("Monospaced", Font.BOLD, 48));
        g2d.setColor(Color.ORANGE);
        g2d.drawString(String.valueOf(enemy.getSymbol()), panelX + panelWidth - 70, panelY + 95);
    }

    /**
     * HP 바 그리기
     */
    private void drawHPBar(Graphics2D g2d, int x, int y, int width, int height,
            int currentHP, int maxHP, Color color) {
        // 배경
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x, y, width, height);

        // HP 바
        double hpRatio = maxHP > 0 ? (double) currentHP / maxHP : 0.0;
        int barWidth = (int) (width * hpRatio);

        // HP 비율에 따른 색상 변화
        Color barColor;
        if (hpRatio > 0.6) {
            barColor = Color.GREEN;
        } else if (hpRatio > 0.3) {
            barColor = Color.YELLOW;
        } else {
            barColor = Color.RED;
        }

        g2d.setColor(barColor);
        g2d.fillRect(x + 1, y + 1, barWidth - 2, height - 2);

        // 테두리
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, width, height);
    }

    /**
     * 액션 메뉴 그리기 (가로 배치)
     */
    private void drawActionMenu(Graphics2D g2d, int width, int height) {
        if (!battleManager.isPlayerTurn() || battleManager.isBattleEnded()) {
            return;
        }

        int menuX = width / 2 - 220;
        int menuY = height - 100;
        int menuWidth = 440;
        int menuHeight = 70;
        int itemSpacing = 100; // 가로 간격

        // 메뉴 배경
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(menuX, menuY, menuWidth, menuHeight);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(menuX, menuY, menuWidth, menuHeight);

        // 메뉴 제목
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("행동을 선택하세요", menuX + 10, menuY + 20);

        // 메뉴 항목 (가로 배치: 공격 | 방어 | 아이템 | 도망)
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
        int startX = menuX + 20;
        int itemY = menuY + 50;

        for (int i = 0; i < actionMenu.length; i++) {
            int itemX = startX + i * itemSpacing;

            if (i == selectedActionIndex) {
                // 선택된 항목 강조 (배경 박스)
                g2d.setColor(new Color(80, 80, 120));
                g2d.fillRect(itemX - 5, itemY - 22, 70, 28);
                g2d.setColor(Color.YELLOW);
            } else {
                g2d.setColor(Color.WHITE);
            }

            g2d.drawString(actionMenu[i], itemX, itemY);
        }

        // 조작 안내
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("←→: 선택 | ENTER: 결정", menuX + 10, menuY + menuHeight - 8);
    }

    /**
     * 아이템 서브메뉴 그리기 (소비 아이템 선택)
     */
    private void drawItemSubMenu(Graphics2D g2d, int width, int height) {
        List<Item> consumables = battleManager.getConsumables();

        int menuX = width / 2 - 220;
        int menuY = height - 150;
        int menuWidth = 440;
        int menuHeight = 120;

        g2d.setColor(new Color(0, 0, 0, 230));
        g2d.fillRect(menuX, menuY, menuWidth, menuHeight);
        g2d.setColor(Color.YELLOW);
        g2d.drawRect(menuX, menuY, menuWidth, menuHeight);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("소비 아이템 선택 (ESC: 취소)", menuX + 15, menuY + 25);

        if (consumables.isEmpty()) {
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2d.setColor(Color.GRAY);
            g2d.drawString("사용 가능한 소비 아이템이 없습니다.", menuX + 15, menuY + 55);
        } else {
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
            int startY = menuY + 50;
            int lineHeight = 28;
            for (int i = 0; i < consumables.size(); i++) {
                Item item = consumables.get(i);
                String line = (i + 1) + ". " + item.getName();
                if (item instanceof HealthPotion hp) {
                    line += " (HP +" + hp.getHealAmount() + ")";
                }
                if (i == selectedConsumableIndex) {
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(menuX + 10, startY + i * lineHeight - 18, menuWidth - 20, 22);
                    g2d.setColor(Color.BLACK);
                } else {
                    g2d.setColor(Color.WHITE);
                }
                g2d.drawString(line, menuX + 15, startY + i * lineHeight);
            }
        }

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("↑↓: 선택 | ENTER: 사용 | ESC: 취소", menuX + 10, menuY + menuHeight - 8);
    }

    /**
     * 전투 로그 그리기
     */
    private void drawBattleLog(Graphics2D g2d, int width, int height) {
        int logX = 50;
        int logY = 50;
        int logWidth = 500; // 너비를 줄여서 적 정보 패널과 겹치지 않도록
        int logHeight = 60; // 높이를 크게 줄임 (3개 메시지만 표시)

        // 로그 배경
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(logX, logY, logWidth, logHeight);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(logX, logY, logWidth, logHeight);

        // 로그 제목
        g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("전투 로그", logX + 10, logY + 18);

        // 로그 메시지들 (최근 3개만 표시)
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2d.setColor(Color.WHITE);

        java.util.List<String> logs = battleManager.getBattleLog();
        int startIndex = Math.max(0, logs.size() - 3); // 최근 3개만 표시

        for (int i = startIndex; i < logs.size(); i++) {
            int y = logY + 35 + (i - startIndex) * 18;
            // 메시지가 너무 길면 잘라내기
            String message = logs.get(i);
            if (message.length() > 50) {
                message = message.substring(0, 47) + "...";
            }
            g2d.drawString(message, logX + 10, y);
        }
    }

    /**
     * 전투 상태 메시지 그리기
     */
    private void drawBattleStatus(Graphics2D g2d, int width, int height) {
        g2d.setFont(new Font("Monospaced", Font.BOLD, 24));

        if (battleManager.isBattleEnded()) {
            BattleManager.BattleResult result = battleManager.getResult();

            switch (result) {
                case VICTORY:
                    g2d.setColor(Color.GREEN);
                    g2d.drawString("승리!", width / 2 - 50, height / 2);
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
                    g2d.setColor(Color.WHITE);
                    g2d.drawString("아무 키나 눌러 계속...", width / 2 - 100, height / 2 + 40);
                    break;
                case DEFEAT:
                    g2d.setColor(Color.RED);
                    g2d.drawString("패배...", width / 2 - 50, height / 2);
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
                    g2d.setColor(Color.WHITE);
                    g2d.drawString("아무 키나 눌러 계속...", width / 2 - 100, height / 2 + 40);
                    break;
                case ESCAPED:
                    g2d.setColor(Color.YELLOW);
                    g2d.drawString("도망 성공!", width / 2 - 70, height / 2);
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
                    g2d.setColor(Color.WHITE);
                    g2d.drawString("아무 키나 눌러 계속...", width / 2 - 100, height / 2 + 40);
                    break;
            }
        } else if (!battleManager.isPlayerTurn()) {
            g2d.setColor(Color.ORANGE);
            g2d.drawString("적의 턴...", width / 2 - 60, height / 2);
        }
    }

    /**
     * 입력 처리
     */
    public void handleInput(KeyEvent e) {
        if (battleManager.isBattleEnded()) {
            return;
        }

        if (!battleManager.isPlayerTurn()) {
            return;
        }

        if (itemSubMenuActive) {
            handleItemSubMenuInput(e);
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                selectedActionIndex = (selectedActionIndex - 1 + actionMenu.length) % actionMenu.length;
                e.consume();
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                selectedActionIndex = (selectedActionIndex + 1) % actionMenu.length;
                e.consume();
                break;
            case KeyEvent.VK_UP:
                selectedActionIndex = (selectedActionIndex - 1 + actionMenu.length) % actionMenu.length;
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
                selectedActionIndex = (selectedActionIndex + 1) % actionMenu.length;
                e.consume();
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                if (selectedActionIndex == 2) {
                    // 아이템: 서브메뉴 열기
                    List<Item> consumables = battleManager.getConsumables();
                    if (consumables.isEmpty()) {
                        // 로그는 BattleManager에서? 아니면 그냥 서브메뉴만 빈 상태로
                        itemSubMenuActive = true;
                        selectedConsumableIndex = 0;
                    } else {
                        itemSubMenuActive = true;
                        selectedConsumableIndex = 0;
                    }
                } else {
                    executeSelectedAction();
                }
                e.consume();
                break;
        }
    }

    private void handleItemSubMenuInput(KeyEvent e) {
        List<Item> consumables = battleManager.getConsumables();

        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                itemSubMenuActive = false;
                e.consume();
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (!consumables.isEmpty()) {
                    selectedConsumableIndex = (selectedConsumableIndex - 1 + consumables.size()) % consumables.size();
                }
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if (!consumables.isEmpty()) {
                    selectedConsumableIndex = (selectedConsumableIndex + 1) % consumables.size();
                }
                e.consume();
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                if (!consumables.isEmpty() && battleManager.executeUseConsumable(selectedConsumableIndex)) {
                    itemSubMenuActive = false;
                }
                e.consume();
                break;
        }
    }

    /**
     * 선택된 액션 실행
     */
    private void executeSelectedAction() {
        BattleManager.BattleAction action;

        switch (selectedActionIndex) {
            case 0:
                action = BattleManager.BattleAction.ATTACK;
                break;
            case 1:
                action = BattleManager.BattleAction.DEFEND;
                break;
            case 2:
                action = BattleManager.BattleAction.ITEM;
                break;
            case 3:
                action = BattleManager.BattleAction.ESCAPE;
                break;
            default:
                return;
        }

        battleManager.executePlayerAction(action);
    }

    public int getSelectedActionIndex() {
        return selectedActionIndex;
    }
}
