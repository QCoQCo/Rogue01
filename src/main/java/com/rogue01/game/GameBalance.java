package com.rogue01.game;

/**
 * 게임 밸런스 설정 - 난이도, 스폰, 드롭률, 스탯 곡선 등
 */
public class GameBalance {

    public enum Difficulty {
        EASY("쉬움"),
        NORMAL("보통"),
        HARD("어려움");

        private final String koreanName;

        Difficulty(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() {
            return koreanName;
        }
    }

    // === 플레이어 스탯 (Lv1 기준) ===
    public static final int PLAYER_BASE_HP = 95;
    public static final int PLAYER_HP_PER_LEVEL = 12;
    public static final int PLAYER_BASE_ATTACK = 7;
    public static final int PLAYER_ATTACK_PER_LEVEL = 2;
    public static final int PLAYER_BASE_DEFENSE = 3;
    public static final int PLAYER_DEFENSE_PER_LEVEL = 1;

    // === 경험치 곡선 ===
    public static final int EXP_BASE = 45;
    public static final int EXP_PER_LEVEL = 35;

    // === 아이템 드롭 ===
    public static final double DROP_CHANCE_BASE = 0.55; // 기본 드롭 확률
    public static final double CONSUMABLE_RATIO = 0.35; // 드롭 시 소비아이템 비율

    // === 장비 레벨 (1~9, 높을수록 드롭 확률 낮음) ===
    public static final int ITEM_LEVEL_MIN = 1;
    public static final int ITEM_LEVEL_MAX = 9;
    /** 레벨별 드롭 가중치 (레벨 1=9, 레벨 9=1, 높을수록 드롭 확률 높음) */
    private static final int[] ITEM_LEVEL_WEIGHTS = { 9, 8, 7, 6, 5, 4, 3, 2, 1 };

    /**
     * 무기 주력(공격력) 범위 [min, max] per level. Lv1: 10~15, Lv2: 16~20, Lv3: 21~25 ...
     */
    private static final int[][] WEAPON_ATK_RANGE = {
            { 10, 15 }, { 16, 20 }, { 21, 25 }, { 26, 30 }, { 31, 35 }, { 36, 40 }, { 41, 45 }, { 46, 50 }, { 51, 55 }
    };
    /** 무기 부가(방어력) 범위 [min, max]. Lv1: 0, Lv2: 0~5, Lv3: 0~7 ... */
    private static final int[][] WEAPON_DEF_RANGE = {
            { 0, 0 }, { 0, 5 }, { 0, 7 }, { 0, 9 }, { 0, 11 }, { 0, 13 }, { 0, 15 }, { 0, 17 }, { 0, 19 }
    };
    /** 방어구 주력(방어력) 범위 [min, max] per level */
    private static final int[][] ARMOR_DEF_RANGE = {
            { 10, 15 }, { 16, 20 }, { 21, 25 }, { 26, 30 }, { 31, 35 }, { 36, 40 }, { 41, 45 }, { 46, 50 }, { 51, 55 }
    };
    /** 방어구 부가(공격력) 범위 [min, max] */
    private static final int[][] ARMOR_ATK_RANGE = {
            { 0, 0 }, { 0, 5 }, { 0, 7 }, { 0, 9 }, { 0, 11 }, { 0, 13 }, { 0, 15 }, { 0, 17 }, { 0, 19 }
    };

    // === 적 스폰 ===
    public static final int SPAWN_MIN_DISTANCE = 8; // 플레이어와 최소 거리
    public static final int ROOM_ENEMY_DIVISOR = 120; // 방 넓이 / 이 값 = 적 수
    public static final int RANDOM_SPAWN_DIVISOR = 450; // 맵 넓이 / 이 값 = 적 수

    // === 적 등장 확률 (누적, 1층 기준) ===
    public static final double SPAWN_GOBLIN = 0.55;
    public static final double SPAWN_SKELETON = 0.82;
    public static final double SPAWN_ORC = 0.94;
    public static final double SPAWN_TROLL = 0.98;
    // DRAGON = 나머지 2%

    /**
     * 층/챕터별 적 스탯 배율 (1-1=1.0, 3-3≈1.5)
     */
    public static double getEnemyStatScale(int chapter, int level) {
        double scale = 1.0;
        scale += (chapter - 1) * 0.12;  // 챕터당 +12%
        scale += (level - 1) * 0.08;   // 층당 +8%
        return Math.max(1.0, scale);
    }

