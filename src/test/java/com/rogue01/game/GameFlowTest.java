package com.rogue01.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rogue01.battle.BattleManager;
import com.rogue01.entity.Enemy;
import com.rogue01.entity.EnemyType;
import com.rogue01.entity.Player;
import com.rogue01.map.Map;
import com.rogue01.map.MapGenerationInfo;
import com.rogue01.map.Tile;
import com.rogue01.ui.GameWindow;
import com.rogue01.util.InputHandler;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

/**
 * 실제 Game 객체로 상태 흐름을 검사하는 통합 테스트 (docs/bugAndFix.md 2·3·4·5단계 회귀 방지)
 *
 * 키 입력은 실제와 같은 경로(InputHandler → InputManager)로 넣고 update()를 직접 호출한다.
 * Game이 창(JFrame)을 만들기 때문에 화면이 없는 환경에서는 건너뛴다.
 * 챕터/층 이동 같은 준비 단계에만 리플렉션을 사용한다.
 */
@DisabledIf("java.awt.GraphicsEnvironment#isHeadless")
class GameFlowTest {
    private static final long RESULT_WAIT_MS = 550; // Game.BATTLE_RESULT_MIN_DISPLAY_MS(500) 이후

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @AfterEach
    void tearDown() {
        game.getGameWindow().dispose();
    }

    // ===== 2단계: 보스 판정 / 보스 문 =====

    @Test
    void regularTrollOnSecondFloorDoesNotUnlockStairs() throws Exception {
        game.startNewGame();
        goToFloor(1, 2);
        MapGenerationInfo info = game.getMap().getGenerationInfo();

        bumpIntoEnemy(EnemyType.TROLL);
        winCurrentBattle();

        assertEquals(GameState.PLAYING, game.getGameState());
        assertEquals(0, game.getMidBossDefeatedCount());
        assertTrue(game.getMap().getTile(info.getStairsX() - 1, info.getStairsY()).isSealWall(), "stairs stay sealed");
    }

    @Test
    void midBossDoorBattleUnlocksStairsAndOpensDoor() throws Exception {
        game.startNewGame();
        goToFloor(1, 2);
        MapGenerationInfo info = game.getMap().getGenerationInfo();
        int[] door = standNextToDoor(false);
        Player player = game.getPlayer();
        int px = player.getX();
        int py = player.getY();

        press(KeyEvent.VK_F);
        assertEquals(GameState.BOSS_DOOR_PROMPT, game.getGameState());
        press(KeyEvent.VK_F);
        assertEquals(GameState.BATTLE, game.getGameState());
        assertEquals(EnemyType.TROLL, game.getBattleManager().getEnemy().getType());
        winCurrentBattle();

        assertEquals(GameState.PLAYING, game.getGameState());
        assertEquals(1, game.getMidBossDefeatedCount());
        assertTrue(game.getMap().getTile(info.getStairsX() - 1, info.getStairsY()).isRubble(), "seal broken");
        Tile doorTile = game.getMap().getTile(door[0], door[1]);
        assertTrue(doorTile.isWalkable() && !doorTile.isBossDoor(), "defeated boss door opens");

        player.setPosition(px, py);
        press(KeyEvent.VK_F);
        assertEquals(GameState.PLAYING, game.getGameState(), "opened door must not prompt again");
    }

    @Test
    void regularDragonOnThirdFloorDoesNotAdvanceChapter() throws Exception {
        game.startNewGame();
        goToFloor(1, 3);

        bumpIntoEnemy(EnemyType.DRAGON);
        winCurrentBattle();

        assertEquals(GameState.PLAYING, game.getGameState());
        assertEquals(1, game.getCurrentChapter());
    }

    @Test
    void chapterBossVictoryAdvancesChapterAndClearsGameAtLastChapter() throws Exception {
        game.startNewGame();
        goToFloor(1, 3);
        enterBossDoor(true);
        winCurrentBattle();
        assertEquals(GameState.CHAPTER_TRANSITION, game.getGameState());

        goToFloor(3, 3);
        enterBossDoor(true);
        winCurrentBattle();
        assertEquals(GameState.GAME_CLEAR, game.getGameState());
    }

