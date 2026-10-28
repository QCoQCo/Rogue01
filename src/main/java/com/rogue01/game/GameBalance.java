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

    // === 전투 ===
    /** 방어 자세일 때 받는 피해 배율 (다음 적 공격 1회) */
    public static final double DEFEND_DAMAGE_MULTIPLIER = 0.5;
    /**
     * 비율 데미지 공식의 방어력 계수: 피해 = 공격 × 100 / (100 + 방어 × 계수)
     * 방어 0 → 그대로, 방어 33 → 1/2, 방어 66 → 약 1/3 (뺄셈 공식처럼 1 데미지로 수렴하지 않음)
     */
    public static final int DAMAGE_DEFENSE_FACTOR = 3;

    // === 경험치 곡선 ===
    public static final int EXP_BASE = 45;
    public static final int EXP_PER_LEVEL = 35;

    // === 아이템 드롭 ===
    public static final double DROP_CHANCE_BASE = 0.55; // 기본 드롭 확률
    public static final double CONSUMABLE_RATIO = 0.35; // 드롭 시 소비아이템 비율

    // === 장비 레벨 (1~9) ===
    public static final int ITEM_LEVEL_MIN = 1;
    public static final int ITEM_LEVEL_MAX = 9;
    /** 드롭 장비 레벨 = 층 번호(1-1=1 … 3-3=9) + 이 중 하나 (−1이 1/4, 0이 1/2, +1이 1/4) */
    private static final int[] ITEM_LEVEL_FLOOR_OFFSETS = { -1, 0, 0, 1 };

    // 장비 수치는 적 성장(getEnemyStatScale)과 맞추기 위해 초기 값의 절반 수준
    /**
     * 무기 주력(공격력) 범위 [min, max] per level. Lv1: 5~8, Lv2: 8~10, Lv3: 11~13 ...
     */
    private static final int[][] WEAPON_ATK_RANGE = {
            { 5, 8 }, { 8, 10 }, { 11, 13 }, { 13, 15 }, { 16, 18 }, { 18, 20 }, { 21, 23 }, { 23, 25 }, { 26, 28 }
    };
    /** 무기 부가(방어력) 범위 [min, max]. Lv1: 0, Lv2: 0~3, Lv3: 0~4 ... */
    private static final int[][] WEAPON_DEF_RANGE = {
            { 0, 0 }, { 0, 3 }, { 0, 4 }, { 0, 5 }, { 0, 6 }, { 0, 7 }, { 0, 8 }, { 0, 9 }, { 0, 10 }
    };
    /** 방어구 주력(방어력) 범위 [min, max] per level */
    private static final int[][] ARMOR_DEF_RANGE = {
            { 5, 8 }, { 8, 10 }, { 11, 13 }, { 13, 15 }, { 16, 18 }, { 18, 20 }, { 21, 23 }, { 23, 25 }, { 26, 28 }
    };
    /** 방어구 부가(공격력) 범위 [min, max] */
    private static final int[][] ARMOR_ATK_RANGE = {
            { 0, 0 }, { 0, 3 }, { 0, 4 }, { 0, 5 }, { 0, 6 }, { 0, 7 }, { 0, 8 }, { 0, 9 }, { 0, 10 }
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

    /** 적 스탯 배율: 챕터당 증가량 */
    public static final double ENEMY_SCALE_PER_CHAPTER = 0.40;
    /** 적 스탯 배율: 층당 증가량 */
    public static final double ENEMY_SCALE_PER_LEVEL = 0.12;

    /**
     * 층/챕터별 적 스탯 배율 (1-1=1.0, 1-3=1.24, 2-1=1.4, 3-3=2.04)
     * 경험치에도 같은 배율이 적용됨 (Enemy 생성자)
     */
    public static double getEnemyStatScale(int chapter, int level) {
        double scale = 1.0;
        scale += (chapter - 1) * ENEMY_SCALE_PER_CHAPTER;
        scale += (level - 1) * ENEMY_SCALE_PER_LEVEL;
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
     * 확률적 반올림: 2.3 → 70% 확률로 2, 30% 확률로 3 (기댓값이 value와 같음)
     * 스폰 밀도 배율처럼 작은 수에 곱할 때 정수 버림으로 효과가 사라지지 않도록 사용
     */
    public static int roundStochastic(double value, java.util.Random rng) {
        int whole = (int) Math.floor(value);
        return whole + (rng.nextDouble() < value - whole ? 1 : 0);
    }

    /**
     * 비율 데미지 공식의 기본 피해 (크리티컬·랜덤 변동 적용 전, 최소 1)
     */
    public static int baseDamage(int attack, int defense) {
        double damage = attack * 100.0 / (100 + Math.max(0, defense) * DAMAGE_DEFENSE_FACTOR);
        return Math.max(1, (int) damage);
    }

    /**
     * 난이도별 스폰 밀도 배율 (높을수록 적 더 많이)
     * 방마다 기본 적 수 × 배율을 roundStochastic으로 반올림 (쉬움은 일부 방이 비고, 어려움은 일부 방에 1마리 추가)
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
     * 층에 맞춘 랜덤 장비 레벨: 층 번호(1-1=1 … 3-3=9) ±1, 1~9로 제한
     */
    public static int rollItemLevel(java.util.Random rng, int chapter, int level) {
        int floorNumber = (chapter - 1) * 3 + level;
        int offset = ITEM_LEVEL_FLOOR_OFFSETS[rng.nextInt(ITEM_LEVEL_FLOOR_OFFSETS.length)];
        return Math.max(ITEM_LEVEL_MIN, Math.min(ITEM_LEVEL_MAX, floorNumber + offset));
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