    /**
     * 층/챕터별 랜덤 적 타입 (높은 층·챕터일수록 강한 적 비율 증가)
     */
    public static com.rogue01.entity.EnemyType rollEnemyType(java.util.Random rng, int chapter, int level) {
        double[] w = getEnemySpawnWeights(chapter, level);
        double roll = rng.nextDouble();
        if (roll < w[0]) return com.rogue01.entity.EnemyType.GOBLIN;
        if (roll < w[0] + w[1]) return com.rogue01.entity.EnemyType.SKELETON;
        if (roll < w[0] + w[1] + w[2]) return com.rogue01.entity.EnemyType.ORC;
        if (roll < w[0] + w[1] + w[2] + w[3]) return com.rogue01.entity.EnemyType.TROLL;
        return com.rogue01.entity.EnemyType.DRAGON;
    }

    private static double[] getEnemySpawnWeights(int chapter, int level) {
        double g = Math.max(0.15, 0.55 - (level - 1) * 0.08 - (chapter - 1) * 0.05);
        double s = Math.max(0.15, 0.27 - (level - 1) * 0.03);
        double o = Math.max(0.08, 0.12 + (level - 1) * 0.04);
        double t = Math.max(0.03, 0.04 + (level - 1) * 0.05 + (chapter - 1) * 0.02);
        double d = Math.max(0.01, 0.02 + (level - 1) * 0.02 + (chapter - 1) * 0.01);
        double sum = g + s + o + t + d;
        return new double[] { g / sum, s / sum, o / sum, t / sum, d / sum };
    }

    /**
     * 난이도별 드롭 확률 배율
     */
    public static double getDropChanceMultiplier(Difficulty diff) {
        return switch (diff) {
            case EASY -> 1.2;
            case NORMAL -> 1.0;
            case HARD -> 0.8;
        };
    }

    /**
     * 난이도별 스폰 밀도 배율 (높을수록 적 더 많이)
     */
    public static double getSpawnDensityMultiplier(Difficulty diff) {
        return switch (diff) {
            case EASY -> 0.7;
            case NORMAL -> 1.0;
            case HARD -> 1.3;
        };
    }

    /**
     * 난이도별 적 공격력 배율
     */
    public static double getEnemyAttackMultiplier(Difficulty diff) {
        return switch (diff) {
            case EASY -> 0.85;
            case NORMAL -> 1.0;
            case HARD -> 1.15;
        };
    }

    /**
     * 난이도별 도망 성공률
     */
    public static double getEscapeChance(Difficulty diff) {
        return switch (diff) {
            case EASY -> 0.85;
            case NORMAL -> 0.7;
            case HARD -> 0.55;
        };
    }

    /**
     * 다음 레벨 필요 경험치
     */
    public static int getExpToNextLevel(int level) {
        return EXP_BASE + level * EXP_PER_LEVEL;
    }

    /**
     * 가중치 기반 랜덤 장비 레벨 (1~9, 높을수록 확률 낮음)
     */
    public static int rollItemLevel(java.util.Random rng) {
        int total = 0;
        for (int w : ITEM_LEVEL_WEIGHTS)
            total += w;
        int roll = rng.nextInt(total);
        for (int i = 0; i < ITEM_LEVEL_WEIGHTS.length; i++) {
            roll -= ITEM_LEVEL_WEIGHTS[i];
            if (roll < 0)
                return i + 1;
        }
        return ITEM_LEVEL_MAX;
    }

    /** 무기 공격력 범위 [min, max] */
    public static int[] getWeaponAttackRange(int level) {
        int idx = Math.max(0, Math.min(level - 1, WEAPON_ATK_RANGE.length - 1));
        return WEAPON_ATK_RANGE[idx];
    }

    /** 무기 방어력 범위 [min, max] */
    public static int[] getWeaponDefenseRange(int level) {
        int idx = Math.max(0, Math.min(level - 1, WEAPON_DEF_RANGE.length - 1));
        return WEAPON_DEF_RANGE[idx];
    }

    /** 방어구 방어력 범위 [min, max] */
    public static int[] getArmorDefenseRange(int level) {
        int idx = Math.max(0, Math.min(level - 1, ARMOR_DEF_RANGE.length - 1));
        return ARMOR_DEF_RANGE[idx];
    }

    /** 방어구 공격력 범위 [min, max] */
    public static int[] getArmorAttackRange(int level) {
        int idx = Math.max(0, Math.min(level - 1, ARMOR_ATK_RANGE.length - 1));
        return ARMOR_ATK_RANGE[idx];
    }
}