    @Test
    void escapingBossStepsAwayFromDoorAndKeepsItClosed() throws Exception {
        game.startNewGame();
        goToFloor(1, 3);
        int[] door = standNextToDoor(true);
        Player player = game.getPlayer();
        int px = player.getX();
        int py = player.getY();
        int behindX = px + Integer.signum(px - door[0]);
        int behindY = py + Integer.signum(py - door[1]);

        press(KeyEvent.VK_F);
        press(KeyEvent.VK_F);
        BattleManager battle = game.getBattleManager();
        setField(battle, "battleState", BattleManager.BattleState.ESCAPED);
        setField(battle, "result", BattleManager.BattleResult.ESCAPED);
        game.update();
        continueFromResultScreen();

        assertEquals(GameState.PLAYING, game.getGameState());
        assertTrue(game.getMap().getTile(door[0], door[1]).isBossDoorChapter(), "door stays closed after escape");
        if (game.getMap().isWalkable(behindX, behindY)) {
            assertEquals(behindX, player.getX());
            assertEquals(behindY, player.getY());
        }
    }

    // ===== 3단계: 시작 / 재시작 / 난이도 =====

    @Test
    void menuDoesNotSpawnUntilStartAndAppliesChosenDifficulty() {
        assertEquals(GameState.MENU, game.getGameState());
        assertTrue(game.getEnemies().isEmpty(), "no enemies before the game starts");

        press(KeyEvent.VK_3);
        press(KeyEvent.VK_ENTER);

        assertEquals(GameState.PLAYING, game.getGameState());
        assertEquals(GameBalance.Difficulty.HARD, game.getDifficulty());
        assertEquals(0, game.getSurvivalTimeSeconds(), "survival clock starts at ENTER");
        assertTrue(allEnemiesAtBaseScale());
    }

    @Test
    void newGameStartsWithSwordAndArmorEquippedAndThreePotions() {
        game.startNewGame();
        var inventory = game.getPlayer().getInventory();

        assertNotNull(inventory.getEquippedItem(com.rogue01.item.ItemType.WEAPON_MAIN), "sword equipped");
        assertNotNull(inventory.getEquippedItem(com.rogue01.item.ItemType.ARMOR), "armor equipped");
        assertEquals(3, inventory.getConsumables().size(), "three potions");
        assertEquals(3, inventory.getSize(), "only potions left in the bag");
    }

    @Test
    void restartAfterLateChapterResetsToFirstFloorStrength() throws Exception {
        game.startNewGame();
        goToFloor(3, 3);
        assertFalse(allEnemiesAtBaseScale(), "3-3 enemies are scaled up");
        game.getPlayer().addExperience(500);
        assertTrue(game.getPlayer().getLevel() > 1);

        game.setGameState(GameState.GAME_OVER);
        press(KeyEvent.VK_R);

        assertEquals(GameState.PLAYING, game.getGameState());
        assertEquals(1, game.getCurrentChapter());
        assertEquals(1, game.getCurrentLevel());
        assertTrue(allEnemiesAtBaseScale(), "restart must spawn 1-1 strength enemies");
        assertEquals(1, game.getPlayer().getLevel());
    }

    @Test
    void mainMenuThenStartUsesNewlyChosenDifficulty() throws Exception {
        game.startNewGame();
        goToFloor(3, 3);
        game.setGameState(GameState.GAME_CLEAR);

        press(KeyEvent.VK_M);
        assertEquals(GameState.MENU, game.getGameState());
        press(KeyEvent.VK_1);
        press(KeyEvent.VK_ENTER);

        assertEquals(GameState.PLAYING, game.getGameState());
        assertEquals(GameBalance.Difficulty.EASY, game.getDifficulty());
        assertEquals(1, game.getCurrentLevel());
        assertTrue(allEnemiesAtBaseScale());
    }

    // ===== 4단계: 전투 입력 / 결과 화면 / 턴 =====

    @Test
    void escInBattleItemMenuClosesMenuWithoutPausing() throws Exception {
        game.startNewGame();
        bumpIntoEnemy(EnemyType.GOBLIN);
        var screen = game.getGameWindow().getBattleScreen(game.getBattleManager());

        press(KeyEvent.VK_RIGHT);
        press(KeyEvent.VK_RIGHT);
        press(KeyEvent.VK_ENTER);
        assertTrue(screen.isItemSubMenuActive(), "item submenu opens");

        press(KeyEvent.VK_ESCAPE);
        assertFalse(screen.isItemSubMenuActive(), "ESC closes the submenu");
        assertEquals(GameState.BATTLE, game.getGameState(), "ESC in submenu must not pause");

        press(KeyEvent.VK_ESCAPE);
        assertEquals(GameState.PAUSED, game.getGameState(), "ESC on main battle menu pauses");
        press(KeyEvent.VK_ESCAPE);
        assertEquals(GameState.BATTLE, game.getGameState(), "resume returns to the battle");
    }

    @Test
    void victoryResultScreenWaitsForKeyAfterMinimumTime() throws Exception {
        game.startNewGame();
        bumpIntoEnemy(EnemyType.GOBLIN);
        game.getBattleManager().getEnemy().setHealth(1);
        int killsBefore = game.getKillCount();

        press(KeyEvent.VK_ENTER); // 공격으로 처치
        idle(3);
        BattleManager battle = game.getBattleManager();
        assertEquals(GameState.BATTLE, game.getGameState(), "result screen is shown");
        assertTrue(battle.isBattleEnded());
        assertTrue(battle.getBattleLog().stream().anyMatch(line -> line.contains("경험치")), "EXP shown on result");

        press(KeyEvent.VK_X);
        assertEquals(GameState.BATTLE, game.getGameState(), "key before minimum time is ignored");
        Thread.sleep(RESULT_WAIT_MS);
        idle(5);
        assertEquals(GameState.BATTLE, game.getGameState(), "stays until a key is pressed");

        press(KeyEvent.VK_ESCAPE);
        assertEquals(GameState.PLAYING, game.getGameState(), "ESC continues from the result screen");
        assertEquals(killsBefore + 1, game.getKillCount());
    }

    @Test
    void defeatShowsResultThenGameOver() throws Exception {
        game.startNewGame();
        bumpIntoEnemy(EnemyType.ORC);
        game.getPlayer().setHealth(1);

        press(KeyEvent.VK_ENTER); // 공격 → 적 턴에 사망
        idle(3);
        assertEquals(GameState.BATTLE, game.getGameState());
        assertEquals(BattleManager.BattleResult.DEFEAT, game.getBattleManager().getResult());

        continueFromResultScreen();
        assertEquals(GameState.GAME_OVER, game.getGameState());
    }

    @Test
    void enemiesDoNotGetFreeTurnsAfterBattleStairsOrPromptCancel() throws Exception {
        game.startNewGame();
        bumpIntoEnemy(EnemyType.GOBLIN);
        winCurrentBattle();
        String snapshot = enemySnapshot();
        idle(5);
        assertEquals(snapshot, enemySnapshot(), "after a battle the player acts first");

        press(KeyEvent.VK_SPACE);
        assertFalse(snapshot.equals(enemySnapshot()), "waiting a turn lets enemies move");

        MapGenerationInfo info = game.getMap().getGenerationInfo();
        game.getPlayer().setPosition(info.getStairsX(), info.getStairsY());
        press(KeyEvent.VK_F);
        assertEquals(2, game.getCurrentLevel());
        snapshot = enemySnapshot();
        idle(5);
        assertEquals(snapshot, enemySnapshot(), "enemies wait on arrival at a new floor");

        standNextToDoor(false);
        snapshot = enemySnapshot();
        press(KeyEvent.VK_F);
        assertEquals(GameState.BOSS_DOOR_PROMPT, game.getGameState());
        press(KeyEvent.VK_ESCAPE);
        idle(5);
        assertEquals(GameState.PLAYING, game.getGameState());
        assertEquals(snapshot, enemySnapshot(), "cancelling the boss prompt costs no turn");
    }

    // ===== 5단계: 화면 그리기 =====

    @Test
    void everyStateRendersWithoutErrors() throws Exception {
        renderOffscreen(); // MENU
        game.startNewGame();
        renderOffscreen();
        for (GameState state : new GameState[] { GameState.PAUSED, GameState.INVENTORY, GameState.MAP_VIEW,
                GameState.BOSS_DOOR_PROMPT, GameState.GAME_OVER, GameState.CHAPTER_TRANSITION,
                GameState.GAME_CLEAR }) {
            game.setGameState(state);
            renderOffscreen();
        }

        game.setGameState(GameState.PLAYING);
        bumpIntoEnemy(EnemyType.GOBLIN);
        renderOffscreen(); // 전투 행동 메뉴
        press(KeyEvent.VK_RIGHT);
        press(KeyEvent.VK_RIGHT);
        press(KeyEvent.VK_ENTER);
        renderOffscreen(); // 아이템 서브메뉴
        press(KeyEvent.VK_ESCAPE);
        game.getBattleManager().getEnemy().setHealth(1);
        press(KeyEvent.VK_ENTER);
        renderOffscreen(); // 승리 결과 화면
    }

    /** 창을 띄우지 않고 게임 패널을 이미지에 그림 (그리기 중 예외가 나면 테스트 실패) */
    private void renderOffscreen() throws Exception {
        game.render();
        Field panelField = GameWindow.class.getDeclaredField("gamePanel");
        panelField.setAccessible(true);
        javax.swing.JPanel panel = (javax.swing.JPanel) panelField.get(game.getGameWindow());
        panel.setSize(1400, 900);
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(1400, 900,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = image.createGraphics();
        try {
            panel.paint(g2d);
        } finally {
            g2d.dispose();
        }
    }

    // ===== helpers =====

    /** 실제와 같은 경로로 키 입력: InputHandler에 눌림 → 한 프레임 갱신 → 뗌 */
    private void press(int keyCode) {
        GameWindow window = game.getGameWindow();
        InputHandler input = window.getInputHandler();
        long now = System.currentTimeMillis();
        input.keyPressed(new KeyEvent(window, KeyEvent.KEY_PRESSED, now, 0, keyCode, KeyEvent.CHAR_UNDEFINED));
        game.update();
        input.keyReleased(new KeyEvent(window, KeyEvent.KEY_RELEASED, now, 0, keyCode, KeyEvent.CHAR_UNDEFINED));
    }

    private void idle(int frames) {
        for (int i = 0; i < frames; i++) {
            game.update();
        }
    }

    private void goToFloor(int chapter, int level) throws Exception {
        setField(game, "currentChapter", chapter);
        setField(game, "currentLevel", level);
        setField(game, "midBossDefeatedCount", 0);
        game.advanceToNextFloor();
    }

    /** 오른쪽 칸에 적을 두고 → 키로 부딪혀 전투 시작 */
    private void bumpIntoEnemy(EnemyType type) throws Exception {
        placePlayerInOpenSpot();
        Player player = game.getPlayer();
        enemyList().add(new Enemy(player.getX() + 1, player.getY(), type));
        press(KeyEvent.VK_RIGHT);
        assertEquals(GameState.BATTLE, game.getGameState());
    }

    /** 적 HP를 1로 만들고 공격(ENTER)으로 처치 후 결과 화면에서 계속 */
    private void winCurrentBattle() throws Exception {
        game.getBattleManager().getEnemy().setHealth(1);
        press(KeyEvent.VK_ENTER);
        assertTrue(game.getBattleManager().isBattleEnded());
        continueFromResultScreen();
    }

    private void continueFromResultScreen() throws Exception {
        Thread.sleep(RESULT_WAIT_MS);
        press(KeyEvent.VK_X);
    }

    private void enterBossDoor(boolean chapterDoor) {
        standNextToDoor(chapterDoor);
        press(KeyEvent.VK_F);
        press(KeyEvent.VK_F);
        assertEquals(GameState.BATTLE, game.getGameState());
    }

    /** 원하는 종류의 보스 문 옆 빈칸으로 이동, 문 좌표 반환 */
    private int[] standNextToDoor(boolean chapterDoor) {
        Map map = game.getMap();
        for (int x = 1; x < map.getWidth() - 1; x++) {
            for (int y = 1; y < map.getHeight() - 1; y++) {
                int[] door = map.isWalkable(x, y) ? map.getAdjacentBossDoor(x, y) : null;
                if (door != null && map.getTile(door[0], door[1]).isBossDoorChapter() == chapterDoor) {
                    game.getPlayer().setPosition(x, y);
                    return door;
                }
            }
        }
        throw new AssertionError("no boss door found (chapter=" + chapterDoor + ")");
    }

    /** 오른쪽 칸이 비어 있고 주변에 적·계단·보스 문이 없는 위치로 플레이어 이동 */
    private void placePlayerInOpenSpot() throws Exception {
        Map map = game.getMap();
        List<Enemy> enemies = enemyList();
        for (int x = 5; x < map.getWidth() - 5; x++) {
            for (int y = 5; y < map.getHeight() - 5; y++) {
                if (!map.isWalkable(x, y) || !map.isWalkable(x + 1, y) || map.getTile(x, y).isStairsDown()
                        || map.getAdjacentBossDoor(x, y) != null) {
                    continue;
                }
                final int fx = x;
                final int fy = y;
                if (enemies.stream().noneMatch(e -> Math.abs(e.getX() - fx) + Math.abs(e.getY() - fy) < 4)) {
                    game.getPlayer().setPosition(x, y);
                    return;
                }
            }
        }
        throw new AssertionError("no open spot found");
    }

    private boolean allEnemiesAtBaseScale() {
        List<Enemy> enemies = game.getEnemies();
        return !enemies.isEmpty() && enemies.stream().allMatch(
                e -> e.getMaxHealth() == e.getType().getMaxHealth() && e.getAttack() == e.getType().getAttack());
    }

    private String enemySnapshot() {
        StringBuilder sb = new StringBuilder();
        for (Enemy e : game.getEnemies()) {
            sb.append(e.getX()).append(',').append(e.getY()).append(';');
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private List<Enemy> enemyList() throws Exception {
        Field field = Game.class.getDeclaredField("enemies");
        field.setAccessible(true);
        List<Enemy> list = (List<Enemy>) field.get(game);
        assertNotNull(list);
        return list;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
